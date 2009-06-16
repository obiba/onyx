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

import java.util.Arrays;
import java.util.Calendar;

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.onyx.util.data.ComparisonOperator;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class DataSourceTest {

  @Test
  public void testComparingToString() {
    ComparingDataSource ds = new ComparingDataSource(new CurrentDateSource(), ComparisonOperator.gt, new ParticipantPropertyDataSource("birthDate"));
    Assert.assertEquals("Comparing[CurrentDate > ParticipantProperty[birthDate]]", ds.toString());

    ds = new ComparingDataSource(null, ComparisonOperator.gt, new ParticipantPropertyDataSource("birthDate"));
    Assert.assertEquals("Comparing[x > ParticipantProperty[birthDate]]", ds.toString());

    ds = new ComparingDataSource(new CurrentDateSource(), ComparisonOperator.gt, null);
    Assert.assertEquals("Comparing[CurrentDate > y]", ds.toString());
  }

  @Test
  public void testComputingToString() {
    ComputingDataSource computing = new ComputingDataSource(DataType.BOOLEAN, "\n     $1 +\t\n\r$2     \n> 4.0\n\r");
    computing.addDataSource(new FixedDataSource(DataBuilder.buildInteger(1)));
    computing.addDataSource(new FixedDataSource(DataBuilder.buildInteger(4)));
    Assert.assertEquals("Computing[$1 + $2 > 4.0,[Data[INTEGER:1], Data[INTEGER:4]]]", computing.toString());
  }

  @Test
  public void testCurrentDateToString() {
    CurrentDateSource ds = new CurrentDateSource(Calendar.YEAR);
    ds.addDateModifier(new DateModifier(Calendar.YEAR, -20));
    ds.addDateModifier(new DateModifier(Calendar.MONTH, 6));
    Assert.assertEquals("CurrentDate[DateModifier[YEAR, -20], DateModifier[MONTH, 6]].YEAR", ds.toString());
  }

  @Test
  public void testDateFieldToString() {
    DateFieldSource ds = new DateFieldSource(new VariableDataSource("Onyx.Questionnaire.BirthDate"), DateField.YEAR);
    Assert.assertEquals("DateField[Variable[Onyx.Questionnaire.BirthDate]].YEAR", ds.toString());
  }

  @Test
  public void testFirstNotNullToString() {
    FirstNotNullDataSource ds = new FirstNotNullDataSource();
    ds.addDataSource(new CurrentDateSource(DateField.YEAR));
    ds.addDataSource(new FixedDataSource(3));

    Assert.assertEquals("FirstNotNull[CurrentDate.YEAR, Data[INTEGER:3]]", ds.toString());
  }

  @Test
  public void testModifiedDateToString() {
    ModifiedDateSource ds = new ModifiedDateSource(new VariableDataSource("Onyx.Admin.Participant.birthDate"), Arrays.asList(new DateModifier[] { new DateModifier(Calendar.YEAR, 5), new DateModifier(Calendar.MONTH, 6) }));
    Assert.assertEquals("ModifiedDate[Variable[Onyx.Admin.Participant.birthDate], [DateModifier[YEAR, 5], DateModifier[MONTH, 6]]]", ds.toString());
  }

  @Test
  public void testParticipantPropertyToString() {
    ParticipantPropertyDataSource ds = new ParticipantPropertyDataSource("age", "years");
    Assert.assertEquals("ParticipantProperty[age]", ds.toString());
  }

  @Test
  public void testVariableToString() {
    VariableDataSource ds = new VariableDataSource("Onyx.Admin.Participant.firstName");
    Assert.assertEquals("Variable[Onyx.Admin.Participant.firstName]", ds.toString());
  }
}
