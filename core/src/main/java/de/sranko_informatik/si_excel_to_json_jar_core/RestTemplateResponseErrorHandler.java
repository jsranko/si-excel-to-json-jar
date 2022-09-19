package de.sranko_informatik.si_excel_to_json_jar_core;

import org.apache.commons.io.IOUtils;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.HttpStatus.Series.CLIENT_ERROR;
import static org.springframework.http.HttpStatus.Series.SERVER_ERROR;

@Component
public class RestTemplateResponseErrorHandler
        implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
            throws IOException {

        return (
                httpResponse.getStatusCode().series() == CLIENT_ERROR
                        || httpResponse.getStatusCode().series() == SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
            throws IOException {

        throw new RestClientException(String.format("Server-Error: Code: %s, Text: %s, Response: %s.", httpResponse.getStatusCode(),
                httpResponse.getStatusText(),
                IOUtils.toString(httpResponse.getBody(), StandardCharsets.UTF_8)));
//        if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
//            throw new RestClientException(String.format("Server-Error: Code: %s, Text: %s.", httpResponse.getStatusCode(), httpResponse.get));
//        } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
//            // handle CLIENT_ERROR
//            if (httpResponse.getStatusCode() == HttpStatus.NOT_FOUND) {
//                throw new RestClientException("URL/URI nicht gefunden");
//            }
//        }
    }

}
