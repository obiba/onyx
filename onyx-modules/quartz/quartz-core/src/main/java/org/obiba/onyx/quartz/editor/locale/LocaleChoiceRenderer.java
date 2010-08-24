package org.obiba.onyx.quartz.editor.locale;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

public class LocaleChoiceRenderer implements IChoiceRenderer<Locale> {

  private static final long serialVersionUID = -6827298756927987879L;

  @Override
  public String getIdValue(Locale locale, int index) {
    return locale.toString();
  }

  @Override
  public Object getDisplayValue(Locale locale) {
    return locale.getDisplayLanguage(Session.get().getLocale());
  }
}
