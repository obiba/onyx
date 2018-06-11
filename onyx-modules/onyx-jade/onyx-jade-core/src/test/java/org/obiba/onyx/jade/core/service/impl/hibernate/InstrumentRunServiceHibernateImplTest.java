/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service.impl.hibernate;

import org.junit.Assert;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.test.spring.DatasetOperationType;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentRunServiceHibernateImplTest extends BaseDefaultSpringContextTestCase {

  private InstrumentRunServiceHibernateImpl newInstrumentRunServiceHibernateImpl;

  @Autowired
  private PersistenceManager persistenceManager;

  @Autowired
  private SessionFactory factory;

  @Autowired
  private InstrumentService instrumentService;

  private Participant participantLauraDupont;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {

    // Initialize activeInstrumentRunService (class being tested).
    newInstrumentRunServiceHibernateImpl = new InstrumentRunServiceHibernateImpl();
    newInstrumentRunServiceHibernateImpl.setPersistenceManager(persistenceManager);
    newInstrumentRunServiceHibernateImpl.setInstrumentService(instrumentService);
    newInstrumentRunServiceHibernateImpl.setSessionFactory(factory);

    participantLauraDupont = persistenceManager.get(Participant.class, 1l);
  }

  @Test(expected = IllegalArgumentException.class)
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void getInstrumentRunValueInstrumentNameTypeIsNullTest() {
    newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(participantLauraDupont, null, "parameterCode", 0);
  }

  /**
   * Instrument name is not null, but instrument does not exist.
   */
  @Test(expected = IllegalArgumentException.class)
  @Dataset
  public void getInstrumentRunValueInstrumentTypeIsNullTest() {
    newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(participantLauraDupont, "FakeInstrument", "parameterCode", 0);
  }

  @Test(expected = IllegalArgumentException.class)
  @Dataset
  public void getInstrumentRunValueParticipantIsNullTest() {
    newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(null, "StandingHeight", "parameterCode", 0);
  }

  @Test(expected = IllegalArgumentException.class)
  @Dataset
  public void getInstrumentRunValueParameterCodeIsNullTest() {
    newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(participantLauraDupont, "StandingHeight", null, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  @Dataset
  public void getInstrumentRunValueMeasurePositionIsNegativeTest() {
    newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(participantLauraDupont, "StandingHeight", "parameterCode", -2);
  }

  /**
   * The {@link InstrumentRun} specified by participant and instrumentTypeName does not exist.
   */
  @Test
  @Dataset
  public void getInstrumentRunValueInstrumentRunIsNullTest() {
    InstrumentRunValue runValue = newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(participantLauraDupont, "Spirometry", "RES_CEN_MEAN_PRESSURE", 1);
    Assert.assertNull(runValue);
  }

  @Test
  @Dataset
  public void getInstrumentRunValueParameterDoesNotExistTest() {
    InstrumentRunValue runValue = newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(participantLauraDupont, "ArtStiffness", "FAKE_PARAMETER_CODE", 1);
    Assert.assertNull(runValue);
  }

  @Test
  @Dataset
  public void getInstrumentRunValueSingleRepeatableValueTest() {
    InstrumentRunValue runValue = newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(participantLauraDupont, "ArtStiffness", "RES_CEN_MEAN_PRESSURE", 1);
    Assert.assertEquals("RES_CEN_MEAN_PRESSURE", runValue.getInstrumentParameter());
  }

  @Test
  @Dataset
  public void getInstrumentRunValueOutOfRangeRepeatableValueTest() {
    int outOfRangeIndex = 5;
    InstrumentRunValue runValue = newInstrumentRunServiceHibernateImpl.getInstrumentRunValue(participantLauraDupont, "ArtStiffness", "RES_CEN_MEAN_PRESSURE", outOfRangeIndex);
    Assert.assertNull(runValue);
  }
}
