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

import java.util.List;
import java.util.Map;

import org.junit.Assert;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.service.SortingClause;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.core.test.spring.DatasetOperationType;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentMeasurementType;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentStatus;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.impl.InstrumentTypeFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentServiceHibernateImplTest extends BaseDefaultSpringContextTestCase {

  private InstrumentServiceHibernateImpl instrumentServiceHibernateImpl;

  @Autowired
  private PersistenceManager persistenceManager;

  @Autowired
  private InstrumentTypeFactoryBean instrumentTypeFactoryBean;

  @Autowired
  private SessionFactory factory;

  private Map<String, InstrumentType> instrumentTypes;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() throws Exception {
    // Initialize instrumentTypes
    instrumentTypes = (Map<String, InstrumentType>) instrumentTypeFactoryBean.getObject();

    // Initialize activeInstrumentRunService (class being tested).
    instrumentServiceHibernateImpl = new InstrumentServiceHibernateImpl();
    instrumentServiceHibernateImpl.setPersistenceManager(persistenceManager);
    instrumentServiceHibernateImpl.setInstrumentTypes(instrumentTypes);
    instrumentServiceHibernateImpl.setSessionFactory(factory);
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void getWorkstationInstrumentsTest() {
    List<Instrument> instruments = instrumentServiceHibernateImpl.getWorkstationInstruments("onyx001-127.0.32.5", new PagingClause(0, 10), new SortingClause("barcode", true));
    Assert.assertEquals(6, instruments.size());
    Assert.assertEquals("CAG0189", instruments.get(0).getBarcode());
    Assert.assertEquals("sta01", instruments.get(4).getBarcode());

    instruments = instrumentServiceHibernateImpl.getWorkstationInstruments("onyx001-127.0.32.6", new PagingClause(0, 10), new SortingClause("model", true));
    Assert.assertEquals(3, instruments.size());
    Assert.assertEquals("MiniSpir", instruments.get(0).getModel());
    Assert.assertEquals("Model 214", instruments.get(1).getModel());
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void countWorkstationInstrumentsTest() {
    int count = instrumentServiceHibernateImpl.countWorkstationInstruments("onyx001-127.0.32.5");
    Assert.assertEquals(6, count);
    count = instrumentServiceHibernateImpl.countWorkstationInstruments("onyx001-127.0.32.6");
    Assert.assertEquals(3, count);
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void getWorkstationInstrumentMeasurementTypesTest() {
    List<InstrumentMeasurementType> instrumentMeasurementTypes = instrumentServiceHibernateImpl.getWorkstationInstrumentMeasurementTypes("onyx001-127.0.32.5", null, (SortingClause[]) null);
    Assert.assertEquals(6, instrumentMeasurementTypes.size());

    instrumentMeasurementTypes = instrumentServiceHibernateImpl.getWorkstationInstrumentMeasurementTypes("onyx001-127.0.32.6", null, (SortingClause[]) null);
    Assert.assertEquals(4, instrumentMeasurementTypes.size());
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void countWorkstationInstrumentMeasurementTypesTest() {
    Assert.assertEquals(6, instrumentServiceHibernateImpl.countWorkstationInstrumentMeasurementTypes("onyx001-127.0.32.5"));
    Assert.assertEquals(4, instrumentServiceHibernateImpl.countWorkstationInstrumentMeasurementTypes("onyx001-127.0.32.6"));
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void updateStatusTest() {
    Instrument instrument = new Instrument();
    instrument.setId(5l);
    Assert.assertEquals(InstrumentStatus.INACTIVE, (persistenceManager.get(Instrument.class, 5l)).getStatus());
    instrumentServiceHibernateImpl.updateStatus(instrument, InstrumentStatus.ACTIVE);

    Assert.assertEquals(InstrumentStatus.ACTIVE, (persistenceManager.get(Instrument.class, 5l)).getStatus());
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void updateWorkstationTest() {
    Instrument instrument = persistenceManager.get(Instrument.class, 2l);
    Assert.assertNull(instrument.getWorkstation());

    instrumentServiceHibernateImpl.updateWorkstation(instrument, "onyx001-127.0.32.5");

    instrument = persistenceManager.get(Instrument.class, 2l);
    Assert.assertEquals("onyx001-127.0.32.5", instrument.getWorkstation());
    // do clean up manually
    instrumentServiceHibernateImpl.deleteInstrument(instrument);
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void createInstrumentTest() {
    Instrument instrument = new Instrument();
    instrument.addType("testType");
    instrument.setBarcode("99999");
    instrument.setModel("908908");
    instrument.setVendor("mySelf");
    instrument.setStatus(InstrumentStatus.ACTIVE);
    instrument.setWorkstation("onyx001-127.0.32.6");

    instrumentServiceHibernateImpl.updateInstrument(instrument);

    List<Instrument> instruments = instrumentServiceHibernateImpl.getWorkstationInstruments("onyx001-127.0.32.6", new PagingClause(0, 10), new SortingClause("barcode", true));
    Assert.assertEquals(4, instruments.size());
    Assert.assertEquals("99999", instruments.get(0).getBarcode());
    // do clean up manually
    instrumentServiceHibernateImpl.deleteInstrument(instrument);
  }

  @Test
  @Dataset(beforeOperation = DatasetOperationType.CLEAN_INSERT)
  public void updateInstrumentTest() {
    Instrument instrument = instrumentServiceHibernateImpl.getInstrumentByBarcode("sta01");
    instrument.setModel("newModel STA");

    instrumentServiceHibernateImpl.updateInstrument(instrument);

    Instrument persistedInstrument = instrumentServiceHibernateImpl.getInstrumentByBarcode("sta01");
    Assert.assertEquals(3l, persistedInstrument.getId());
    Assert.assertEquals("newModel STA", persistedInstrument.getModel());
    // do clean up manually
    instrumentServiceHibernateImpl.deleteInstrument(persistedInstrument);
  }
}
