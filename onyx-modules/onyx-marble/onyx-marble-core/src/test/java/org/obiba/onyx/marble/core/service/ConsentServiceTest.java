package org.obiba.onyx.marble.core.service;

import java.util.Date;
import java.util.Locale;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.marble.core.service.impl.ConsentServiceImpl;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.obiba.onyx.marble.domain.consent.ConsentMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ConsentServiceTest extends BaseDefaultSpringContextTestCase {

  @Autowired(required = true)
  PersistenceManager persistenceManager;

  private ConsentServiceImpl consentService;

  private Participant participant;

  private static final String STAGE_NAME = "Consent";

  @Before
  public void setup() {
    consentService = new ConsentServiceImpl();
    consentService.setPersistenceManager(persistenceManager);

    participant = new Participant();
    participant.setSiteNo("siteNo");
    participant.setEnrollmentId("1");
    participant.setFirstName("first");
    participant.setLastName("last");
    persistenceManager.save(participant);
  }

  private Interview createInterview() {
    Interview interview = new Interview();
    interview.setStartDate(new Date());
    interview.setStatus(InterviewStatus.IN_PROGRESS);
    participant.setInterview(interview);
    persistenceManager.save(interview);
    return interview;
  }

  private Consent createConsent(Interview interview, boolean deleted) {
    Consent consent = new Consent();
    consent.setConsentName(STAGE_NAME);
    consent.setDeleted(deleted);
    consent.setInterview(interview);

    consent.setMode(ConsentMode.ELECTRONIC);
    consent.setLocale(Locale.getDefault());
    consent.setTimeStart(new Date());

    persistenceManager.save(consent);

    return consent;
  }

  private Consent createConsent(Interview interview) {
    return createConsent(interview, false);
  }

  @Test
  public void testGetConsent() {
    Interview interview = createInterview();

    Consent consent = createConsent(interview);

    Consent retrievedConsent = consentService.getConsent(interview, STAGE_NAME);
    Assert.assertEquals(retrievedConsent.getId(), consent.getId());

    persistenceManager.delete(consent);
    persistenceManager.delete(interview);

  }

  @Test
  public void testSaveConsent() {
    Interview interview = createInterview();
    Consent consent = createConsent(interview);

    Consent retrievedConsent = (Consent) persistenceManager.matchOne(consent);
    Assert.assertEquals(retrievedConsent.getId(), consent.getId());

    persistenceManager.delete(consent);

  }

  @Test
  public void testDeletePreviousConsent() {
    Interview interview = createInterview();
    createConsent(interview);

    consentService.deletePreviousConsent(interview, STAGE_NAME);

    Consent template = new Consent();
    template.setInterview(interview);
    template.setDeleted(true);

    Assert.assertTrue(persistenceManager.matchOne(template).isDeleted());

  }

  @Test
  public void testPurgeConsent() {
    Interview interview = createInterview();

    createConsent(interview, true);
    createConsent(interview, true);
    createConsent(interview, false);

    consentService.purgeConsent(interview);

    Consent template = new Consent();
    template.setInterview(interview);

    Assert.assertEquals(0, persistenceManager.match(template).size());
  }

}
