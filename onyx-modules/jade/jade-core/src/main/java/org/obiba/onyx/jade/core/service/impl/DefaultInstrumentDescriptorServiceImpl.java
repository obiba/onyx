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

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.jade.core.domain.instrument.InstrumentDescriptor;
import org.obiba.onyx.jade.core.service.InstrumentDescriptorService;

public class DefaultInstrumentDescriptorServiceImpl implements InstrumentDescriptorService {

  private Map<String, InstrumentDescriptor> descriptorMap;;

  public DefaultInstrumentDescriptorServiceImpl() {
    descriptorMap = new HashMap<String, InstrumentDescriptor>();
  }

  public String getCodeBase(String instrumentBarCode) {
    InstrumentDescriptor descriptor = descriptorMap.get(instrumentBarCode);
    if(descriptor == null) return null;
    else
      return descriptor.getCodeBase();
  }

  public void setCodeBase(String instrumentBarCode, String codeBase) {
    InstrumentDescriptor descriptor = descriptorMap.get(instrumentBarCode);
    if (descriptor == null) {
      descriptor = new InstrumentDescriptor(instrumentBarCode);
      descriptorMap.put(instrumentBarCode, descriptor);  
    }
    descriptor.setCodeBase(codeBase);
  }

}
