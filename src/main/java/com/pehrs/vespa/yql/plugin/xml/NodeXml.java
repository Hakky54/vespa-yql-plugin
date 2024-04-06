package com.pehrs.vespa.yql.plugin.xml;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.sun.xml.txw2.annotation.XmlElement;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper=false)
@JsonTypeName("node")
@XmlElement("node")
public class NodeXml {

  String hostalias;

  @JsonProperty("jvm-options")
  String jvmOptions;

  /**
   * Only used in context of a Content setup
   */
  @JsonProperty("distribution-key")
  String distributionKey;

}
