package com.pehrs.vespa.yql.plugin;

import com.intellij.testFramework.ParsingTestCase;

public class YqlParserTest extends ParsingTestCase {

  public YqlParserTest() {
    super("", "yql", new YqlParserDefinition());
  }

  public void testSimple() {
    doTest(true);
  }


  public void testBooksQuery() {
    doTest(true);
  }


  @Override
  protected String getTestDataPath() {
    return "src/test/testData";
  }
}