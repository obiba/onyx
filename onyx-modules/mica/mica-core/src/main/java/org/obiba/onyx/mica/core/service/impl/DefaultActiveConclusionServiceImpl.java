/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.mica.core.service.impl;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultActiveConclusionServiceImpl extends PersistenceManagerAwareService implements ActiveConclusionService {
  
  private static final Logger log = LoggerFactory.getLogger(DefaultActiveConclusionServiceImpl.class);
  
  private Boolean conclusion = true;
  
  public Boolean getConclusion() {
    return conclusion;
  }
  
  public void setConclusion(Boolean conclusion) {
    this.conclusion = conclusion;
  }

  public void validate() {
    // TODO Auto-generated method stub
    
  }

}
