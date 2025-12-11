package businessmanager.management;

import businessmanager.database.ConnectionDAO;

import java.util.ArrayList;

public class Entity {
    int  id, cp, phone;
    String nif, name, address, city, province, country, email;
    char type;

    public Entity(int id, String nif, String name, char type, String address, String city, String province, String country, String email) {
        this.id = id;
        this.nif = nif;
        this.name = name;
        this.type = type;
        this.address = address;
        this.city = city;
        this.province = province;
        this.country = country;
        this.email = email;
    }

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }

    public ArrayList<Bill> getBills() {
        return ConnectionDAO.getFacturas(getNif());
    }
    public void addBill(Bill bill) {
        ConnectionDAO.insertFactura(bill, getNif());
    }
    public void removeBill(Bill bill) {
        ConnectionDAO.deleteFactura(bill.getId());
    }

    public String getNif(){return nif;}
    public void setNif(String nif){this.nif = nif;}

    public String getName(){return name;}
    public void setName(String name){this.name=name;}

    public char getType(){return type;}
    public void setType(char type){this.type = type;}

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

}
