package com.pehrs.vespa.yql.plugin.deploy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.junit.Test;

public class VespaAppUploaderTest extends TestCase {

  protected static final Pattern labelPattern = Pattern.compile("([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9-]{0,61}[A-Za-z0-9])");
  protected static final Pattern domainNamePattern = Pattern.compile("(" + labelPattern + "\\.)*" + labelPattern + "\\.?");

  @Test
  public void testDomainName() {

//    String regexStr = "(([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9-]{0,61}[A-Za-z0-9])\\\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9-]{0,61}[A-Za-z0-9])\\\\.?";
//    Pattern regex = Pattern.compile(regexStr);

    // Not allowed to use dashes in the names!!!
    // String fqdn = "vespaconfig2.vespa-cluster_vespa";
    String fqdn = "web-0.nginx.default.svc.cluster.local";

    Matcher match = domainNamePattern.matcher(fqdn);

    assertTrue(match.matches());
  }

}