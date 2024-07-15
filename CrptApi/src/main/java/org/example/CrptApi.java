package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


@Getter
@Setter
public class CrptApi {

    private TimeUnit timeUnit;
    private Semaphore semaphore;
    private int requestLimit;
    private ScheduledExecutorService scheduledExecutorService;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
        this.semaphore = new Semaphore(requestLimit);
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    @Getter
    @Setter
    public static class Description {
        private String participantlnn;
    }

    @Getter
    @Setter
    public static class Document_Info {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private Date production_date;
        private String production_type;
        private Date reg_date;
        private String reg_number;
    }

    @Getter
    @Setter
    public static class Product {
        private String certificate_document;
        private Date certificate_document_date;

        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private Date production_date;
        private String tnved_Code;
        private String uit_code;
        private String uitu_code;

    }

    public void createDocument(Object document, String signature) throws IOException, ParseException, InterruptedException {
        synchronized (this)
        {

            semaphore.acquire();
            scheduledExecutorService.scheduleAtFixedRate(()->semaphore.release(),1,1,timeUnit);
            ObjectMapper objectMapper = new ObjectMapper();

            String json = objectMapper.writeValueAsString(document);

            StringEntity stringEntity = new StringEntity(json);

            HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");;
            httpPost.setEntity(stringEntity);
            httpPost.setHeader("Content-type","/application/json");
            httpPost.setHeader("Signature",signature);

            try (CloseableHttpClient client = HttpClients.createDefault();
                 CloseableHttpResponse response = client.execute(httpPost)) {
                HttpEntity responseEntity = response.getEntity();
                String responseInStringFormat = EntityUtils.toString(responseEntity,"UTF-8");
                System.out.println("Document created "+responseInStringFormat);
            }
        }
    }
    public static void main(String[] args) throws IOException, ParseException, InterruptedException {

        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS,5);
        Object document = new Document_Info();
        String signature = "signature";
        crptApi.createDocument(document,signature);

    }
}
