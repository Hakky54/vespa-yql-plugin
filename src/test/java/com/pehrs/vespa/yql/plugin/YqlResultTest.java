package com.pehrs.vespa.yql.plugin;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.pehrs.vespa.yql.plugin.YqlResult.YqlResultRow;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

public class YqlResultTest {

  @Test
  public void testColumns() throws JsonProcessingException {

    YqlResult result = new YqlResult(YqlResult.YQL_SAMPLE_RESULT);

    List<String> expected = List.of(
        "documentid", "segment_index", "title", "content", "embedding"
    );

    List<String> columnNames = result.getColumnNames();

    Assert.assertEquals(expected, columnNames);
  }

  @Test
  public void testRows() throws JsonProcessingException {

    YqlResult result = new YqlResult(YqlResult.YQL_SAMPLE_RESULT);

    List<YqlResultRow> rows = result.getRows();

    Assert.assertEquals(5, rows.size());
    Set<Boolean> embedLengths = rows.stream().map(row -> {
          List<Float> embedding = (List<Float>) row.fields().get("embedding");
          return embedding.size() == 384;
        })
        .collect(Collectors.toSet());
    Assert.assertEquals(1, embedLengths.size());
    Assert.assertEquals(true, embedLengths.stream().toList().get(0));
  }

}