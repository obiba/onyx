/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
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
    InstrumentType type1 = instrumentService.createInstrumentType("BLP", "Blood pressure");
    
    flushCache();
    
    type1 = persistenceManager.get(InstrumentType.class, type1.getId());
    Assert.assertTrue("No type 1", type1 != null);
    
    List<Instrument> instruments = instrumentService.getInstruments("STA");
    Assert.assertEquals("Wrong STA instrument count", 2, instruments.size());
  }
  
  @Test
  @Dataset
  public void testInstrumentTypeDependencies() {
    
    Assert.assertEquals("Wrong STA depends on count", 0, instrumentService.getInstrumentType("STA").getDependsOnTypes().size());
    Assert.assertEquals("Wrong STA dependent count", 2, instrumentService.getInstrumentType("STA").getDependentTypes().size());
    
    Assert.assertEquals("Wrong STA depends on count", 1, instrumentService.getInstrumentType("BIM").getDependsOnTypes().size());
    Assert.assertEquals("Wrong STA dependent count", 1, instrumentService.getInstrumentType("BIM").getDependentTypes().size());
    
    Assert.assertEquals("Wrong STA depends on count", 2, instrumentService.getInstrumentType("SPI").getDependsOnTypes().size());
    Assert.assertEquals("Wrong STA dependent count", 0, instrumentService.getInstrumentType("SPI").getDependentTypes().size());
    
  }

}
