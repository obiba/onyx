/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.workstation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionBuilder;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.wicket.workstation.ExperimentalConditionSorter;
import org.obiba.onyx.util.data.DataType;

/**
 *
 */
public class ExperimentalConditionSorterTest {

  private ExperimentalConditionSorter experimentalConditionSorter;

  private List<ExperimentalCondition> experimentalConditions;

  private static final Date sevenAm = ExperimentalConditionSorterTest.constructDate(7);

  private static final Date eightAm = ExperimentalConditionSorterTest.constructDate(8);

  private static final Date nineAm = ExperimentalConditionSorterTest.constructDate(9);

  private static final Date tenAm = ExperimentalConditionSorterTest.constructDate(10);

  private static final Date elevenAm = ExperimentalConditionSorterTest.constructDate(11);

  private ExperimentalCondition scaleOne;

  private ExperimentalCondition scaleTwo;

  private ExperimentalCondition scaleThree;

  private ExperimentalCondition scaleFour;

  private ExperimentalCondition scaleFive;

  @Before
  public void setUp() throws Exception {
    experimentalConditions = new ArrayList<ExperimentalCondition>(5);

    scaleOne = ExperimentalConditionBuilder.anExperimentalCondition().name("ScaleCalibration").time(sevenAm).user("Jonathan", "Coulton").workstation("Zuko").value(ExperimentalConditionService.INSTRUMENT_BARCODE, "0000008", DataType.TEXT).value("20kg", 19L, DataType.INTEGER).value("40kg", 39.2, DataType.DECIMAL).value("note", "A note.", DataType.TEXT).value("date", sevenAm, DataType.DATE).build();
    scaleTwo = ExperimentalConditionBuilder.anExperimentalCondition().name("ScaleCalibration").time(eightAm).user("Polly", "Scattergoods").workstation("TyLee").value(ExperimentalConditionService.INSTRUMENT_BARCODE, "0000008", DataType.TEXT).value("20kg", 20L, DataType.INTEGER).value("40kg", 39.3, DataType.DECIMAL).value("note", "B note.", DataType.TEXT).value("date", eightAm, DataType.DATE).build();
    scaleThree = ExperimentalConditionBuilder.anExperimentalCondition().name("ScaleCalibration").time(nineAm).user("Carl", "Quin").workstation("Aang").value(ExperimentalConditionService.INSTRUMENT_BARCODE, "0000008", DataType.TEXT).value("20kg", 21L, DataType.INTEGER).value("40kg", 39.4, DataType.DECIMAL).value("note", "C note.", DataType.TEXT).value("date", nineAm, DataType.DATE).build();
    scaleFour = ExperimentalConditionBuilder.anExperimentalCondition().name("ScaleCalibration").time(eightAm).user("Eric", "Evans").workstation("Momo").value(ExperimentalConditionService.INSTRUMENT_BARCODE, "0000008", DataType.TEXT).value("20kg", 22L, DataType.INTEGER).value("40kg", 39.5, DataType.DECIMAL).value("note", "D note.", DataType.TEXT).value("date", tenAm, DataType.DATE).build();
    scaleFive = ExperimentalConditionBuilder.anExperimentalCondition().name("ScaleCalibration").time(eightAm).user("Kent", "Beck").workstation("Appa").value(ExperimentalConditionService.INSTRUMENT_BARCODE, "0000008", DataType.TEXT).value("20kg", 23L, DataType.INTEGER).value("40kg", 39.6, DataType.DECIMAL).value("note", "E note.", DataType.TEXT).value("date", elevenAm, DataType.DATE).build();

    experimentalConditions.add(scaleOne);
    experimentalConditions.add(scaleTwo);
    experimentalConditions.add(scaleThree);
    experimentalConditions.add(scaleFour);
    experimentalConditions.add(scaleFive);

    experimentalConditionSorter = new ExperimentalConditionSorter(experimentalConditions);
  }

  @Test
  public void testSortableListSize() throws Exception {
    assertThat(experimentalConditionSorter.getSortableList().size(), is(5));
  }

  @Test
  public void testSortUserAsc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("user", true)).get(0), is(scaleThree)); // Carl
  }

  @Test
  public void testSortUserDesc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("user", false)).get(0), is(scaleTwo)); // Polly
  }

  @Test
  public void testSort20kgIntegerAsc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("20kg", true)).get(0), is(scaleOne));
  }

  @Test
  public void testSort20kgIntegerDesc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("20kg", false)).get(0), is(scaleFive));
  }

  @Test
  public void testSort40kgDecimalAsc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("40kg", true)).get(0), is(scaleOne));
  }

  @Test
  public void testSort40kgDecimalDesc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("40kg", false)).get(0), is(scaleFive));
  }

  @Test
  public void testSortNoteAsc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("note", true)).get(0), is(scaleOne));
  }

  @Test
  public void testSortNoteDesc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("note", false)).get(0), is(scaleFive));
  }

  @Test
  public void testSortDateAsc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("date", true)).get(0), is(scaleOne));
  }

  @Test
  public void testSortDateDesc() throws Exception {
    assertThat(experimentalConditionSorter.getSortedList(new SortingClause("date", false)).get(0), is(scaleFive));
  }

  @Test
  public void testSortNoteDescWithPaging() throws Exception {
    PagingClause pagingClause = new PagingClause(2, 3);
    assertThat(experimentalConditionSorter.getSortedList(pagingClause, new SortingClause("note", false)).get(0), is(scaleThree));
  }

  private static Date constructDate(int hour) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.HOUR, hour);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);
    calendar.set(Calendar.AM_PM, Calendar.AM);
    return calendar.getTime();
  }
}
