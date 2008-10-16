/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.service;

import java.util.Locale;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.SessionScope;

public class ActiveQuestionnaireAdministrationServiceTest extends BaseDefaultSpringContextTestCase {

  @Autowired
  PersistenceManager persistenceManager;

  ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @Before
  public void setUp() {
    ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext("test-spring-context.xml");
    applicationContext.getBeanFactory().registerScope("session", new SessionScope());

    MockHttpServletRequest request = new MockHttpServletRequest();
    MockHttpSession session = new MockHttpSession();
    request.setSession(session);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    activeQuestionnaireAdministrationService = (ActiveQuestionnaireAdministrationService) applicationContext.getBean("activeQuestionnaireAdministrationService");
  }

  @Test
  @Dataset
  public void testStart() {
    activeQuestionnaireAdministrationService.setQuestionnaire(new Questionnaire("QUE1", "1.0"));
    Participant participant = persistenceManager.get(Participant.class, Long.valueOf("1"));
    QuestionnaireParticipant questionnaireParticipant = activeQuestionnaireAdministrationService.start(participant, Locale.GERMAN);
    Assert.assertEquals(questionnaireParticipant.getLocale(), Locale.GERMAN);
    Assert.assertEquals(questionnaireParticipant.getQuestionnaireName(), "QUE1");
    Assert.assertEquals(questionnaireParticipant.getQuestionnaireVersion(), "1.0");
    Assert.assertEquals(questionnaireParticipant.getParticipant(), participant);
  }
}
