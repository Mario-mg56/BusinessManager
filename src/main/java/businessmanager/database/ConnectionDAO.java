package businessmanager.database;

import businessmanager.management.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;

public class ConnectionDAO  {

    // ==========================================
    //           1. MAPEO (Helpers Privados)
    // ==========================================

    private static Entity mapEntity(ResultSet rs, char tipo) throws SQLException {
        Entity e = new Entity(
            rs.getInt("id"), rs.getString("nombre"), tipo,
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
        int numFactura = 0;
        try { numFactura = Integer.parseInt(rs.getString("numero")); } catch (Exception e) {}

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
                int nifNum = 0;
                try { nifNum = Integer.parseInt(rs.getString("nif").replaceAll("[^0-9]", "")); } catch (Exception e) {}

                Company c = new Company(
                    nifNum, rs.getString("nombre"), rs.getString("direccion"),
                    rs.getString("ciudad"), rs.getString("provincia"), rs.getString("pais"),
                    rs.getString("email"), rs.getString("domicilio_fiscal")
                );
                try { c.setCp(Integer.parseInt(rs.getString("cp"))); } catch (Exception e) {}
                try { c.setPhone(Integer.parseInt(rs.getString("telefono"))); } catch (Exception e) {}
                
                // c.setBills(getFacturas(c)); // Descomentar si tienes el setter
                lista.add(c);
            }
        } catch (SQLException e) { e.printStackTrace(); }
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
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Empresa WHERE nif=?")) {
            pstmt.setString(1, nif);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ==========================================
    //                3. ENTIDADES
    // ==========================================

    public static ArrayList<Entity> getEntidades() {
        return executeQueryAndMap("SELECT * FROM Entidad", 'E');
    }

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

    public static boolean insertEntity(Entity e) {
        String sql = "INSERT INTO Entidad (codigo, nombre, email, telefono, direccion, cp, ciudad, provincia, pais) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, 0); // Código dummy
            pstmt.setString(2, e.getName());
            pstmt.setString(3, e.getEmail());
            pstmt.setString(4, String.valueOf(e.getPhone()));
            pstmt.setString(5, e.getAddress());
            pstmt.setString(6, String.valueOf(e.getCp()));
            pstmt.setString(7, e.getCity());
            pstmt.setString(8, e.getProvince());
            pstmt.setString(9, e.getCountry());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static boolean updateEntity(Entity e) {
        String sql = "UPDATE Entidad SET nombre=?, email=?, telefono=?, direccion=?, cp=?, ciudad=?, provincia=?, pais=? WHERE id=?";
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, e.getName());
            pstmt.setString(2, e.getEmail());
            pstmt.setString(3, String.valueOf(e.getPhone()));
            pstmt.setString(4, e.getAddress());
            pstmt.setString(5, String.valueOf(e.getCp()));
            pstmt.setString(6, e.getCity());
            pstmt.setString(7, e.getProvince());
            pstmt.setString(8, e.getCountry());
            pstmt.setString(9, e.getNif());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex) { ex.printStackTrace(); return false; }
    }

    public static boolean deleteEntity(String id) {
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Entidad WHERE id=?")) {
            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
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
        try (Connection conn = ConexionDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Producto WHERE id=?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
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
}