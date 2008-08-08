package org.obiba.onyx.webapp.panel;

import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.obiba.wicket.markup.html.table.EntityListTablePanel;
import org.obiba.wicket.markup.html.table.IColumnProvider;

public class OnyxEntityList<T> extends EntityListTablePanel<T> {

  private static final long serialVersionUID = 7891474467194294293L;

  public OnyxEntityList(String id, Class<T> type, IColumnProvider columns, IModel title) {
    super(id, type, columns, title);
    setAllowColumnSelection(false);
  }
  
  public OnyxEntityList(String id, T template, IColumnProvider columns, IModel title) {
    super(id, template, columns, title);
    setAllowColumnSelection(false);
  }

  public OnyxEntityList(String id, SortableDataProvider dataProvider, IColumnProvider columns, IModel title) {
    super(id, dataProvider, columns, title, 100);
    setAllowColumnSelection(false);
  }
  
}
