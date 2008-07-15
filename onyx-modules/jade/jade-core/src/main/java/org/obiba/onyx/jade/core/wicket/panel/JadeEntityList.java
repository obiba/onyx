package org.obiba.onyx.jade.core.wicket.panel;

import org.apache.wicket.model.IModel;
import org.obiba.wicket.markup.html.table.EntityListTablePanel;
import org.obiba.wicket.markup.html.table.IColumnProvider;

public class JadeEntityList<T> extends EntityListTablePanel<T> {

  private static final long serialVersionUID = 7891474467194294293L;

  public JadeEntityList(String id, Class<T> type, IColumnProvider columns, IModel title) {
    super(id, type, columns, title);
    setAllowColumnSelection(false);
  }

}
