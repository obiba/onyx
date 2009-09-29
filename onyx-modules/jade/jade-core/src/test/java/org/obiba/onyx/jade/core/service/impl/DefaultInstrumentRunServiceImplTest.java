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

import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.Measure;
import org.obiba.onyx.jade.core.service.impl.hibernate.InstrumentRunServiceHibernateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 
 */
@Transactional
public class DefaultInstrumentRunServiceImplTest extends BaseDefaultSpringContextTestCase {

  private DefaultInstrumentRunServiceImpl defaultInstrumentRunService;

  @Autowired
  private PersistenceManager persistenceManager;

  @Autowired
  private InstrumentTypeFactoryBean instrumentTypeFactoryBean;

  private Map<String, InstrumentType> instrumentTypes;

  private User user;

  private Participant participant;

  @Before
  public void setUp() throws Exception {
    // Initialize instrumentTypes
    instrumentTypes = (Map<String, InstrumentType>) instrumentTypeFactoryBean.getObject();

    // Initialize activeInstrumentRunService (class being tested).
    defaultInstrumentRunService = new InstrumentRunServiceHibernateImpl();
    defaultInstrumentRunService.setPersistenceManager(persistenceManager);
    defaultInstrumentRunService.setInstrumentTypes(instrumentTypes);

  }

  @Test
  @Ignore
  public void instrumentNameTypeIsNullTest() {
    defaultInstrumentRunService.getInstrumentRun(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  @Dataset
  public void getInstrumentRuninstrumentNameTypeIsNullTest() {
    Participant participant = persistenceManager.get(Participant.class, 1l);
    defaultInstrumentRunService.getInstrumentRun(participant, null);
  }

  /**
   * Instrument name is not null, but instrument does not exist.
   */
  @Test(expected = IllegalArgumentException.class)
  @Dataset
  public void getInstrumentRuninstrumentTypeIsNullTest() {
    Participant participant = persistenceManager.get(Participant.class, 1l);
    defaultInstrumentRunService.getInstrumentRun(participant, "FakeInstrument");
  }

  @Test(expected = IllegalArgumentException.class)
  public void getInstrumentRunParticipantIsNullTest() {
    defaultInstrumentRunService.getInstrumentRun(null, "StandingHeight");
  }

  @Test
  @Dataset
  public void getInstrumentRunTest() {
    Participant participant = persistenceManager.get(Participant.class, 1l);
    InstrumentRun instrumentRun = defaultInstrumentRunService.getInstrumentRun(participant, "StandingHeight");
    Assert.assertNotNull(instrumentRun);
    Assert.assertEquals(InstrumentRunStatus.COMPLETED, instrumentRun.getStatus());
  }

  /**
   * The {@link InstrumentRun} specified by participant and instrumentTypeName does not exist.
   */
  @Test
  @Dataset
  public void getInstrumentRunIsNullTest() {
    Participant participant = persistenceManager.get(Participant.class, 1l);
    InstrumentRun instrumentRun = defaultInstrumentRunService.getInstrumentRun(participant, "ArtStiffness");
    Assert.assertNull(instrumentRun);
  }

  @Test
  public void printInstrumentsTest() {
    for(Map.Entry<String, InstrumentType> entry : instrumentTypes.entrySet()) {
      System.out.println(entry.getKey() + " " + entry.getValue());
    }
  }

  @Test
  @Dataset
  public void testDeleteInstrumentRun() {
    defaultInstrumentRunService.deleteInstrumentRun(persistenceManager.get(Participant.class, 1l), "StandingHeight");

    Assert.assertNull(persistenceManager.get(InstrumentRun.class, 1l));
    Assert.assertNotNull(persistenceManager.get(InstrumentRun.class, 2l));
    Assert.assertNotNull(persistenceManager.get(InstrumentRun.class, 3l));

    Assert.assertNull(persistenceManager.get(Measure.class, 1l));
    Assert.assertNull(persistenceManager.get(Measure.class, 2l));

    Assert.assertNull(persistenceManager.get(InstrumentRunValue.class, 1l));
    Assert.assertNull(persistenceManager.get(InstrumentRunValue.class, 2l));
    Assert.assertNotNull(persistenceManager.get(InstrumentRunValue.class, 3l));

  }

  @Test
  @Dataset
  public void testDeleteAllInstrumentRuns() {
    defaultInstrumentRunService.deleteAllInstrumentRuns(persistenceManager.get(Participant.class, 2l));

    Assert.assertNotNull(persistenceManager.get(InstrumentRun.class, 1l));
    Assert.assertNull(persistenceManager.get(InstrumentRun.class, 2l));
    Assert.assertNull(persistenceManager.get(InstrumentRun.class, 3l));

    Assert.assertNotNull(persistenceManager.get(InstrumentRunValue.class, 1l));
    Assert.assertNotNull(persistenceManager.get(InstrumentRunValue.class, 2l));
    Assert.assertNull(persistenceManager.get(InstrumentRunValue.class, 3l));

  }
}
