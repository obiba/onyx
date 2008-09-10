package org.obiba.onyx.engine;

import java.io.Serializable;

/**
 * An {@link ActionDefinition} builder, to allow friendly action definition creation.
 * 
 * @see ActionType, ActionDefinition
 * @author Yannick Marcon
 * 
 */
public class ActionDefinitionBuilder implements Serializable {

  private static final long serialVersionUID = 5052458659458743800L;

  /**
   * Default start action.
   */
  public static final ActionDefinition START_ACTION = ActionDefinitionBuilder.create(ActionType.EXECUTE, "Start").setDescription("You may enter a comment before starting this stage.").setAskPassword(true).getActionDefinition();

  /**
   * Default cancel action.
   */
  public static final ActionDefinition CANCEL_ACTION = ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("You may enter a comment before cancelling this stage.").getActionDefinition();

  /**
   * Cancel action for a skipped stage (requires a different description).
   */
  public static final ActionDefinition CANCEL_SKIPPED_ACTION = ActionDefinitionBuilder.create(ActionType.STOP, "Cancel").setDescription("Please explain why you are reinstating this stage.").getActionDefinition();
  
  /**
   * Default comment action.
   */
  public static final ActionDefinition COMMENT_ACTION = ActionDefinitionBuilder.create(ActionType.COMMENT, "Comment").setDescription("You may enter a free comment.").getActionDefinition();

  /**
   * Default complete action.
   */
  public static final ActionDefinition COMPLETE_ACTION = ActionDefinitionBuilder.create(ActionType.COMPLETE, "Complete").getActionDefinition();

  private ActionDefinition actionDefinition;

  private ActionDefinitionBuilder() {
  }

  public ActionDefinition getActionDefinition() {
    return actionDefinition;
  }

  public static ActionDefinitionBuilder create(ActionType type, String label) {
    ActionDefinitionBuilder builder = new ActionDefinitionBuilder();
    builder.actionDefinition = new ActionDefinition(type, label);
    return builder;
  }

  public ActionDefinitionBuilder setDescription(String description) {
    actionDefinition.setDescription(description);
    return this;
  }

  public ActionDefinitionBuilder setAskPassword(boolean askPassword) {
    actionDefinition.setAskPassword(askPassword);
    return this;
  }

  public ActionDefinitionBuilder setCommentMandatory(boolean commentMandatory) {
    actionDefinition.setCommentMandatory(commentMandatory);
    return this;
  }

  public ActionDefinitionBuilder setReasonMandatory(boolean reasonMandatory) {
    actionDefinition.setReasonMandatory(reasonMandatory);
    return this;
  }

  public ActionDefinitionBuilder setDefaultReason(String defaultReason) {
    actionDefinition.setDefaultReason(defaultReason);
    return this;
  }

  public ActionDefinitionBuilder addReason(String reason) {
    actionDefinition.addReason(reason);
    return this;
  }

  public ActionDefinitionBuilder addReasons(String[] reasons) {
    actionDefinition.addReasons(reasons);
    return this;
  }

  public ActionDefinitionBuilder setIconPath(String iconPath) {
    actionDefinition.setIconPath(iconPath);
    return this;
  }

}
