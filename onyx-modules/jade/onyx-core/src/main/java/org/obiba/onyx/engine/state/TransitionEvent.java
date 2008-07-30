package org.obiba.onyx.engine.state;

public enum TransitionEvent {

  START("start"), SKIP("skip"), CANCEL("cancel"), RESUME("resume"), INTERRUPT("interrupt"), COMPLETE("complete"), SEND("send");

  private String name;

  private TransitionEvent(String name) {
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
