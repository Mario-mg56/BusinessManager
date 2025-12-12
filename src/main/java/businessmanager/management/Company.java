package businessmanager.management;

import businessmanager.database.ConnectionDAO;

import java.util.ArrayList;

public class Company {
    int  cp, phone;
    String nif,name, address, city, province, country, email, taxAddress;

    public Company(String nif, String name, String address, String city, String province, String country, String email, String taxAddress) {
        this.nif = nif;
        this.name = name;
        this.address = address;
        this.city = city;
        this.province = province;
        this.country = country;
        this.email = email;
        this.taxAddress = taxAddress;
    }

    public ArrayList<Product> getInventory() {
        return ConnectionDAO.getProductosPorEmpresa(getNif());
    }
    public void addProduct(Product product) {
        ConnectionDAO.insertProduct(product);
    }
    public void removeProduct(Product product) {
        ConnectionDAO.deleteProduct(product.getId());
    }

    public String getNif(){return nif;}
    public void setNif(String nif){this.nif=nif;}

    public String getName(){return name;}
    public void setName(String name){this.name=name;}

    public String getAddress(){return address;}
    public void setAddress(String address){this.address=address;}

    public int getCp(){return cp;}
    public void setCp(int cp){this.cp=cp;}

    public String getCity(){return city;}
    public void setCity(String city){this.city=city;}

    public String getProvince(){return province;}
    public void setProvince(String province){this.province=province;}

    public String getCountry(){return country;}
    public void setCountry(String country){this.country=country;}

    public int getPhone(){return phone;}
    public void setPhone(int phone){this.phone=phone;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email=email;}

    public String getTaxAddress(){return taxAddress;}
    public void setTaxAddress(String taxAddress){this.taxAddress=taxAddress;}

    public void updateProduct(Product product) {
        ConnectionDAO.updateProduct(product);
    }

}
