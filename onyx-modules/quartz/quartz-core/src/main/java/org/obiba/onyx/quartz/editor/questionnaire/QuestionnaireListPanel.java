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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
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
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.editor.behavior.AjaxDownload;
import org.obiba.onyx.quartz.editor.behavior.tooltip.TooltipBehavior;
import org.obiba.onyx.quartz.editor.questionnaire.utils.DataSourceConverter;
import org.obiba.onyx.quartz.editor.questionnaire.utils.DataSourceConverterException;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.questionnaire.utils.StructureAnalyser;
import org.obiba.onyx.quartz.editor.questionnaire.utils.StructureAnalyserException;
import org.obiba.onyx.quartz.editor.utils.ZipResourceStream;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class QuestionnaireListPanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private final ModalWindow modalWindow;

  public QuestionnaireListPanel(String id) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(QuestionnaireListPanel.class, "QuestionnaireListPanel.css"));

    modalWindow = new ModalWindow("modalWindow");
    modalWindow.setCssClassName("onyx");
    modalWindow.setInitialWidth(1100);
    modalWindow.setInitialHeight(601);
    modalWindow.setResizable(false);
    modalWindow.setTitle(new ResourceModel("Questionnaire"));

    @SuppressWarnings("rawtypes")
    Form<?> form = new Form("form");
    form.add(modalWindow);
    add(form);

    final OnyxEntityList<Questionnaire> questionnaireList = new OnyxEntityList<Questionnaire>("questionnaires", new QuestionnaireProvider(), new QuestionnaireListColumnProvider(), new ResourceModel("Questionnaires"));
    add(questionnaireList);

    modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        target.addComponent(questionnaireList); // reload questionnaire list
        return true; // same as cancel
      }
    });

    add(new AjaxLink<Void>("addQuestionnaire") {
      @Override
      public void onClick(AjaxRequestTarget target) {

        Questionnaire newQuestionnaire = new Questionnaire(new StringResourceModel("NewQuestionnaire", QuestionnaireListPanel.this, null).getString(), "1.0");
        newQuestionnaire.setConvertedToMagmaVariables(true);
        Model<Questionnaire> questionnaireModel = new Model<Questionnaire>(newQuestionnaire);
        final EditionPanel editionPanel = new EditionPanel("content", questionnaireModel);
        QuestionnairePanel rightPanel = new QuestionnairePanel(EditionPanel.RIGHT_PANEL, questionnaireModel, true) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Questionnaire questionnaire) {
            persist(target);
            editionPanel.restoreDefaultRightPanel(target);
            target.addComponent(editionPanel.getTree());
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            modalWindow.close(target);
          }
        };
        editionPanel.setRightPanel(rightPanel, new Model<String>(""), null, null);

        modalWindow.setTitle(newQuestionnaire.getName());
        modalWindow.setContent(editionPanel);
        modalWindow.show(target);
      }
    }.add(new Image("img", Images.ADD)));
  }

  private class QuestionnaireProvider extends SortableDataProvider<Questionnaire> {

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

    public QuestionnaireListColumnProvider() {
      columns.add(new AbstractColumn<Questionnaire>(new ResourceModel("Name"), "name") {
        @Override
        public void populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          final Questionnaire questionnaire = rowModel.getObject();
          final String name = questionnaire.getName();
          cellItem.add(new AjaxLazyLoadPanel(componentId) {
            @Override
            public Component getLazyLoadComponent(String componentId1) {
              try {
                StructureAnalyser.getInstance(questionnaire).analyze();
                if(!questionnaire.isConvertedToMagmaVariables()) {
                  DataSourceConverter.getInstance(questionnaire).convert();
                  questionnaire.setConvertedToMagmaVariables(true);
                  questionnairePersistenceUtils.persist(questionnaire);
                }
              } catch(StructureAnalyserException e) {
                log.error("Unsupported questionnaire structure", e);
                return new Label(componentId1, name + " <img src=\"" + RequestCycle.get().urlFor(Images.ERROR) + "\"/>").setEscapeModelStrings(false).add(new TooltipBehavior(new StringResourceModel("analyze.error", QuestionnaireListPanel.this, null, new Object[] { e.getMessage() })));
              } catch(DataSourceConverterException e) {
                log.error("Cannot convert questionnaire", e);
                return new Label(componentId1, name + " <img src=\"" + RequestCycle.get().urlFor(Images.ERROR) + "\"/>").setEscapeModelStrings(false).add(new TooltipBehavior(new StringResourceModel("converting.error", QuestionnaireListPanel.this, null, new Object[] { e.getMessage() })));
              } catch(Exception e) {
                log.error("Cannot convert questionnaire", e);
                return new Label(componentId1, name + " <img src=\"" + RequestCycle.get().urlFor(Images.ERROR) + "\"/>").setEscapeModelStrings(false).add(new TooltipBehavior(new StringResourceModel("converting.error", QuestionnaireListPanel.this, null, new Object[] { e.getMessage() })));
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

    @SuppressWarnings("rawtypes")
    public LinkFragment(String id, final IModel<Questionnaire> rowModel) {
      super(id, "linkFragment", QuestionnaireListPanel.this, rowModel);
      final Questionnaire questionnaire = rowModel.getObject();

      add(new AjaxLink<Questionnaire>("editLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          modalWindow.setTitle(questionnaire.getName());
          modalWindow.setContent(new EditionPanel("content", rowModel));
          modalWindow.show(target);
        }
      });

      final AjaxDownload download = new AjaxDownload() {
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
        @Override
        public void onClick(AjaxRequestTarget target) {
          download.initiate(target);
        }
      });

      add(new Link<Questionnaire>("exportLink", rowModel) {
        @Override
        public void onClick() {
          activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
          activeQuestionnaireAdministrationService.setDefaultLanguage(questionnaire.getLocales().get(0));
          activeQuestionnaireAdministrationService.setQuestionnaireDevelopmentMode(true);
          setResponsePage(new SingleDocumentQuestionnairePage(new QuestionnaireModel<Questionnaire>(questionnaire)));
        }
      });
    }
  }
}
