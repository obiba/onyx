package org.obiba.onyx.quartz.editor.input;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 * @author cedric.thiebault
 */
public class LocaleListModel<T> extends LoadableDetachableModel<List<T>> {

  private static final long serialVersionUID = 1L;

  // @SpringBean
  // private GenericDao genericDao;
  //
  // private final Class<TIdentifiable> clazz;
  //
  // private final String[] orderBy;

  public LocaleListModel() {
    // this.clazz = clazz;
    // this.orderBy = orderBy;
    InjectorHolder.getInjector().inject(this);
  }

  @Override
  protected List<T> load() {
    return (List<T>) Arrays.asList(Locale.getAvailableLocales());
  }

}