package de.sranko_informatik.si_excel_to_json_jar_core;

import org.apache.commons.io.FileUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Service
public class FileService {

    private Environment env;

    @Value("${server.ssl.key-store}")
    public String keyStore;

    @Value("${app.upload.dir:${user.home}}")
    public String uploadDir;

    private Path cacheFile;
    private String name;
    private long size;
    private String contentType;

    public void uploadFile(MultipartFile file) {


        try {
            this.cacheFile = Paths
                    .get(uploadDir + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), this.cacheFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileStorageException("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!");
        }

        this.setName(file.getOriginalFilename());
        this.setSize(file.getSize());
        this.setType(file.getContentType());

    }

    public void processFile(MultipartFile file) {
        try {

            this.uploadFile(file);
            //this.parseFile(this.cacheFile);

        } catch (FileStorageException e) {
            e.printStackTrace();
        }
    }

    public JSONObject parseFile(MultipartFile file) throws IOException {

        return ExcelParser.getJSONObject(file);
    }

    public TainasResponse sendData(JSONObject callbackData, JSONObject clientInfo, String callbackUrl, String trustStore, String password) throws IOException {

        Logger logger = LoggerFactory.getLogger(ExcelParser.class);

        JSONObject payload = new JSONObject()
                .put("callbackData", callbackData)
                .put("clientInfo", clientInfo);

        logger.debug(String.format("Payload erstellt: %s,", payload.toString()));

        try {

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<String>(payload.toString(), headers);

            //RestTemplate restTemplate = getRestTemplate1();
            //RestTemplate restTemplate = getRestTemplate2();
            RestTemplate restTemplate = getRestTemplate3(trustStore, password);

            logger.debug(String.format("Payload wird an %s übermittelt.", callbackUrl.toString()));

            TainasResponse response = restTemplate.postForObject(callbackUrl, entity, TainasResponse.class);

            logger.debug(String.format("Response erhalten: %s", response.toString()));

            return response;

        } catch (HttpStatusCodeException ex) {
            return new TainasResponse("Error", "HttpStatusCodeException", ex.getMessage(), "N/A");
        }

    }


    public void setName(String originalFilename) {
        this.name = originalFilename;
    }

    public void setSize(long size) { this.size = size; }

    public void setType(String contentType) {
        this.contentType = contentType;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getHumanReadableSize() {

        return FileUtils.byteCountToDisplaySize(size);
    }

    public String getContentType() {
        return contentType;
    }

    private RestTemplate getRestTemplate1() {
        try {

            //String keyStore = env.getProperty("server.ssl.key-store");
            //String trustStore = env.getProperty("server.ssl.trust-store");

            SSLContext sslContext = SSLContexts.custom()
                    // keystore wasn't within the question's scope, yet it might be handy:
                    .loadKeyMaterial(
                            new File(getClass().getClassLoader().getResource("keystore/keystore.jks").getFile()),
                            "password".toCharArray(),
                            "password".toCharArray())
                    .loadTrustMaterial(
                            new File(getClass().getClassLoader().getResource("keystore/truststore.ts").getFile()),
                            "password".toCharArray(),
                            // use this for self-signed certificates only:
                            new TrustSelfSignedStrategy())
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    // use NoopHostnameVerifier with caution, see https://stackoverflow.com/a/22901289/3890673
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()))
                    .build();

            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private RestTemplate getRestTemplate2() {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);

        return new RestTemplate(requestFactory);

    }

    private RestTemplate getRestTemplate3(String trustStore, String password) {
        try {

            //String keyStore = env.getProperty("server.ssl.key-store");
            //String trustStore = env.getProperty("server.ssl.trust-store");

            SSLContext sslContext = SSLContexts.custom()

                    .loadTrustMaterial(
                            ResourceUtils.getFile(trustStore),
                            password.toCharArray())
                    .build();

            CloseableHttpClient httpClient = HttpClients.custom()
                    // use NoopHostnameVerifier with caution, see https://stackoverflow.com/a/22901289/3890673
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, new NoopHostnameVerifier()))
                    .build();

            return new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }
}
