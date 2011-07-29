/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.jtech;

import org.obiba.paradox.ParadoxRecord;

public class GripTest {

  private final ParadoxRecord record;

  GripTest(ParadoxRecord record) {
    this.record = record;
  }

  int getExamId() {
    return record.getValue("ExamID");
  }

  int getTestId() {
    return record.getValue("TestID");
  }

  String getTest() {
    return record.getValue("Test");
  }

  Integer getRung() {
    return record.getValue("Rung");
  }

  Integer getMaxReps() {
    return record.getValue("MaxReps");
  }

  String getSequence() {
    return record.getValue("Sequence");
  }

  Integer getRestTime() {
    return record.getValue("RestTime");
  }

  Integer getRate() {
    return record.getValue("Rate");
  }

  Integer getThreshold() {
    return record.getValue("Threshold");
  }

  Integer getNormType() {
    return record.getValue("NormType");
  }

  Integer getComparision() {
    return record.getValue("Comparision");
  }
}
