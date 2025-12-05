

import businessmanager.database.ConexionDB;
import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class TestFacturaReal {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA DEFINITIVA (Compilando .jrxml) ===");
        
        Connection con = null;

        try {
            // 1. CONEXIÓN
            con = ConexionDB.getConnection();
            if (con == null) return;

            // 2. VERIFICAR ARCHIVO
            // Buscamos el código fuente (.jrxml), no el compilado (.jasper)
            String rutaArchivo = "/reports/Blank_A4.jrxml";
            InputStream reporteStream = TestFacturaReal.class.getResourceAsStream(rutaArchivo);

            if (reporteStream == null) {
                System.err.println("❌ ERROR CRÍTICO: No se encuentra el archivo '" + rutaArchivo + "'");
                System.err.println("   PASOS PARA ARREGLARLO:");
                System.err.println("   1. Copia 'Blank_A4.jrxml' desde Jaspersoft Studio.");
                System.err.println("   2. Pégalo en NetBeans en: src/main/resources/reports/");
                System.err.println("   3. Haz clic derecho en el proyecto -> 'Clean and Build'.");
                return;
            } else {
                System.out.println("✅ Archivo .jrxml encontrado. Procediendo a compilar...");
            }

            // 3. COMPILAR (Esto elimina el error de versiones)
            JasperReport reporteCompilado = JasperCompileManager.compileReport(reporteStream);
            System.out.println("✅ Compilación exitosa en memoria.");

            // 4. PARÁMETROS
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("id_factura", 1); 

            // 5. LLENAR Y EXPORTAR
            System.out.println("Generando PDF...");
            JasperPrint print = JasperFillManager.fillReport(reporteCompilado, parametros, con);

            File pdfFile = File.createTempFile("Factura_Final_", ".pdf");
            JasperExportManager.exportReportToPdfFile(print, pdfFile.getAbsolutePath());
            
            System.out.println("✅ ¡PDF Creado!: " + pdfFile.getAbsolutePath());
            
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR:");
            e.printStackTrace();
        } finally {
            if (con != null) try { con.close(); } catch (Exception ex) {}
        }
    }
}