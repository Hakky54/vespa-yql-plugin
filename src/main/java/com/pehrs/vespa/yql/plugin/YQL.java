package com.pehrs.vespa.yql.plugin;

import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.io.IOException;
import java.util.List;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Static facade for global functions and constants
 */
public abstract class YQL {
  static Logger log = LoggerFactory.getLogger(YQL.class);

  private YQL(){}

  public static List<String> YQL_KEYWORDS = List.of(
      // Basic keywords
      "select",
      "from",
      "where",
      "order by",
      "limit",
      "offset",
      "timeout",
      // where keywords
      "nearestNeighbor",
      "weightedSet",
      "predicate",
      "dotProduct",
      "userQuery",
      "nonEmpty",
      "userInput",
      "geoLocation",
      "sameElement",
      "matches",
      "range",
      "contains",
      "weakAnd",
      "phrase",
      "fuzzy",
      "equiv",
      "onear",
      "wand",
      "true",
      "false",
      "rank",
      "near",
      "and",
      "not",
      "uri",
      "or"
  );

  public static YqlResult executeQuery(String yqlQueryRequest) {

    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();

    HttpPost request = new HttpPost(settings.getCurrentQueryUrl());
    request.addHeader("content-type", "application/json;charset=UTF-8");
    StringEntity entity = new StringEntity(yqlQueryRequest, ContentType.APPLICATION_JSON);
    request.setEntity(entity);

    try (CloseableHttpClient httpClient = HttpClients.custom()
        .build()) {
      CloseableHttpResponse response = httpClient.execute(request);
      try {
        String responseString = EntityUtils.toString(response.getEntity());
        log.debug("HTTP Response Status: " + response.getStatusLine());
        log.trace("HTTP Response: " + responseString);
        return new YqlResult(responseString);
      } finally {
        response.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
