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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentServiceTest extends BaseDefaultSpringContextTestCase {

  @Autowired(required = true)
  PersistenceManager persistenceManager;

  @Autowired(required = true)
  InstrumentService instrumentService;

  @Test
  @Dataset
  public void testInstrumentType() {
    List<Instrument> instruments = instrumentService.getInstruments("STA");
    Assert.assertEquals("Wrong STA instrument count", 2, instruments.size());
  }

}
