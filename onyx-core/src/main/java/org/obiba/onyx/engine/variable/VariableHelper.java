/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable;

import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Helper class for provisioning a variable with localized attributes.
 */
public class VariableHelper implements ApplicationContextAware {

  public static final String VALIDATION = "validation";

  public static final String CONDITION = "condition";

  public static final String SOURCE = "source";

  public static final String OCCURRENCE = "occurrence";

  private ApplicationContext applicationContext;

  public VariableHelper() {
    super();
  }

  public VariableHelper(ApplicationContext applicationContext) {
    super();
    this.applicationContext = applicationContext;
  }

  /**
   * Add "label" localized attributes for the given property.
   * @param variable
   * @param property
   */
  public void addLocalizedAttributes(Variable variable, String property) {
    if(property != null) {
      for(Locale locale : getLocales()) {
        try {
          String message = applicationContext.getMessage(property, null, locale);
          if(message.trim().length() > 0) {
            variable.addAttributes(new Attribute("label", locale, message));
          }
        } catch(NoSuchMessageException ex) {
          // ignore
        }
      }
    }
  }

  /**
   * Add "label" localized attributes for the given message source.
   * @param variable
   * @param resolvable
   */
  public void addLocalizedAttributes(Variable variable, MessageSourceResolvable resolvable) {
    if(resolvable != null) {
      for(Locale locale : getLocales()) {
        try {
          String message = applicationContext.getMessage(resolvable, locale);
          if(message.trim().length() > 0) {
            variable.addAttributes(new Attribute("label", locale, message));
          }
        } catch(NoSuchMessageException ex) {
          // ignore
        }
      }
    }
  }

  /**
   * Get the locales.
   * @return
   */
  private Locale[] getLocales() {
    // TODO get the list of available languages !
    return new Locale[] { Locale.ENGLISH, Locale.FRENCH };
  }

  /**
   * Add "label" localized attributes from the variable name.
   * @param variable
   */
  public void addLocalizedAttributes(Variable variable) {
    addLocalizedAttributes(variable, variable.getName());
  }

  public void addConditionAttribute(Variable variable, Object source) {
    addAttribute(variable, CONDITION, source);
  }

  public void addValidationAttribute(Variable variable, Object source) {
    addAttribute(variable, VALIDATION, source);
  }

  public void addSourceAttribute(Variable variable, Object source) {
    addAttribute(variable, SOURCE, source);
  }

  private void addAttribute(Variable variable, String key, Object source) {
    if(source != null) {
      variable.addAttributes(new Attribute(key, source.toString()));
    }
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
