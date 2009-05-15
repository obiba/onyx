package org.obiba.onyx.marble.core.service;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.Assert;

import org.apache.wicket.util.io.Streams;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.marble.core.service.impl.DefaultActiveConsentServiceImpl;
import org.obiba.onyx.marble.domain.consent.Consent;

public class ActiveConsentServiceTest {

  DefaultActiveConsentServiceImpl activeConsentService;

  ActiveInterviewService activeInterviewService;

  @SuppressWarnings("serial")
  @Before
  public void setup() {

    activeInterviewService = createMock(ActiveInterviewService.class);
    Interview interview = new Interview();
    interview.setId("1");
    expect(activeInterviewService.getInterview()).andReturn(interview);

    activeConsentService = new DefaultActiveConsentServiceImpl();
    activeConsentService.setActiveInterviewService(activeInterviewService);

    replay(activeInterviewService);

  }

  @Test
  public void testGetConsent() {
    Consent consent = activeConsentService.getConsent();
    Assert.assertNotNull(consent);
  }

  private void setPdfForm(InputStream pdfStream) throws IOException {
    ByteArrayOutputStream convertedStream = new ByteArrayOutputStream();

    Streams.copy(pdfStream, convertedStream);
    convertedStream.close();

    Consent consent = activeConsentService.getConsent();
    consent.setPdfForm(convertedStream.toByteArray());
  }

  @Test
  public void testValidateElectronicConsentValidForm() throws IOException {
    setPdfForm(getClass().getResourceAsStream("/validConsentForm.pdf"));
    Assert.assertTrue(activeConsentService.validateElectronicConsent());
  }

  @Test
  public void testValidateElectronicConsentMissingSignature() throws IOException {
    setPdfForm(getClass().getResourceAsStream("/invalidConsentFormMissingSignature.pdf"));
    Assert.assertFalse(activeConsentService.validateElectronicConsent());
  }

  @Test
  public void testValidateElectronicConsentMissingField() throws IOException {
    setPdfForm(getClass().getResourceAsStream("/invalidConsentFormMissingField.pdf"));
    Assert.assertFalse(activeConsentService.validateElectronicConsent());
  }

}
