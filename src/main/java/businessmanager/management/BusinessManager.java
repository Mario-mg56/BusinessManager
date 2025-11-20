package businessmanager.management;

import java.util.ArrayList;

public class BusinessManager {
    ArrayList<Company> companies;
    ArrayList<Entity> entities;
    public BusinessManager() {
        companies = new ArrayList<>();
        entities = new ArrayList<>();
    }
    
    public ArrayList<Company> getCompany() {return companies;}
    public void setCompanies(ArrayList<Company> companies) {this.companies = companies;}
    public void addCompany(Company company) {companies.add(company);}
    public void removeCompany(Company company) {companies.remove(company);}
    
    public ArrayList<Entity> getEntity() {return entities;}
    public void setEntities(ArrayList<Entity> entities) {this.entities = entities;}
    public void addEntity(Entity entity) {entities.add(entity);}
    public void removeEntity(Entity entity) {entities.remove(entity);}
}
