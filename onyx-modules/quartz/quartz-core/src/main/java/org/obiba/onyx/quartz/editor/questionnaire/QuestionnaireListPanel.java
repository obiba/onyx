/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.Page;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.core.service.PagingClause;
import org.obiba.core.service.PersistenceManager;
import org.obiba.core.service.SortingClause;
import org.obiba.core.validation.exception.ValidationRuntimeException;
import org.obiba.onyx.core.domain.participant.Interview;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.user.User;
import org.obiba.onyx.core.service.InterviewManager;
import org.obiba.onyx.core.service.UserSessionService;
import org.obiba.onyx.quartz.core.domain.answer.QuestionnaireParticipant;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument.SingleDocumentQuestionnairePage;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class QuestionnaireListPanel extends Panel {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  protected QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  private InterviewManager interviewManager;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private UserSessionService userSessionService;

  protected ModalWindow modalWindow;

  protected ModalWindow layoutWindow;

  public QuestionnaireListPanel(String id, ModalWindow modalWindow) {
    super(id);
    this.modalWindow = modalWindow;

    layoutWindow = new ModalWindow("layoutWindow");
    layoutWindow.setCssClassName("onyx");
    layoutWindow.setInitialWidth(1000);
    layoutWindow.setInitialHeight(600);
    layoutWindow.setResizable(true);
    layoutWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true; // same as cancel
      }
    });
    add(layoutWindow);

    add(new OnyxEntityList<Questionnaire>("questionnaire-list", new QuestionnaireProvider(), new QuestionnaireListColumnProvider(), new StringResourceModel("QuestionnaireList", QuestionnaireListPanel.this, null)));
  }

  protected class QuestionnaireProvider extends SortableDataProvider<Questionnaire> {

    @Override
    public Iterator<Questionnaire> iterator(int first, int count) {
      Set<QuestionnaireBundle> bundles = questionnaireBundleManager.bundles();
      List<Questionnaire> questionnaires = new ArrayList<Questionnaire>(bundles.size());
      for(QuestionnaireBundle bundle : bundles) {
        questionnaires.add(bundle.getQuestionnaire());
      }
      return questionnaires.iterator();
    }

    @Override
    public int size() {
      return questionnaireBundleManager.bundles().size();
    }

    @Override
    public IModel<Questionnaire> model(Questionnaire questionnaire) {
      return new Model<Questionnaire>(questionnaire);
    }

  }

  private class QuestionnaireListColumnProvider implements IColumnProvider<Questionnaire>, Serializable {

    private final List<IColumn<Questionnaire>> columns = new ArrayList<IColumn<Questionnaire>>();

    private final List<IColumn<Questionnaire>> additional = new ArrayList<IColumn<Questionnaire>>();

    public QuestionnaireListColumnProvider() {
      columns.add(new PropertyColumn<Questionnaire>(new StringResourceModel("Name", QuestionnaireListPanel.this, null), "name", "name"));
      columns.add(new PropertyColumn<Questionnaire>(new StringResourceModel("Version", QuestionnaireListPanel.this, null), "version", "version"));
      columns.add(new AbstractColumn<Questionnaire>(new StringResourceModel("Language(s)", QuestionnaireListPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          StringBuilder localeList = new StringBuilder();
          Locale sessionLocale = Session.get().getLocale();
          for(Locale locale : rowModel.getObject().getLocales()) {
            if(localeList.length() != 0) localeList.append(", ");
            localeList.append(locale.getDisplayLanguage(sessionLocale));
          }
          cellItem.add(new Label(componentId, localeList.toString()));
        }
      });

      columns.add(new HeaderlessColumn<Questionnaire>() {
        @Override
        public void populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<Questionnaire>> getAdditionalColumns() {
      return additional;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<Questionnaire>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<Questionnaire>> getRequiredColumns() {
      return columns;
    }

  }

  public class LinkFragment extends Fragment {

    public LinkFragment(String id, final IModel<Questionnaire> rowModel) {
      super(id, "linkFragment", QuestionnaireListPanel.this, rowModel);

      add(new AjaxLink<Questionnaire>("editLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          modalWindow.setTitle(new StringResourceModel("Questionnaire", this, null));
          modalWindow.setContent(new QuestionnairePropertiesPanel("content", rowModel, modalWindow));
          modalWindow.show(target);
        }
      });
      add(new AjaxLink<Questionnaire>("layoutLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          layoutWindow.setTitle(new StringResourceModel("Questionnaire", this, null));
          layoutWindow.setContent(new QuestionnaireTreePanel("content", rowModel));
          layoutWindow.show(target);
        }
      });
      add(new AjaxLink<Questionnaire>("previewLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          layoutWindow.setContent(new Panel("contents"));
          layoutWindow.show(target);
        }
      });
      add(new AjaxLink<Questionnaire>("exportLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          activeQuestionnaireAdministrationService.setQuestionnaire(rowModel.getObject());
          final Participant participant = new Participant();
          participant.setId(1L);
          participant.setInterview(new Interview());
          final QuestionnaireParticipant questionnaireParticipant = new QuestionnaireParticipant();
          PersistenceManager persistenceManager = new PersistenceManager() {

            @Override
            public <T> T refresh(T entity) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public <T> T matchOne(T template, SortingClause... clauses) {
              if(template instanceof Participant) {
                return (T) participant;
              } else if(template instanceof QuestionnaireParticipant) {
                return (T) questionnaireParticipant;
              }
              return null;
            }

            @Override
            public <T> List<T> match(T template, PagingClause paging, SortingClause... clauses) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public <T> List<T> match(T template, SortingClause... clauses) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public <T> List<T> list(Class<T> type, PagingClause paging, SortingClause... clauses) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public <T> List<T> list(Class<T> type, SortingClause... clauses) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public Serializable getId(Object o) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public <T> T get(Class<T> type, Serializable id) {
              if(type.isAssignableFrom(User.class)) {
                return (T) userSessionService.getUser();
              } else if(type.isAssignableFrom(QuestionnaireParticipant.class)) {
                return (T) questionnaireParticipant;
              } else if(type.isAssignableFrom(Participant.class)) {
                return (T) participant;
              }
              return null;
            }

            @Override
            public int count(Object template) {
              // TODO Auto-generated method stub
              return 0;
            }

            @Override
            public int count(Class<?> type) {
              // TODO Auto-generated method stub
              return 0;
            }

            @Override
            public <T> T save(T entity) throws ValidationRuntimeException {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public <T> T newInstance(Class<T> type) {
              // TODO Auto-generated method stub
              return null;
            }

            @Override
            public void delete(Object entity) {
              // TODO Auto-generated method stub

            }
          };
          // activeQuestionnaireAdministrationService.setPersistenceManager(persistenceManager);
          // interviewManager.setPersistenceManager(persistenceManager);
          interviewManager.overrideInterview(participant);
          layoutWindow.setPageCreator(new ModalWindow.PageCreator() {

            @Override
            public Page createPage() {
              return new SingleDocumentQuestionnairePage(rowModel);
            }
          });
          layoutWindow.show(target);
        }
      });
    }
  }
}
