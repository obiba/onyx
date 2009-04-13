/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.mica.core.service.impl;

import org.hibernate.SessionFactory;
import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.mica.core.service.ActiveConclusionService;
import org.obiba.onyx.mica.domain.conclusion.Conclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultActiveConclusionServiceImpl extends PersistenceManagerAwareService implements ActiveConclusionService {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(DefaultActiveConclusionServiceImpl.class);

  private ActiveInterviewService activeInterviewService;

  private boolean balsacConfirmationRequired = false;

  protected SessionFactory sessionFactory = null;

  public void setSessionFactory(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  private Conclusion conclusion;

  public Conclusion getConclusion() {
    return getConclusion(false);
  }

  public Conclusion getConclusion(boolean newInstance) {

    // Consent doesn't exist or new object requested.
    if(conclusion == null || newInstance) {
      conclusion = new Conclusion();
      conclusion.setInterview(activeInterviewService.getInterview());
    }

    return conclusion;

  }

  public void save() {
    // Fixes ONYX-457 Duplicate rows in conclusion table
    // This quick fix required injecting the sessionFactory into this class. This is undesirable. It would be preferable
    // for the Persistence Manager to handle these details. This is logged as ONYX-458.
    sessionFactory.getCurrentSession().saveOrUpdate(this.conclusion);
  }

  public Consent getParticipantConsent() {
    // Retrieve consent for current interview.
    Consent template = new Consent();
    template.setInterview(activeInterviewService.getInterview());
    template.setDeleted(false);
    return getPersistenceManager().matchOne(template);
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
