package org.obiba.onyx.quartz.editor.locale.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.wicket.Session;
import org.obiba.onyx.wicket.model.SpringDetachableModel;

/**
 * Model for List of Locale sorted by locale name (for locale of current user)
 */
public class LocaleListModel extends SpringDetachableModel<List<Locale>> {

  private static final long serialVersionUID = 1L;

  private static LocaleListModel INSTANCE;

  private LocaleListModel() {

  }

  @Override
  protected List<Locale> load() {
    List<String> localesStr = new ArrayList<String>();
    for(String language : Locale.getISOLanguages()) {
      localesStr.add(language);
    }
    SortedSet<Locale> locales = new TreeSet<Locale>(new Comparator<Locale>() {

      @Override
      public int compare(Locale locale1, Locale locale2) {
        return locale1.getDisplayLanguage(Session.get().getLocale()).compareTo(locale2.getDisplayLanguage(Session.get().getLocale()));
      }
    });
    for(String localeStr : localesStr) {
      locales.add(new Locale(localeStr));
    }
    return new ArrayList<Locale>(locales);
  }

  public static LocaleListModel getInstance() {
    if(INSTANCE == null) INSTANCE = new LocaleListModel();
    return INSTANCE;
  }
}