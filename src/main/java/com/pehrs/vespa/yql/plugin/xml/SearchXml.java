package com.pehrs.vespa.yql.plugin.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchXml {

}
