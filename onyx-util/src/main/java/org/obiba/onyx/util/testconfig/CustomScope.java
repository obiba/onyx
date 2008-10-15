package org.obiba.onyx.util.testconfig;

import org.springframework.beans.factory.config.Scope;
import org.springframework.beans.factory.ObjectFactory;


public class CustomScope implements Scope {

  public String getConversationId() {
    return null;
  }

  public Object get(String name, ObjectFactory objectFactory) {
    return objectFactory.getObject();
  }

  public Object remove(String name) {
    return null;
  }

  public void registerDestructionCallback(String name, Runnable callback) {
  }

}
