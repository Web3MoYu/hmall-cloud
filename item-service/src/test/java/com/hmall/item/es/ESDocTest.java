package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.item.domain.po.Item;
import com.hmall.item.domain.po.ItemDoc;
import com.hmall.item.service.IItemService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class ESDocTest {

    private RestHighLevelClient client;

    @Resource
    private IItemService itemService;

    @Test
    void testIndexDoc() throws IOException {
        // 0.准备文档数据
        Item item = itemService.getById(317578L);
        ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
        // 1.准备Request
        // 既可以增加又可以全量需改，只要id一样就会将之前的删除再把当前的写进去
        IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
        // 2.准备请求参数
        request.source(JSON.toJSONString(itemDoc), XContentType.JSON);
        // 3.发送请求
        client.index(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetDoc() throws IOException {
        // 1.准备Request
        GetRequest request = new GetRequest("items", "317578");
        // 3.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        ItemDoc bean = JSONUtil.toBean(response.getSourceAsString(), ItemDoc.class);
        System.out.println(bean);
    }

    @Test
    void testDeleteDoc() throws IOException {
        // 1.准备Request
        DeleteRequest request = new DeleteRequest("items", "317578");
        // 3.发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }

    @Test
    void testUpdateDoc() throws IOException {
        // 1.准备request
        UpdateRequest request = new UpdateRequest("items", "317578");
        // 2.准备doc参数
        request.doc(
                "sold", 11,
                "price", 9999
        );
        // 3.请求
        client.update(request, RequestOptions.DEFAULT);
    }

    // 文档的批处理
    @Test
    void testBulk() throws InterruptedException {
        int pageNo = 1, pageSize = 500;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 30,
                TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        int count = itemService.lambdaQuery()
                .eq(Item::getStatus, 1).count();
        CountDownLatch latch = new CountDownLatch((count / 500) + 1);
        while (true) {
            // 0.准备文档数据
            Page<Item> page = itemService.lambdaQuery()
                    .eq(Item::getStatus, 1)
                    .page(Page.of(pageNo, pageSize));
            List<Item> records = page.getRecords();
            if (records == null || records.isEmpty()) {
                latch.await();
                return;
            }
            executor.submit(() -> {
                // 1.准备request
                BulkRequest request = new BulkRequest();
                // 2.准备批处理
                for (Item item : records) {
                    ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                    request.add(new IndexRequest("items")
                            .id(item.getId().toString())
                            .source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
                }
                // 3.发送请求
                try {
                    client.bulk(request, RequestOptions.DEFAULT);
                    latch.countDown();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            pageNo++;
        }
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
