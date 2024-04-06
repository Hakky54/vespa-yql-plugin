package com.pehrs.vespa.yql.plugin.xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.pehrs.vespa.yql.plugin.YQL;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.junit.Test;

public class VespaServicesXmlTest {
  XmlMapper xmlMapper = new XmlMapper();

  ObjectMapper mapper = new ObjectMapper();
  ObjectWriter objectWriter = mapper
      .setSerializationInclusion(Include.NON_NULL)
      .writerWithDefaultPrettyPrinter();


  @Test
  public void readSingleNodeServicesXml() throws IOException {
    String xml = YQL.getResource("/vespaTestData/services-single-node.xml");

    VespaServicesXml services = xmlMapper.readValue(xml, VespaServicesXml.class);

    assertEquals(1, services.getContainers().size());


    ContentXml content = services.getContent();
    assertEquals((Integer) 1, content.getRedundancy());
    assertNull(content.getGroup());
    assertNotNull(content.getNodes());
    assertEquals(1, content.getNodes().size());
    assertEquals(2, content.getDocuments().getDocuments().size());
  }

  @Test
  public void readSingleGroupServicesXml() throws IOException {
    String xml = YQL.getResource("/vespaTestData/services-single-group.xml");
    VespaServicesXml services = xmlMapper.readValue(xml, VespaServicesXml.class);

    assertEquals(2, services.getContainers().size());

    ContentXml content = services.getContent();
    assertEquals((Integer) 2, content.getRedundancy());
    assertNull(content.getGroup());
    assertNotNull(content.getNodes());
    assertEquals(3, content.getNodes().size());
    assertEquals(2, content.getDocuments().getDocuments().size());
    assertEquals("feed", services.getContent().getDocuments().getDocumentProcessing().getClusterName());

  }


  @Test
  public void readMultipleGroupServicesXml() throws IOException {
    String xml = YQL.getResource("/vespaTestData/services-multiple-groups.xml");

    VespaServicesXml services = xmlMapper.readValue(xml, VespaServicesXml.class);

    assertEquals(2, services.getContainers().size());

    assertEquals((Integer) 2, services.getContent().getMinRedundancy());
    assertNotNull(services.getContent().getGroup());
    assertEquals(3, services.getContent().getGroup().getGroups().size());
    assertEquals(1, services.getContent().getDocuments().getDocuments().size());
    assertEquals("feed", services.getContent().getDocuments().getDocumentProcessing().getClusterName());

    Optional<ContainerXml> feedContainer = services.getContainer("feed");
    assertTrue(feedContainer.isPresent());
    assertTrue(feedContainer.get().hasDocumentApi());
    assertFalse(feedContainer.get().hasSearchApi());
    assertEquals(2, feedContainer.get().getNodesOrJvm().getNodes().size());
    assertEquals("1|1|*", services.getContent().getGroup().getDistribution().getPartitions());

    AdminXml admin = services.getAdmin();
    assertNotNull(admin.getAdminserver());
    assertEquals("node3", admin.getAdminserver().getHostalias());
    assertEquals(3, admin.getConfigServers().size());
    assertEquals("node0", admin.getConfigServers().get(0).getHostalias());
    assertEquals(3, admin.getClusterControllers().size());
    assertEquals("node0", admin.getClusterControllers().get(0).getHostalias());
    assertEquals(3, admin.getSlobroks().size());
    assertEquals("node0", admin.getSlobroks().get(0).getHostalias());

  }


}
