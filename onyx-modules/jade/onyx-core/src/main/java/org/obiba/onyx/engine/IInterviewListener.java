package org.obiba.onyx.engine;

import org.obiba.onyx.core.domain.participant.Interview;

public interface IInterviewListener {

  public void onInterviewClosed(Interview interview);
  
}
