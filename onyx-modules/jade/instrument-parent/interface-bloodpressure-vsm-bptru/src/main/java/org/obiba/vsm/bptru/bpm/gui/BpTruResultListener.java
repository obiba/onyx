/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.vsm.bptru.bpm.gui;

import java.util.Date;

import org.obiba.vsm.bptru.bpm.Data;

public interface BpTruResultListener {

  public void onBpResult(Date startTime, Date endTime, Data.BloodPressure result);

  public void onAvgResult(Data.AvgPressure result);

}
