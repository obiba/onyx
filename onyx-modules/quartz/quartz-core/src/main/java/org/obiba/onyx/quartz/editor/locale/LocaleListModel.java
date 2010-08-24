package org.obiba.onyx.quartz.editor.locale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Session;
import org.obiba.onyx.wicket.model.SpringDetachableModel;

public class LocaleListModel extends SpringDetachableModel<List<Locale>> {

  private static final long serialVersionUID = 1L;

  @Override
  protected List<Locale> load() {

    List<Locale> locales = new ArrayList<Locale>();
    for(String language : Locale.getISOLanguages()) {
      locales.add(new Locale(language));
    }
    Collections.sort(locales, new Comparator<Locale>() {

      @Override
      public int compare(Locale locale1, Locale locale2) {
        return locale1.getDisplayLanguage(Session.get().getLocale()).compareTo(locale2.getDisplayLanguage(Session.get().getLocale()));
      }
    });
    return locales;
  }
}