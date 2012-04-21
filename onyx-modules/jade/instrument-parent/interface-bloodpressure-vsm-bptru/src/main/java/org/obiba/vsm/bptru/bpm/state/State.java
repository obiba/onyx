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

import org.obiba.vsm.bptru.bpm.BpmMessageHandler;

public interface State {

  enum States {
    CONNECTING(false, false), READY(true, false), MEASURING(false, true);

    private boolean canStart;

    private boolean canStop;

    private States(boolean canStart, boolean canStop) {
      this.canStart = canStart;
      this.canStop = canStop;
    }

    public boolean canStart() {
      return canStart;
    }

    public boolean canStop() {
      return canStop;
    }

    public boolean isConnected() {
      return this != CONNECTING;
    }
  }

  public States getName();

  public void start();

  public void add();

  public BpmMessageHandler getMessageHandler();

  public void stop();

}
