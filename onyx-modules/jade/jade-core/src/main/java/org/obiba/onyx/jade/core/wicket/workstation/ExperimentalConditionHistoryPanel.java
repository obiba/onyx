/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.wicket.workstation;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.model.SpringStringResourceModel;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;

public class ExperimentalConditionHistoryPanel extends Panel {

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  private InstrumentCalibration selectedInstrumentCalibration;

  private OnyxEntityList<ExperimentalCondition> list;

  private Instrument instrument;

  public ExperimentalConditionHistoryPanel(String id, List<InstrumentCalibration> instrumentCalibrations, final int pageSize, Instrument instrument) {
    super(id);
    setOutputMarkupId(true);
    add(new AttributeModifier("class", true, new Model<String>("experimental-condition-history-panel")));

    this.instrument = instrument;

    WebMarkupContainer selectCalibrationId = new WebMarkupContainer("selectCalibrationId");

    if(instrumentCalibrations.size() >= 1) selectedInstrumentCalibration = instrumentCalibrations.get(0);

    final DropDownChoice<InstrumentCalibration> instrumentCalibrationChoice = new DropDownChoice<InstrumentCalibration>("instrumentCalibrationChoice", new PropertyModel<InstrumentCalibration>(this, "selectedInstrumentCalibration"), instrumentCalibrations, new ChoiceRenderer<InstrumentCalibration>() {
      private static final long serialVersionUID = 1L;

      @Override
      public Object getDisplayValue(InstrumentCalibration object) {
        return new SpringStringResourceModel(object.getName(), object.getName()).getString();
      }

      @Override
      public String getIdValue(InstrumentCalibration object, int index) {
        return object.getName();
      }
    });
    instrumentCalibrationChoice.add(new OnChangeAjaxBehavior() {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        OnyxEntityList<ExperimentalCondition> newTable = getTable(pageSize);
        list.replaceWith(newTable);
        list = newTable;
        target.appendJavascript("styleOnyxEntityListNavigationBar('" + getMarkupId() + "');");
        target.addComponent(list);
      }

    });
    selectCalibrationId.add(instrumentCalibrationChoice);
    add(selectCalibrationId);
    if(instrumentCalibrations.size() <= 1) {
      selectCalibrationId.setVisible(false);
      list = getTable(pageSize + 2);
    } else {
      list = getTable(pageSize);
    }

