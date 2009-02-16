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

import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.math.impl.MathEclipseEvaluator;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class ComputingDataSourceTest {

  @Test
  public void testBooleanExpression() {
    Participant participant = new Participant();
    ComputingDataSource computing = new ComputingDataSource(DataType.BOOLEAN, "True && False");
    computing.setAlgorithmEvaluator(MathEclipseEvaluator.getInstance());

    Data data = computing.getData(participant);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals("false", data.getValueAsString());
  }

  @Test
  public void testIntegerExpression() {
    Participant participant = new Participant();
    ComputingDataSource computing = new ComputingDataSource(DataType.INTEGER, "1 + 2");
    computing.setAlgorithmEvaluator(MathEclipseEvaluator.getInstance());

    Data data = computing.getData(participant);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals("3", data.getValueAsString());

    computing.setExpression("1/2-0.1");
    data = computing.getData(participant);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals("0", data.getValueAsString());

    computing.setExpression("1/2+0.1");
    data = computing.getData(participant);

    Assert.assertEquals(DataType.INTEGER, data.getType());
    Assert.assertEquals("1", data.getValueAsString());
  }

  @Test
  public void testDoubleExpression() {
    Participant participant = new Participant();
    ComputingDataSource computing = new ComputingDataSource(DataType.DECIMAL, "1 + 2");
    computing.setAlgorithmEvaluator(MathEclipseEvaluator.getInstance());

    Data data = computing.getData(participant);

    Assert.assertEquals(DataType.DECIMAL, data.getType());
    Assert.assertEquals("3.0", data.getValueAsString());
  }

  @Test
  public void testVariableExpression() {
    Participant participant = new Participant();
    ComputingDataSource computing = new ComputingDataSource(DataType.DECIMAL, "$1 + $2");
    computing.setAlgorithmEvaluator(MathEclipseEvaluator.getInstance());
    computing.addDataSource(new FixedDataSource(DataBuilder.buildInteger(1)));
    computing.addDataSource(new FixedDataSource(DataBuilder.buildDecimal(2d)));

    Data data = computing.getData(participant);

    Assert.assertEquals(DataType.DECIMAL, data.getType());
    Assert.assertEquals("3.0", data.getValueAsString());
  }

  @Test
  public void testDateCompare() {
    Participant participant = new Participant();
    ComputingDataSource computing = new ComputingDataSource(DataType.BOOLEAN, "$1 < $2");
    computing.setAlgorithmEvaluator(MathEclipseEvaluator.getInstance());
    computing.addDataSource(new CurrentDateSource().addDateModifier(new DateModifier(Calendar.YEAR, -1)));
    computing.addDataSource(new CurrentDateSource());

    Data data = computing.getData(participant);

    Assert.assertEquals(DataType.BOOLEAN, data.getType());
    Assert.assertEquals("true", data.getValueAsString());
  }
}
