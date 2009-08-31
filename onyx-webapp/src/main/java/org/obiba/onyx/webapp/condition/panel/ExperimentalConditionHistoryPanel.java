/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.condition.panel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.condition.ExperimentalCondition;
import org.obiba.onyx.core.domain.condition.ExperimentalConditionValue;
import org.obiba.onyx.core.service.ExperimentalConditionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;

public class ExperimentalConditionHistoryPanel extends Panel {

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  @SpringBean
  private EntityQueryService queryService;

  public ExperimentalConditionHistoryPanel(String id, ExperimentalCondition template, String title, int pageSize) {
    super(id);
    add(new AttributeModifier("class", true, new Model("experimental-condition-history-panel")));

    List<ExperimentalCondition> conditions = experimentalConditionService.getExperimentalConditions(template, null);
    OnyxEntityList<ExperimentalCondition> list = new OnyxEntityList<ExperimentalCondition>("experimentalConditionHistoryList", new ExperimentalConditionProvider(template), new ExperimentalConditionColumnProvider(conditions.size() > 0 ? conditions.get(0) : null), new Model(title));
    list.setPageSize(pageSize);
    add(list);
  }

  private static final long serialVersionUID = 1L;

  private class ExperimentalConditionProvider extends SortableDataProviderEntityServiceImpl<ExperimentalCondition> {

    private static final long serialVersionUID = 1L;

    private ExperimentalCondition template;

    public ExperimentalConditionProvider(ExperimentalCondition template) {
      super(queryService, ExperimentalCondition.class);
      this.template = template;
      setSort(new SortParam("time", true));
    }

    @Override
    protected List<ExperimentalCondition> getList(PagingClause paging, SortingClause... clauses) {
      return experimentalConditionService.getExperimentalConditions(template, paging, clauses);
    }

    @Override
    public int size() {
      return experimentalConditionService.getExperimentalConditions(null, null).size();
    }

  }

  private class ExperimentalConditionColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = 1L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    public ExperimentalConditionColumnProvider(ExperimentalCondition experimentalCondition) {
      InjectorHolder.getInjector().inject(this);

      columns.add(new PropertyColumn(new ResourceModel("DateTime", "DateTime"), "time", "time"));
      columns.add(new PropertyColumn(new SpringStringResourceModel("User"), "user", "user"));
      columns.add(new PropertyColumn(new SpringStringResourceModel("Workstation"), "workstation", "workstation"));

      if(experimentalCondition != null) {
        for(final ExperimentalConditionValue value : experimentalCondition.getExperimentalConditionValues()) {
          columns.add(new AbstractColumn(new ResourceModel(value.getAttributeName(), value.getAttributeName())) {
            private static final long serialVersionUID = 1L;

            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
              DetachableEntityModel ec = (DetachableEntityModel) rowModel;
              Data data = null;
              for(ExperimentalConditionValue ecv : ((ExperimentalCondition) ec.getObject()).getExperimentalConditionValues()) {
                if(ecv.getAttributeName().equals(value.getAttributeName())) {
                  data = ecv.getData();
                }
              }
              cellItem.add(new Label(componentId, new Model(data.getValueAsString())));
            }

          });
        }
      }

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