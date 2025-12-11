package businessmanager.database;

import businessmanager.management.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

public class ConnectionDAO  {

    // ==========================================
    //           1. MAPEO (Helpers Privados)
    // ==========================================

    private static Entity mapEntity(ResultSet rs, char tipo) throws SQLException {
        Entity e = new Entity(
                rs.getInt("id"),rs.getString("nif"), rs.getString("nombre"), tipo,
                rs.getString("direccion"), rs.getString("ciudad"),
                rs.getString("provincia"), rs.getString("pais"), rs.getString("email")
        );
        // Convertimos Strings a int de forma segura (si falla pone 0)
        try { e.setCp(Integer.parseInt(rs.getString("cp"))); } catch (Exception ex) { e.setCp(0); }
        try { e.setPhone(Integer.parseInt(rs.getString("telefono"))); } catch (Exception ex) { e.setPhone(0); }
        return e;
    }

    private static Product mapProduct(ResultSet rs) throws SQLException {
        // Buscamos el proveedor dentro del producto
        Entity proveedor = getEntidadById(rs.getInt("proveedor_id"));

        int code = 0;
        try { code = Integer.parseInt(rs.getString("codigo")); } catch (Exception e) {}

        return new Product(
                rs.getInt("id"), code, rs.getInt("stock"), proveedor,
                (int) rs.getDouble("precio_coste"), (int) rs.getDouble("precio_venta"),
                0, rs.getString("descripcion")
        );
    }

    private static Bill mapBill(ResultSet rs, java.sql.Connection conn) throws SQLException {
        int id = rs.getInt("id");
        Entity tercero = getEntidadById(rs.getInt("tercero_id"));
        ArrayList<Product> productos = getLineasFactura(id, conn); // Busca los productos de esta factura

        // Parseo seguro del numero de factura
        String numFactura = null;
        try { numFactura = rs.getString("numero"); } catch (Exception e) {}

        // Obtener caracter de tipo
        String tStr = rs.getString("tipo");
        char tipo = (tStr != null && !tStr.isEmpty()) ? tStr.charAt(0) : ' ';

        Bill b = new Bill(
                id, numFactura, productos, tercero,
                (int) rs.getDouble("base_imponible"), rs.getString("estado"),
                rs.getString("concepto"), rs.getString("observaciones"),
                rs.getDate("fecha_emision").toLocalDate(), tipo
        );
        b.setIva((int) rs.getDouble("iva_total"));
        return b;
    }

    // ==========================================
    //                2. EMPRESAS
    // ==========================================

