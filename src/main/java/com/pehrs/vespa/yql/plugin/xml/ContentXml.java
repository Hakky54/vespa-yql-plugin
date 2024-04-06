package com.pehrs.vespa.yql.plugin.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class ContentXml {

  String id;

  Integer redundancy;

  @JsonProperty("min-redundancy")
  Integer minRedundancy;

  DocumentsXml documents;

  // Top level nodes only
  @JacksonXmlElementWrapper(useWrapping = true)
  List<NodeXml> nodes;

  GroupXml group;

  @JsonIgnore
  public int getGroupCount() {
    if(this.group != null) {
      return this.group.getGroups().size();
    }
    if(this.nodes != null) {
      return nodes.size();
    }
    return 0;
  }

}
