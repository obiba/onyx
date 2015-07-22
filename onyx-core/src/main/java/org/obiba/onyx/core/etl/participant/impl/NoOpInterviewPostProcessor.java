package org.obiba.onyx.core.etl.participant.impl;

import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.etl.participant.IInterviewPostProcessor;

/**
 * No-operation interview post processor.
 */
public class NoOpInterviewPostProcessor implements IInterviewPostProcessor {

  @Override
  public void onCreation(Interview interview) {

  }

  @Override
  public void onProgress(Interview interview) {

  }
}
