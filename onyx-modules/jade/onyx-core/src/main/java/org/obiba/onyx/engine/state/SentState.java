package org.obiba.onyx.engine.state;

public abstract class SentState extends StageState {

  public boolean isInteractive() {
    return true;
  }

  public boolean isFinal() {
    return true;
  }

  public boolean isCompleted() {
    return true;
  }

}
