package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.remote.AuthenticationType;
import org.apache.calcite.avatica.server.AvaticaServerConfiguration;
import org.apache.calcite.avatica.server.RemoteUserExtractor;

import java.util.concurrent.Callable;
@Slf4j
public class CustomAvaticaServerConfiguration implements AvaticaServerConfiguration {

    private final String userName;
    private final String password;


    public CustomAvaticaServerConfiguration(String userName, String password) {
        this.userName = userName;
        this.password = password;

    }

    @Override
    public AuthenticationType getAuthenticationType() {
        return AuthenticationType.BASIC;
    }

    @Override
    public String getKerberosRealm() {
        return "";
    }

    @Override
    public String getKerberosServiceName() {
        return "";
    }

    @Override
    public String getKerberosHostName() {
        return "";
    }

    @Override
    public String getKerberosPrincipal() {
        return "";
    }

    @Override
    public String[] getAllowedRoles() {
        return new String[]{"user"};
    }

    @Override
    public String getHashLoginServiceRealm() {
        return "CalciteAuthRealm";
    }

    @Override
    public String getHashLoginServiceProperties() {


        return "avatica-users.properties";
    }

    @Override
    public boolean supportsImpersonation() {
        return false;
    }

    @Override
    public <T> T doAsRemoteUser(String s, String s1, Callable<T> callable) throws Exception {
        log.info("Authenticating user: {} from address: {}", s, s1);
        T result = callable.call();
        log.info("User {} authenticated successfully", s);
        return result;
    }

    @Override
    public RemoteUserExtractor getRemoteUserExtractor() {
        return null;
    }
}
