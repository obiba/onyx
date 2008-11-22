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

/**
 * Holds all {@link ActionDefinition} instances that were configured through the configuration files.
 * <p>
 * Currently, this implementations returns the same {@code ActionDefinition} in every situation.
 */
public class ActionDefinitionConfiguration {

  private static final char SEPARATOR = '.';

  private static final String ACTION_PREFIX = "action" + SEPARATOR;

  public ActionDefinition getActionDefinition(ActionType type, String code) {
    return new ActionDefinition(type, ACTION_PREFIX + type.toString());
  }

  public ActionDefinition getActionDefinition(ActionType type, String stateName, String module, String stage) {
    StringBuilder sb = new StringBuilder(ACTION_PREFIX);
    sb.append(type).append(SEPARATOR).append(stateName).append(SEPARATOR).append(module).append(SEPARATOR).append(stage);

    ActionDefinition ad = new ActionDefinition(type, sb.toString());
    ad.setAskParticipantId(true);
    if(type == ActionType.EXECUTE) {
      ad.setAskPassword(true);
    }
    return ad;
  }
}
