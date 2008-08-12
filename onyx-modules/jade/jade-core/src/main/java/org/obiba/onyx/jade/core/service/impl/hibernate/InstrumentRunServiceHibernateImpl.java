package org.obiba.onyx.jade.core.service.impl.hibernate;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.obiba.core.service.SortingClause;
import org.obiba.core.service.impl.hibernate.AssociationCriteria;
import org.obiba.core.service.impl.hibernate.AssociationCriteria.Operation;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.jade.core.domain.instrument.FixedSource;
import org.obiba.onyx.jade.core.domain.instrument.InputSource;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.onyx.jade.core.domain.instrument.OperatorSource;
import org.obiba.onyx.jade.core.domain.instrument.OutputParameterSource;
import org.obiba.onyx.jade.core.domain.instrument.ParticipantPropertySource;
import org.obiba.onyx.jade.core.domain.run.InstrumentRun;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunStatus;
import org.obiba.onyx.jade.core.domain.run.InstrumentRunValue;
import org.obiba.onyx.jade.core.domain.run.ParticipantInterview;
import org.obiba.onyx.jade.core.service.InputDataSourceVisitor;
import org.obiba.onyx.jade.core.service.impl.DefaultInstrumentRunServiceImpl;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class InstrumentRunServiceHibernateImpl extends DefaultInstrumentRunServiceImpl implements InputDataSourceVisitor {

  private Data data;

  private Participant participant;

  private SessionFactory factory;

  public void setSessionFactory(SessionFactory factory) {
    this.factory = factory;
  }

  private Session getSession() {
    return factory.getCurrentSession();
  }

  public InstrumentRun getLastInstrumentRun(ParticipantInterview participantInterview, InstrumentType instrumentType) {
    return (InstrumentRun) AssociationCriteria.create(InstrumentRun.class, getSession()).add("instrument.instrumentType", Operation.eq, instrumentType).add("participantInterview", Operation.eq, participantInterview).addSortingClauses(new SortingClause("timeEnd", false)).getCriteria().uniqueResult();
  }

  public InstrumentRun getLastCompletedInstrumentRun(ParticipantInterview participantInterview, InstrumentType instrumentType) {
    return (InstrumentRun) AssociationCriteria.create(InstrumentRun.class, getSession()).add("instrument.instrumentType", Operation.eq, instrumentType).add("participantInterview", Operation.eq, participantInterview).add("status", Operation.eq, InstrumentRunStatus.COMPLETED).addSortingClauses(new SortingClause("timeEnd", false)).getCriteria().uniqueResult();
  }

  public InstrumentRunValue findInstrumentRunValue(ParticipantInterview participantInterview, InstrumentType instrumentType, String parameterName) {
    InstrumentRunValue runValue = null;
    InstrumentRun run = getLastCompletedInstrumentRun(participantInterview, instrumentType);

    if(run != null) {
      runValue = (InstrumentRunValue) AssociationCriteria.create(InstrumentRunValue.class, getSession()).add("instrumentRun", Operation.eq, run).add("instrumentParameter.name", Operation.eq, parameterName).getCriteria().uniqueResult();
    }

    return runValue;
  }

  public synchronized Data getData(Participant participant, InputSource source) {
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

      data = new Data(convertDataType(propertyMethod.getReturnType()), (Serializable) propertyValue);
    } catch(Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private DataType convertDataType(Class c) {
    if(c.equals(Date.class)) {
      return (DataType.DATE);
    } else {
      return (DataType.TEXT);
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
    interview = getPersistenceManager().matchOne(interview);
    if(interview != null) {
      InstrumentRunValue runValue = findInstrumentRunValue(interview, source.getInstrumentType(), source.getParameterName());
      if(runValue != null) data = runValue.getData();
    }
  }

}
