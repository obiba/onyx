/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.mica.core.service.impl;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.mica.domain.conclusion.Conclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultActiveConclusionServiceImpl extends PersistenceManagerAwareService implements ActiveConclusionService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultActiveConclusionServiceImpl.class);

  private ActiveInterviewService activeInterviewService;

  private boolean balsacConfirmationRequired = false;

  private Conclusion conclusion;

  public Conclusion getConclusion() {
    Conclusion wConclusion = null;

    // Attempt to retrieve from database.
    if(conclusion == null) {
      wConclusion = new Conclusion();
      wConclusion.setInterview(activeInterviewService.getInterview());
      conclusion = getPersistenceManager().matchOne(wConclusion);
    }

    return conclusion;
  }

  public void setConclusion(Conclusion conclusion) {
    this.conclusion = conclusion;
  }

  public void save() {
    getPersistenceManager().save(this.conclusion);
  }

  public ActiveInterviewService getActiveInterviewService() {
    return activeInterviewService;
  }

  public void setActiveInterviewService(ActiveInterviewService activeInterviewService) {
    this.activeInterviewService = activeInterviewService;
  }

  public boolean isBalsacConfirmationRequired() {
    return balsacConfirmationRequired;
  }

  public void setBalsacConfirmationRequired(boolean balsacConfirmationRequired) {
    this.balsacConfirmationRequired = balsacConfirmationRequired;
  }

}
