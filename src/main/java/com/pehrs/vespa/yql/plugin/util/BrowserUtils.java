package com.pehrs.vespa.yql.plugin.util;

import com.intellij.ide.browsers.BrowserLauncher;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BrowserUtils {

  public static List<String> extractUrls(String text) {
    List<String> containedUrls = new ArrayList<>();
    // FIXME: What schemes are supported bu BrowserLauncher?
    String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
    Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
    Matcher urlMatcher = pattern.matcher(text);
    while (urlMatcher.find()) {
      containedUrls.add(text.substring(urlMatcher.start(0),
          urlMatcher.end(0)));
    }
    return containedUrls;
  }

  public static void openBrowser(URI uri) throws IOException {
    //    YqlAppSettingsState settings = YqlAppSettingsState.getInstance();
    //    String cmd = String.format("%s %s", settings.browserScript, uri.toString());
    //    Process p = Runtime.getRuntime().exec(cmd);
    BrowserLauncher.getInstance().browse(uri);
  }

}
