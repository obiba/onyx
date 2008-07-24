package org.obiba.onyx.core.service.impl;

import org.obiba.core.service.impl.PersistenceManagerAwareService;
import org.obiba.onyx.core.domain.Participant;
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
    
    return participant;
  }
}
