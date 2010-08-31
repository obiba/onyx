package org.obiba.onyx.quartz.editor.locale.model;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

/**
 * IChoiceRenderer for Locale
 */
public class LocaleChoiceRenderer implements IChoiceRenderer<Locale> {

  private static final long serialVersionUID = -6827298756927987879L;

  private static LocaleChoiceRenderer INSTANCE;

  private LocaleChoiceRenderer() {

  }

  @Override
  public String getIdValue(Locale locale, int index) {
    return locale.toString();
  }

  @Override
  public Object getDisplayValue(Locale locale) {
    return locale.getDisplayLanguage(Session.get().getLocale());
  }

  public static LocaleChoiceRenderer getInstance() {
    if(INSTANCE == null) INSTANCE = new LocaleChoiceRenderer();
    return INSTANCE;
  }
}
