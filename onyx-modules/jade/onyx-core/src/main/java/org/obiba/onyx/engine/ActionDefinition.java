package org.obiba.onyx.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * An action definition, defines how getting information from the user to produce an {@link Action}.
 * 
 * @see ActionType
 * @author Yannick Marcon
 * 
 */
public class ActionDefinition implements Serializable {

  private static final long serialVersionUID = 5052458659458743800L;

  /**
   * Default start action.
   */
  public static final ActionDefinition START_ACTION = new ActionDefinition(ActionType.EXECUTE, "Start", "Please give some comments before starting this stage.", true);

  /**
   * Default cancel action.
   */
  public static final ActionDefinition CANCEL_ACTION = new ActionDefinition(ActionType.STOP, "Cancel", "Please explain why you are cancelling this stage.");

  /**
   * Default comment action.
   */
  public static final ActionDefinition COMMENT_ACTION = new ActionDefinition(ActionType.COMMENT, "Comment", "Please enter a free comment.");

  /**
   * Default complete action.
   */
  public static final ActionDefinition COMPLETE_ACTION = new ActionDefinition(ActionType.COMPLETE, "Complete");

  private ActionType type;

  /**
   * Label to be displayed to user for action identification.
   */
  private String label;

  /**
   * Description of the information needed to produce an action.
   */
  private String description;

  /**
   * Says if it requires operator authentication (using its password).
   */
  private boolean askPassword = false;

  /**
   * Says if a comment is mandatory.
   */
  private boolean commentMandatory = true;

  /**
   * Says if a reason is mandatory.
   */
  private boolean reasonMandatory = false;

  /**
   * Specify the default reason.
   */
  private String defaultReason;

  /**
   * List of reasons to be picked-up.
   */
  private List<String> reasons;

  /**
   * Path to icon for displaying action in a friendly way.
   */
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
