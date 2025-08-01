package com.example.client.config;


import org.springframework.jdbc.datasource.AbstractDataSource;


import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class AvaticaDataSource extends AbstractDataSource{

    private final RemoteAvaticaConfig config;

    static {
        try {
            Class.forName("org.apache.calcite.avatica.remote.Driver");
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public AvaticaDataSource(RemoteAvaticaConfig config) {
        this.config = config;
    }
    @Override
    public Connection getConnection() throws SQLException {
        String url = config.getUrl() + ";serialization=" + config.getSerialization();
        return DriverManager.getConnection(url);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        String url = config.getUrl() + ";serialization=" + config.getSerialization();
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return super.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter pw) throws SQLException {
        super.setLogWriter(pw);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLoginTimeout(int timeout) throws SQLException {
        DriverManager.setLoginTimeout(timeout);
    }

    @Override
    public Logger getParentLogger() {
        return super.getParentLogger();
    }
}
