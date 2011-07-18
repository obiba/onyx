/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.summitdoppler;

import org.obiba.onyx.jade.instrument.ExternalAppLauncherHelper;
import org.obiba.onyx.jade.instrument.InstrumentRunner;
import org.obiba.onyx.jade.instrument.LocalSettingsHelper;
import org.obiba.onyx.jade.instrument.service.InstrumentExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class VantageABIInstrumentRunner implements InstrumentRunner, InitializingBean {

  protected Logger log = LoggerFactory.getLogger(VantageABIInstrumentRunner.class);

  protected ExternalAppLauncherHelper externalAppHelper;

  protected LocalSettingsHelper settingsHelper;

  // Injected by spring.
  protected InstrumentExecutionService instrumentExecutionService;

  public VantageABIInstrumentRunner() throws Exception {
    super();
  }

  @Override
  public void initialize() {
    // TODO Auto-generated method stub

  }

  @Override
  public void run() {
    // TODO Auto-generated method stub

  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  @Override
  public void afterPropertiesSet() throws Exception {
    // TODO Auto-generated method stub

  }

  public void setExternalAppHelper(ExternalAppLauncherHelper externalAppHelper) {
    this.externalAppHelper = externalAppHelper;
  }

  public void setSettingsHelper(LocalSettingsHelper settingsHelper) {
    this.settingsHelper = settingsHelper;
  }

  public void setInstrumentExecutionService(InstrumentExecutionService instrumentExecutionService) {
    this.instrumentExecutionService = instrumentExecutionService;
  }

}
