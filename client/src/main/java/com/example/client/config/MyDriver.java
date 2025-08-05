package com.example.client.config;


public class MyDriver extends org.apache.calcite.avatica.remote.Driver{
    static {
        new MyDriver().register();
    }

    @Override
    protected String getConnectStringPrefix() {
        return "jdbc:ah:";
    }
}
