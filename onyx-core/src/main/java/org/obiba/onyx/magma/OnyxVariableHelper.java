/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.magma;

import java.util.Locale;

import org.obiba.magma.Variable.Builder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Helper class for provisioning a variable with localized attributes.
 */
public class OnyxVariableHelper implements ApplicationContextAware {

  public static final String LABEL = "label";

  public static final String VALIDATION = "validation";

  public static final String CONDITION = "condition";

  public static final String CAPTUREMETHOD = "captureMethod";

  public static final String ISMANUALCAPTUREALLOWED = "isManualCaptureAllowed";

  public static final String SOURCE = "source";

  public static final String OCCURRENCECOUNT = "occurrenceCount";

  public static final String REQUIRED = "required";

  public static final String MINCOUNT = "minCount";

  public static final String MAXCOUNT = "maxCount";

  public static final String GROUP = "group";

  private ApplicationContext applicationContext;

  public OnyxVariableHelper() {
    super();
  }

  public OnyxVariableHelper(ApplicationContext applicationContext) {
    super();
    this.applicationContext = applicationContext;
  }

  /**
   * Add "label" localized attributes for the given property.
   * @param variable
   * @param property
   */
  public void addLocalizedAttributes(Builder variableBuilder, String property) {
    addLocalizedAttributes(variableBuilder, LABEL, property);
  }

  /**
   * Add localized attributes for the given key/property.
   * @param variable
   * @param key
   * @param property
   */
  public void addLocalizedAttributes(Builder variableBuilder, String key, String property) {
    if(property != null) {
      for(Locale locale : getLocales()) {
        try {
          String message = applicationContext.getMessage(property, null, locale);
          if(message.trim().length() > 0) {
            addAttribute(variableBuilder, locale, key, message);
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
  public void addLocalizedAttributes(Builder variableBuilder, MessageSourceResolvable resolvable) {
    addLocalizedAttributes(variableBuilder, LABEL, resolvable);
  }

  /**
   * Add localized attributes for the given key/message source.
   * @param variable
   * @param key
   * @param resolvable
   */
  public void addLocalizedAttributes(Builder variableBuilder, String key, MessageSourceResolvable resolvable) {
    if(resolvable != null) {
      for(Locale locale : getLocales()) {
        try {
          String message = applicationContext.getMessage(resolvable, locale);
          if(message.trim().length() > 0) {
            addAttribute(variableBuilder, locale, key, message);
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
  // public void addLocalizedAttributes(Variable variable) {
  // addLocalizedAttributes(variable, variable.getName());
  // }
  public static void addIsManualCaptureAllowedAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, ISMANUALCAPTUREALLOWED, source);
  }

  public static void addDefaultCaptureMethodAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, CAPTUREMETHOD, source);
  }

  public static void addConditionAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, CONDITION, source);
  }

  public static void addValidationAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, VALIDATION, source);
  }

  public static void addSourceAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, SOURCE, source);
  }

  public static void addRequiredAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, REQUIRED, source);
  }

  public static void addMinCountAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, MINCOUNT, source);
  }

  public static void addMaxCountAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, MAXCOUNT, source);
  }

  public static void addOccurrenceCountAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, OCCURRENCECOUNT, source);
  }

  public static void addGroupAttribute(Builder variableBuilder, Object source) {
    addAttribute(variableBuilder, GROUP, source);
  }

  public static void addAttribute(Builder variableBuilder, String key, Object source) {
    if(source != null) {
      variableBuilder.addAttribute(key, source.toString());
    }
  }

  public static void addAttribute(Builder variableBuilder, Locale locale, String key, Object source) {
    if(source != null) {
      variableBuilder.addAttribute(key, source.toString(), locale);
    }
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
