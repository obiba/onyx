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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.obiba.magma.Attribute;
import org.obiba.onyx.quartz.editor.widget.attributes.FactorizedAttribute;

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
   * @return
   */
  public static boolean containsAttribute(List<Attribute> attributes, String namespace, String name) {
    return Iterables.any(attributes, predicate(namespace, name));
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
   * @param namespace
   * @param name
   */
  public static void removeAttributes(final List<Attribute> attributes, final String namespace, final String name) {
    Iterables.removeIf(attributes, predicate(namespace, name));
  }

  public static Predicate<Attribute> predicate(final String namespace, final String name) {
    final String nullIfEmptyNamespace = Strings.emptyToNull(namespace);
    return new Predicate<Attribute>() {
      @Override
      public boolean apply(Attribute input) {
        return input.getName().equals(name)
            && (input.getNamespace() == null
            ? nullIfEmptyNamespace == null
            : input.getNamespace().equals(nullIfEmptyNamespace));
      }
    };
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

  /**
   * @param attributes
   * @return
   */
  public static List<FactorizedAttribute> factorize(List<Attribute> attributes, List<Locale> locales) {
    if(attributes == null) {
      return null;
    }
    List<FactorizedAttribute> list = Lists.newArrayList();
    HashMultimap<String, Attribute> multi = HashMultimap.create();
    for(Attribute attribute : attributes) {
      //TODO find a way to group pair of namespace/name instead of concatenation
      multi.put(attribute.getNamespace() + ":" + attribute.getName(), attribute);
    }
    for(String uniqueKey : multi.keySet()) {
      FactorizedAttribute factorizedAttribute = new FactorizedAttribute(locales);
      list.add(factorizedAttribute);
      for(Attribute attribute : multi.get(uniqueKey)) {
        factorizedAttribute.setNamespace(attribute.getNamespace());
        factorizedAttribute.setName(attribute.getName());
        factorizedAttribute.setValue(attribute.getLocale(), (String) attribute.getValue().getValue());
      }
    }
    Collections.sort(list, new Comparator<FactorizedAttribute>() {
      @Override
      public int compare(FactorizedAttribute o1, FactorizedAttribute o2) {
        String key = o1.getNamespace() + " " + o1.getName();
        String key2 = o2.getNamespace() + " " + o2.getName();
        return key.compareTo(key2);
      }
    });
    return list;
  }
}
