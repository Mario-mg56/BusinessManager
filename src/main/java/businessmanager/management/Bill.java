package businessmanager.management;

import java.time.LocalDate;
import java.util.ArrayList;

public class Bill {
    private int id, number, baseImponible, iva;
    private String status, concept, observations;
    private LocalDate issueDate;
    private char type;
    private Entity thirdParty;
    private ArrayList<Product> products;
    public Bill(int id, int number, ArrayList<Product> products, Entity thirdParty,
                int baseImponible, String status, String concept, String observations, LocalDate issueDate, char type) {
        this.id = id;
        this.number = number;
        this.products = products;
        this.thirdParty = thirdParty;
        this.baseImponible = baseImponible;
        this.status = status;
        this.concept = concept;
        this.observations = observations;
        this.issueDate = issueDate;
        this.type = type;
    }

    public int getTotal(){
        int total = 0;
        if (type == 'C') for (Product p : products) total += p.getPurchasePrize();
        else for (Product p : products) total += p.getSellingPrice();
        return total;
    }

    public Product getProduct(int id){
        for (Product p : products) if (id == p.getId()) return p;
        return null;
    }
    public ArrayList<Product> getProducts() {return products;}
    public void setProducts(ArrayList<Product> products) {this.products = products;}
    public void addProduct(Product product){products.add(product);}
    public void deleteProduct(Product p){products.remove(p);}

    public int getId(){return id;}
    public void setId(int id){this.id=id;}

    public int getNumber(){return number;}
    public void setNumber(int number){this.number=number;}

    public int getBaseImponible(){return baseImponible;}
    public void setBaseImponible(int baseImponible){this.baseImponible=baseImponible;}

    public int getIva(){return iva;}
    public void setIva(int iva){this.iva=iva;}

    public String getStatus(){return status;}
    public void setStatus(String status){this.status=status;}

    public String getConcept(){return concept;}
    public void setConcept(String concept){this.concept=concept;}

    public String getObservations(){return observations;}
    public void setObservations(String observations){this.observations=observations;}

    public LocalDate getIssueDate(){return issueDate;}
    public void setIssueDate(LocalDate issueDate){this.issueDate=issueDate;}
}
