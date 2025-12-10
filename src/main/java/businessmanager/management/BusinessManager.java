package businessmanager.management;

import businessmanager.database.ConnectionDAO;

import java.util.ArrayList;

public class BusinessManager {
    private static BusinessManager instance;
    public boolean editing;

    private Company currentCompany;
    private BusinessManager() {
        editing = false;
    }

    public void addCompany(Company company) {ConnectionDAO.insertEmpresa(company);}
    public void updateCompany(Company newCompany) {ConnectionDAO.updateEmpresa(newCompany);}
    public void deleteCompany(String nif){ConnectionDAO.deleteEmpresa(nif);}

    public void addEntity(Entity company) {ConnectionDAO.insertEntity(company);}
    public void updateEntity(Entity newEntity) {ConnectionDAO.updateEntity(newEntity);}
    public void deleteEntity(String nif){ConnectionDAO.deleteEntity(nif);}

    public static BusinessManager getInstance() {
        if (instance == null) instance = new BusinessManager();
        return instance;
    }

    public Company getCurrentCompany() {return currentCompany;}
    public void setCurrentCompany(Company currentCompany) {this.currentCompany = currentCompany;}
}
