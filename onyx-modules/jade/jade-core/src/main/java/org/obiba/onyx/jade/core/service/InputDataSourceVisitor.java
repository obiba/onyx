package org.obiba.onyx.jade.core.service;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.InputSource;
import org.obiba.onyx.util.data.Data;

public interface InputDataSourceVisitor extends InputSourceVisitor {

  public Data getData(Participant participant, InputSource source);

}
