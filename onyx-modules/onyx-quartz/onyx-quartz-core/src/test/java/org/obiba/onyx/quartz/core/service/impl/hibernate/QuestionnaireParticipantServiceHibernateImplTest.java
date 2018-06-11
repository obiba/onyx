/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.service.impl.hibernate;

import org.junit.Assert;
import org.junit.Test;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.test.spring.BaseDefaultSpringContextTestCase;
import org.obiba.core.test.spring.Dataset;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.quartz.core.service.QuestionnaireParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
public class QuestionnaireParticipantServiceHibernateImplTest extends BaseDefaultSpringContextTestCase {

  @Autowired
  PersistenceManager persistenceManager;

  @Autowired
  QuestionnaireParticipantService service;

  @Test
  @Dataset(filenames = { "onyx-299.xml" })
  public void testIsActiveWhenNoQuestionnaireParticipant() {

    Participant participant3 = persistenceManager.get(Participant.class, 3l);

    // Participant with id 3 has no QuestionParticipant instance associated
    Boolean active = service.isQuestionActive(participant3, "QUE1", "q1");
    // ONYX-299: method used to throw org.hibernate.NonUniqueResultException dues to other entries in database

    // Assert that the method return successfully and that its return value is null.
    Assert.assertNull(active);
  }
}
