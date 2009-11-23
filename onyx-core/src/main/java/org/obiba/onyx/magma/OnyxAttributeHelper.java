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

import org.obiba.magma.AttributeAwareBuilder;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

/**
 * Helper class for provisioning a variable with localized attributes.
 */
public class OnyxAttributeHelper implements ApplicationContextAware {

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

  public OnyxAttributeHelper() {
    super();
  }

  public OnyxAttributeHelper(ApplicationContext applicationContext) {
    super();
    this.applicationContext = applicationContext;
  }

  /**
   * Add "label" localized attributes for the given property.
   * @param variable
   * @param property
   */
  public void addLocalizedAttributes(AttributeAwareBuilder<?> builder, String property) {
    addLocalizedAttributes(builder, LABEL, property);
  }

  /**
   * Add localized attributes for the given key/property.
   * @param variable
   * @param key
   * @param property
   */
  public void addLocalizedAttributes(AttributeAwareBuilder<?> builder, String key, String property) {
    if(property != null) {
      for(Locale locale : getLocales()) {
        try {
          String message = applicationContext.getMessage(property, null, locale);
          if(message.trim().length() > 0) {
            addAttribute(builder, locale, key, message);
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
  public void addLocalizedAttributes(AttributeAwareBuilder<?> builder, MessageSourceResolvable resolvable) {
    addLocalizedAttributes(builder, LABEL, resolvable);
  }

  /**
   * Add localized attributes for the given key/message source.
   * @param variable
   * @param key
   * @param resolvable
   */
  public void addLocalizedAttributes(AttributeAwareBuilder<?> builder, String key, MessageSourceResolvable resolvable) {
    if(resolvable != null) {
      for(Locale locale : getLocales()) {
        try {
          String message = applicationContext.getMessage(resolvable, locale);
          if(message.trim().length() > 0) {
            addAttribute(builder, locale, key, message);
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
  public static void addIsManualCaptureAllowedAttribute(AttributeAwareBuilder<?> builder, Object source) {
    addAttribute(builder, ISMANUALCAPTUREALLOWED, source);
  }

  public static void addDefaultCaptureMethodAttribute(AttributeAwareBuilder<?> builder, Object source) {
    addAttribute(builder, CAPTUREMETHOD, source);
  }

  public static void addConditionAttribute(AttributeAwareBuilder<?> builder, Object source) {
    addAttribute(builder, CONDITION, source);
  }

  public static <T extends AttributeAwareBuilder> void addValidationAttribute(T builder, Object source) {
    addAttribute(builder, VALIDATION, source);
  }

  public static <T extends AttributeAwareBuilder> void addSourceAttribute(T builder, Object source) {
    addAttribute(builder, SOURCE, source);
  }

  public static <T extends AttributeAwareBuilder> void addRequiredAttribute(T builder, Object source) {
    addAttribute(builder, REQUIRED, source);
  }

  public static <T extends AttributeAwareBuilder> void addMinCountAttribute(T builder, Object source) {
    addAttribute(builder, MINCOUNT, source);
  }

  public static <T extends AttributeAwareBuilder> void addMaxCountAttribute(T builder, Object source) {
    addAttribute(builder, MAXCOUNT, source);
  }

  public static <T extends AttributeAwareBuilder> void addOccurrenceCountAttribute(T builder, Object source) {
    addAttribute(builder, OCCURRENCECOUNT, source);
  }

  public static <T extends AttributeAwareBuilder> void addGroupAttribute(T builder, Object source) {
    addAttribute(builder, GROUP, source);
  }

  public static <T extends AttributeAwareBuilder> void addAttribute(T builder, String key, Object source) {
    if(source != null) {
      builder.addAttribute(key, source.toString());
    }
  }

  public static <T extends AttributeAwareBuilder> void addAttribute(T builder, Locale locale, String key, Object source) {
    if(source != null) {
      builder.addAttribute(key, source.toString(), locale);
    }
  }

  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

}
