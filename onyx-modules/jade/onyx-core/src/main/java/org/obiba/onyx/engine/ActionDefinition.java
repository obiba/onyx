package org.obiba.onyx.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ActionDefinition implements Serializable {

  private static final long serialVersionUID = 5052458659458743800L;

  public static final ActionDefinition START_ACTION = new ActionDefinition(ActionType.EXECUTE, "Start", "Please give some comments before starting this stage.", true);

  public static final ActionDefinition CANCEL_ACTION = new ActionDefinition(ActionType.STOP, "Cancel", "Please explain why you are cancelling this stage.");

  public static final ActionDefinition COMMENT_ACTION = new ActionDefinition(ActionType.COMMENT, "Comment", "Please enter a free comment.");

  public static final ActionDefinition COMPLETE_ACTION = new ActionDefinition(ActionType.COMPLETE, "Complete");

  private ActionType type;

  private String label;

  private String description;

  private boolean askPassword = false;

  private boolean commentMandatory = true;

  private boolean reasonMandatory = false;

  private String defaultReason;

  private List<String> reasons;

  private String iconPath;

  public ActionDefinition(ActionType type, String label) {
    this.type = type;
    this.label = label;
  }

  public ActionDefinition(ActionType type, String label, String description) {
    this(type, label);
    this.description = description;
  }
  
  public ActionDefinition(ActionType type, String label, String description, boolean askPassword) {
    this(type, label);
    this.description = description;
    this.askPassword = askPassword;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  public String getDefaultReason() {
    return defaultReason;
  }

  public void setDefaultReason(String defaultReason) {
    this.defaultReason = defaultReason;
  }

  public List<String> getReasons() {
    return reasons != null ? reasons : (reasons = new ArrayList<String>());
  }

  public void addReason(String reason) {
    getReasons().add(reason);
  }

  public void addReasons(String[] reasons) {
    for(String reason : reasons) {
      getReasons().add(reason);
    }
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
