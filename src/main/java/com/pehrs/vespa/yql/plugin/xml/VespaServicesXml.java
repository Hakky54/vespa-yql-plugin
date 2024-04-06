package com.pehrs.vespa.yql.plugin.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VespaServicesXml {

  @JacksonXmlElementWrapper(useWrapping = false)
  @JsonProperty("container")
  List<ContainerXml> containers;

  AdminXml admin;

  ContentXml content;

  public Optional<ContainerXml> getContainer(String containerId) {
    return this.containers.stream().filter(container -> container.getId().equals(containerId))
        .findFirst();
  }
}
