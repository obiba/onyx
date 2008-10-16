package org.obiba.onyx.mica.core.service;

public interface ActiveConclusionService {

  public Boolean getConclusion();
  
  public void setConclusion(Boolean conclusion);
  
  public void validate();
  
}
