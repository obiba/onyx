package org.obiba.onyx.core.etl.participant.impl;

import java.util.List;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.etl.participant.IParticipantPostProcessor;
import org.springframework.batch.item.ExecutionContext;

/**
 * No-operation participant post processor.
 */
public class NoOpParticipantPostProcessor implements IParticipantPostProcessor {

  @Override
  public void process(ExecutionContext context, List<Participant> participants) {

  }

}
