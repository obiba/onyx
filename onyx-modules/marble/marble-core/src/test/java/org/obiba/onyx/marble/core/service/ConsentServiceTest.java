package org.obiba.onyx.marble.core.service;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.marble.core.service.impl.ConsentServiceImpl;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsentServiceTest extends BaseDefaultSpringContextTestCase {

  @Autowired(required = true)
  PersistenceManager persistenceManager;

  private ConsentServiceImpl consentService;

  @Before
  public void setup() {
    consentService = new ConsentServiceImpl();
    consentService.setPersistenceManager(persistenceManager);
  }

  private Interview createInterview() {
    Interview interview = new Interview();
    persistenceManager.save(interview);
    return interview;
  }

  private Consent createConsent(Interview interview) {
    Consent consent = new Consent();
    consent.setDeleted(false);
    consent.setInterview(interview);
    persistenceManager.save(consent);
    return consent;
  }

  @Test
  public void testGetConsent() {
    Interview interview = createInterview();
    Consent consent = createConsent(interview);

    Consent retrievedConsent = consentService.getConsent(interview);
    Assert.assertEquals(retrievedConsent.getId(), consent.getId());

    persistenceManager.delete(consent);
    persistenceManager.delete(interview);

  }

  @Test
  public void testSaveConsent() {
    Consent consent = createConsent(null);

    Consent retrievedConsent = (Consent) persistenceManager.matchOne(consent);
    Assert.assertEquals(retrievedConsent.getId(), consent.getId());

    persistenceManager.delete(consent);

  }

  @Test
  public void testDeletePreviousConsent() {
    Interview interview = createInterview();
    createConsent(interview);

    consentService.deletePreviousConsent(interview);

    Consent template = new Consent();
    template.setInterview(interview);
    template.setDeleted(true);

    Assert.assertTrue(persistenceManager.matchOne(template).isDeleted());

  }

}
