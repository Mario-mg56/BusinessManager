package businessmanager.management;

public class Product {
    private int id, code, quantity, purchasePrize, sellingPrice, iva;
    private Entity supplier;
    String description;
    public Product(int id, int code, int quantity, Entity supplier, int purchasePrize, int sellingPrice, int iva, String description) {
        this.id = id;
        this.code = code;
        this.quantity = quantity;
        this.supplier = supplier;
        this.purchasePrize = purchasePrize;
        this.sellingPrice = sellingPrice;
        this.iva = iva;
        this.description = description;
    }

    @Override
    public String toString() {return "Product [id=" + id + ", code=" + code + ", quantity=" + quantity + ", supplier=" + supplier + "]";}

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public int getCode() {return code;}
    public void setCode(int code) {this.code = code;}

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}

    public Entity getSupplier() {return supplier;}
    public void setSupplier(Entity supplier) {this.supplier = supplier;}

    public int getPurchasePrize() {return purchasePrize;}
    public void setPurchasePrize(int purchasePrize) {this.purchasePrize = purchasePrize;}

    public int getSellingPrice() {return sellingPrice;}
    public void setSellingPrice(int sellingPrice) {this.sellingPrice = sellingPrice;}

    public int getIva() {return iva;}
    public void setIva(int iva) {this.iva = iva;}

    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}

}
