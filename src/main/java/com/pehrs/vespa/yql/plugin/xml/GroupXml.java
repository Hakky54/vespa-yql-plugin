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
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupXml {

  String name;

  DisributionXml distribution;

  @JsonProperty("distribution-key")
  String distributionKey;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("node")
  List<NodeXml> nodes;

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("group")
  List<GroupXml> groups;

}
