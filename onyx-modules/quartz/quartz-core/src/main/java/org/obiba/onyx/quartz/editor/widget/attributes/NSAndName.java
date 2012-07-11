package org.obiba.onyx.quartz.editor.widget.attributes;

import java.io.Serializable;

public class NSAndName implements Serializable {

  private String namespace;

  private String name;

  public NSAndName(String namespace, String name) {
    this.namespace = namespace;
    this.name = name;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
