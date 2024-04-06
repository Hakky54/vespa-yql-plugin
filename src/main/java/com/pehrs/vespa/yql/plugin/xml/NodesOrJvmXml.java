package com.pehrs.vespa.yql.plugin.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodesOrJvmXml {

  JvmXml jvm;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("node")
  List<NodeXml> nodes;
}
