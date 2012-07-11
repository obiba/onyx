/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.util.List;
import java.util.Locale;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.obiba.magma.Attribute;

public class Attributes {
  /**
   * @param attributes
   * @param index
   * @param namespace
   * @param name
   * @param value
   * @param locale
   */
  public static void addAttribute(List<Attribute> attributes, int index, String namespace, String name, String value,
      Locale locale) {
    attributes.add(index, buildAttribute(namespace, name, value, locale));
  }

  /**
   * @param attributes
   * @param namespace
   * @param name
   * @param value
   * @param locale
   */
  public static void addAttribute(List<Attribute> attributes, String namespace, String name, String value,
      Locale locale) {
    attributes.add(buildAttribute(namespace, name, value, locale));
  }

  /**
   * @param namespace
   * @param name
   * @param value
   * @param locale
   * @return
   */
  private static Attribute buildAttribute(String namespace, String name, String value, Locale locale) {
    Attribute.Builder attributeBuilder = Attribute.Builder.newAttribute();
    attributeBuilder.withNamespace(Strings.emptyToNull(namespace));
    attributeBuilder.withName(name);
    attributeBuilder.withValue(Strings.nullToEmpty(value));
    attributeBuilder.withLocale(locale);
    Attribute attribute = attributeBuilder.build();
    return attribute;
  }

  /**
   * @param attributes
   * @param attribute
   * @return
   */
  public static boolean containsAttribute(List<Attribute> attributes, Attribute attribute) {
    return attributes.contains(attribute);
  }

  /**
   * @param attributes
   * @param namespace
   * @param name
   * @param locale
   * @return
   */
  public static Attribute getAttribute(List<Attribute> attributes, String namespace, String name, Locale locale) {
    return Iterables.find(attributes, predicate(namespace, name, locale));
  }

  /**
   * @param attributes
   * @param attribute
   * @param namespace
   * @param name
   * @param value
   * @param locale
   */
  public static void updateAttribute(List<Attribute> attributes, Attribute attribute, String namespace, String name,
      String value, Locale locale) {
    if(attributes.contains(attribute)) {
      int index = attributes.indexOf(attribute);
      attributes.remove(attribute);
      addAttribute(attributes, index, namespace, name, value, locale);
    } else {
      addAttribute(attributes, namespace, name, value, locale);
    }
  }

  /**
   * @param attributes
   * @param attribute
   */
  public static void removeAttribute(List<Attribute> attributes, Attribute attribute) {
    attributes.remove(attribute);
  }

  /**
   * @param namespace
   * @param name
   * @param locale
   * @return
   */
  public static Predicate<Attribute> predicate(final String namespace, final String name, final Locale locale) {
    final String nullIfEmptyNamespace = Strings.emptyToNull(namespace);
    return new Predicate<Attribute>() {
      @Override
      public boolean apply(Attribute input) {
        return
            input.getName().equals(name)
                && (input.getNamespace() == null
                ? nullIfEmptyNamespace == null
                : input.getNamespace().equals(nullIfEmptyNamespace))

                && (input.getLocale() == null
                ? locale == null
                : input.getLocale().equals(locale));
      }
    };
  }
}
