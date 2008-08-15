package org.obiba.onyx.jade.core.service.impl;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.FixedSource;
import org.obiba.onyx.jade.core.domain.instrument.InputSource;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.OutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantPropertySource;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;

public class InputDataSourceVisitorImpl implements InputDataSourceVisitor {

  private EntityQueryService queryService;
  
  private InstrumentRunService instrumentRunService;
  
  private Data data;

  private Participant participant;
  
  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public Data getData(Participant participant, InputSource source) {
    if(source == null) return null;

    this.participant = participant;
    data = null;
    source.accept(this);
    return data;
  }

  public void visit(ParticipantPropertySource source) {
    Class participantClass = Participant.class;
    Method propertyMethod;
    try {
      propertyMethod = participantClass.getDeclaredMethod("get" + source.getProperty().substring(0, 1).toUpperCase() + source.getProperty().substring(1));

      Object propertyValue = propertyMethod.invoke(participant);
      if(propertyMethod.invoke(participant) instanceof Gender) propertyValue = propertyValue.toString();
      
      if(propertyMethod.invoke(participant) instanceof Date)
        data = new Data(DataType.DATE, (Date) propertyValue);
      else
        data = new Data(DataType.TEXT, (Serializable) propertyValue);
      
    } catch(Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void visit(FixedSource source) {
    data = new Data(DataType.TEXT, source.getValue());
  }

  public void visit(OperatorSource source) {
    // TODO Auto-generated method stub

  }

  public void visit(OutputParameterSource source) {
    ParticipantInterview interview = new ParticipantInterview();
    interview.setParticipant(participant);
    interview = queryService.matchOne(interview);
    if(interview != null) {
      InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(interview, source.getInstrumentType(), source.getParameterName());
      if(runValue != null) data = runValue.getData();
    }
  }
}
