package de.sranko_informatik.si_excel_to_json_jar_gui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class GuiApplication {

    @Value("${server.ssl.trust-store}")
    private Resource keyStore;

    @Value("${server.ssl.trust-store-password}")
    private String keyStorePassword;

    public static void main(String[] args) {SpringApplication.run(GuiApplication.class, args);

    }

    private void setUpTrustStoreForApplication() throws IOException {

        System.out.println(keyStore.getURI().toString());
        System.setProperty("javax.net.ssl.trustStore", keyStore.getURI().toString());
        System.setProperty("javax.net.ssl.trustStorePassword", this.keyStorePassword);
    }
}