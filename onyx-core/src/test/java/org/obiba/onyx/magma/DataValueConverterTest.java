/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obiba.magma.MagmaEngine;
import org.obiba.magma.Value;
import org.obiba.magma.ValueType;
import org.obiba.magma.type.BinaryType;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.DateType;
import org.obiba.magma.type.DecimalType;
import org.obiba.magma.type.IntegerType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class DataValueConverterTest {

  @Before
  public void setUp() throws Exception {
    new MagmaEngine();
  }

  @After
  public void cleanUp() throws Exception {
    MagmaEngine.get().shutdown();
  }

  @Test
  public void testDataToValueBoolean() throws Exception {
    Data data = new Data(DataType.BOOLEAN, Boolean.FALSE);
    Value value = DataValueConverter.dataToValue(data);
    assertThat(value.getValueType(), equalTo((ValueType) BooleanType.get()));
    assertThat((Boolean) value.getValue(), is(Boolean.FALSE));
  }

  @Test
  public void testDataToValueText() throws Exception {
    Data data = new Data(DataType.TEXT, "Text message.");
    Value value = DataValueConverter.dataToValue(data);
    assertThat(value.getValueType(), equalTo((ValueType) TextType.get()));
    assertThat((String) value.getValue(), is("Text message."));
  }

  @Test
  public void testDataToValueInteger() throws Exception {
    Data data = new Data(DataType.INTEGER, 42);
    Value value = DataValueConverter.dataToValue(data);
    assertThat(value.getValueType(), equalTo((ValueType) IntegerType.get()));
    assertThat((Integer) value.getValue(), is(42));
  }

  @Test
  public void testDataToValueDate() throws Exception {
    Date date = new Date();
    Data data = new Data(DataType.DATE, date);
    Value value = DataValueConverter.dataToValue(data);
    assertThat(value.getValueType(), equalTo((ValueType) DateType.get()));
    assertThat((Date) value.getValue(), is(date));
  }

  @Test
  public void testDataToValueDecimal() throws Exception {
    Data data = new Data(DataType.DECIMAL, 35.32);
    Value value = DataValueConverter.dataToValue(data);
    assertThat(value.getValueType(), equalTo((ValueType) DecimalType.get()));
    assertThat((Double) value.getValue(), is(35.32));
  }

  @Test
  public void testDataToValueBinaryData() throws Exception {
    byte[] binaryData = "binary data".getBytes();
    Data data = new Data(DataType.DATA, binaryData);
    Value value = DataValueConverter.dataToValue(data);
    assertThat(value.getValueType(), equalTo((ValueType) BinaryType.get()));
    assertThat((byte[]) value.getValue(), is(binaryData));
  }

  @Test
  public void testValueToDataBoolean() throws Exception {
    Value value = ValueType.Factory.newValue(BooleanType.get(), Boolean.TRUE);
    Data data = DataValueConverter.valueToData(value);
    assertThat(data.getType(), equalTo(DataType.BOOLEAN));
    assertThat((Boolean) data.getValue(), is(Boolean.TRUE));
  }

  @Test
  public void testValueToDataText() throws Exception {
    Value value = ValueType.Factory.newValue(TextType.get(), "Some Text");
    Data data = DataValueConverter.valueToData(value);
    assertThat(data.getType(), equalTo(DataType.TEXT));
    assertThat((String) data.getValue(), is("Some Text"));
  }

  @Test
  public void testValueToDataInteger() throws Exception {
    Value value = ValueType.Factory.newValue(IntegerType.get(), 100);
    Data data = DataValueConverter.valueToData(value);
    assertThat(data.getType(), equalTo(DataType.INTEGER));
    assertThat((Integer) data.getValue(), is(100));
  }

  @Test
  public void testValueToDataDate() throws Exception {
    Date date = new Date();
    Value value = ValueType.Factory.newValue(DateType.get(), date);
    Data data = DataValueConverter.valueToData(value);
    assertThat(data.getType(), equalTo(DataType.DATE));
    assertThat((Date) data.getValue(), is(date));
  }

  @Test
  public void testValueToDataDecimal() throws Exception {
    Value value = ValueType.Factory.newValue(DecimalType.get(), 54.32);
    Data data = DataValueConverter.valueToData(value);
    assertThat(data.getType(), equalTo(DataType.DECIMAL));
    assertThat((Double) data.getValue(), is(54.32));
  }

  @Test
  public void testValueToDataBinaryData() throws Exception {
    byte[] binaryData = "binary data".getBytes();
    Value value = ValueType.Factory.newValue(BinaryType.get(), binaryData);
    Data data = DataValueConverter.valueToData(value);
    assertThat(data.getType(), equalTo(DataType.DATA));
    assertThat((byte[]) data.getValue(), is(binaryData));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testValueToDataSequence() throws Exception {
    List<Value> values = new ArrayList<Value>(3);
    values.add(ValueType.Factory.newValue(TextType.get(), "valueOne"));
    values.add(ValueType.Factory.newValue(TextType.get(), "valueTwo"));
    values.add(ValueType.Factory.newValue(TextType.get(), "valueThree"));

    Value value = ValueType.Factory.newSequence(TextType.get(), values);
    DataValueConverter.valueToData(value);
  }
}
