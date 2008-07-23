package org.obiba.onyx.webapp.panel.stage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.webapp.page.stage.StagePage;
import org.obiba.onyx.webapp.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StageSelectionPanel extends Panel {

  private static final long serialVersionUID = 6282742572162384139L;

  private static final Logger log = LoggerFactory.getLogger(StageSelectionPanel.class);

  @SpringBean
  private EntityQueryService queryService;

  public StageSelectionPanel(String id) {
    super(id);
    add(new OnyxEntityList<Stage>("list", new StageProvider(), new StageListColumnProvider(), new StringResourceModel("StageList", StageSelectionPanel.this, null)));
  }

  private class StageProvider extends SortableDataProviderEntityServiceImpl<Stage> {

    private static final long serialVersionUID = 6022606267778864539L;

    public StageProvider() {
      super(queryService, Stage.class);
      setSort(new SortParam("displayOrder", true));
    }

  }

  private class StageListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = -9121583835345457007L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public StageListColumnProvider() {
      columns.add(new PropertyColumn(new Model("#"), "displayOrder", "displayOrder"));
      columns.add(new PropertyColumn(new StringResourceModel("Name", StageSelectionPanel.this, null), "name", "name"));
      columns.add(new PropertyColumn(new StringResourceModel("Description", StageSelectionPanel.this, null), "description", "description"));
      columns.add(new PropertyColumn(new Model("Module"), "module", "module"));
      columns.add(new AbstractColumn(new Model("")) {

        public void populateItem(Item item, String componentId, IModel model) {
          item.add(new StageStarter(componentId, new DetachableEntityModel(queryService, model.getObject())));
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

  private class StageStarter extends Fragment {

    private static final long serialVersionUID = -4496489530512108516L;

    @SuppressWarnings("serial")
    public StageStarter(String id, final DetachableEntityModel model) {
      super(id, "startFragment", StageSelectionPanel.this, model);

      Form form = new Form("form", model);
      add(form);

      form.add(new Button("start") {

        @Override
        public void onSubmit() {
          log.info("Start " + model.getObject());
          setResponsePage(new StagePage(model));
        }
      });
    }

  }
}
