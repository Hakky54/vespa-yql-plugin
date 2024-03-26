package com.pehrs.vespa.yql.plugin.dock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.pehrs.vespa.yql.plugin.YqlResult;
import com.pehrs.vespa.yql.plugin.traceorg.TraceRow;
import com.pehrs.vespa.yql.plugin.traceorg.TraceUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;

public class YqlTraceTableModelTest extends TestCase {


  @Test
  public void testTreeParsing() throws JsonProcessingException {

    YqlResult result = new YqlResult(YqlResult.YQL_SAMPLE_RESULT);

    List<TraceRow> rows = TraceUtils.getTraceRows(result);
    rows.forEach(System.out::println);

  }

  @Test
  public void testSdf() throws ParseException {
    String dStr = "2024-03-21 20:56:36.604 UTC";
    Date date = TraceUtils.sdf.parse(dStr);
    System.out.println(date);
  }

}