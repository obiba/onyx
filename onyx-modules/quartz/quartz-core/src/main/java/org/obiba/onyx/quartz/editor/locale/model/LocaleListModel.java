package org.obiba.onyx.quartz.editor.locale.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

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
    SortedSet<String> localesStr = new TreeSet<String>();
    for(String language : Locale.getISOLanguages()) {
      localesStr.add(language);
    }
    List<Locale> locales = new ArrayList<Locale>();
    for(String localeStr : localesStr) {
      locales.add(new Locale(localeStr));
    }
    return locales;
  }

  public static LocaleListModel getInstance() {
    if(INSTANCE == null) INSTANCE = new LocaleListModel();
    return INSTANCE;
  }
}