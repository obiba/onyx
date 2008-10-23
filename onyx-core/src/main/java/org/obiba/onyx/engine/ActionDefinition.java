/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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
   * Says if it requires participant authentication (using its id).
   */
  private boolean askParticipantId = false;

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

  public boolean isAskParticipantId() {
    return askParticipantId;
  }

  public void setAskParticipantId(boolean askParticipantId) {
    this.askParticipantId = askParticipantId;
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
