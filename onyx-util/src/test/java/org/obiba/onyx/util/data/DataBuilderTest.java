package org.obiba.onyx.util.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;

import org.junit.Assert;
import org.junit.Test;

public class DataBuilderTest {

  @Test
  public void testBuildBoolean() {
    Data booleanData = DataBuilder.buildBoolean(true);
    Assert.assertEquals(booleanData.getType(), DataType.BOOLEAN);
    Assert.assertEquals(booleanData.getValue(), true);
  }

  @Test
  public void testBuildDate() {
    Date date = new Date(System.currentTimeMillis());

    Data dateData = DataBuilder.buildDate(date);
    Assert.assertEquals(dateData.getType(), DataType.DATE);
    Assert.assertEquals(dateData.getValue(), date);
  }

  @Test
  public void testBuildDecimal() {
    Data decimalData = DataBuilder.buildDecimal(new Double("1.12345"));
    Assert.assertEquals(decimalData.getType(), DataType.DECIMAL);
    Assert.assertEquals(decimalData.getValue(), new Double("1.12345"));

    decimalData = DataBuilder.buildDecimal(new Float("1.12345"));
    Assert.assertEquals(decimalData.getType(), DataType.DECIMAL);
    Assert.assertEquals(decimalData.getValue(), new Float("1.12345"));
  }

  @Test
  public void testBuildInteger() {
    Data decimalData = DataBuilder.buildInteger(new Integer("12345"));
    Assert.assertEquals(decimalData.getType(), DataType.INTEGER);
    Assert.assertEquals(decimalData.getValue(), new Integer("12345"));

    decimalData = DataBuilder.buildInteger(new Long("12345"));
    Assert.assertEquals(decimalData.getType(), DataType.INTEGER);
    Assert.assertEquals(decimalData.getValue(), new Long("12345"));
  }

  @Test
  public void testBuildText() {
    Data decimalData = DataBuilder.buildText("this is a test");
    Assert.assertEquals(decimalData.getType(), DataType.TEXT);
    Assert.assertEquals(decimalData.getValue(), "this is a test");
  }

  @Test
  public void testBuildBinary() throws IOException {

    // Get source file size
    File file = new File("/test.jpg");
    long fileSize = file.length();

    // Get source file checksum
    CheckedInputStream inputStream = new CheckedInputStream(new FileInputStream(file), new Adler32());
    byte[] tempBuf = new byte[1024];
    while(inputStream.read(tempBuf) > -1) {
    }
    
    Data binaryData = DataBuilder.buildBinary(new FileInputStream(file));
    checkIntegrity(fileSize, inputStream, binaryData);    
    
    binaryData = DataBuilder.buildBinary(file);
    checkIntegrity(fileSize, inputStream, binaryData);

  }

  private void checkIntegrity(long fileSize, CheckedInputStream inputStream, Data binaryData) {
   
    Assert.assertEquals(binaryData.getType(), DataType.DATA);
    
    // Verify source file and target byte[] are of the same length 
    Assert.assertEquals(((byte[]) binaryData.getValue()).length, fileSize);

    // Verify source file and target byte[] have the same checksum
    Adler32 checksumTest = new Adler32();
    checksumTest.update((byte[]) binaryData.getValue());
    Assert.assertEquals(checksumTest.getValue(), inputStream.getChecksum().getValue());
    Assert.assertEquals(((byte[]) binaryData.getValue()).length, fileSize);
  }
}
