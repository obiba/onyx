package org.obiba.onyx.engine;


public class ActionDefinition {

  private ActionType type;

  private boolean askPassword = false;

  private boolean commentMandatory = true;

  private boolean reasonMandatory = false;

  private String iconPath;

  public ActionDefinition(ActionType type) {
    this.type = type;
  }

  public ActionType getType() {
    return type;
  }

  public void setType(ActionType type) {
    this.type = type;
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