    add(list);
  }

  private OnyxEntityList<ExperimentalCondition> getTable(int pageSize) {

    ExperimentalCondition ec = new ExperimentalCondition();
    ec.setName(selectedInstrumentCalibration.getName());

    ExperimentalConditionValue ecv = new ExperimentalConditionValue();
    ecv.setAttributeType(DataType.TEXT);
    ecv.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
    ecv.setData(new Data(DataType.TEXT, instrument.getBarcode()));
    ecv.setExperimentalCondition(ec);
    ec.addExperimentalConditionValue(ecv);

    SpringStringResourceModel titleModel = new SpringStringResourceModel(selectedInstrumentCalibration.getName(), selectedInstrumentCalibration.getName());
    List<ExperimentalCondition> conditions = experimentalConditionService.getExperimentalConditions(ec);
    OnyxEntityList<ExperimentalCondition> list = new OnyxEntityList<ExperimentalCondition>("experimentalConditionHistoryList", new ExperimentalConditionProvider(ec), new ExperimentalConditionColumnProvider(conditions.size() > 0 ? conditions.get(0) : null), titleModel) {
      @Override
      protected void onPageChanged() {
        IRequestTarget target = getRequestCycle().getRequestTarget();
        if(getRequestCycle().getRequestTarget() instanceof AjaxRequestTarget) {
          ((AjaxRequestTarget) target).appendJavascript("styleOnyxEntityListNavigationBar('" + ExperimentalConditionHistoryPanel.this.getMarkupId() + "');");
        }
        super.onPageChanged();
      }
    };
    list.setPageSize(pageSize);
    return list;

  }

  public ExperimentalConditionHistoryPanel(String id, ExperimentalCondition template, IModel<String> title, int pageSize) {
    super(id);
    setOutputMarkupId(true);
    add(new AttributeModifier("class", true, new Model<String>("experimental-condition-history-panel")));

    WebMarkupContainer selectCalibrationId = new WebMarkupContainer("selectCalibrationId");
    Label instrumentCalibrationChoice = new Label("instrumentCalibrationChoice", "instrumentCalibrationChoice");
    selectCalibrationId.add(instrumentCalibrationChoice);
    add(selectCalibrationId);
    selectCalibrationId.setVisible(false);

    List<ExperimentalCondition> conditions = experimentalConditionService.getExperimentalConditions(template);
    OnyxEntityList<ExperimentalCondition> list = new OnyxEntityList<ExperimentalCondition>("experimentalConditionHistoryList", new ExperimentalConditionProvider(template), new ExperimentalConditionColumnProvider(conditions.size() > 0 ? conditions.get(0) : null), title) {
      @Override
      protected void onPageChanged() {
        IRequestTarget target = getRequestCycle().getRequestTarget();
        if(getRequestCycle().getRequestTarget() instanceof AjaxRequestTarget) {
          ((AjaxRequestTarget) target).appendJavascript("styleOnyxEntityListNavigationBar('" + ExperimentalConditionHistoryPanel.this.getMarkupId() + "');");
        }
        super.onPageChanged();
      }
    };
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
      setSort(new SortParam("time", false));
    }

    @Override
    protected List<ExperimentalCondition> getList(PagingClause paging, SortingClause... clauses) {
      List<ExperimentalCondition> result = experimentalConditionService.getExperimentalConditions(template);
      ExperimentalConditionSorter ecs = new ExperimentalConditionSorter(result);
      result = ecs.getSortedList(paging, clauses);
      return result;
    }

    @Override
    public int size() {
      return experimentalConditionService.getExperimentalConditions(template).size();
    }

  }

  private class ExperimentalConditionColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = 1L;

    private List<IColumn<ExperimentalCondition>> columns = new ArrayList<IColumn<ExperimentalCondition>>();

    private List<IColumn<ExperimentalCondition>> additional = new ArrayList<IColumn<ExperimentalCondition>>();

    public ExperimentalConditionColumnProvider(ExperimentalCondition experimentalCondition) {
      InjectorHolder.getInjector().inject(this);

      columns.add(new AbstractColumn<ExperimentalCondition>(new ResourceModel("DateTime", "DateTime"), "time") {
        private static final long serialVersionUID = 1L;

        public void populateItem(Item<ICellPopulator<ExperimentalCondition>> cellItem, String componentId, IModel<ExperimentalCondition> rowModel) {
          cellItem.add(new Label(componentId, DateModelUtils.getDateTimeModel(new PropertyModel<ExperimentalConditionColumnProvider>(ExperimentalConditionColumnProvider.this, "dateTimeFormat"), new PropertyModel<ExperimentalCondition>(rowModel, "time"))));
        }

      });
      columns.add(new PropertyColumn<ExperimentalCondition>(new SpringStringResourceModel("User"), "user", "user.fullName"));

      if(experimentalCondition != null) {
        for(final ExperimentalConditionValue value : getExperimentalConditionValuesSortedInXmlConfigurationFileOrder(experimentalCondition.getExperimentalConditionValues())) {

          String unit = experimentalConditionService.getAttribute(value).getUnit();
          unit = surroundStringIfNotNull(unit, " (", ")");
          columns.add(new AbstractColumn<ExperimentalCondition>(new Model(new SpringStringResourceModel(value.getAttributeName(), value.getAttributeName()).getObject() + unit), value.getAttributeName()) {
            private static final long serialVersionUID = 1L;

            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
              DetachableEntityModel<ExperimentalCondition> ec = (DetachableEntityModel<ExperimentalCondition>) rowModel;
              Data data = new Data(DataType.TEXT, "");
              for(ExperimentalConditionValue ecv : ((ExperimentalCondition) ec.getObject()).getExperimentalConditionValues()) {
                if(ecv.getAttributeName().equals(value.getAttributeName())) {
                  data = ecv.getData();
                }
              }
              cellItem.add(new Label(componentId, new Model<String>(data.getValueAsString())));
            }

          });
        }
      }

    }

    private String surroundStringIfNotNull(String text, String leftText, String rightText) {
      if(text == null || text.equals("")) return "";
      StringBuilder sb = new StringBuilder();
      sb.append(leftText).append(text).append(rightText);
      return sb.toString();
    }

    public List getAdditionalColumns() {
      return additional;
    }

    public List<String> getColumnHeaderNames() {
      return null;
    }

    public List getDefaultColumns() {
      return columns;
    }

    public List getRequiredColumns() {
      return columns;
    }

    public DateFormat getDateTimeFormat() {
      return userSessionService.getDateTimeFormat();
    }

  }

  /**
   * The Calibration form displays the attributes in the order they are found in the xml configuration file. We would
   * like to display the attributes in the same order in the table. Here we explicitly sort them in that order.
   * @param unsorted A list of attributes belonging to one {@link ExperimentalCondition}.
   * @return a List of {@link ExperimentalConditionValue}s sort in the same order as the xml configuration file.
   */
  private List<ExperimentalConditionValue> getExperimentalConditionValuesSortedInXmlConfigurationFileOrder(List<ExperimentalConditionValue> unsorted) {
    List<ExperimentalConditionValue> sorted = new ArrayList<ExperimentalConditionValue>(unsorted.size());
    if(unsorted.isEmpty()) return sorted;

    String experimentalConditionLogName = unsorted.get(0).getExperimentalCondition().getName();
    Map<String, ExperimentalConditionValue> experimentalConditionLogAttributeMap = new LinkedHashMap<String, ExperimentalConditionValue>();
    for(Attribute attribute : experimentalConditionService.getExperimentalConditionLogByName(experimentalConditionLogName).getAttributes()) {
      experimentalConditionLogAttributeMap.put(attribute.getName(), null);
    }

    for(ExperimentalConditionValue ecv : unsorted) {
      if(experimentalConditionLogAttributeMap.containsKey(ecv.getAttributeName())) {
        experimentalConditionLogAttributeMap.put(ecv.getAttributeName(), ecv);
      }
    }

    for(ExperimentalConditionValue ecv : experimentalConditionLogAttributeMap.values()) {
      if(ecv != null) sorted.add(ecv);
    }
    return sorted;
  }

  @Override
  protected void onAfterRender() {
    super.onAfterRender();
    IRequestTarget target = getRequestCycle().getRequestTarget();
    if(getRequestCycle().getRequestTarget() instanceof AjaxRequestTarget) {
      ((AjaxRequestTarget) target).appendJavascript("styleOnyxEntityListNavigationBar('" + getMarkupId() + "');");
    }
  }

}