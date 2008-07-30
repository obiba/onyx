package org.obiba.onyx.core.service.impl;

import java.util.Date;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ParticipantService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class DefaultParticipantServiceImpl extends PersistenceManagerAwareService implements ParticipantService {

  public Participant getCurrentParticipant() {
    Participant template = new Participant();
    template.setFirstName("Michel");
    template.setLastName("Tremblay");
    
    Participant participant = getPersistenceManager().matchOne(template);
    
    if (participant == null) {
      participant = getPersistenceManager().save(template);
    }
    
    if (participant.getInterview() == null) {
      Interview interview = new Interview();
      interview.setParticipant(participant);
      interview.setStartDate(new Date());
      getPersistenceManager().save(interview);
      participant = getPersistenceManager().refresh(participant);
    }
    
    return participant;
  }
}
