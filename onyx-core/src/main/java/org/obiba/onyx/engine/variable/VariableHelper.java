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

  public static final String LABEL = "label";

  public static final String VALIDATION = "validation";

  public static final String CONDITION = "condition";

  public static final String SOURCE = "source";

  public static final String OCCURRENCECOUNT = "occurrenceCount";

  public static final String REQUIRED = "required";

  public static final String MINCOUNT = "minCount";

  public static final String MAXCOUNT = "maxCount";

  public static final String GROUP = "group";

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
    addLocalizedAttributes(variable, LABEL, property);
  }

  /**
   * Add localized attributes for the given key/property.
   * @param variable
   * @param key
   * @param property
   */
  public void addLocalizedAttributes(Variable variable, String key, String property) {
    if(property != null) {
      for(Locale locale : getLocales()) {
        try {
          String message = applicationContext.getMessage(property, null, locale);
          if(message.trim().length() > 0) {
            addAttribute(variable, locale, key, message);
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
    addLocalizedAttributes(variable, LABEL, resolvable);
  }

  /**
   * Add localized attributes for the given key/message source.
   * @param variable
   * @param key
   * @param resolvable
   */
  public void addLocalizedAttributes(Variable variable, String key, MessageSourceResolvable resolvable) {
    if(resolvable != null) {
      for(Locale locale : getLocales()) {
        try {
          String message = applicationContext.getMessage(resolvable, locale);
          if(message.trim().length() > 0) {
            addAttribute(variable, locale, key, message);
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

  public static void addConditionAttribute(Variable variable, Object source) {
    addAttribute(variable, CONDITION, source);
  }

  public static void addValidationAttribute(Variable variable, Object source) {
    addAttribute(variable, VALIDATION, source);
  }

  public static void addSourceAttribute(Variable variable, Object source) {
    addAttribute(variable, SOURCE, source);
  }

  public static void addRequiredAttribute(Variable variable, Object source) {
    addAttribute(variable, REQUIRED, source);
  }

  public static void addMinCountAttribute(Variable variable, Object source) {
    addAttribute(variable, MINCOUNT, source);
  }

  public static void addMaxCountAttribute(Variable variable, Object source) {
    addAttribute(variable, MAXCOUNT, source);
  }

  public static void addOccurrenceCountAttribute(Variable variable, Object source) {
    addAttribute(variable, OCCURRENCECOUNT, source);
  }

  public static void addGroupAttribute(Variable variable, Object source) {
    addAttribute(variable, GROUP, source);
  }

  public static void addAttribute(Variable variable, String key, Object source) {
    if(source != null) {
      variable.addAttributes(new Attribute(key, source.toString()));
    }
  }

  public static void addAttribute(Variable variable, Locale locale, String key, Object source) {
    if(source != null) {
      variable.addAttributes(new Attribute(key, locale, source.toString()));
    }
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
