package org.obiba.onyx.webapp.participant.page;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.panel.OnyxEntityList;
import org.obiba.onyx.webapp.participant.panel.ParticipantModalPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.onyx.wicket.util.DateUtils;
import org.obiba.wicket.markup.html.link.AjaxLinkList;
import org.obiba.wicket.markup.html.table.DetachableEntityModel;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class ParticipantSearchPage extends BasePage {

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private ActiveInterviewService activeInterviewService;

  private OnyxEntityList<Participant> participantList;

  private Participant template = new Participant();

  private ModalWindow participantDetailsModalWindow;

  @SuppressWarnings("serial")
  public ParticipantSearchPage() {
    super();

    participantDetailsModalWindow = new ModalWindow("participantDetailsModalWindow");
    participantDetailsModalWindow.setTitle(new StringResourceModel("Participant", this, null));
    add(participantDetailsModalWindow);

    Form form = new Form("searchForm");
    add(form);

    form.add(new TextField("barcode", new PropertyModel(template, "barcode")));

    form.add(new AjaxButton("searchByCode", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantByCodeProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("Participants", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
      }

    });

    form.add(new TextField("lastName", new PropertyModel(template, "lastName")));

    form.add(new AjaxButton("searchByLastName", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantByLastNameProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("Participants", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
      }

    });

    form.add(new AjaxButton("submit", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantProvider(), new ParticipantListColumnProvider(), new StringResourceModel("Participants", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
      }

    });

    form.add(new AjaxButton("appointments", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new AppointedParticipantProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("AppointmentsOfTheDay", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
      }

    });

    form.add(new AjaxButton("interviews", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new InterviewedParticipantProvider(), new ParticipantListColumnProvider(), new StringResourceModel("CurrentInterviews", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
      }

    });

    add(new AjaxLink("advanced") {

      @Override
      public void onClick(AjaxRequestTarget target) {

      }

    });

    add(new AjaxLink("volunteer") {

      @Override
      public void onClick(AjaxRequestTarget target) {

      }

    });

    add(new AjaxLink("print") {

      @Override
      public void onClick(AjaxRequestTarget target) {

      }

    });

    add(new AjaxLink("excel") {

      @Override
      public void onClick(AjaxRequestTarget target) {

      }

    });

    participantList = new OnyxEntityList<Participant>("participant-list", new AppointedParticipantProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("AppointmentsOfTheDay", ParticipantSearchPage.this, null));
    add(participantList);
  }

  private void replaceParticipantList(AjaxRequestTarget target, OnyxEntityList<Participant> replacement) {
    participantList.replaceWith(replacement);
    participantList = replacement;

    target.addComponent(participantList);
  }

  @SuppressWarnings("serial")
  private class ParticipantProvider extends SortableDataProviderEntityServiceImpl<Participant> {

    public ParticipantProvider() {
      super(queryService, Participant.class);
      setSort(new SortParam("lastName", true));
    }

    @Override
    protected List<Participant> getList(PagingClause paging, SortingClause... clauses) {
      return participantService.getParticipants(null, null, paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipants(null, null);
    }

  }

  @SuppressWarnings("serial")
  private class ParticipantByCodeProvider extends SortableDataProviderEntityServiceImpl<Participant> {

    private Participant template;

    public ParticipantByCodeProvider(Participant template) {
      super(queryService, Participant.class);
      this.template = template;
      setSort(new SortParam("barcode", true));
    }

    @Override
    protected List<Participant> getList(PagingClause paging, SortingClause... clauses) {
      return participantService.getParticipants(template.getBarcode(), null, paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipants(template.getBarcode(), null);
    }

  }

  @SuppressWarnings("serial")
  private class ParticipantByLastNameProvider extends SortableDataProviderEntityServiceImpl<Participant> {

    private Participant template;

    public ParticipantByLastNameProvider(Participant template) {
      super(queryService, Participant.class);
      this.template = template;
      setSort(new SortParam("lastName", true));
    }

    @Override
    protected List<Participant> getList(PagingClause paging, SortingClause... clauses) {
      return participantService.getParticipants(null, template.getLastName(), paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipants(null, template.getLastName());
    }

  }

  @SuppressWarnings("serial")
  private class AppointedParticipantProvider extends SortableDataProviderEntityServiceImpl<Participant> {

    private Participant template;

    private Date from;

    private Date to;

    public AppointedParticipantProvider(Participant template) {
      super(queryService, Participant.class);

      this.template = template;
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
      this.from = cal.getTime();
      cal.add(Calendar.DAY_OF_MONTH, 1);
      this.to = cal.getTime();
    }

    @Override
    protected List<Participant> getList(PagingClause paging, SortingClause... clauses) {
      return participantService.getParticipants(template.getBarcode(), template.getLastName(), from, to, paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipants(template.getBarcode(), template.getLastName(), from, to);
    }

  }

  @SuppressWarnings("serial")
  private class InterviewedParticipantProvider extends SortableDataProviderEntityServiceImpl<Participant> {

    public InterviewedParticipantProvider() {
      super(queryService, Participant.class);
      setSort(new SortParam("lastName", true));
    }

    @Override
    protected List<Participant> getList(PagingClause paging, SortingClause... clauses) {
      return participantService.getParticipants(null, null, InterviewStatus.IN_PROGRESS, paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipants(null, null, InterviewStatus.IN_PROGRESS);
    }

  }

  private class ParticipantListColumnProvider implements IColumnProvider, Serializable {

    private static final long serialVersionUID = -9121583835357007L;

    private List<IColumn> columns = new ArrayList<IColumn>();

    private List<IColumn> additional = new ArrayList<IColumn>();

    @SuppressWarnings("serial")
    public ParticipantListColumnProvider() {
      columns.add(new PropertyColumn(new StringResourceModel("ParticipantCode", ParticipantSearchPage.this, null), "barcode", "barcode"));
      columns.add(new PropertyColumn(new StringResourceModel("LastName", ParticipantSearchPage.this, null), "lastName", "lastName"));
      columns.add(new PropertyColumn(new StringResourceModel("FirstName", ParticipantSearchPage.this, null), "firstName", "firstName"));
      columns.add(new AbstractColumn(new StringResourceModel("Gender", ParticipantSearchPage.this, null), "gender") {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Participant p = (Participant) rowModel.getObject();
          cellItem.add(new Label(componentId, new StringResourceModel("Gender." + p.getGender(), ParticipantSearchPage.this, null)));
        }

      });
      columns.add(new AbstractColumn(new StringResourceModel("BirthDate", ParticipantSearchPage.this, null), "birthDate") {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Participant p = (Participant) rowModel.getObject();
          cellItem.add(new Label(componentId, DateUtils.getDateModel(new Model(p.getBirthDate()))));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Appointment", ParticipantSearchPage.this, null), "appointment.date") {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Participant p = (Participant) rowModel.getObject();
          cellItem.add(new Label(componentId, DateUtils.getFullDateModel(new Model(p.getAppointment().getDate()))));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Status", ParticipantSearchPage.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Participant p = (Participant) rowModel.getObject();
          if(p.getInterview() != null) cellItem.add(new Label(componentId, new StringResourceModel("InterviewStatus." + p.getInterview().getStatus(), ParticipantSearchPage.this, null)));
          else
            cellItem.add(new Label(componentId));
        }

      });
      columns.add(new AbstractColumn(new StringResourceModel("Actions", ParticipantSearchPage.this, null)) {

        public void populateItem(final Item cellItem, String componentId, IModel rowModel) {
          final List<IModel> actions = new ArrayList<IModel>();
          final Participant p = (Participant) rowModel.getObject();
          actions.add(new StringResourceModel("View", ParticipantSearchPage.this, null));
          if(p.getBarcode() != null) actions.add(new StringResourceModel("Interview", ParticipantSearchPage.this, null));
          else
            actions.add(new StringResourceModel("Receive", ParticipantSearchPage.this, null));

          cellItem.add(new AjaxLinkList(componentId, actions, "") {

            @Override
            public void onClick(IModel model, AjaxRequestTarget target) {
              if(actions.indexOf(model) == 0) {
                participantDetailsModalWindow.setContent(new ParticipantModalPanel("content", new ParticipantPanel("content", p), participantDetailsModalWindow));
                participantDetailsModalWindow.show(target);
              } else if(actions.indexOf(model) == 1) {
                if(p.getBarcode() != null) {
                  activeInterviewService.setParticipant(p);
                  setResponsePage(InterviewPage.class);
                } else {
                  setResponsePage(new ParticipantReceptionPage(new DetachableEntityModel(queryService, p)));
                }
              }
            }

          });
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
