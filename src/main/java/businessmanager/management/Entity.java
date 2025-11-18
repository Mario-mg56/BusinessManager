package businessmanager.management;

import java.util.ArrayList;

public class Entity {
    int id, cp, phone;
    String name, address, city, province, country, email;
    char type;
    ArrayList<Bill> bills;

    public Entity(int id, String name, char type, String address, String city, String province, String country, String email) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.address = address;
        this.city = city;
        this.province = province;
        this.country = country;
        this.email = email;
        this.bills = new ArrayList();
    }
    
    public ArrayList<Bill> getBills() {return bills;}
    public void setBills(ArrayList<Bill> bills) {this.bills = bills;}
    public void addBill(Bill bill) {bills.add(bill);}
    public void removeBill(Bill bill) {bills.remove(bill);}

    public int getId(){return id;}
    public void setNif(int id){this.id=id;}

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
