package org.obiba.onyx.engine.state;

public class TransitionEvent {
  
  public static final TransitionEvent START = new TransitionEvent("start");
  public static final TransitionEvent CANCEL = new TransitionEvent("cancel");
  public static final TransitionEvent COMPLETE = new TransitionEvent("complete");
  public static final TransitionEvent SKIP = new TransitionEvent("skip");
  public static final TransitionEvent INTERRUPT = new TransitionEvent("interrupt");
  public static final TransitionEvent RESUME = new TransitionEvent("resume");

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
