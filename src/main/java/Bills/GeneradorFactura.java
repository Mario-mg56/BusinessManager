package Bills;

import businessmanager.database.ConexionDB;
import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import net.sf.jasperreports.engine.JasperCompileManager; // <--- FALTABA ESTE IMPORT
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport; // <--- FALTABA ESTE IMPORT

public class GeneradorFactura {

    public void generarFactura(int idFactura) {
        
        new Thread(() -> {
            Connection con = null;
            try {
                // 1. CARGAR EL ARCHIVO FUENTE (.JRXML)
                // Asegúrate que en NetBeans tienes 'Blank_A4.jrxml' en src/main/resources/reports/
                InputStream reporteStream = getClass().getResourceAsStream("/reports/Blank_A4.jrxml");
                
                if (reporteStream == null) {
                    mostrarErrorFX("Error de Archivo", "No se encuentra el archivo .jrxml en /reports/");
                    return;
                }

                // 2. CONECTAR
                con = ConexionDB.getConnection();
                if (con == null) {
                    mostrarErrorFX("Error de Conexión", "No se pudo conectar a la base de datos.");
                    return;
                }

                // --- EL PASO QUE FALTABA: COMPILAR ---
                // Convertimos el XML (.jrxml) en un objeto Java (JasperReport)
                JasperReport reporteCompilado = JasperCompileManager.compileReport(reporteStream); // <--- ESTO ES CRUCIAL

                // 3. PARÁMETROS
                Map<String, Object> parametros = new HashMap<>();
                parametros.put("id_factura", idFactura);

                // 4. LLENAR REPORTE (Usamos 'reporteCompilado', NO el stream directo)
                JasperPrint print = JasperFillManager.fillReport(reporteCompilado, parametros, con);

                // 5. EXPORTAR A PDF TEMPORAL
                File pdfFile = File.createTempFile("Factura_" + idFactura + "_", ".pdf");
                JasperExportManager.exportReportToPdfFile(print, pdfFile.getAbsolutePath());

                // 6. ABRIR EL ARCHIVO
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    mostrarErrorFX("Error de Sistema", "No se puede abrir el PDF automáticamente.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                mostrarErrorFX("Error al Generar Factura", e.getMessage());
            } finally {
                if (con != null) {
                    try { con.close(); } catch (SQLException e) {}
                }
            }
        }).start();
    }

    private void mostrarErrorFX(String titulo, String contenido) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle(titulo);
            alert.setHeaderText(null);
            alert.setContentText(contenido);
            alert.showAndWait();
        });
    }
}