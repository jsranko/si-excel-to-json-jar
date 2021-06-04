package de.sranko_informatik.si_excel_to_json_jar_gui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import javax.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private Environment env;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.httpBasic().disable();
        http.csrf().disable();
    }

    @PostConstruct
    private void configureSSL() {
        System.setProperty("javax.net.ssl.trustStore", env.getProperty("server.ssl.trust-store"));
        System.out.println("\"javax.net.ssl.trustStore\":".concat(System.getProperty("javax.net.ssl.trustStore")));
        System.setProperty("javax.net.ssl.trustStorePassword",env.getProperty("server.ssl.trust-store-password"));
        System.out.println("\"javax.net.ssl.trustStorePassword\":".concat(System.getProperty("javax.net.ssl.trustStorePassword")));
        System.setProperty("javax.net.ssl.trustStoreType",env.getProperty("server.ssl.trust-store-type"));
        System.out.println("\"javax.net.ssl.trustStoreType\":".concat(System.getProperty("javax.net.ssl.trustStoreType")));
    }
}