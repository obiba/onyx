/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.localization;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;

/**
 * Get the property localization key for a {@link IQuestionnaireElement} questionnaire element.
 * @author Yannick Marcon
 * 
 */
public interface IPropertyKeyNamingStrategy {

  /**
   * Get the property localization key.
   * @param localizable
   * @param property
   * @return
   * @throws IllegalArgumentException if given property is not associated to the localizable.
   */
  public String getPropertyKey(IQuestionnaireElement localizable, String property);

}
