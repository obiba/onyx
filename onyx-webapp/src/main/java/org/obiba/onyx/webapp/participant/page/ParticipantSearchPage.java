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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeAction;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeActions;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.target.resource.ResourceStreamRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.core.service.EntityQueryService;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.participant.InterviewStatus;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantMetadata;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.engine.variable.export.OnyxDataExport;
import org.obiba.onyx.webapp.base.page.BasePage;
import org.obiba.onyx.webapp.participant.panel.EditParticipantModalPanel;
import org.obiba.onyx.webapp.participant.panel.EditParticipantPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantModalPanel;
import org.obiba.onyx.webapp.participant.panel.ParticipantPanel;
import org.obiba.onyx.webapp.participant.panel.UnlockInterviewPanel;
import org.obiba.onyx.wicket.behavior.EnterOnKeyPressBehaviour;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.util.DateModelUtils;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.obiba.wicket.markup.html.table.SortableDataProviderEntityServiceImpl;
import org.obiba.wicket.util.resource.CsvResourceStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AuthorizeInstantiation( { "SYSTEM_ADMINISTRATOR", "PARTICIPANT_MANAGER", "DATA_COLLECTION_OPERATOR" })
public class ParticipantSearchPage extends BasePage {

  private static final Logger log = LoggerFactory.getLogger(ParticipantSearchPage.class);

  @SpringBean
  private EntityQueryService queryService;

  @SpringBean
  private UserSessionService userSessionService;

  @SpringBean
  private ParticipantService participantService;

  @SpringBean
  private InterviewManager interviewManager;

  @SpringBean
  private ParticipantMetadata participantMetadata;

  @SpringBean
  private OnyxDataExport onyxDataExport;

  private OnyxEntityList<Participant> participantList;

  private Participant template = new Participant();

  private ModalWindow participantDetailsModalWindow;

  private ModalWindow editParticipantDetailsModalWindow;

  private ModalWindow unlockInterviewWindow;

  private UpdateParticipantListWindow updateParticipantListWindow;

