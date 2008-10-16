/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.participant.page;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.panel.OnyxEntityList;
import org.obiba.onyx.webapp.participant.panel.ParticipantModalPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.JavascriptEventConfirmation;
import org.obiba.wicket.markup.html.link.AjaxLinkList;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;
import org.obiba.wicket.util.resource.CsvResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.ObjectError;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class ParticipantSearchPage extends BasePage {

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(ParticipantSearchPage.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private ParticipantService participantService;

  @SpringBean(name="activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  private OnyxEntityList<Participant> participantList;

  private Participant template = new Participant();

  private ModalWindow participantDetailsModalWindow;

  @SuppressWarnings("serial")
  public ParticipantSearchPage() {
    super();

    participantDetailsModalWindow = new ModalWindow("participantDetailsModalWindow");
    participantDetailsModalWindow.setTitle(new StringResourceModel("Participant", this, null));
    participantDetailsModalWindow.setInitialHeight(300);
    participantDetailsModalWindow.setInitialWidth(400);
    add(participantDetailsModalWindow);

    Form form = new Form("searchForm");
    add(form);

    form.add(new TextField("barcode", new PropertyModel(template, "barcode")));

    form.add(new AjaxButton("searchByCode", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement;
        if(template.getBarcode() == null) {
          replacement = getAllParticipantsList();
        } else {
          replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantByCodeProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("ParticipantsByCode", ParticipantSearchPage.this, new Model(new ValueMap("code=" + template.getBarcode()))));
        }
        replaceParticipantList(target, replacement);
        target.addComponent(getFeedbackPanel());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(getFeedbackPanel());
      }

    });

    form.add(new TextField("lastName", new PropertyModel(template, "lastName")));

    form.add(new AjaxButton("searchByLastName", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement;
        if(template.getLastName() == null) {
          replacement = getAllParticipantsList();
        } else {
          replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantByNameProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("ParticipantsByName", ParticipantSearchPage.this, new Model(new ValueMap("name=" + template.getLastName()))));
        }
        replaceParticipantList(target, replacement);
        target.addComponent(getFeedbackPanel());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(getFeedbackPanel());
      }

    });

    form.add(new AjaxButton("submit", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = getAllParticipantsList();
        replaceParticipantList(target, replacement);
        target.addComponent(getFeedbackPanel());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(getFeedbackPanel());
      }

    });

    form.add(new AjaxButton("appointments", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new AppointedParticipantProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("AppointmentsOfTheDay", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
        target.addComponent(getFeedbackPanel());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(getFeedbackPanel());
      }

    });

    form.add(new AjaxButton("interviews", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement = new OnyxEntityList<Participant>("participant-list", new InterviewedParticipantProvider(), new ParticipantListColumnProvider(), new StringResourceModel("CurrentInterviews", ParticipantSearchPage.this, null));
        replaceParticipantList(target, replacement);
        target.addComponent(getFeedbackPanel());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(getFeedbackPanel());
      }

    });

    add(new AjaxLink("volunteer") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        // TODO enroll volunteer
        target.addComponent(getFeedbackPanel());
      }

    });

    Link link = new Link("update") {

      @Override
      public void onClick() {
        try {
          participantService.updateParticipantList();
          info(ParticipantSearchPage.this.getString("ParticipantsListSuccessfullyUpdated"));
        } catch(ValidationRuntimeException e) {
          for(ObjectError oe : e.getAllObjectErrors()) {
            Object[] args = oe.getArguments();
            IModel model = null;
            if(oe.getCode().equals("ParticipantInterviewCompletedWithAppointmentInTheFuture") && args != null && args.length == 4) {
              ValueMap map = new ValueMap("line=" + args[0] + ",id=" + args[1]);
              model = new Model(map);
            } else if(oe.getCode().equals("WrongParticipantSiteName") && args != null && args.length >= 3) {
              ValueMap map = new ValueMap("line=" + args[0] + ",id=" + args[1] + ",site=" + args[2]);
              model = new Model(map);
            }
            error(ParticipantSearchPage.this.getString(oe.getCode(), model, oe.getDefaultMessage()));
          }
          log.error("Failed updating participants: {}", e.toString());
        }
      }

    };
    link.add(new JavascriptEventConfirmation("onclick", new StringResourceModel("ConfirmParticipantsListUpdate", this, null)));
    add(link);

    add(new Link("excel") {

      @Override
      public void onClick() {
        getRequestCycle().setRequestTarget(new ResourceStreamRequestTarget(participantList.getReportStream()) {
          @Override
          public String getFileName() {
            SimpleDateFormat formater = new SimpleDateFormat("yyyyMMdd_HHmm");
            String name = formater.format(new Date()) + "_participants";

            return name + "." + CsvResourceStream.FILE_SUFFIX;
          }
        });
      }

    });

    participantList = new OnyxEntityList<Participant>("participant-list", new AppointedParticipantProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("AppointmentsOfTheDay", ParticipantSearchPage.this, null));
    add(participantList);
  }

  private OnyxEntityList<Participant> getAllParticipantsList() {
    return new OnyxEntityList<Participant>("participant-list", new ParticipantProvider(), new ParticipantListColumnProvider(), new StringResourceModel("Participants", ParticipantSearchPage.this, null));
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
      return queryService.list(Participant.class, paging, clauses);
    }

    @Override
    public int size() {
      return queryService.count(Participant.class);
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
      return participantService.getParticipantsByCode(template.getBarcode(), paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipantsByCode(template.getBarcode());
    }

  }

  @SuppressWarnings("serial")
  private class ParticipantByNameProvider extends SortableDataProviderEntityServiceImpl<Participant> {

    private Participant template;

    public ParticipantByNameProvider(Participant template) {
      super(queryService, Participant.class);
      this.template = template;
      setSort(new SortParam("lastName", true));
    }

    @Override
    protected List<Participant> getList(PagingClause paging, SortingClause... clauses) {
      return participantService.getParticipantsByName(template.getLastName(), paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipantsByName(template.getLastName());
    }

  }

  @SuppressWarnings("serial")
  private class AppointedParticipantProvider extends SortableDataProviderEntityServiceImpl<Participant> {

    private Date from;

    private Date to;

    public AppointedParticipantProvider(Participant template) {
      super(queryService, Participant.class);
      setSort(new SortParam("appointment.date", true));

      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
      this.from = cal.getTime();
      cal.add(Calendar.DAY_OF_MONTH, 1);
      this.to = cal.getTime();
    }

    @Override
    protected List<Participant> getList(PagingClause paging, SortingClause... clauses) {
      return participantService.getParticipants(from, to, paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipants(from, to);
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
      return participantService.getParticipants(InterviewStatus.IN_PROGRESS, paging, clauses);
    }

    @Override
    public int size() {
      return participantService.countParticipants(InterviewStatus.IN_PROGRESS);
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
          cellItem.add(new Label(componentId, DateModelUtils.getShortDateModel(new Model(p.getBirthDate()))));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Appointment", ParticipantSearchPage.this, null), "appointment.date") {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          Participant p = (Participant) rowModel.getObject();
          if(p.getAppointment() != null) cellItem.add(new Label(componentId, DateModelUtils.getShortDateTimeModel(new Model(p.getAppointment().getDate()))));
          else
            cellItem.add(new Label(componentId, ""));
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

        public void populateItem(final Item cellItem, String componentId, final IModel rowModel) {
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
                participantDetailsModalWindow.setContent(new ParticipantModalPanel("content", new ParticipantPanel("content", rowModel), participantDetailsModalWindow));
                participantDetailsModalWindow.show(target);
              } else if(actions.indexOf(model) == 1) {
                if(p.getBarcode() != null) {
                  activeInterviewService.setParticipant(p);
                  setResponsePage(InterviewPage.class);
                } else {
                  setResponsePage(new ParticipantReceptionPage(rowModel, ParticipantSearchPage.this));
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
