/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.engine.variable.impl;

import java.util.List;

import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;

/**
 * Strategy for determining an Instrument's capture date range.
 */
public class InstrumentCaptureDateRangeStrategy extends AbstractJadeCaptureDateRangeStrategy {
  //
  // AbstractJadeCaptureDateRangeStrategy Methods
  //

  public String getEntityType() {
    return "Instrument";
  }

  protected List<ExperimentalCondition> getExperimentalConditions(String entityIdentifier) {
    return experimentalConditionService.getInstrumentCalibrations(entityIdentifier);
  }
}
