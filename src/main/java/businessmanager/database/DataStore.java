package businessmanager.database;

import businessmanager.management.Company;

public class DataStore { //Clase provicional para guardar datos con el método estático, a futuro lo pueden borrar, da igual xd 👍👌
    public static Company selectedCompany;
    public static String checkCSPText;
    public static String typeCSP;

    public static String getCheckCSPText() {
        return checkCSPText;
    }

    public static void setCheckCSPText(String checkCSPText) {
        DataStore.checkCSPText = checkCSPText;
    }

    public static String getTypeCSP() {
        return typeCSP;
    }

    public static void setTypeCSP(String typeCSP) {
        DataStore.typeCSP = typeCSP;
    }
}