package com.pehrs.vespa.yql.plugin.trace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.pehrs.vespa.yql.plugin.YqlResult;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;

public class TraceUtilsTest extends TestCase {

  @Test
  public void testTraceUtil() throws JsonProcessingException {
    YqlResult result = new YqlResult(YqlResult.YQL_SAMPLE_RESULT);

    List<YqlTraceMessage> tracesMessages = TraceUtils.getYqlTraceMessages(result);

    tracesMessages.forEach(System.out::println);

    ObjectMapper mapper = new ObjectMapper();

    System.out.println(mapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(tracesMessages)
    );

  }

  @Test
  public void testZipkinSpans() throws JsonProcessingException {
    YqlResult result = new YqlResult(YqlResult.YQL_SAMPLE_RESULT);

    List<ZipkinSpan> spans = TraceUtils.getZipkinSpans(result);
    ObjectMapper mapper = new ObjectMapper();
    ObjectWriter printer = mapper
        // .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .writerWithDefaultPrettyPrinter();
    try {
      System.out.println(printer.writeValueAsString(spans));
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }


  }
}