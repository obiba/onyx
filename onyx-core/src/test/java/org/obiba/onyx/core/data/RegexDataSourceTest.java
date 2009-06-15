/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;


public class RegexDataSourceTest {

  private IDataSource postalCodeDataSource;

  private Participant participant;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    participant = createParticipant();
    postalCodeDataSource = new FixedDataSource(new Data(DataType.TEXT, "M5G 2C1"));
  }

  @Test
  public void fooTest() {
    RegexDataSource regexDataSource = new RegexDataSource(postalCodeDataSource, "");
    Data postalCodePrefix = regexDataSource.getData(participant);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorRegexMustNotBeNullTextTest() {
    new RegexDataSource(postalCodeDataSource, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void constructorDataSourceMustNotBeNullTextTest() {
    new RegexDataSource(null, "");
  }

  @Test(expected = IllegalArgumentException.class)
  public void modifyParticipantMustNotBeNullTextTest() {
    RegexDataSource regexDataSource = new RegexDataSource(postalCodeDataSource, "");
    Data data = new Data(DataType.TEXT, "M5G 2C1");
    regexDataSource.modify(data, null);
  }

  public void modifyDataSetToNullReturnsNullTest() {
    RegexDataSource regexDataSource = new RegexDataSource(postalCodeDataSource, "");
    Data modifiedData = regexDataSource.modify(null, null);
    Assert.assertNull(modifiedData);
  }

  @Test(expected = IllegalArgumentException.class)
  public void modifyDataTypeMustBeTextTest() {
    RegexDataSource regexDataSource = new RegexDataSource(postalCodeDataSource, "");
    Data data = new Data(DataType.BOOLEAN, true);
    regexDataSource.modify(data, participant);
  }

  @Test
  public void obtainFirstThreeCharactersTest() {
    IDataSource postalCodeWithWhiteSpace = new FixedDataSource(new Data(DataType.TEXT, " T5G 2B1"));
    RegexDataSource regexDataSource = new RegexDataSource(postalCodeWithWhiteSpace, "^(...).*$");
    Data regexResult = regexDataSource.getData(participant);
    Assert.assertEquals(" T5", regexResult.getValueAsString());
  }

  @Test
  public void obtainPostalCodePreFixTest() {
    IDataSource postalCodeWithWhiteSpace = new FixedDataSource(new Data(DataType.TEXT, "   T5G 2B1 "));
    RegexDataSource regexDataSource = new RegexDataSource(postalCodeWithWhiteSpace, "^\\s*([a-zA-Z]\\d[a-zA-Z]).*$");
    Data regexResult = regexDataSource.getData(participant);
    Assert.assertEquals("T5G", regexResult.getValueAsString());
  }

  @Test
  public void obtainPostalCodePostFixTest() {
    IDataSource postalCodeWithWhiteSpace = new FixedDataSource(new Data(DataType.TEXT, " T5G 2B1 "));
    RegexDataSource regexDataSource = new RegexDataSource(postalCodeWithWhiteSpace, "^.*(\\d[a-zA-Z]\\d)\\s*$");
    Data regexResult = regexDataSource.getData(participant);
    Assert.assertEquals("2B1", regexResult.getValueAsString());
  }

  private Participant createParticipant() {
    Participant p = new Participant();
    p.setBarcode("1187432");
    p.setLastName("Tremblay");
    p.setFirstName("Patricia");
    p.setGender(Gender.FEMALE);
    Calendar c = Calendar.getInstance();
    c.set(1973, 5, 15);
    p.setBirthDate(c.getTime());
    return p;
  }

}
