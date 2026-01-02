package org.example;
import java.sql.*;

public class DBManager {
    private static DBManager instance;
    private Connection connection;

    private DBManager() {
        try {
            String url = "jdbc:sqlite:Database/SportManager.db";
            connection = DriverManager.getConnection(url);
            System.out.println("Connessione al database");
        } catch (SQLException e) {
            System.err.println("Errore di connessione: " + e.getMessage());
        }
    }

    public static DBManager getInstance() {
        if (instance == null)
            instance = new DBManager();
        return instance;
    }

    // check => evita ripetizioni codice
    private boolean checkConnection() {
        try {
            if (connection == null || !connection.isValid(5)) {
                System.err.println("Errore di connessione");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Errore di connessione: " + e.getMessage());
            return false;
        }
        return true;
    }

}