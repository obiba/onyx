package org.obiba.onyx.quartz.editor.input;

import java.util.Locale;

import org.apache.wicket.markup.html.form.IChoiceRenderer;

/**
 * @author cedric.thiebault
 */
public class LocaleChoiceRenderer implements IChoiceRenderer<Locale> {

  private static final long serialVersionUID = -6827298756927987879L;

  @Override
  public String getIdValue(Locale locale, int index) {
    return locale.getDisplayName();
  }

  @Override
  public Object getDisplayValue(Locale locale) {
    return locale.getDisplayName();
  }
}
