package businessmanager.management;

import businessmanager.database.ConnectionDAO;

import java.util.ArrayList;

public class BusinessManager {
    private static BusinessManager instance;
    public boolean editing;

    private Company currentCompany;
    public BusinessManager() {
        editing = false;
    }

    public void addCompany(Company company) {ConnectionDAO.insertEmpresa(company);}
    public void updateCompany(Company newCompany) {ConnectionDAO.updateEmpresa(newCompany);}
    public void deleteCompany(String nif){ConnectionDAO.deleteEmpresa(nif);}

    public static BusinessManager getInstance() {
        if (instance == null) instance = new BusinessManager();
        return instance;
    }

    public Company getCurrentCompany() {return currentCompany;}


    public void setCurrentCompany(Company currentCompany) {this.currentCompany = currentCompany;}


}
