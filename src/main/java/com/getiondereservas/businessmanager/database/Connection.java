package com.getiondereservas.businessmanager.database;

import java.io.InputStream;
import java.sql.DriverManager;
import java.util.Properties;

public class Connection {

    java.sql.Connection connection;

    protected java.sql.Connection getConnection() {

        String url, user, password;

        try{

            InputStream input = getClass().getResourceAsStream("/database.properties");
            Properties prop = new Properties();
            prop.load(input);

            url = prop.getProperty("url");
            user = prop.getProperty("user");
            password = prop.getProperty("password");

            connection = DriverManager.getConnection(url, user, password);

            return connection;

        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
}
