package com.pehrs.vespa.yql.plugin;

import ai.vespa.feed.client.DocumentId;
import ai.vespa.feed.client.FeedClient;
import ai.vespa.feed.client.FeedClientBuilder;
import ai.vespa.feed.client.OperationParameters;
import ai.vespa.feed.client.Result;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.junit.Ignore;
import org.junit.Test;

public class VespaDocApiTest {

  @Test
  @Ignore
  public void testDocApi() {

    // http://localhost:8080/document/v1/embeddings/books/docid/466F756E646174696F6E20616E64204561727468202D204973616163204173696D6F762E65707562-256

    // id:embeddings:books::466F756E646174696F6E20616E64204561727468202D204973616163204173696D6F762E65707562-256

    try (FeedClient client = FeedClientBuilder.create(
        URI.create("http://localhost:8080/")
    ).build()) {
      DocumentId id = DocumentId.of(
          "embeddings", "books", "466F756E646174696F6E20616E64204561727468202D204973616163204173696D6F762E65707562-256"
      );
      // String json = "{\"fields\": {\"title\": \"hello world2\"}}";
      String json = "{\"fields\": {\"content\": {\"assign\": \"content\"}}}";
      OperationParameters params = OperationParameters.empty()
          .timeout(Duration.ofSeconds(5))
          // .route("myvesparoute")
      ;
      CompletableFuture<Result> promise = client.put(id, json, params);
      promise.whenComplete(((result, throwable) -> {
        if (throwable != null) {
          throwable.printStackTrace();
        } else {
          System.out.printf("'%s' for document '%s': %s%n",
              result.type(), result.documentId(), result.resultMessage()
          );
        }
      }));
    }

  }

}