    public static ArrayList<Company> getEmpresas() {
        ArrayList<Company> lista = new ArrayList<>();
        String sql = "SELECT * FROM Empresa";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // CORRECCIÓN: Leemos el NIF directamente como String, sin quitar letras
                String nifReal = rs.getString("nif");

                // Asegúrate de que tu constructor de Company acepte String en el primer parámetro
                Company c = new Company(
                        nifReal, // <--- Aquí pasamos el NIF completo con letra
                        rs.getString("nombre"),
                        rs.getString("direccion"),
                        rs.getString("ciudad"),
                        rs.getString("provincia"),
                        rs.getString("pais"),
                        rs.getString("email"),
                        rs.getString("domicilio_fiscal")
                );

                // Parseo seguro de enteros para CP y Teléfono
                try {
                    String cpStr = rs.getString("cp");
                    c.setCp(cpStr != null ? Integer.parseInt(cpStr) : 0);
                } catch (Exception e) { c.setCp(0); }

                try {
                    String phStr = rs.getString("telefono");
                    c.setPhone(phStr != null ? Integer.parseInt(phStr) : 0);
                } catch (Exception e) { c.setPhone(0); }

                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public static boolean insertEmpresa(Company c) {
        String sql = "INSERT INTO Empresa (nif, nombre, direccion, cp, ciudad, provincia, pais, telefono, email, domicilio_fiscal) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getNif());
            pstmt.setString(2, c.getName());
            pstmt.setString(3, c.getAddress());
            pstmt.setString(4, String.valueOf(c.getCp()));
            pstmt.setString(5, c.getCity());
            pstmt.setString(6, c.getProvince());
            pstmt.setString(7, c.getCountry());
            pstmt.setString(8, String.valueOf(c.getPhone()));
            pstmt.setString(9, c.getEmail());
            pstmt.setString(10, c.getTaxAddress());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean updateEmpresa(Company c) {
        String sql = "UPDATE Empresa SET nombre=?, direccion=?, cp=?, ciudad=?, provincia=?, pais=?, telefono=?, email=?, domicilio_fiscal=? WHERE nif=?";
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getName());
            pstmt.setString(2, c.getAddress());
            pstmt.setString(3, String.valueOf(c.getCp()));
            pstmt.setString(4, c.getCity());
            pstmt.setString(5, c.getProvince());
            pstmt.setString(6, c.getCountry());
            pstmt.setString(7, String.valueOf(c.getPhone()));
            pstmt.setString(8, c.getEmail());
            pstmt.setString(9, c.getTaxAddress());
            pstmt.setString(10, c.getNif());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteEmpresa(String nif) {
        Connection conn = null;
        try {
            System.out.println("--- INICIO BORRADO EMPRESA: " + nif + " ---");
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // Inicio Transacción

            // 1. LÍNEAS DE FACTURA
            // Borramos las líneas de todas las facturas de esa empresa
            String sqlLineas = "DELETE FROM Factura_Linea WHERE factura_id IN (SELECT id FROM Factura WHERE empresa_nif = ?)";
            try (PreparedStatement pstLineas = conn.prepareStatement(sqlLineas)) {
                pstLineas.setString(1, nif);
                int lineasBorradas = pstLineas.executeUpdate();
                System.out.println("Paso 1: Líneas borradas = " + lineasBorradas);
            }

            // 2. FACTURAS
            String sqlFacturas = "DELETE FROM Factura WHERE empresa_nif = ?";
            try (PreparedStatement pstFacturas = conn.prepareStatement(sqlFacturas)) {
                pstFacturas.setString(1, nif);
                int facturasBorradas = pstFacturas.executeUpdate();
                System.out.println("Paso 2: Facturas borradas = " + facturasBorradas);
            }

            // 3. PRODUCTOS
            String sqlProductos = "DELETE FROM Producto WHERE empresa_nif = ?";
            try (PreparedStatement pstProductos = conn.prepareStatement(sqlProductos)) {
                pstProductos.setString(1, nif);
                int productosBorrados = pstProductos.executeUpdate();
                System.out.println("Paso 3: Productos borrados = " + productosBorrados);
            }


            String sqlUnlinkCli = "UPDATE Cliente SET empresa_nif = NULL WHERE empresa_nif = ?";
            try (PreparedStatement pstCli = conn.prepareStatement(sqlUnlinkCli)) {
                pstCli.setString(1, nif);
                pstCli.executeUpdate();
                System.out.println("Paso 3.5a: Clientes desvinculados");
            }

            String sqlUnlinkProv = "UPDATE Proveedor SET empresa_nif = NULL WHERE empresa_nif = ?";
            try (PreparedStatement pstProv = conn.prepareStatement(sqlUnlinkProv)) {
                pstProv.setString(1, nif);
                pstProv.executeUpdate();
                System.out.println("Paso 3.5b: Proveedores desvinculados");
            }


            // 4. EMPRESA
            String sqlEmpresa = "DELETE FROM Empresa WHERE nif = ?";
            try (PreparedStatement pstEmpresa = conn.prepareStatement(sqlEmpresa)) {
                pstEmpresa.setString(1, nif);
                int empresaBorrada = pstEmpresa.executeUpdate();
                System.out.println("Paso 4: Empresa borrada (filas afectadas) = " + empresaBorrada);

                if (empresaBorrada > 0) {
                    conn.commit();
                    System.out.println("--- ÉXITO: COMMIT REALIZADO ---");
                    return true;
                } else {
                    conn.rollback();
                    System.err.println("--- FALLO: No se encontró el NIF " + nif + " en la tabla Empresa ---");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("--- ERROR SQL DETECTADO ---");
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ==========================================
    //                3. ENTIDADES
    // ==========================================


    public static ArrayList<Entity> getClientes() {
        return executeQueryAndMap("SELECT e.* FROM Entidad e INNER JOIN Cliente c ON e.id = c.entidad_id", 'C');
    }

    public static ArrayList<Entity> getProveedores() {
        return executeQueryAndMap("SELECT e.* FROM Entidad e INNER JOIN Proveedor p ON e.id = p.entidad_id", 'P');
    }

    // Método privado para simplificar las 3 consultas de arriba
    private static ArrayList<Entity> executeQueryAndMap(String sql, char tipo) {
        ArrayList<Entity> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) lista.add(mapEntity(rs, tipo));
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public static Entity getEntidadById(int id) {
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Entidad WHERE id=?")) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapEntity(rs, 'E');
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public static int getEntidadIdByNif(String nif) {
        String sql = "SELECT id FROM Entidad WHERE nif = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Aseguramos que no haya espacios extra
            pstmt.setString(1, nif.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Retornamos -1 si no se encuentra la entidad
        return -1;
    }

    public static boolean insertEntity(Entity e, String nifEmpresa) {
        // 1. SQL para la tabla padre
        String sqlEntidad = "INSERT INTO Entidad (codigo, nif, nombre, email, telefono, direccion, cp, ciudad, provincia, pais) VALUES (?,?,?,?,?,?,?,?,?,?)";

        // 2. SQL para la tabla hija (Depende del tipo)
        String sqlChild = (e.getType() == 'C')
                ? "INSERT INTO Cliente (entidad_id, empresa_nif) VALUES (?, ?)"
                : "INSERT INTO Proveedor (entidad_id, empresa_nif) VALUES (?, ?)";

        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // INICIO TRANSACCIÓN

            long idGenerado = -1;

            // PASO 1: Insertar en Entidad y recuperar el ID generado
            try (PreparedStatement pst = conn.prepareStatement(sqlEntidad, Statement.RETURN_GENERATED_KEYS)) {
                pst.setInt(1, 0); // Código dummy (o genera uno aleatorio si quieres)
                pst.setString(2, e.getNif());
                pst.setString(3, e.getName());
                pst.setString(4, e.getEmail());
                pst.setString(5, String.valueOf(e.getPhone()));
                pst.setString(6, e.getAddress());
                pst.setString(7, String.valueOf(e.getCp()));
                pst.setString(8, e.getCity());
                pst.setString(9, e.getProvince());
                pst.setString(10, e.getCountry());

                int rows = pst.executeUpdate();
                if (rows == 0) throw new SQLException("Fallo al crear la entidad, no se insertaron filas.");

                // Recuperar el ID autogenerado (Auto-Increment)
                try (ResultSet gk = pst.getGeneratedKeys()) {
                    if (gk.next()) {
                        idGenerado = gk.getLong(1);
                        e.setId((int)idGenerado); // Actualizamos el objeto Java
                    } else {
                        throw new SQLException("Fallo al crear la entidad, no se obtuvo el ID.");
                    }
                }
            }

            // PASO 2: Insertar en Cliente o Proveedor vinculando a TU empresa
            try (PreparedStatement pstChild = conn.prepareStatement(sqlChild)) {
                pstChild.setLong(1, idGenerado); // El ID que acabamos de crear
                pstChild.setString(2, nifEmpresa); // El NIF de tu empresa (Tech Solutions / MegaStore)
                pstChild.executeUpdate();
            }

            conn.commit(); // FIN TRANSACCIÓN (Todo bien)
            return true;

        } catch (SQLException ex) {
            ex.printStackTrace();
            // Si algo falla, deshacemos todo (Rollback)
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException x) { x.printStackTrace(); }
            }
            return false;
        } finally {
            // Restaurar modo normal y cerrar
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException x) { x.printStackTrace(); }
            }
        }
    }

    public static boolean updateEntity(Entity e) {
        // IMPORTANTE: Separar campos con COMAS (,), nunca usar AND aquí.
        String sql = "UPDATE Entidad SET nif=?, nombre=?, email=?, telefono=?, direccion=?, cp=?, ciudad=?, provincia=?, pais=? WHERE id=?";

        try (java.sql.Connection conn = ConexionDB.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 1. nif
            pstmt.setString(1, e.getNif());
            // 2. nombre
            pstmt.setString(2, e.getName());
            // 3. email
            pstmt.setString(3, e.getEmail());
            // 4. telefono (Convertimos a String)
            pstmt.setString(4, String.valueOf(e.getPhone()));
            // 5. direccion
            pstmt.setString(5, e.getAddress());
            // 6. cp (Convertimos a String)
            pstmt.setString(6, String.valueOf(e.getCp()));
            // 7. ciudad
            pstmt.setString(7, e.getCity());
            // 8. provincia
            pstmt.setString(8, e.getProvince());
            // 9. pais
            pstmt.setString(9, e.getCountry());

            // 10. WHERE id (El ID es el último)
            pstmt.setInt(10, e.getId());

            return pstmt.executeUpdate() > 0;

        } catch (java.sql.SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEntity(String id) {
        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // Transacción para seguridad

            // 1. VALIDACIÓN: ¿Tiene facturas asociadas?
            // No debemos borrar una entidad si tiene historial de facturación
            String checkFacturas = "SELECT count(*) FROM Factura WHERE tercero_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(checkFacturas)) {
                pst.setString(1, id);
                ResultSet rs = pst.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RuntimeException("No se puede borrar: La entidad tiene facturas asociadas.");
                }
            }

            // 2. VALIDACIÓN: ¿Es un proveedor con productos?
            String checkProductos = "SELECT count(*) FROM Producto WHERE proveedor_id = ?";
            try (PreparedStatement pst = conn.prepareStatement(checkProductos)) {
                pst.setString(1, id);
                ResultSet rs = pst.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new RuntimeException("No se puede borrar: El proveedor tiene productos en catálogo.");
                }
            }

            // 3. BORRAR DE TABLAS HIJAS (Cliente / Proveedor)
            // Intentamos borrar de ambas por seguridad. Si no existe, no pasa nada.
            try (PreparedStatement pst = conn.prepareStatement("DELETE FROM Cliente WHERE entidad_id = ?")) {
                pst.setString(1, id);
                pst.executeUpdate();
            }
            try (PreparedStatement pst = conn.prepareStatement("DELETE FROM Proveedor WHERE entidad_id = ?")) {
                pst.setString(1, id);
                pst.executeUpdate();
            }

            // 4. BORRAR ENTIDAD PADRE
            try (PreparedStatement pst = conn.prepareStatement("DELETE FROM Entidad WHERE id=?")) {
                pst.setString(1, id);
                int affected = pst.executeUpdate();

                if (affected > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
        } catch (Exception e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            // Relanzamos la excepción para que el controlador muestre el mensaje de alerta
            throw new RuntimeException(e.getMessage());
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    // ==========================================
    //   FILTRAR POR RELACIÓN (TABLAS HIJAS)
    // ==========================================

    public static ArrayList<Entity> getClientesPorEmpresa(String nifEmpresa) {
        // Seleccionamos los datos de la Entidad, pero filtramos por la columna del Cliente
        String sql = "SELECT e.* FROM Entidad e " +
                "INNER JOIN Cliente c ON e.id = c.entidad_id " +
                "WHERE c.empresa_nif = ?"; // <--- EL CAMBIO ESTÁ AQUÍ

        return executeQueryAndMap(sql, nifEmpresa, 'C');
    }

    public static ArrayList<Entity> getProveedoresPorEmpresa(String nifEmpresa) {
        // Lo mismo para proveedores
        String sql = "SELECT e.* FROM Entidad e " +
                "INNER JOIN Proveedor p ON e.id = p.entidad_id " +
                "WHERE p.empresa_nif = ?"; // <--- EL CAMBIO ESTÁ AQUÍ

        return executeQueryAndMap(sql, nifEmpresa, 'P');
    }

    // Helper (Asegúrate de tener este método que acepta parámetros)
    private static ArrayList<Entity> executeQueryAndMap(String sql, String param, char tipo) {
        ArrayList<Entity> lista = new ArrayList<>();
        try (java.sql.Connection conn = ConexionDB.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (param != null) pstmt.setString(1, param);

            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lista.add(mapEntity(rs, tipo));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // ==========================================
    //                4. PRODUCTOS
    // ==========================================

    public static ArrayList<Product> getProductos() {
        return getProductosGeneric("SELECT * FROM Producto", null);
    }

    public static ArrayList<Product> getProductosPorEmpresa(String nif) {
        return getProductosGeneric("SELECT * FROM Producto WHERE empresa_nif = ?", nif);
    }

    public static Product getProductoPorCodigo(String codigo) {
        ArrayList<Product> list = getProductosGeneric("SELECT * FROM Producto WHERE codigo = ?", codigo);
        return list.isEmpty() ? null : list.get(0);
    }

    // Helper para simplificar consultas de productos
    private static ArrayList<Product> getProductosGeneric(String sql, String param) {
        ArrayList<Product> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (param != null) pstmt.setString(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) lista.add(mapProduct(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public static boolean insertProduct(Product p) {
        String sql = "INSERT INTO Producto (codigo, descripcion, proveedor_id, precio_coste, precio_venta, stock) VALUES (?,?,?,?,?,?)";
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, String.valueOf(p.getCode()));
            pstmt.setString(2, p.getDescription());
            if(p.getSupplier() != null) pstmt.setString(3, p.getSupplier().getNif()); else pstmt.setNull(3, Types.BIGINT);
            pstmt.setDouble(4, p.getPurchasePrize());
            pstmt.setDouble(5, p.getSellingPrice());
            pstmt.setInt(6, p.getQuantity());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean updateProduct(Product p) {
        String sql = "UPDATE Producto SET codigo=?, descripcion=?, proveedor_id=?, precio_coste=?, precio_venta=?, stock=? WHERE id=?";
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, String.valueOf(p.getCode()));
            pstmt.setString(2, p.getDescription());
            if(p.getSupplier() != null) pstmt.setString(3, p.getSupplier().getNif()); else pstmt.setNull(3, Types.BIGINT);
            pstmt.setDouble(4, p.getPurchasePrize());
            pstmt.setDouble(5, p.getSellingPrice());
            pstmt.setInt(6, p.getQuantity());
            pstmt.setInt(7, p.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteProduct(int id) {
        // 1. VALIDACIÓN: ¿El producto aparece en alguna factura?
        String sqlCheck = "SELECT count(*) FROM Factura_Linea WHERE producto_id = ?";

        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement pst = conn.prepareStatement(sqlCheck)) {

            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Si el producto se ha vendido, NO lo borramos físicamente.
                throw new RuntimeException("No se puede borrar: El producto aparece en facturas existentes.");
            }

            // 2. Si no se ha usado nunca, procedemos al borrado
            try (PreparedStatement pstDel = conn.prepareStatement("DELETE FROM Producto WHERE id=?")) {
                pstDel.setInt(1, id);
                return pstDel.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    // ==========================================
    //                5. FACTURAS
    // ==========================================

    public static ArrayList<Bill> getFacturas(String nif) {
        return getFacturasGeneric("SELECT * FROM Factura WHERE empresa_nif = ?", nif, null, null);
    }

    public static ArrayList<Bill> getFacturasPorEstado(String nif, String estado) {
        return getFacturasGeneric("SELECT * FROM Factura WHERE empresa_nif = ? AND estado = ?", nif, estado, null);
    }

    public static ArrayList<Bill> getFacturasPorFecha(String nif, LocalDate ini, LocalDate fin) {
        return getFacturasGeneric("SELECT * FROM Factura WHERE empresa_nif = ? AND fecha_emision BETWEEN ? AND ?", nif, ini, fin);
    }

    // Helper genérico para facturas
    private static ArrayList<Bill> getFacturasGeneric(String sql, String param1, Object param2, Object param3) {
        ArrayList<Bill> facturas = new ArrayList<>();
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, param1);
            if (param2 != null) {
                if (param2 instanceof String) pstmt.setString(2, (String) param2);
                else if (param2 instanceof LocalDate) pstmt.setDate(2, Date.valueOf((LocalDate) param2));
            }
            if (param3 != null && param3 instanceof LocalDate) pstmt.setDate(3, Date.valueOf((LocalDate) param3));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) facturas.add(mapBill(rs, conn));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return facturas;
    }

    public static boolean insertFactura(Bill b, String nifEmpresa) {
        String sqlHead = "INSERT INTO Factura (empresa_nif, tipo, numero, fecha_emision, tercero_id, concepto, base_imponible, iva_total, total_factura, estado, observaciones) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        String sqlLine = "INSERT INTO Factura_Linea (factura_id, producto_id, descripcion, cantidad, precio_unitario, porcentaje_iva, importe_base) VALUES (?,?,?,?,?,?,?)";

        java.sql.Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // Inicio Transacción

            long idFactura = -1;
            try (PreparedStatement pst = conn.prepareStatement(sqlHead)) {
                pst.setString(1, nifEmpresa);
                pst.setString(2, String.valueOf(b.getType()));
                pst.setString(3, String.valueOf(b.getNumber()));
                pst.setDate(4, Date.valueOf(b.getIssueDate()));
                if(b.getThirdParty() != null) pst.setString(5, b.getThirdParty().getNif()); else pst.setNull(5, Types.BIGINT);
                pst.setString(6, b.getConcept());
                pst.setDouble(7, b.getBaseImponible());
                pst.setDouble(8, b.getIva());
                pst.setDouble(9, b.getTotal());
                pst.setString(10, b.getStatus());
                pst.setString(11, b.getObservations());

                if (pst.executeUpdate() == 0) throw new SQLException("Error insertando factura");
                try (ResultSet gk = pst.getGeneratedKeys()) {
                    if (gk.next()) { idFactura = gk.getLong(1); b.setId((int)idFactura); }
                }
            }

            try (PreparedStatement pstL = conn.prepareStatement(sqlLine)) {
                for (Product p : b.getProducts()) {
                    pstL.setLong(1, idFactura);
                    pstL.setInt(2, p.getId());
                    pstL.setString(3, p.getDescription());
                    pstL.setDouble(4, p.getQuantity());
                    pstL.setDouble(5, p.getSellingPrice());
                    pstL.setDouble(6, p.getIva());
                    pstL.setDouble(7, p.getQuantity() * p.getSellingPrice());
                    pstL.addBatch();
                }
                pstL.executeBatch();
            }

            conn.commit(); // Fin Transacción
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    public static boolean updateFacturaHeader(Bill b) {
        String sql = "UPDATE Factura SET numero=?, fecha_emision=?, concepto=?, base_imponible=?, iva_total=?, estado=?, observaciones=? WHERE id=?";
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, String.valueOf(b.getNumber()));
            pstmt.setDate(2, Date.valueOf(b.getIssueDate()));
            pstmt.setString(3, b.getConcept());
            pstmt.setDouble(4, b.getBaseImponible());
            pstmt.setDouble(5, b.getIva());
            pstmt.setString(6, b.getStatus());
            pstmt.setString(7, b.getObservations());
            pstmt.setInt(8, b.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public static boolean deleteFactura(int id) {
        // Borramos líneas primero, luego cabecera
        java.sql.Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement("DELETE FROM Factura_Linea WHERE factura_id=?")) {
                p1.setInt(1, id);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = conn.prepareStatement("DELETE FROM Factura WHERE id=?")) {
                p2.setInt(1, id);
                int r = p2.executeUpdate();
                conn.commit();
                return r > 0;
            }
        } catch (SQLException e) {
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            e.printStackTrace(); return false;
        } finally {
            if(conn!=null) try{conn.close();}catch(Exception ex){}
        }
    }

    // Método privado para obtener los productos de una factura (Usado por mapBill)
    private static ArrayList<Product> getLineasFactura(int facturaId, java.sql.Connection conn) throws SQLException {
        ArrayList<Product> lineas = new ArrayList<>();
        String sql = "SELECT * FROM Factura_Linea WHERE factura_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, facturaId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Creamos productos temporales para la factura
                    Product p = new Product(
                            rs.getInt("producto_id"), 0, (int) rs.getDouble("cantidad"), null,
                            0, (int) rs.getDouble("precio_unitario"),
                            (int) rs.getDouble("porcentaje_iva"), rs.getString("descripcion")
                    );
                    lineas.add(p);
                }
            }
        }
        return lineas;
    }

    public static boolean updateFacturaCompleta(Bill b) {
        // SQLs necesarios
        String sqlHeader = "UPDATE Factura SET numero=?, fecha_emision=?, concepto=?, base_imponible=?, iva_total=?, total_factura=?, estado=?, observaciones=? WHERE id=?";
        String sqlDeleteLines = "DELETE FROM Factura_Linea WHERE factura_id=?";
        String sqlInsertLine = "INSERT INTO Factura_Linea (factura_id, producto_id, descripcion, cantidad, precio_unitario, porcentaje_iva, importe_base) VALUES (?,?,?,?,?,?,?)";

        Connection conn = null;
        try {
            conn = ConexionDB.getConnection();
            conn.setAutoCommit(false); // INICIO TRANSACCIÓN

            // 1. Actualizar Cabecera
            try (PreparedStatement pst = conn.prepareStatement(sqlHeader)) {
                pst.setString(1, b.getNumber()); // Ahora es String
                pst.setDate(2, Date.valueOf(b.getIssueDate()));
                pst.setString(3, b.getConcept());
                pst.setDouble(4, b.getBaseImponible());
                pst.setDouble(5, b.getIva());
                pst.setDouble(6, b.getTotal());
                pst.setString(7, b.getStatus());
                pst.setString(8, b.getObservations());
                pst.setInt(9, b.getId());
                pst.executeUpdate();
            }

            // 2. Borrar líneas antiguas (Limpieza)
            try (PreparedStatement pstDel = conn.prepareStatement(sqlDeleteLines)) {
                pstDel.setInt(1, b.getId());
                pstDel.executeUpdate();
            }

            // 3. Insertar líneas nuevas (Las que están en pantalla)
            try (PreparedStatement pstLine = conn.prepareStatement(sqlInsertLine)) {
                for (Product p : b.getProducts()) {
                    pstLine.setInt(1, b.getId());
                    pstLine.setInt(2, p.getId());
                    pstLine.setString(3, p.getDescription());
                    pstLine.setDouble(4, p.getQuantity());
                    pstLine.setDouble(5, p.getSellingPrice());
                    // Si tienes getIva() en producto úsalo, sino usa 21.0 por defecto
                    double ivaPercent = (p.getIva() > 0) ? p.getIva() : 21.0;
                    pstLine.setDouble(6, ivaPercent);
                    pstLine.setDouble(7, p.getQuantity() * p.getSellingPrice());
                    pstLine.addBatch(); // Añadir al lote
                }
                pstLine.executeBatch(); // Ejecutar todas a la vez
            }

            conn.commit(); // FIN TRANSACCIÓN (Guardar cambios)
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}