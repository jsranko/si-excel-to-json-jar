package de.sranko_informatik.si_excel_to_json_jar_core;

import org.apache.commons.io.FileUtils;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicStatusLine;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {
    private static final BasicStatusLine HTTP_200_STATUS_LINE = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
    private static final BasicStatusLine HTTP_500_STATUS_LINE = new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 500, "Error");


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

    public String sendData(JSONObject callbackData, JSONObject clientInfo, String callbackUrl) throws IOException {

        JSONObject payload = new JSONObject()
                .put("callbackData", callbackData)
                .put("clientInfo", clientInfo);

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(callbackUrl);

        StringEntity entity = new StringEntity(payload.toString());
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        httpPost.setHeader("Accept", "application/json");

        CloseableHttpResponse response = client.execute(httpPost);
        client.close();

        if (HTTP_200_STATUS_LINE.equals(response.getStatusLine())) {
            return "Upload war erfolgreich";
        } else if (HTTP_500_STATUS_LINE.equals(response.getStatusLine())) {
            return "Upload ist fehlgeschlagen";
        } else {
            return String.format("Server % ist nicht erreichbar", callbackUrl);
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


}
