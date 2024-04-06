package com.pehrs.vespa.yql.plugin.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.w3c.dom.Node;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminXml {

  NodeXml adminserver;

  @JacksonXmlElementWrapper(useWrapping = true, localName = "configservers")
  @JsonProperty("configservers")
  List<NodeXml> configServers;


  @JacksonXmlElementWrapper(useWrapping = true, localName = "cluster-controllers")
  @JsonProperty("cluster-controllers")
  List<NodeXml> clusterControllers;


  @JacksonXmlElementWrapper(useWrapping = true, localName = "slobroks")
  @JsonProperty("slobroks")
  List<NodeXml> slobroks;

}
