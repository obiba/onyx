package org.obiba.onyx.jade.core.test.service;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentServiceTest extends BaseDefaultSpringContextTestCase {
  
  @Autowired(required=true)
  PersistenceManager persistenceManager;
  
  @Autowired(required=true)
  InstrumentService instrumentService;
  
  @Test
  @Dataset
  public void testInstrumentType() {
    InstrumentType type1 = instrumentService.createInstrumentType("STA", "Height measurement");
    
    flushCache();
    
    type1 = persistenceManager.get(InstrumentType.class, type1.getId());
    
    Assert.assertTrue("No type 1", type1 != null);
  }

}
