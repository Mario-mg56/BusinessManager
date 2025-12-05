package businessmanager.database;

import businessmanager.management.Company;
import businessmanager.management.Entity;
import businessmanager.management.Product;

public class DataStore {
    public static Company selectedCompany;
    public static Entity selectedEntity;
    public static Product selectedProduct;
    public static String checkCSPText;
    public static String typeCSP;

    // Getters y Setters
    public static String getCheckCSPText() { return checkCSPText; }
    public static void setCheckCSPText(String checkCSPText) { DataStore.checkCSPText = checkCSPText; }

    public static String getTypeCSP() { return typeCSP; }
    public static void setTypeCSP(String typeCSP) { DataStore.typeCSP = typeCSP; }

    // Limpiar todos los datos
    public static void clear() {
        selectedCompany = null;
        selectedEntity = null;
        selectedProduct = null;
        checkCSPText = null;
        typeCSP = null;
    }
}