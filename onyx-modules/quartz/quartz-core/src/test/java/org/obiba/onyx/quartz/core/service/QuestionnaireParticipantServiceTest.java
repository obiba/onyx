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

import junit.framework.Assert;

import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionAnswer;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.springframework.beans.factory.annotation.Autowired;

public class QuestionnaireParticipantServiceTest extends BaseDefaultSpringContextTestCase {
  
  @Autowired(required=true)
  PersistenceManager persistenceManager;
  
  @Autowired(required = true)
  QuestionnaireParticipantService questionnaireParticipantService;
  
  @Test
  @Dataset
  public void testGetLastQuestionnaireParticipant() {
    Participant participant = persistenceManager.get(Participant.class, Long.valueOf("1"));
    QuestionnaireParticipant questionnaireParticipant = questionnaireParticipantService.getLastQuestionnaireParticipant(participant, "QUE1");
    Assert.assertEquals((long) 1, questionnaireParticipant.getId());
  }
  
  @Test
  @Dataset
  public void testDeleteQuestionnaireParticipant() {
    questionnaireParticipantService.deleteQuestionnaireParticipant(Long.valueOf("2"));
    Assert.assertNull(persistenceManager.get(QuestionnaireParticipant.class, Long.valueOf("2")));
    Assert.assertNotNull(persistenceManager.get(QuestionnaireParticipant.class, Long.valueOf("1")));
    Assert.assertNull(persistenceManager.get(QuestionAnswer.class, Long.valueOf("1")));
    Assert.assertNull(persistenceManager.get(QuestionAnswer.class, Long.valueOf("2")));
    Assert.assertNotNull(persistenceManager.get(QuestionAnswer.class, Long.valueOf("3")));
    Assert.assertNull(persistenceManager.get(CategoryAnswer.class, Long.valueOf("2")));
    Assert.assertNull(persistenceManager.get(CategoryAnswer.class, Long.valueOf("3")));
    Assert.assertNotNull(persistenceManager.get(CategoryAnswer.class, Long.valueOf("4")));
  }
}
