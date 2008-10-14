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
