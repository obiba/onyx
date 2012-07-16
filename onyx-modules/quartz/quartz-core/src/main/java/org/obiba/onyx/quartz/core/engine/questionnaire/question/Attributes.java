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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.wicket.model.IModel;
import org.obiba.magma.Attribute;
import org.obiba.onyx.quartz.editor.widget.attributes.FactorizedAttribute;

public class Attributes {

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
   * @param namespace
   * @param name
   * @return
   */
  public static boolean containsAttribute(List<Attribute> attributes, String namespace, String name) {
    return Iterables.any(attributes, predicate(namespace, name));
  }

  public static boolean containsAttribute(List<Attribute> attributes, String namespace, String name, Locale locale) {
    return Iterables.any(attributes, predicate(namespace, name, locale));
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
  public static void removeAttributes(List<Attribute> attributes, String namespace, String name) {
    Iterables.removeIf(attributes, predicate(namespace, name));
  }

  /**
   * @param attributes
   * @param namespace
   * @param name
   */
  public static void removeAttributes(List<Attribute> attributes, String namespace, final String name,
      Locale locale) {
    Iterables.removeIf(attributes, predicate(namespace, name, locale));
  }

  /**
   * @param namespace
   * @param name
   * @return
   */
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
        return input.getName().equals(name)
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
   * @param locales
   * @return
   */
  public static List<FactorizedAttribute> factorize(List<Attribute> attributes, List<Locale> locales) {
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

  /**
   * Merge given attributes. The last argument is the more specific (then will be in result list)
   *
   * @param listAttributes
   * @return
   */
  public static List<Attribute> overrideAttributes(List<Attribute>... listAttributes) {
    List<Attribute> result = new ArrayList<Attribute>();
    for(List<Attribute> attributes : listAttributes) {
      if(attributes != null) {
        for(Attribute attribute : attributes) {
          String ns = attribute.getNamespace();
          String name = attribute.getName();
          Locale locale = attribute.getLocale();
          if(Attributes.containsAttribute(result, ns, name, locale)) {
            Attributes.removeAttributes(result, ns, name, locale);
          }
          Attributes.addAttribute(result, ns, name, (String) attribute.getValue().getValue(), locale);
        }
      }
    }
    return result;
  }

  public static List<Attribute> copy(List<Attribute> from) {
    List<Attribute> copy = new ArrayList<Attribute>();
    for(Attribute attribute : from) {
      Attribute.Builder builder = Attribute.Builder.newAttribute();
      builder.withNamespace(attribute.getNamespace());
      builder.withName(attribute.getName());
      builder.withLocale(attribute.getLocale() == null ? null : new Locale(attribute.getLocale().getLanguage()));
      builder.withValue(attribute.getValue().copy());
      copy.add(builder.build());
    }
    return copy;
  }

  /**
   * @param attribute
   * @return
   */
  public static String formatAttribute(Attribute attribute) {
    String formatted = "";
    if(Strings.isNullOrEmpty(attribute.getNamespace()) == false) {
      formatted = attribute.getNamespace() + "::";
    }
    formatted += attribute.getName();

    if(attribute.getLocale() != null) {
      formatted += (":" + attribute.getLocale().getLanguage());
    }
    formatted += ("=" + attribute.getValue());
    return formatted;
  }

  /**
   * @param factorizedAttribute
   * @return
   */
  public static String formatName(FactorizedAttribute factorizedAttribute) {
    String formattedNS = "";
    if(Strings.isNullOrEmpty(factorizedAttribute.getNamespace()) == false) {
      formattedNS = factorizedAttribute.getNamespace() + "::";
    }
    return formattedNS + factorizedAttribute.getName();
  }

  /**
   * @param factorizedAttribute
   * @return
   */
  public static String formatValue(FactorizedAttribute factorizedAttribute) {
    String formattedValue = "";
    for(Map.Entry<Locale, IModel<String>> entry : factorizedAttribute.getValues().entrySet()) {
      String value = entry.getValue().getObject();
      if(Strings.isNullOrEmpty(value) == false) {
        if(Strings.isNullOrEmpty(formattedValue) == false) {
          formattedValue += ", ";
        }
        String formattedLocale = entry.getKey() == null ? "" : " [" + entry.getKey() + "] ";
        formattedValue += (formattedLocale + value);
      }
    }
    return formattedValue;
  }

}
