/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.vsm.bptru.bpm.state;

import java.util.Date;

import org.obiba.vsm.bptru.bpm.Data;

public interface BpmSession {

  public void setState(State.States state);

  public void setFirmware(String firmware);

  public int getCycle();

  public void setCycle(int cycle);

  public void setCuffPressure(int pressure);

  public void incrementReading();

  public void setReading(int reading);

  public void addResult(Date startTime, Date endTime, Data.BloodPressure result);

  public void addAverage(Data.AvgPressure datum);

  public void clearResults();

}
