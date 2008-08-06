package org.obiba.onyx.webapp.participant.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.EntityQueryService;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.panel.OnyxEntityList;
import org.obiba.onyx.webapp.util.DateUtils;
import org.obiba.wicket.markup.html.table.FilteredSortableDataProviderEntityServiceImpl;
import org.obiba.wicket.markup.html.table.IColumnProvider;

public class ParticipantSearchPage extends BasePage {

  @SpringBean
  private EntityQueryService queryService;

  private OnyxEntityList<Participant> participantList;

  private Participant template = new Participant();

  @SuppressWarnings("serial")
  public ParticipantSearchPage() {
    super();

    Form form = new Form("searchForm");
    add(form);

    form.add(new TextField("barcode", new PropertyModel(template, "barcode")));
    form.add(new TextField("name", new PropertyModel(template, "LastName")));
    form.add(new AjaxButton("submit", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("Participants", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
      }

    });

    form.add(new AjaxButton("appointments", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("AppointmentsOfTheDay", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
      }

    });

    form.add(new AjaxButton("interviews", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("CurrentInterviews", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
      }

    });

    add(new AjaxLink("volunteer") {

      @Override
      public void onClick(AjaxRequestTarget target) {

      }

    });

    participantList = new OnyxEntityList<Participant>("participant-list", new ParticipantProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("Participants", ParticipantSearchPage.this, null));
    add(participantList);
  }

  private void replaceParticipantList(AjaxRequestTarget target, OnyxEntityList<Participant> replacement) {
    participantList.replaceWith(replacement);
    participantList = replacement;

    target.addComponent(participantList);
  }

  private class ParticipantProvider extends FilteredSortableDataProviderEntityServiceImpl<Participant> {

    private static final long serialVersionUID = 6022606267778869L;

    public ParticipantProvider(Participant template) {
      super(queryService, template);
      setSort(new SortParam("lastName", true));
    }

    public ParticipantProvider(Participant template, String sortParam) {
      super(queryService, template);
      setSort(new SortParam(sortParam, true));
    }

  }

  private class ParticipantListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = -9121583835357007L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public ParticipantListColumnProvider() {
      columns.add(new PropertyColumn(new StringResourceModel("Code", ParticipantSearchPage.this, null), "barcode", "barcode"));
      columns.add(new PropertyColumn(new StringResourceModel("FirstName", ParticipantSearchPage.this, null), "firstName", "firstName"));
      columns.add(new PropertyColumn(new StringResourceModel("LastName", ParticipantSearchPage.this, null), "lastName", "lastName"));
      columns.add(new AbstractColumn(new StringResourceModel("Gender", ParticipantSearchPage.this, null), "gender") {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Participant p = (Participant)rowModel.getObject();
          cellItem.add(new Label(componentId, new StringResourceModel("Gender." + p.getGender(), ParticipantSearchPage.this, null)));
        }
        
      });
      columns.add(new AbstractColumn(new StringResourceModel("BirthDate", ParticipantSearchPage.this, null), "birthDate") {
        
        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Participant p = (Participant)rowModel.getObject();
          cellItem.add(new Label(componentId, DateUtils.getDateModel(new Model(p.getBirthDate()))));
        }
        
      });
      columns.add(new AbstractColumn(new StringResourceModel("Appointment", ParticipantSearchPage.this, null)) {
        
        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Participant p = (Participant)rowModel.getObject();
          Appointment appointment = null;
          for (Appointment app : p.getAppointments()) {
            if (appointment == null)
              appointment = app;
          }
          cellItem.add(new Label(componentId, DateUtils.getFullDateModel(new Model(appointment.getDate()))));
        }
        
      });
      columns.add(new AbstractColumn(new StringResourceModel("Actions", ParticipantSearchPage.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new Label(componentId));
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
