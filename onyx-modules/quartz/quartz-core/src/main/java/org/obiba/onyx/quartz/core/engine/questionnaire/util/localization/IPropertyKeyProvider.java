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

import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;

/**
 * Provides/ckecks the property localization key for a {@link ILocalizable} questionnaire element.
 * @author Yannick Marcon
 *
 */
public interface IPropertyKeyProvider {
  
  /**
   * Get the property localization key.
   * @param localizable
   * @param property
   * @return
   * @throws IllegalArgumentException if given property is not associated to the localizable.
   */
  public String getPropertyKey(ILocalizable localizable, String property);
  
  /**
   * Get the property keys associated to given localizable.
   * @param localizable
   * @return
   */
  public List<String> getProperties(ILocalizable localizable);
  
}
