package Bills;

import businessmanager.database.ConexionDB;
import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class GeneradorFactura {

    public void generarFactura(int idFactura) {

        System.out.println("=== GENERANDO FACTURA CON NOMBRE Y RUTA PERSONALIZADA ===");

        Connection con = null;

        try {
            // 1. CONEXIÓN
            con = ConexionDB.getConnection();
            if (con == null) return;

            // --- NUEVO: OBTENER NÚMERO DE FACTURA PARA EL NOMBRE DEL ARCHIVO ---
            String numeroFactura = "SinNumero";
            String sqlName = "SELECT numero FROM Factura WHERE id = ?";
            try (PreparedStatement pst = con.prepareStatement(sqlName)) {
                pst.setInt(1, idFactura);
                ResultSet rs = pst.executeQuery();
                if (rs.next()) {
                    numeroFactura = rs.getString("numero");
                }
            }
            // Sanear el nombre (cambiar / por - para evitar errores de ruta)
            numeroFactura = numeroFactura.replaceAll("[\\\\/:*?\"<>|]", "-");
            // ------------------------------------------------------------------

            // 2. CARGAR REPORTE
            String rutaArchivo = "/reports/Blank_A4.jrxml";
            InputStream reporteStream = GeneradorFactura.class.getResourceAsStream(rutaArchivo);

            if (reporteStream == null) {
                System.err.println("❌ No se encuentra el archivo .jrxml");
                mostrarAlerta("Error", "No se encuentra la plantilla del reporte.");
                return;
            }

            // 3. COMPILAR
            JasperReport reporteCompilado = JasperCompileManager.compileReport(reporteStream);

            // 4. PARÁMETROS
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("id_factura", idFactura);

            // 5. LLENAR
            JasperPrint print = JasperFillManager.fillReport(reporteCompilado, parametros, con);

            // --- NUEVO: GUARDAR EN DOCUMENTOS ---
            // Obtenemos la ruta de "Mis Documentos" del usuario
            String userHome = System.getProperty("user.home");
            File documentosFolder = new File(userHome, "Documents");

            // Si "Documents" no existe (ej: Windows en español a veces es "Documentos"), probamos fallback o usamos userHome
            if (!documentosFolder.exists()) {
                documentosFolder = new File(userHome, "Documentos");
            }
            if (!documentosFolder.exists()) {
                documentosFolder = new File(userHome); // Si falla todo, a la carpeta de usuario
            }

            // Nombre del archivo: Factura_FAC-2023-001.pdf
            String nombreArchivo = "Factura_" + numeroFactura + ".pdf";
            File pdfFile = new File(documentosFolder, nombreArchivo);

            // Exportar
            JasperExportManager.exportReportToPdfFile(print, pdfFile.getAbsolutePath());

            System.out.println("✅ ¡PDF Creado!: " + pdfFile.getAbsolutePath());

            // --- NUEVO: ABRIR Y MOSTRAR ALERTA ---
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

            // Usamos Platform.runLater porque estamos en un hilo secundario y la Alerta es visual
            String finalPath = pdfFile.getAbsolutePath();
            Platform.runLater(() -> {
                mostrarAlerta("Factura Generada", "El archivo se ha guardado en:\n" + finalPath);
            });

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> mostrarAlerta("Error", "Error al generar PDF: " + e.getMessage()));
        } finally {
            if (con != null) try { con.close(); } catch (Exception ex) {}
        }
    }

    // Helper para mostrar alertas
    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}