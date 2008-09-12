package org.obiba.onyx.core.etl.participant;

import java.io.IOException;
import java.io.InputStream;

import org.obiba.core.validation.exception.ValidationRuntimeException;

public interface IParticipantReader {

  public void process(InputStream input) throws IOException, ValidationRuntimeException;
  
  public void addParticipantReadListener(IParticipantReadListener listener);
  
  public void removeParticipantReadListener(IParticipantReadListener listener);
  
}
