package org.obiba.onyx.engine.state;

public class TransitionEvent {

  private String name;

  public TransitionEvent(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name;
  }

}
