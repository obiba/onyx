package org.obiba.onyx.jade.core.service.impl;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.text.SimpleDateFormat;

import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.FixedSource;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentInputParameter;
import org.obiba.onyx.jade.core.domain.instrument.MultipleOutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.OutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantPropertySource;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.jade.core.service.InstrumentRunService;
import org.obiba.onyx.util.data.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputDataSourceVisitorImpl implements InputDataSourceVisitor {

  private static final Logger log = LoggerFactory.getLogger(InputDataSourceVisitorImpl.class);

  private EntityQueryService queryService;

  private InstrumentRunService instrumentRunService;

  private Data data;

  private Participant participant;

  private InstrumentInputParameter parameter;

  public void setQueryService(EntityQueryService queryService) {
    this.queryService = queryService;
  }

  public void setInstrumentRunService(InstrumentRunService instrumentRunService) {
    this.instrumentRunService = instrumentRunService;
  }

  public Data getData(Participant participant, InstrumentInputParameter parameter) {
    if(parameter == null) return null;
    if(parameter.getInputSource() == null) return null;

    this.participant = participant;
    this.parameter = parameter;
    data = null;
    parameter.getInputSource().accept(this);
    return data;
  }

  public void visit(ParticipantPropertySource source) {
    try {
      log.info("source.participant.property=" + source.getProperty());
      for(PropertyDescriptor pd : Introspector.getBeanInfo(Participant.class).getPropertyDescriptors()) {
        if(source.getProperty().equals(pd.getName())) {
          Object propertyValue = pd.getReadMethod().invoke(participant);
          log.info("source.participant.property." + source.getProperty() + "=" + propertyValue + " " + propertyValue.getClass().getSimpleName());
          if(propertyValue instanceof Gender) {
            propertyValue = propertyValue.toString();
          }

          data = new Data(parameter.getDataType(), (Serializable) propertyValue);
        }
      }

    } catch(Exception e) {
      log.warn("Failed getting Participant property: " + source.getProperty(), e);
    }
  }

  public void visit(FixedSource source) {
    Serializable value = null;
    if(source.getValue() != null) {
      try {
        switch(parameter.getDataType()) {
        case BOOLEAN:
          value = Boolean.parseBoolean(source.getValue());
          break;
        case INTEGER:
          value = Long.parseLong(source.getValue());
          break;
        case DECIMAL:
          value = Double.parseDouble(source.getValue());
          break;
        case DATE:
          value = SimpleDateFormat.getInstance().parse(source.getValue());
          break;
        case TEXT:
          value = source.getValue();
          break;
        case DATA:
          value = source.getValue().getBytes();
          break;
        }
      } catch(Exception e) {
        log.warn("Failed getting Fixed value: " + source.getValue(), e);
      }
    }
    data = new Data(parameter.getDataType(), value);
  }

  public void visit(OperatorSource source) {
    data = null;
  }

  public void visit(OutputParameterSource source) {
    ParticipantInterview interview = new ParticipantInterview();
    interview.setParticipant(participant);
    interview = queryService.matchOne(interview);
    if(interview != null) {
      InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(interview, source.getInstrumentType(), source.getParameterName());
      if(runValue != null) {
        // TODO unit conversion when necessary
        // TODO type conversion when possible (INTEGER->DECIMAL->TEXT...)
        data = runValue.getData();
      }
    }
  }
  
  public void visit(MultipleOutputParameterSource source) {
    ParticipantInterview interview = new ParticipantInterview();
    interview.setParticipant(participant);
    interview = queryService.matchOne(interview);

    if(interview != null) {
      for(OutputParameterSource outputParameterSource : source.getOutputParameterSourceList()) {
        InstrumentRunValue runValue = instrumentRunService.findInstrumentRunValue(interview, outputParameterSource.getInstrumentType(), outputParameterSource.getParameterName());
        if(runValue != null) {
          if (runValue.getData().getValue() != null){
            data = runValue.getData();
            break;
          }
        }
      }
    }
  }
}
