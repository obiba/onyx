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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.jade.core.domain.instrument.InstrumentMeasurementType;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;
import org.obiba.onyx.jade.core.service.ExperimentalConditionService;
import org.obiba.onyx.jade.core.service.InstrumentService;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.reusable.Dialog;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;

/**
 * 
 */
public class WorkstationPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private InstrumentService instrumentService;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean(name = "userSessionService")
  private UserSessionService userSessionService;

  @SpringBean
  private ExperimentalConditionService experimentalConditionService;

  private Dialog addInstrumentWindow;

  // private InstrumentEntityList instrumentList;

  private InstrumentMeasurementTypeEntityList instrumentMTypeList;

  /**
   * @param id
   */
  public WorkstationPanel(String id) {
    super(id);

    addInstrumentContent();

    final WorkstationLogPanel workstationLogPanel = new WorkstationLogPanel("workstationLogPanel");
    WebMarkupContainer workstationLogBorder = new WebMarkupContainer("workstationLogBorder") {
      private static final long serialVersionUID = 1L;

      @Override
      public boolean isVisible() {
        return workstationLogPanel.isVisible();
      }

    };
    add(workstationLogBorder);
    workstationLogBorder.add(workstationLogPanel);

  }

  @SuppressWarnings("unchecked")
  private void addInstrumentContent() {

    add(addInstrumentWindow = createAddInstrumentWindow("addInstrumentWindow"));

    AjaxLink addInstrumentLink = new AjaxLink("addInstrument") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        InstrumentPanel component = new InstrumentPanel("content", new Model<Instrument>(new Instrument()), addInstrumentWindow, false);
        component.add(new AttributeModifier("class", true, new Model("obiba-content instrument-panel-content")));
        addInstrumentWindow.setContent(component);
        addInstrumentWindow.show(target);
      }
    };

    add(addInstrumentLink);

    // instrumentList = new InstrumentEntityList("instrument-list", new InstrumentProvider(), new
    // InstrumentListColumnProvider(), new StringResourceModel("WorkstationInstruments", WorkstationPanel.this, null));
    // add(instrumentList);

    instrumentMTypeList = new InstrumentMeasurementTypeEntityList("instrument-list", new InstrumentMeasurementTypeProvider(), new InstrumentMeasurementTypeListColumnProvider(), new StringResourceModel("WorkstationInstruments", WorkstationPanel.this, null));
    instrumentMTypeList.setPageSize(5);
    add(instrumentMTypeList);
  }

  private Dialog createAddInstrumentWindow(String id) {
    Dialog addInstrumentDialog = new Dialog(id);
    addInstrumentDialog.setTitle(new ResourceModel("RegisterInstrument"));
    addInstrumentDialog.setHeightUnit("em");
    addInstrumentDialog.setWidthUnit("em");
    addInstrumentDialog.setInitialHeight(20);
    addInstrumentDialog.setInitialWidth(34);
    addInstrumentDialog.setType(Dialog.Type.PLAIN);
    addInstrumentDialog.setOptions(Dialog.Option.OK_CANCEL_OPTION, "Register");
    return addInstrumentDialog;
  }

  //
  // Internal class
  //
  @SuppressWarnings("unchecked")
  private class InstrumentMeasurementTypeEntityList extends OnyxEntityList<InstrumentMeasurementType> {

    private static final long serialVersionUID = 1L;

    public InstrumentMeasurementTypeEntityList(String id, SortableDataProvider dataProvider, IColumnProvider columns, IModel title) {
      super(id, dataProvider, columns, title);
    }

    @Override
    protected void onPageChanged() {
      IRequestTarget target = getRequestCycle().getRequestTarget();
      if(getRequestCycle().getRequestTarget() instanceof AjaxRequestTarget) {
        ((AjaxRequestTarget) target).appendJavascript("styleWorkstationNavigationBar();");
      }
      super.onPageChanged();
    }
  }

  @SuppressWarnings("serial")
  private class InstrumentMeasurementTypeProvider extends SortableDataProviderEntityServiceImpl<InstrumentMeasurementType> {

    public InstrumentMeasurementTypeProvider() {
      super(queryService, InstrumentMeasurementType.class);
      setSort(new SortParam("type", true));
    }

    @Override
    protected List<InstrumentMeasurementType> getList(PagingClause paging, SortingClause... clauses) {
      return instrumentService.getWorkstationInstrumentMeasurementTypes(userSessionService.getWorkstation(), paging, clauses);
    }

    @Override
    public int size() {
      return instrumentService.countWorkstationInstrumentMeasurementTypes(userSessionService.getWorkstation());
    }

  }

  @SuppressWarnings("unchecked")
  private class InstrumentMeasurementTypeListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = -9121583835357007L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public InstrumentMeasurementTypeListColumnProvider() {

      columns.add(new PropertyColumn(new StringResourceModel("Measurement", WorkstationPanel.this, null), "type", "type"));
      columns.add(new PropertyColumn(new StringResourceModel("Name", WorkstationPanel.this, null), "instrument.name", "instrument.name"));
      columns.add(new PropertyColumn(new StringResourceModel("Barcode", WorkstationPanel.this, null), "instrument.barcode", "instrument.barcode"));

      columns.add(new AbstractColumn(new ResourceModel("Status")) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          InstrumentMeasurementType instrumentMType = (InstrumentMeasurementType) rowModel.getObject();
          cellItem.add(new Label(componentId, new ResourceModel("InstrumentUsage." + instrumentMType.getInstrument().getUsage())));
        }

      });

      columns.add(new AbstractColumn(new ResourceModel("Actions")) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new ActionsPanel(componentId, rowModel));
        }

      });

      columns.add(new AbstractColumn(new ResourceModel("LastCalibration", "LastCalibration")) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          InstrumentMeasurementType instrumentMType = (InstrumentMeasurementType) rowModel.getObject();
          if(experimentalConditionService.instrumentCalibrationExists(instrumentMType.getType())) {
            ExperimentalCondition calibration = getLatestInstrumentMeasurementTypeCalibration(instrumentMType);
            if(calibration != null) {
              cellItem.add(new Label(componentId, DateModelUtils.getDateTimeModel(new PropertyModel<InstrumentMeasurementTypeListColumnProvider>(InstrumentMeasurementTypeListColumnProvider.this, "dateTimeFormat"), new Model(calibration.getTime()))));
              return;
            }
          }
          cellItem.add(new Label(componentId, ""));
        }

      });

      columns.add(new AbstractColumn(new ResourceModel("Log", "Log")) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          ViewCalibrationLogPanel viewCalibrationLogLink = new ViewCalibrationLogPanel(componentId, rowModel);

          cellItem.add(viewCalibrationLogLink);

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

    public DateFormat getDateTimeFormat() {
      return userSessionService.getDateTimeFormat();
    }
  }

  public InstrumentMeasurementTypeEntityList getInstrumentMeasurementTypeList() {
    return instrumentMTypeList;
  }

  private ExperimentalCondition getLatestInstrumentMeasurementTypeCalibration(InstrumentMeasurementType instrumentMType) {
    List<InstrumentCalibration> instrumentCalibrations = experimentalConditionService.getInstrumentCalibrationsByType(instrumentMType.getType());
    List<ExperimentalCondition> allCalibrations = new ArrayList<ExperimentalCondition>();
    for(InstrumentCalibration instrumentCalibration : instrumentCalibrations) {
      allCalibrations.addAll(getExperimentalConditions(instrumentCalibration.getName(), instrumentMType.getInstrument().getBarcode()));
    }
    Collections.sort(allCalibrations, new Comparator<ExperimentalCondition>() {

      public int compare(ExperimentalCondition arg0, ExperimentalCondition arg1) {
        return arg1.getTime().compareTo(arg0.getTime());
      }

    });
    if(allCalibrations.isEmpty()) return null;
    return allCalibrations.get(0);
  }

  private List<ExperimentalCondition> getExperimentalConditions(String instrumentCalibrationName, String barcode) {
    ExperimentalCondition template = new ExperimentalCondition();
    template.setName(instrumentCalibrationName);

    ExperimentalConditionValue ecv = new ExperimentalConditionValue();
    ecv.setAttributeType(DataType.TEXT);
    ecv.setAttributeName(ExperimentalConditionService.INSTRUMENT_BARCODE);
    ecv.setData(new Data(DataType.TEXT, barcode));
    ecv.setExperimentalCondition(template);
    template.addExperimentalConditionValue(ecv);

    return experimentalConditionService.getExperimentalConditions(template);
  }

}
