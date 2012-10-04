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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.IResourceStream;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument.SingleDocumentQuestionnairePage;
import org.obiba.onyx.quartz.editor.behavior.AjaxDownload;
import org.obiba.onyx.quartz.editor.behavior.tooltip.TooltipBehavior;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnaireConverter;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnaireConverterException;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnaireRegister;
import org.obiba.onyx.quartz.editor.questionnaire.utils.StructureAnalyser;
import org.obiba.onyx.quartz.editor.questionnaire.utils.StructureAnalyserException;
import org.obiba.onyx.quartz.editor.utils.ZipResourceStream;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.obiba.onyx.wicket.Images.ERROR;

public class QuestionnaireListPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD",
      justification = "Need to be be re-initialized upon deserialization")
  private QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD",
      justification = "Need to be be re-initialized upon deserialization")
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  @SpringBean
  private QuestionnaireRegister questionnaireRegister;

  private final ModalWindow modalWindow;

  private final ModalWindow uploadWindow;

  private OnyxEntityList<Questionnaire> questionnaireList;

  private ConfirmationDialog deleteConfirm;

  public QuestionnaireListPanel(String id) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(QuestionnaireListPanel.class, "QuestionnaireListPanel.css"));

    modalWindow = new ModalWindow("modalWindow");
    modalWindow.setCssClassName("onyx");
    modalWindow.setInitialWidth(1100);
    modalWindow.setInitialHeight(801);
    modalWindow.setResizable(true);
    modalWindow.setTitle(new ResourceModel("Questionnaire"));

    uploadWindow = new ModalWindow("uploadWindow");
    uploadWindow.setCssClassName("onyx");
    uploadWindow.setInitialWidth(500);
    uploadWindow.setInitialHeight(150);
    uploadWindow.setResizable(false);

    add(uploadWindow);

    Form<?> form = new Form<Void>("form");
    form.setMultiPart(false);
    form.add(modalWindow);
    add(form);

    add(questionnaireList = new OnyxEntityList<Questionnaire>("questionnaires", new QuestionnaireProvider(), new QuestionnaireListColumnProvider(), new ResourceModel("Questionnaires")));

    add(deleteConfirm = new ConfirmationDialog("deleteConfirm"));
    deleteConfirm.setTitle(new StringResourceModel("ConfirmDelete", this, null));
    deleteConfirm.setContent(new MultiLineLabel(deleteConfirm.getContentId(), new ResourceModel("DeleteQuestionnaireConfirmInfos")));

    add(new IndicatingAjaxLink<Void>("addQuestionnaire") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {

        Questionnaire newQuestionnaire = new Questionnaire(new StringResourceModel("NewQuestionnaire", QuestionnaireListPanel.this, null).getString(), "1.0");
        newQuestionnaire.setConvertedToMagmaVariables(true);
        Model<Questionnaire> questionnaireModel = new Model<Questionnaire>(newQuestionnaire);
        final EditionPanel editionPanel = new EditionPanel("content", questionnaireModel, true);
        QuestionnairePanel rightPanel = new QuestionnairePanel(EditionPanel.RIGHT_PANEL, questionnaireModel, true) {
          private static final long serialVersionUID = 1L;

          @Override
          public void prepareSave(AjaxRequestTarget target, Questionnaire questionnaire) {

          }

          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Questionnaire questionnaire) {
            editionPanel.restoreDefaultRightPanel(target);
            target.addComponent(editionPanel.getTree());
            editionPanel.setNewQuestionnaire(false);
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            modalWindow.close(target);
          }
        };
        editionPanel.setRightPanel(rightPanel, new Model<String>(""), null, null);
        modalWindow.setTitle(newQuestionnaire.getName());
        modalWindow.setContent(editionPanel);
        modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
          private static final long serialVersionUID = 1L;

          @Override
          public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            target.addComponent(questionnaireList); // reload questionnaire list
            return true; // same as cancel
          }
        });
        modalWindow.show(target);
      }
    }.add(new Image("addImg", Images.ADD)));

    AjaxLink<?> uploadLink = new AjaxLink<Void>("uploadQuestionnaire") {
      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        uploadWindow.setTitle(new ResourceModel("UploadNewQuestionnaire"));
        uploadWindow.setContent(new UploadQuestionnairePanel("content", uploadWindow) {
          private static final long serialVersionUID = 1L;

          @Override
          protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            target.addComponent(questionnaireList);
          }
        });
        uploadWindow.show(target);
      }
    };
    uploadLink.add(new Image("uploadImg", Images.UPLOAD));
    add(uploadLink);

  }

  private class QuestionnaireProvider extends SortableDataProvider<Questionnaire> {

    private static final long serialVersionUID = 1L;

    @Override
    public Iterator<Questionnaire> iterator(int first, int count) {
      Set<QuestionnaireBundle> bundles = questionnaireBundleManager.persistedBundles();
      List<Questionnaire> questionnaires = new ArrayList<Questionnaire>(bundles.size());
      for(QuestionnaireBundle bundle : bundles) {
        questionnaires.add(bundle.getQuestionnaire());
      }
      return questionnaires.iterator();
    }

    @Override
    public int size() {
      return questionnaireBundleManager.countQuestionnaires();
    }

    @Override
    public IModel<Questionnaire> model(Questionnaire questionnaire) {
      return new Model<Questionnaire>(questionnaire);
    }

  }

  private class QuestionnaireListColumnProvider implements IColumnProvider<Questionnaire>, Serializable {

    private static final long serialVersionUID = 1L;

    private final List<IColumn<Questionnaire>> columns = new ArrayList<IColumn<Questionnaire>>();

    public QuestionnaireListColumnProvider() {
      columns.add(new AbstractColumn<Questionnaire>(new ResourceModel("Name"), "name") {
        private static final long serialVersionUID = 1L;

        @Override
        public
            void
            populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          final Questionnaire questionnaire = rowModel.getObject();
          final String name = questionnaire.getName();
          cellItem.add(new AjaxLazyLoadPanel(componentId) {
            private static final long serialVersionUID = 1L;

            @Override
            public Component getLazyLoadComponent(String componentId1) {
              try {
                StructureAnalyser.getInstance(questionnaire).analyze();
                if(!questionnaire.isConvertedToMagmaVariables()) {
                  QuestionnaireConverter.getInstance(questionnaire).convert();
                  questionnaire.setConvertedToMagmaVariables(true);
                  questionnairePersistenceUtils.persist(questionnaire);
                }
              } catch(StructureAnalyserException e) {
                log.error("Unsupported questionnaire structure", e);
                return new Label(componentId1, name + " <img src=\"" + RequestCycle.get().urlFor(ERROR) + "\"/>").setEscapeModelStrings(false).add(new TooltipBehavior(new StringResourceModel("analyze.error", QuestionnaireListPanel.this, null, new Object[] { e.getMessage() })));
              } catch(QuestionnaireConverterException e) {
                log.error("Cannot convert questionnaire", e);
                return new Label(componentId1, name + " <img src=\"" + RequestCycle.get().urlFor(ERROR) + "\"/>").setEscapeModelStrings(false).add(new TooltipBehavior(new StringResourceModel("converting.error", QuestionnaireListPanel.this, null, new Object[] { e.getMessage() })));
              } catch(Exception e) {
                log.error("Cannot convert questionnaire", e);
                return new Label(componentId1, name + " <img src=\"" + RequestCycle.get().urlFor(ERROR) + "\"/>").setEscapeModelStrings(false).add(new TooltipBehavior(new StringResourceModel("converting.error", QuestionnaireListPanel.this, null, new Object[] { e.getMessage() })));
              }
              return new Label(componentId1, name);
            }

            @Override
            public Component getLoadingComponent(String markupId) {
              String message = new StringResourceModel("converting", QuestionnaireListPanel.this, null).getString();
              return new Label(markupId, name + "<span class=\"converting\"><img alt=\"" + message + "\" src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>" + message + "</span>").setEscapeModelStrings(false);
            }

          });
        }
      });

      columns.add(new PropertyColumn<Questionnaire>(new ResourceModel("Version"), "version", "version"));
      columns.add(new AbstractColumn<Questionnaire>(new ResourceModel("Language(s)")) {
        private static final long serialVersionUID = 1L;

        @Override
        public
            void
            populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
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
        private static final long serialVersionUID = 1L;

        @Override
        public
            void
            populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<Questionnaire>> getAdditionalColumns() {
      return null;
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

  private class LinkFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("rawtypes")
    public LinkFragment(String id, final IModel<Questionnaire> rowModel) {
      super(id, "linkFragment", QuestionnaireListPanel.this, rowModel);
      final Questionnaire questionnaire = rowModel.getObject();

      add(new AjaxLink<Questionnaire>("editLink", rowModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          modalWindow.setTitle(questionnaire.getName());
          modalWindow.setContent(new EditionPanel("content", rowModel));
          modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean onCloseButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
              target.addComponent(questionnaireList); // reload questionnaire list
              return true; // same as cancel
            }
          });
          modalWindow.show(target);
        }
      });

      add(new AjaxLink<Questionnaire>("uploadLink", rowModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          uploadWindow.setTitle(new ResourceModel("UploadQuestionnaire"));
          uploadWindow.setContent(new UploadQuestionnairePanel("content", uploadWindow, rowModel) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target) {
              target.addComponent(questionnaireList);
            }
          });
          uploadWindow.show(target);
        }
      });

      add(new AjaxLink<Questionnaire>("validateLink", rowModel) {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          modalWindow.setTitle(new StringResourceModel("ValidationResults", QuestionnaireListPanel.this, null, new Object[] { questionnaire.getName() }));
          modalWindow.setContent(new ValidationPanel("content", rowModel));
          modalWindow.setCloseButtonCallback(null);
          modalWindow.show(target);
        }
      });

      final AjaxDownload download = new AjaxDownload() {
        private static final long serialVersionUID = 1L;

        @Override
        protected IResourceStream getResourceStream() {
          try {
            return new ZipResourceStream(questionnaireBundleManager.generateBundleZip(questionnaire.getName()));
          } catch(IOException e) {
            log.error("Cannot generate questionnaire zip", e);
            return null;
          }
        }

        @Override
        protected String getFileName() {
          return questionnaire.getName() + ".zip";
        }
      };
      add(download);

      add(new AjaxLink("downloadLink") {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          download.initiate(target);
        }
      });

      add(new AjaxLink("deleteLink") {
        private static final long serialVersionUID = 1L;

        @Override
        public void onClick(AjaxRequestTarget target) {
          deleteConfirm.setYesButtonCallback(new OnYesCallback() {

            private static final long serialVersionUID = 1L;

            @SuppressWarnings("hiding")
            @Override
            public void onYesButtonClicked(AjaxRequestTarget target) {
              questionnairePersistenceUtils.delete(questionnaire);
              target.addComponent(questionnaireList);
            }
          });
          deleteConfirm.show(target);
        }

        @Override
        public boolean isEnabled() {
          return !questionnaireRegister.isConclusionQuestionnaire(questionnaire);
        }
      });

      Loop links = new Loop("exportLinks", questionnaire.getLocales().size()) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void populateItem(final LoopItem item) {
          final Locale language = questionnaire.getLocales().get(item.getIteration());
          Link<Questionnaire> link = new Link<Questionnaire>("exportLink", rowModel) {

            private static final long serialVersionUID = 1L;

            @Override
            public void onClick() {
              activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
              activeQuestionnaireAdministrationService.setDefaultLanguage(language);
              activeQuestionnaireAdministrationService.setQuestionnaireDevelopmentMode(true);
              setResponsePage(new SingleDocumentQuestionnairePage(rowModel));
            }
          };
          item.add(link);
          link.add(new Label("labelLink", language.getDisplayLanguage(Session.get().getLocale())));
        }
      };
      add(links);
    }
  }
}
