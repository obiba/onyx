/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.marble.core.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.ApplicationConfigurationService;
import org.obiba.onyx.core.service.UserService;
import org.obiba.onyx.marble.domain.consent.Consent;
import org.springframework.core.io.ClassPathResource;

import com.lowagie.text.pdf.AcroFields.Item;
import com.lowagie.text.pdf.FdfReader;

import static org.easymock.EasyMock.isA;

/**
 * 
 */
public class FdfProducerTest {

  FdfProducer producer = new FdfProducer() {
    public InputStream getPdfTemplate() throws IOException {
      return new ClassPathResource("cag.pdf", FdfProducerTest.class).getInputStream();
    }
  };

  ActiveConsentService consentService = EasyMock.createMock(ActiveConsentService.class);

  ActiveInterviewService interviewService = EasyMock.createMock(ActiveInterviewService.class);

  ApplicationConfigurationService applicationConfigurationService = EasyMock.createMock(ApplicationConfigurationService.class);

  UserService userService = EasyMock.createMock(UserService.class);

  Consent consent = new Consent();

  Interview interview = new Interview();

  Participant participant = new Participant();

  User user = new User();

  @Before
  public void setupProducer() {
    producer.setActiveConsentService(consentService);
    producer.setActiveInterviewService(interviewService);
    producer.setAppConfigService(applicationConfigurationService);
    producer.setUserService(userService);
    consent.setLocale(Locale.ENGLISH);
    participant.setFirstName("Philippe");
    user.setLogin("user");
    user.setFirstName("User");
  }

  @Test
  public void testElectronicConsentFieldsFilledUp() throws IOException {
    EasyMock.expect(consentService.getConsent()).andReturn(consent);
    EasyMock.expect(interviewService.getInterview()).andReturn(interview);
    EasyMock.expect(interviewService.getOperator()).andReturn(user.getLogin());
    EasyMock.expect(interviewService.getParticipant()).andReturn(participant);
    EasyMock.expect(userService.getUserWithLogin(isA(String.class))).andReturn(user);
    EasyMock.replay(consentService, interviewService, userService);

    byte[] fdf = producer.buildFdf("http://localhost/file.pdf", "http://localhost/accept", "http://localhost/refuse");

    FdfReader reader = new FdfReader(fdf);

    Map<String, Item> fields = reader.getFields();

    for(String fieldName : fields.keySet()) {
      if(fieldName.contains("Participant\\.firstName")) {
        Assert.assertEquals(participant.getFirstName(), reader.getFieldValue(fieldName));
      }

      if(fieldName.contains("User\\.firstName")) {
        Assert.assertEquals(user.getFirstName(), reader.getFieldValue(fieldName));
      }
    }
  }
}
