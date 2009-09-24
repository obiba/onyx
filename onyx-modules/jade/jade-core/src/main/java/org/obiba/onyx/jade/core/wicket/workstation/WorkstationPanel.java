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
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.jade.core.domain.instrument.Instrument;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;

/**
 * 
 */
public class WorkstationPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private UserSessionService userSessionService;

  /**
   * @param id
   */
  public WorkstationPanel(String id) {
    super(id);

    addInstrumentContent();
    add(new WorkstationLogPanel("workstationLogPanel"));

  }

  private void addInstrumentContent() {
    AjaxLink addInstrumentLink = new AjaxLink("addInstrument") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {

      }

    };
    add(addInstrumentLink);

    InstrumentEntityList instrumentList = new InstrumentEntityList("instrument-list", new InstrumentProvider(), new InstrumentListColumnProvider(), new StringResourceModel("WorkstationInstruments", WorkstationPanel.this, null));
    instrumentList.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
    add(instrumentList);
  }

  //
  // Internal class
  //
  @SuppressWarnings("unchecked")
  private class InstrumentEntityList extends OnyxEntityList<Instrument> {

    private static final long serialVersionUID = 1L;

    // public InstrumentEntityList(String id, Class<Instrument> type, IColumnProvider columns, IModel title) {
    // super(id, type, columns, title);
    // }
    //
    // public InstrumentEntityList(String id, Instrument template, IColumnProvider columns, IModel title) {
    // super(id, template, columns, title);
    // }

    public InstrumentEntityList(String id, SortableDataProvider dataProvider, IColumnProvider columns, IModel title) {
      super(id, dataProvider, columns, title);
    }

    // @Override
    // protected void onPageChanged() {
    // IRequestTarget target = getRequestCycle().getRequestTarget();
    // if(getRequestCycle().getRequestTarget() instanceof AjaxRequestTarget) {
    // ((AjaxRequestTarget) target).appendJavascript("styleParticipantSearchNavigationBar();");
    // }
    // super.onPageChanged();
    // }
  }

  @SuppressWarnings("serial")
  private class InstrumentProvider extends SortableDataProviderEntityServiceImpl<Instrument> {

    public InstrumentProvider() {
      super(queryService, Instrument.class);
      setSort(new SortParam("type", true));
    }

    @Override
    protected List<Instrument> getList(PagingClause paging, SortingClause... clauses) {
      return queryService.list(Instrument.class, paging, clauses);
    }

    @Override
    public int size() {
      return queryService.count(Instrument.class);
    }

  }

  @SuppressWarnings("unchecked")
  private class InstrumentListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = -9121583835357007L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public InstrumentListColumnProvider() {
      columns.add(new PropertyColumn(new StringResourceModel("Measurement", WorkstationPanel.this, null), "type", "type"));
      columns.add(new PropertyColumn(new StringResourceModel("Name", WorkstationPanel.this, null), "name", "name"));
      columns.add(new PropertyColumn(new StringResourceModel("Barcode", WorkstationPanel.this, null), "barcode", "barcode"));
      columns.add(new PropertyColumn(new StringResourceModel("Status", WorkstationPanel.this, null), "status", "status"));

      columns.add(new AbstractColumn(new StringResourceModel("Actions", WorkstationPanel.this, null)) {

        public void populateItem(final Item cellItem, String componentId, final IModel rowModel) {
          // cellItem.add(new ActionListFragment(componentId, rowModel));
          cellItem.add(new Label("test action"));
        }

      });

      // columns.add(new PropertyColumn(new StringResourceModel("LastCalibration", WorkstationPage.this, null),
      // "lastCalibration", "lastCalibration"));
      // columns.add(new PropertyColumn(new StringResourceModel("Log", WorkstationPage.this, null), "log", "log"));

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
}
