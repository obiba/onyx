/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl;

import java.util.Date;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultInstrumentRunServiceImpl extends PersistenceManagerAwareService implements InstrumentRunService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultInstrumentRunServiceImpl.class);

  public void setInstrumentRunStatus(InstrumentRun run, InstrumentRunStatus status) {
    // if(status.equals(InstrumentRunStatus.CANCELED)) {
    // getPersistenceManager().delete(run);
    // } else {
    // run.setStatus(status);
    // getPersistenceManager().save(run);
    // }
    run.setStatus(status);
    getPersistenceManager().save(run);
  }

  public void end(InstrumentRun run) {
    if(run.getTimeEnd() != null) throw new IllegalArgumentException("Instrument run already ended the " + run.getTimeEnd());

    run.setTimeEnd(new Date());
    getPersistenceManager().save(run);
  }

}
