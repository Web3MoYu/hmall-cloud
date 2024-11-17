package com.hmall.item.es;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ESTest {

    private RestHighLevelClient client;

    @Test
    void testConnection() {
        System.out.println("client: " + client);
    }


    @BeforeEach
    public void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://localhost:9200")));
    }

    @AfterEach
    public void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
    }
}
