package org.obiba.onyx.core.etl.participant;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.springframework.batch.item.ExecutionContext;

/**
 * This is called by the {@link org.obiba.onyx.core.etl.participant.impl.AppointmentListUpdateListener} for handling additional
 * operations after the {@link org.obiba.onyx.core.domain.participant.Participant} has been persisted in the database.
 */
public interface IParticipantPostProcessor {

  void process(ExecutionContext context, List<Participant> participants);

}
