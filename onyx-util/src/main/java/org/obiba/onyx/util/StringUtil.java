package org.obiba.onyx.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public class StringUtil {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(StringUtil.class);
  
  public static List<String> getCSVString(String csvString) {
    List<String> strings = new ArrayList<String>();
    
    log.info("csvString={}", csvString);
    CSVReader reader = new CSVReader(new StringReader(csvString));
    try {
      String[] array = reader.readNext();
      if (array != null) {
        for (String str : array) {
          strings.add(str);
        }
      }
    } catch(IOException e) {
      log.error("Failed parsing CSV string:" + csvString, e);
    }
    log.info("strings={}", strings);
    
    return strings;
  }
  
}
