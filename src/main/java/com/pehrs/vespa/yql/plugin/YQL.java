package com.pehrs.vespa.yql.plugin;

import com.pehrs.vespa.yql.plugin.settings.YqlAppSettingsState;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
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

  private static FileSystem jarFs = null;

  public static String getResource(String resourceName) {
    try {

      URL url;
      url = YQL.class.getResource(resourceName);
      // Workaround for java.nio.file.FileSystemNotFoundException issue in Intellij plugins
      final Map<String, String> env = new HashMap<>();
      final String[] array = url.toURI().toString().split("!");
      if(array.length > 1) {
        if(jarFs == null) {
          jarFs = FileSystems.newFileSystem(URI.create(array[0]), env);
        }
        final Path path = jarFs.getPath(array[1]);
        return Files.readString(path, Charset.forName("utf-8"));
      } else {
        return Files.readString(Paths.get(url.toURI()), Charset.forName("utf-8"));
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }


  public static String getBuildTimestamp() {

    try {
      String propsStr = YQL.getResource("/build-info.properties");
      Properties properties = new Properties();
      try (StringReader reader = new StringReader(propsStr)) {
        properties.load(reader);
        String ts = properties.getProperty("build-timestamp");
        if(ts == null) {
          return "";
        }
        return "Build: " + ts;
      }
    } catch (Exception ex) {
      return "";
    }
  }

  public static String getDefaultBrowserScript() {
    String OS = System.getProperty("os.name", "linux").toLowerCase(Locale.ENGLISH);
    if ((OS.contains("mac")) || (OS.contains("darwin"))) {
      return "open";
    } else if (OS.contains("win")) {
      return "start";
    } else {
      return "/usr/bin/xdg-open";
    }
  }

}
