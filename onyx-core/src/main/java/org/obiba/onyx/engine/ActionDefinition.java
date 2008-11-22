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

import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;

/**
 * An action definition, defines how getting information from the user to produce an {@link Action}.
 * 
 * @see ActionType
 */
public class ActionDefinition implements Serializable {

  private static final long serialVersionUID = 5052458659458743800L;

  private ActionType type;

  /** A unique code for this action definition */
  private String code;

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

  public ActionDefinition(ActionType type, String code) {
    this.type = type;
    this.code = code;
  }

  public ActionType getType() {
    return type;
  }

  public void setType(ActionType type) {
    this.type = type;
  }

  public String getCode() {
    return code;
  }

  public MessageSourceResolvable getLabel() {
    return new DefaultMessageSourceResolvable(calculateCodes(null), getType().toString());
  }

  public MessageSourceResolvable getDescription() {
    return new DefaultMessageSourceResolvable(calculateCodes(".description"), getType().toString());
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

  @Override
  public String toString() {
    return code.toString();
  }

  protected String[] calculateCodes(String suffix) {
    ArrayList<String> codes = new ArrayList<String>();
    StringBuilder sb = new StringBuilder(getCode());
    codes.add(sb.toString() + (suffix != null ? suffix : ""));
    int lastDotIndex;
    while((lastDotIndex = sb.lastIndexOf(".")) > 0) {
      int length = sb.length();
      sb.delete(lastDotIndex, length);
      codes.add(sb.toString() + (suffix != null ? suffix : ""));
    }
    return codes.toArray(new String[codes.size()]);
  }
}
