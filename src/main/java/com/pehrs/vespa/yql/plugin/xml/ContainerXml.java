package com.pehrs.vespa.yql.plugin.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Value
@Builder
@Getter
@Setter
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContainerXml {

  String id;
  String version;

  SearchXml search;

  @JsonProperty("document-api")
  DocumentApiXml documentApi;

  // @JacksonXmlElementWrapper(useWrapping = true, localName = "nodes")
  // List<NodeXml> nodes;
  @JsonProperty("nodes")
  NodesOrJvmXml nodesOrJvm;

  @JsonProperty("document-processing")
  DocumentProcessingXml documentProcessing;

  @JsonIgnore
  public boolean hasSearchApi() {
    return this.search != null;
  }

  @JsonIgnore
  public boolean hasDocumentApi() {
    return this.documentApi != null;
  }

//  @JsonIgnore
//  public List<NodeXml> getProperNodes() {
//    return this.nodes.stream().filter(NodeXml::isProperNode).collect(Collectors.toList());
//  }
}
