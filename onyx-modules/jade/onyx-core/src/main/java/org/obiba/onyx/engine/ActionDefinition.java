package org.obiba.onyx.engine;

import java.io.Serializable;


public class ActionDefinition implements Serializable {

  private static final long serialVersionUID = 5052458659458743800L;

  private ActionType type;
  
  private String label;

  private boolean askPassword = false;

  private boolean commentMandatory = true;

  private boolean reasonMandatory = false;

  private String iconPath;

  public ActionDefinition(ActionType type, String label) {
    this.type = type;
    this.label = label;
  }

  public ActionType getType() {
    return type;
  }

  public void setType(ActionType type) {
    this.type = type;
  }
  
  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isAskPassword() {
    return askPassword;
  }

  public void setAskPassword(boolean askPassword) {
    this.askPassword = askPassword;
  }

  public boolean isCommentMandatory() {
    return commentMandatory;
  }

  public void setCommentMandatory(boolean commentMandatory) {
    this.commentMandatory = commentMandatory;
  }

  public boolean isReasonMandatory() {
    return reasonMandatory;
  }

  public void setReasonMandatory(boolean reasonMandatory) {
    this.reasonMandatory = reasonMandatory;
  }

  public String getIconPath() {
    return iconPath;
  }

  public void setIconPath(String iconPath) {
    this.iconPath = iconPath;
  }
  
  @Override
  public String toString() {
    return type.toString();
  }

}
