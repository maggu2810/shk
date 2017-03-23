
package de.maggu2810.shk.persistence.es.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Test implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TransportClient client;

    public Test(final InetSocketTransportAddress addr) {
        client = new PreBuiltTransportClient(Settings.EMPTY);
        client.addTransportAddress(addr);
    }

    @Override
    public void close() {
        client.close();
    }

    private void printResponse(final IndexResponse response) {
        // Index name
        final String _index = response.getIndex();
        // Type name
        final String _type = response.getType();
        // Document ID (generated or not)
        final String _id = response.getId();
        // Version (if it's the first time you index this document, you will get: 1)
        final long _version = response.getVersion();
        // status has stored current instance statement.
        final RestStatus status = response.status();

        logger.info("index: {}, type: {}, id: {}, version: {}, status: {}", _index, _type, _id, _version, status);
    }

    private Map<String, Object> getTweet(final String user, final Date date, final String message) {
        final Map<String, Object> json = new HashMap<>();
        json.put("user", user);
        json.put("postDate", date);
        json.put("message", message);
        return json;
    }

    public void test() throws IOException {
        final String index = "twitter2";
        final String type = "tweet";

        Map<String, Object> tweet;

        tweet = getTweet("kimchy", new Date(), "Trying out Elasticsearch");
        printResponse(client.prepareIndex(index, type).setSource(tweet).execute().actionGet());

        tweet = getTweet("foo", new Date(), "Sleeping");
        printResponse(client.prepareIndex(index, type).setSource(tweet).execute().actionGet());

        tweet = getTweet("bar", new Date(), "awake");
        printResponse(client.prepareIndex(index, type).setSource(tweet).execute().actionGet());

        // MatchAll on the whole cluster with all default options
        final SearchResponse response = client.prepareSearch(index).setFrom(0).setSize(60).setExplain(true).get();
        final SearchHit[] results = response.getHits().getHits();
        for (final SearchHit hit : results) {
            final String sourceAsString = hit.getSourceAsString();
            logger.info("index: {}, type: {}, id: {}; {}", hit.getIndex(), hit.getType(), hit.getId(), sourceAsString);
        }
    }

    public static InetSocketTransportAddress getSocketTransportAddress() {
        final InetAddress addr;
        try {
            addr = InetAddress.getByName("127.0.0.1");
        } catch (final UnknownHostException ex) {
            return null;
        }
        return new InetSocketTransportAddress(addr, 9300);
    }

    public static void main(final String[] args) throws Exception {
        final Test test = new Test(Test.getSocketTransportAddress());

        test.test();

        // on shutdown
        test.close();
    }

}