  @SuppressWarnings("serial")
  public ParticipantSearchPage() {
    super();

    participantDetailsModalWindow = new ModalWindow("participantDetailsModalWindow");
    participantDetailsModalWindow.setCssClassName("onyx");
    participantDetailsModalWindow.setTitle(new StringResourceModel("Participant", this, null));
    participantDetailsModalWindow.setInitialHeight(300);
    participantDetailsModalWindow.setInitialWidth(400);
    add(participantDetailsModalWindow);

    editParticipantDetailsModalWindow = new ModalWindow("editParticipantDetailsModalWindow");
    editParticipantDetailsModalWindow.setCssClassName("onyx");
    editParticipantDetailsModalWindow.setTitle(new StringResourceModel("EditParticipantInfo", this, null));
    editParticipantDetailsModalWindow.setInitialHeight(400);
    editParticipantDetailsModalWindow.setInitialWidth(600);
    add(editParticipantDetailsModalWindow);

    unlockInterviewWindow = new ModalWindow("unlockInterview");
    unlockInterviewWindow.setCssClassName("onyx");
    unlockInterviewWindow.setTitle(new ResourceModel("UnlockInterview"));
    unlockInterviewWindow.setResizable(false);
    unlockInterviewWindow.setUseInitialHeight(false);
    add(unlockInterviewWindow);

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
          replacement.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
        }
        replaceParticipantList(target, replacement);
        target.addComponent(getFeedbackPanel());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(getFeedbackPanel());
      }

    });

    AjaxButton lastNameButton;
    form.add(lastNameButton = new AjaxButton("searchByLastName", form) {

      @Override
      protected void onSubmit(AjaxRequestTarget target, Form form) {
        OnyxEntityList<Participant> replacement;
        if(template.getLastName() == null) {
          replacement = getAllParticipantsList();
        } else {
          replacement = new OnyxEntityList<Participant>("participant-list", new ParticipantByNameProvider(template), new ParticipantListColumnProvider(), new StringResourceModel("ParticipantsByName", ParticipantSearchPage.this, new Model(new ValueMap("name=" + template.getLastName()))));
          replacement.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
        }
        replaceParticipantList(target, replacement);
        target.addComponent(getFeedbackPanel());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(getFeedbackPanel());
      }

    });

    TextField lastNameTextField = new TextField("lastName", new PropertyModel(template, "lastName"));
    lastNameTextField.add(new EnterOnKeyPressBehaviour(lastNameButton));
    form.add(lastNameTextField);

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
        replacement.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
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
        replacement.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
        replaceParticipantList(target, replacement);
        target.addComponent(getFeedbackPanel());
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form form) {
        target.addComponent(getFeedbackPanel());
      }

    });

    add(new ActionFragment("actions"));

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
    participantList.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
    add(participantList);

    updateParticipantListWindow = new UpdateParticipantListWindow("updateParticipantListWindow");
    updateParticipantListWindow.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
      public void onClose(AjaxRequestTarget target) {
        target.addComponent(participantList);
      }
    });
    add(updateParticipantListWindow);
  }

  @Override
  protected void onModelChanged() {
    super.onModelChanged();
    this.participantList.modelChanged();
  }

  private OnyxEntityList<Participant> getAllParticipantsList() {
    OnyxEntityList<Participant> participantList = new OnyxEntityList<Participant>("participant-list", new ParticipantProvider(), new ParticipantListColumnProvider(), new StringResourceModel("Participants", ParticipantSearchPage.this, null));
    participantList.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
    return participantList;
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
      setSort(new SortParam("lastName", true));
      this.template = template;
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
      if(participantMetadata.getSupportedRecruitmentTypes().contains(RecruitmentType.ENROLLED)) columns.add(new PropertyColumn(new StringResourceModel("EnrollmentId", ParticipantSearchPage.this, null), "enrollmentId", "enrollmentId"));
      columns.add(new PropertyColumn(new StringResourceModel("ParticipantCode", ParticipantSearchPage.this, null), "barcode", "barcode"));
      columns.add(new PropertyColumn(new StringResourceModel("LastName", ParticipantSearchPage.this, null), "lastName", "lastName"));
      columns.add(new PropertyColumn(new StringResourceModel("FirstName", ParticipantSearchPage.this, null), "firstName", "firstName"));
      columns.add(new AbstractColumn(new StringResourceModel("Gender", ParticipantSearchPage.this, null), "gender") {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new Label(componentId, new StringResourceModel("Gender.${gender}", ParticipantSearchPage.this, new Model(new GenderObject(rowModel)))));
        }

      });
      columns.add(new AbstractColumn(new StringResourceModel("BirthDate", ParticipantSearchPage.this, null), "birthDate") {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new Label(componentId, DateModelUtils.getDateModel(new PropertyModel(ParticipantListColumnProvider.this, "dateFormat"), new PropertyModel(rowModel, "birthDate"))));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Appointment", ParticipantSearchPage.this, null), "appointment.date") {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new Label(componentId, DateModelUtils.getDateTimeModel(new PropertyModel(ParticipantListColumnProvider.this, "dateTimeFormat"), new PropertyModel(rowModel, "appointment.date"))));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Status", ParticipantSearchPage.this, null)) {

        public void populateItem(Item cellItem, String componentId, IModel rowModel) {
          cellItem.add(new Label(componentId, new StringResourceModel("InterviewStatus.${status}", ParticipantSearchPage.this, new PropertyModel(rowModel, "interview"), "")));
        }

      });

      columns.add(new AbstractColumn(new StringResourceModel("Actions", ParticipantSearchPage.this, null)) {

        public void populateItem(final Item cellItem, String componentId, final IModel rowModel) {
          cellItem.add(new ActionListFragment(componentId, rowModel));
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

    public DateFormat getDateFormat() {
      return userSessionService.getDateFormat();
    }

    public DateFormat getDateTimeFormat() {
      return userSessionService.getDateTimeFormat();
    }
  }

  @AuthorizeActions(actions = { @AuthorizeAction(action = Action.RENDER, roles = "PARTICIPANT_MANAGER") })
  private class ActionFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public ActionFragment(String id) {
      super(id, "actionFragment", ParticipantSearchPage.this);

      AjaxLink volunteerLink = new AjaxLink("volunteer") {
        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          Participant volunteer = new Participant();
          volunteer.setRecruitmentType(RecruitmentType.VOLUNTEER);
          setResponsePage(new ParticipantReceptionPage(new Model(volunteer), ParticipantSearchPage.this));
        }

        @Override
        public boolean isVisible() {
          return participantMetadata.getSupportedRecruitmentTypes().contains(RecruitmentType.VOLUNTEER);
        }
      };
      add(volunteerLink);

      AjaxLink updateParticipantsLink = new AjaxLink("update") {
        private static final long serialVersionUID = 1L;

        public void onClick(AjaxRequestTarget target) {
          updateParticipantListWindow.showConfirmation();
          updateParticipantListWindow.show(target);

          target.addComponent(updateParticipantListWindow.get("content"));
        }
      };
      updateParticipantsLink.setVisible(participantMetadata.getSupportedRecruitmentTypes().contains(RecruitmentType.ENROLLED));
      add(updateParticipantsLink);

      AjaxLink exportLink = new AjaxLink("export") {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          try {
            onyxDataExport.exportCompletedInterviews();
          } catch(Exception e) {
            log.error("Error on data export.", e);
          }
        }

      };
      // exportLink.setVisible(((OnyxApplication) OnyxApplication.get()).isDevelopmentMode());
      add(exportLink);
    }
  }

  /**
   * This fragment uses link visibility in order to hide/display links within the list of available links. It replaces
   * the previous use of AjaxLinkList which would add/not add a component to the list in order to hide/display it. The
   * problem with this approach is that when the model changes, a component that should now be displayed cannot since it
   * isn't present in the list.
   * @see ONYX-169
   */
  private class ActionListFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    public ActionListFragment(String id, IModel participantModel) {
      super(id, "actionList", ParticipantSearchPage.this, participantModel);

      RepeatingView repeater = new RepeatingView("link");

      // View
      AjaxLink link = new AjaxLink(repeater.newChildId(), participantModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          participantDetailsModalWindow.setContent(new ParticipantModalPanel("content", new ParticipantPanel(ParticipantModalPanel.CONTENT_PANEL_ID, getModel()), participantDetailsModalWindow));
          participantDetailsModalWindow.show(target);
        }
      };
      link.add(new Label("label", new ResourceModel("View")));
      repeater.add(link);

      // Interview
      link = new AjaxLink(repeater.newChildId(), participantModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          if(interviewManager.isInterviewAvailable(getParticipant()) == false) {
            unlockInterviewWindow.setContent(new UnlockInterviewPanel(unlockInterviewWindow.getContentId(), getModel()));
            target.appendJavascript("Wicket.Window.unloadConfirmation = false;");
            unlockInterviewWindow.show(target);
          } else {
            interviewManager.obtainInterview(getParticipant());
            setResponsePage(InterviewPage.class);
          }
        }

        @Override
        public boolean isVisible() {
          // Visible when participant has been assigned a barcode
          return getParticipant().getBarcode() != null;
        }
      };
      link.add(new Label("label", new ResourceModel("Interview")));
      repeater.add(link);

      // Receive
      link = new AjaxLink(repeater.newChildId(), participantModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          setResponsePage(new ParticipantReceptionPage(getModel(), ParticipantSearchPage.this));
        }

        @Override
        public boolean isVisible() {
          // Reception allowed when participant has no barcode associated
          return getParticipant().getBarcode() == null;
        }
      };
      link.add(new Label("label", new ResourceModel("Receive")));
      MetaDataRoleAuthorizationStrategy.authorize(link, RENDER, "PARTICIPANT_MANAGER");
      repeater.add(link);

      // Edit
      link = new AjaxLink(repeater.newChildId(), participantModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          editParticipantDetailsModalWindow.setContent(new EditParticipantModalPanel("content", new EditParticipantPanel("content", getModel(), ParticipantSearchPage.this, editParticipantDetailsModalWindow)));
          editParticipantDetailsModalWindow.show(target);
        }

        @Override
        public boolean isVisible() {
          // Visible if participant has been received and some attributes are editable.
          return getParticipant().getBarcode() != null && participantMetadata.hasEditableAfterReceptionConfiguredAttribute();
        }
      };
      link.add(new Label("label", new ResourceModel("Edit")));
      MetaDataRoleAuthorizationStrategy.authorize(link, RENDER, "PARTICIPANT_MANAGER");
      repeater.add(link);

      add(repeater);
    }

    Participant getParticipant() {
      return (Participant) getModelObject();
    }
  }

  private class GenderObject implements Serializable {

    private static final long serialVersionUID = 1L;

    private IModel participantModel;

    /**
     * @param participantModel
     */
    public GenderObject(IModel participantModel) {
      super();
      this.participantModel = participantModel;
    }

    public String getGender() {
      PropertyModel genderModel = new PropertyModel(participantModel, "gender");
      if(genderModel.getObject() != null) {
        return genderModel.getObject().toString();
      } else {
        return "Null";
      }
    }

  }
}
