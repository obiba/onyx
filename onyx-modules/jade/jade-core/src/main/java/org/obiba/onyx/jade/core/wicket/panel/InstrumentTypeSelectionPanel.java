package org.obiba.onyx.jade.core.wicket.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentType;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstrumentTypeSelectionPanel extends Panel {

  private static final long serialVersionUID = 6282742572162384139L;
  
  private static final Logger log = LoggerFactory.getLogger(InstrumentTypeSelectionPanel.class);
  
  @SpringBean
  private EntityQueryService queryService;
  
  public InstrumentTypeSelectionPanel(String id) {
    super(id);
    add(new JadeEntityList<InstrumentType>("list", InstrumentType.class, new InstrumentTypeListColumnProvider(), new Model("Instrument type list")));
  }

  public class InstrumentTypeListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = -9121583835345457007L;

    private List<IColumn> columns = new ArrayList<IColumn>();
    private List<IColumn> additional = new ArrayList<IColumn>();

    public InstrumentTypeListColumnProvider() {
      columns.add(new PropertyColumn(new Model("Name"), "name", "name"));
      columns.add(new PropertyColumn(new Model("Description"), "description", "description"));
      columns.add(new AbstractColumn(new Model("")) {

        public void populateItem(Item item, String componentId, IModel model) {
          item.add(new InstrumentLauncherPanel(componentId, new DetachableEntityModel(queryService, model.getObject())));
        }
        
      });
    }

    public List<IColumn> getAdditionalColumns() {
      return additional;
    }

    public List<String> getColumnHeaderNames() {
      return null;
    }

    public List<IColumn> getDefaultColumns() {
      return columns;
    }

    public List<IColumn> getRequiredColumns() {
      return columns;
    }

  }
}
