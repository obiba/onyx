package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.util.List;
import java.util.Locale;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import org.obiba.magma.Attribute;

public class Attributes {

  public static void addAttribute(List<Attribute> attributes, String namespace, String name, String value,
      Locale locale) {
    Attribute.Builder attributeBuilder = Attribute.Builder.newAttribute();
    attributeBuilder.withNamespace(Strings.emptyToNull(namespace));
    attributeBuilder.withName(name);
    attributeBuilder.withValue(value);
    attributeBuilder.withLocale(locale);
    Attribute attribute = attributeBuilder.build();
    attributes.add(attribute);
  }

  public static boolean containsAttribute(List<Attribute> attributes, Attribute attribute) {
    return attributes.contains(attribute);
  }

  public static Attribute getAttribute(List<Attribute> attributes, String namespace, String name, Locale locale) {
    return Iterables.find(attributes, predicate(namespace, name, locale));
  }

  public static void updateAttribute(List<Attribute> attributes, Attribute attribute, String namespace, String name,
      String value, Locale locale) {
    attributes.remove(attribute);
    addAttribute(attributes, namespace, name, value, locale);
  }

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
