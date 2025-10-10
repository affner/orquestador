package com.digipro.emulator.database;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 * Administra las conexiones JDBC o JNDI según el modo configurado en database.properties.
 */
public class DatabaseManager {

    private static DatabaseManager instance;
    private Properties props;
    private boolean useJndi;

    private DatabaseManager() {
        loadProperties();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadProperties() {
        props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("database.properties")) {
            if (is != null) {
                props.load(is);
                useJndi = Boolean.parseBoolean(props.getProperty("jdbc.jndi.enabled", "false"));
                System.out.println("[DatabaseManager] Propiedades cargadas correctamente. Modo JNDI=" + useJndi);
            } else {
                throw new RuntimeException("No se encontró database.properties en el classpath.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error cargando configuración de base de datos", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (useJndi) {
            return getConnectionFromJndi();
        } else {
            return getConnectionDirect();
        }
    }

    private Connection getConnectionFromJndi() throws SQLException {
        try {
            String jndiName = props.getProperty("jdbc.jndi.name");
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(jndiName);
            System.out.println("[DatabaseManager] Conexión obtenida vía JNDI: " + jndiName);
            return ds.getConnection();
        } catch (Exception e) {
            throw new SQLException("Error obteniendo conexión vía JNDI", e);
        }
    }

    private Connection getConnectionDirect() throws SQLException {
        String host = props.getProperty("db.host");
        String port = props.getProperty("db.port");
        String dbName = props.getProperty("db.name");
        String user = props.getProperty("db.user");
        String pass = props.getProperty("db.password");

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
        try {
            Class.forName("org.postgresql.Driver");
            Connection cn = DriverManager.getConnection(url, user, pass);
            System.out.println("[DatabaseManager] Conexión directa establecida con " + url);
            return cn;
        } catch (Exception e) {
            throw new SQLException("Error conectando a PostgreSQL (" + url + ")", e);
        }
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }
}
