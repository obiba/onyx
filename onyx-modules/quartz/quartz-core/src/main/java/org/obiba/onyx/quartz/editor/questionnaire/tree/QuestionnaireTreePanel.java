/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.tree;

import static org.apache.commons.lang.ClassUtils.getShortClassName;
import static org.apache.commons.lang.StringUtils.trimToNull;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.target.basic.StringRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasSection;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.page.PagePanel;
import org.obiba.onyx.quartz.editor.page.PagePreviewPanel;
import org.obiba.onyx.quartz.editor.question.EditQuestionPanel;
import org.obiba.onyx.quartz.editor.question.QuestionPreviewPanel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePanel;
import org.obiba.onyx.quartz.editor.questionnaire.tree.JsonNode.JsonNodeAttribute;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.section.SectionPanel;
import org.obiba.onyx.quartz.editor.variable.VariablePanel;
import org.obiba.onyx.quartz.editor.variable.VariablePreview;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnNoCallback;
import org.obiba.onyx.wicket.reusable.ConfirmationDialog.OnYesCallback;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model is reloaded only on cancel. <br>
 * Tree is jsTree is reloaded only on addChild.
 */
@SuppressWarnings("serial")
public abstract class QuestionnaireTreePanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  private static final String ID_PREFIX = "element_";

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  private final Map<String, TreeNode> elements = new HashMap<String, TreeNode>();

  private int elementCounter;

  private final AbstractDefaultAjaxBehavior moveBehavior;

  private final Label moveCallback;

  private final AbstractDefaultAjaxBehavior editBehavior;

  private final Label editCallback;

  private final AbstractDefaultAjaxBehavior deleteBehavior;

  private final Label deleteCallback;

  private final AbstractDefaultAjaxBehavior addChildBehavior;

  private final Label addChildCallback;

  private final AbstractDefaultAjaxBehavior previewBehavior;

  private final Label previewCallback;

  private final WebMarkupContainer tree;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final LocaleProperties localeProperties;

  private ConfirmationDialog editingConfirmationDialog;

  /**
   * Flag that indicates if user is currently editing an element. It allows us to block action in the tree while
   * edition.
   */
  private boolean editingElement = false;

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model) {
    super(id, model);
    final Questionnaire questionnaire = model.getObject();

    editingConfirmationDialog = new ConfirmationDialog("editingConfirm");
    editingConfirmationDialog.setContent(new MultiLineLabel(editingConfirmationDialog.getContentId(), new ResourceModel("CancelChanges")));
    add(editingConfirmationDialog);

    localeProperties = localePropertiesUtils.load(questionnaire);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    add(JavascriptPackageResource.getHeaderContribution(QuestionnaireTreePanel.class, "QuestionnaireTreePanel.js"));

    tree = new WebMarkupContainer("tree");
    tree.setOutputMarkupId(true);
    add(tree);

    tree.add(new JsTreeBehavior());

    tree.add(new AbstractDefaultAjaxBehavior() {
      @Override
      public void renderHead(IHeaderResponse response) {
        response.renderOnLoadJavascript("Wicket.QTree.buildTree('" + tree.getMarkupId(true) + "', '" + getCallbackUrl() + "')");
      }

      @Override
      protected void respond(AjaxRequestTarget target) {
        try {
          final RequestCycle requestCycle = RequestCycle.get();
          StringWriter sw = new StringWriter();
          JsonNode rootNode = populateNode((IQuestionnaireElement) QuestionnaireTreePanel.this.getDefaultModelObject());
          JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
          new ObjectMapper().writeValue(gen, rootNode);
          requestCycle.setRequestTarget(new StringRequestTarget("application/json", "utf-8", sw.toString()));
        } catch(Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    add(moveBehavior = new MoveBehavior());
    moveCallback = new Label("moveCallback", "");
    moveCallback.setOutputMarkupId(true);
    moveCallback.setEscapeModelStrings(false);
    add(moveCallback);

    add(editBehavior = new EditBehavior());
    editCallback = new Label("editCallback", "");
    editCallback.setOutputMarkupId(true);
    editCallback.setEscapeModelStrings(false);
    add(editCallback);

    add(deleteBehavior = new DeleteBehavior());
    deleteCallback = new Label("deleteCallback", "");
    deleteCallback.setOutputMarkupId(true);
    deleteCallback.setEscapeModelStrings(false);
    add(deleteCallback);

    add(addChildBehavior = new AddChildBehavior());
    addChildCallback = new Label("addChildCallback", "");
    addChildCallback.setOutputMarkupId(true);
    addChildCallback.setEscapeModelStrings(false);
    add(addChildCallback);

    add(previewBehavior = new PreviewBehavior());
    previewCallback = new Label("previewCallback", "");
    previewCallback.setOutputMarkupId(true);
    previewCallback.setEscapeModelStrings(false);
    add(previewCallback);

  }

  public abstract void show(Component component, IModel<String> title, AjaxRequestTarget target);

  public abstract String getShownComponentId();

  @Override
  protected void onBeforeRender() {
    super.onBeforeRender();
    moveCallback.setDefaultModelObject("Wicket.QTree.moveNode = function(nodeId, newParentId, newPosition, previousParentId) {\n" + //
    "  wicketAjaxGet('" + moveBehavior.getCallbackUrl(true) + "&nodeId='+ nodeId +'&newParentId='+ newParentId +'&newPosition='+ newPosition +'&previousParentId='+ previousParentId, function() { }, function() { alert('Cannot communicate with server...'); });" + //
    "\n}");

    editCallback.setDefaultModelObject("Wicket.QTree.editElement = function(nodeId) {\n" + //
    "  wicketAjaxGet('" + editBehavior.getCallbackUrl(true) + "&nodeId='+ nodeId, function() { }, function() { alert('Cannot communicate with server...'); });" + //
    "\n}");

    deleteCallback.setDefaultModelObject("Wicket.QTree.deleteElement = function(nodeId) {\n" + //
    "  wicketAjaxGet('" + deleteBehavior.getCallbackUrl(true) + "&nodeId='+ nodeId, function() { }, function() { alert('Cannot communicate with server...'); });" + //
    "\n}");

    addChildCallback.setDefaultModelObject("Wicket.QTree.addChild = function(nodeId, type) {\n" + //
    "  wicketAjaxGet('" + addChildBehavior.getCallbackUrl(true) + "&nodeId='+ nodeId +'&type='+ type, function() { }, function() { alert('Cannot communicate with server...'); });" + //
    "\n}");

    previewCallback.setDefaultModelObject("Wicket.QTree.previewNode = function(nodeId) {\n" + //
    "  wicketAjaxGet('" + previewBehavior.getCallbackUrl(true) + "&nodeId='+ nodeId, function() { }, function() { alert('Cannot communicate with server...'); });" + //
    "\n}");
  }

  protected class MoveBehavior extends AbstractDefaultAjaxBehavior {

    @Override
    protected void respond(AjaxRequestTarget target) {
      Request request = RequestCycle.get().getRequest();
      String nodeId = request.getParameter("nodeId");
      String newParentId = trimToNull(request.getParameter("newParentId"));
      int position = Integer.parseInt(request.getParameter("newPosition"));

      TreeNode node = elements.get(nodeId);
      TreeNode newNode = elements.get(newParentId);

      @SuppressWarnings("unchecked")
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      final Questionnaire questionnaire = questionnaireModel.getObject();
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
      questionnaireFinder.buildQuestionnaireCache();
      if(node.isSection() && newNode.isHasSection()) {
        Section section = questionnaireFinder.findSection(node.getName());
        final IHasSection newHasSection;
        if(newNode.isSection()) {
          newHasSection = questionnaireFinder.findSection(newNode.getName());
        } else {
          newHasSection = questionnaire;
        }
        if(section.getParentSection() == null) {
          questionnaire.removeSection(section);
        } else {
          section.getParentSection().removeSection(section);
        }
        if(newHasSection instanceof Questionnaire) {
          section.setParentSection(null);
        }
        newHasSection.addSection(section, Math.min(newHasSection.getSections().size(), position));
        persitQuestionnaire(target);
      } else if(node.isPage() && newNode.isSection()) {
        Page page = questionnaireFinder.findPage(node.getName());
        Section newParentSection = questionnaireFinder.findSection(newNode.getName());
        page.getSection().removePage(page);
        newParentSection.addPage(page, Math.min(newParentSection.getPages().size(), position));
        persitQuestionnaire(target);
      } else if(node.isQuestion() && newNode.isHasSection()) {
        Question question = questionnaireFinder.findQuestion(node.getName());
        Page newParentPage = questionnaireFinder.findPage(newNode.getName());
        question.getPage().removeQuestion(question);
        newParentPage.addQuestion(question, Math.min(position, newParentPage.getQuestions().size()));
        persitQuestionnaire(target);
      }
    }
  }

  protected class EditBehavior extends AbstractDefaultAjaxBehavior {

    @Override
    protected void respond(final AjaxRequestTarget target) {
      final String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      log.info("Edit " + nodeId);

      // retrieve updated model
      @SuppressWarnings("unchecked")
      final IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      final Questionnaire questionnaire = questionnaireModel.getObject();

      final TreeNode node = elements.get(nodeId);
      final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
      questionnaireFinder.buildQuestionnaireCache();
      editingElement = true;
      if(node.isQuestionnaire()) {
        QuestionnairePanel questionnairePanel = new QuestionnairePanel(getShownComponentId(), new Model<Questionnaire>(questionnaire), false) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") Questionnaire questionnaire) {
            persist(target);
            node.setName(questionnaire.getName());
            // update node name in jsTree
            target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(node) + "');");
            preview(nodeId, node, target);
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            reloadModel();
            preview(nodeId, node, target);
          }
        };
        IModel<String> title = new Model<String>(new StringResourceModel("Questionnaire", QuestionnaireTreePanel.this, null).getString() + " " + questionnaire.getName());
        show(questionnairePanel, title, target);
      }
      if(node.isSection()) {
        Section section = questionnaireFinder.findSection(node.getName());
        SectionPanel sectionPanel = new SectionPanel(getShownComponentId(), new Model<Section>(section), questionnaireModel) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") Section section) {
            persist(target);
            node.setName(section.getName());
            // update node name in jsTree
            target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(node) + "');");
            preview(nodeId, node, target);
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            reloadModel();
            preview(nodeId, node, target);
          }
        };
        IModel<String> title = new Model<String>(new StringResourceModel("Section", QuestionnaireTreePanel.this, null).getString() + " " + section.getName());
        show(sectionPanel, title, target);
      } else if(node.isPage()) {
        editPage(nodeId, node, questionnaireModel, questionnaireFinder, target);
      } else if(node.isQuestion()) {
        editQuestion(nodeId, node, questionnaireModel, questionnaireFinder, target);
      } else if(node.isVariable()) {
        editVariable(nodeId, node, questionnaireModel, target);
      }
    }

  }

  protected class DeleteBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(AjaxRequestTarget target) {
      String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      log.info("Delete " + nodeId);

      @SuppressWarnings("unchecked")
      final Questionnaire questionnaire = ((IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel()).getObject();
      TreeNode node = elements.get(nodeId);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
      questionnaireFinder.buildQuestionnaireCache();
      if(node.isSection()) {
        Section section = questionnaireFinder.findSection(node.getName());
        Section parentSection = section.getParentSection();
        if(parentSection == null) {
          questionnaire.removeSection(section);
        } else {
          parentSection.removeSection(section);
        }
      } else if(node.isPage()) {
        Page page = questionnaireFinder.findPage(node.getName());
        page.getSection().removePage(page);
      } else if(node.isQuestion()) {
        Question question = questionnaireFinder.findQuestion(node.getName());
        question.getPage().removeQuestion(question);
      }
      persitQuestionnaire(target);
      elements.remove(nodeId);
      // remove node from jsTree
      target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('delete_node', $('#" + nodeId + "'));");
    }
  }

  private class PreviewBehavior extends AbstractDefaultAjaxBehavior {

    @Override
    protected void respond(final AjaxRequestTarget target) {
      final String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      final TreeNode node = elements.get(nodeId);

      if(editingElement) {
        editingConfirmationDialog.setYesButtonCallback(new OnYesCallback() {
          @Override
          public void onYesButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            // TODO hide context menu
            // cancel current editing and reload model
            reloadModel();
            preview(nodeId, node, target);
          }
        });
        editingConfirmationDialog.setNoButtonCallback(new OnNoCallback() {
          @Override
          public void onNoButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            // TODO: hide context menu and reselect edited node
          }
        });
        editingConfirmationDialog.show(target);
      } else {
        preview(nodeId, node, target);
      }
    }
  }

  protected class AddChildBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(final AjaxRequestTarget target) {
      Request request = RequestCycle.get().getRequest();
      final String nodeId = request.getParameter("nodeId");
      String type = request.getParameter("type");
      log.info("Add " + type + " to " + nodeId);

      @SuppressWarnings("unchecked")
      final IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      final Questionnaire questionnaire = questionnaireModel.getObject();
      final TreeNode node = elements.get(nodeId);
      final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
      questionnaireFinder.buildQuestionnaireCache();
      if(node.isHasSection() && "section".equals(type)) {
        editingElement = true;
        SectionPanel sectionPanel = new SectionPanel(getShownComponentId(), new Model<Section>(new Section(null)), questionnaireModel) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Section section) {
            final IHasSection hasSection = node.isSection() ? questionnaireFinder.findSection(node.getName()) : questionnaire;
            hasSection.addSection(section);
            persist(target);
            preview(nodeId, node, target);
            String json = createNode(section);
            target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('create_node', $('#" + nodeId + "'), 'last'," + json + ");");
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            reloadModel();
            preview(nodeId, node, target);
          }
        };
        show(sectionPanel, new StringResourceModel("Section", QuestionnaireTreePanel.this, null), target);

      } else if(node.isSection() && "page".equals(type)) {
        editingElement = true;
        PagePanel pagePanel = new PagePanel(getShownComponentId(), new Model<Page>(new Page(null)), questionnaireModel) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Page page) {
            questionnaireFinder.findSection(node.getName()).addPage(page);
            questionnaire.addPage(page);
            persist(target);
            preview(nodeId, node, target);
            String json = createNode(page);
            target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('create_node', $('#" + nodeId + "'), 'last'," + json + ");");
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            reloadModel();
            preview(nodeId, node, target);
          }
        };
        show(pagePanel, new StringResourceModel("Page", QuestionnaireTreePanel.this, null), target);

      } else if(node.isPage() && "question".equals(type)) {
        editingElement = true;
        EditQuestionPanel questionPanel = new EditQuestionPanel(getShownComponentId(), new Model<Question>(new Question(null)), questionnaireModel) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Question question) {
            questionnaireFinder.findPage(node.getName()).addQuestion(question);
            persist(target);
            preview(nodeId, node, target);
            String json = createNode(question);
            target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('create_node', $('#" + nodeId + "'), 'last'," + json + ");");
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            reloadModel();
            preview(nodeId, node, target);
          }
        };
        show(questionPanel, new StringResourceModel("Question", QuestionnaireTreePanel.this, null), target);
      } else if("variable".equals(type)) {
        editingElement = true;
        @SuppressWarnings({ "unchecked", "rawtypes" })
        VariablePanel variablePanel = new VariablePanel(getShownComponentId(), new Model(null), questionnaireModel) {
          @Override
          public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Variable variable) {
            questionnaire.addVariable(variable);
            persist(target);
            preview(nodeId, node, target);
            // TODO just add node to JSTree and do not reload JSTree and add node to element map
            target.addComponent(tree);
          }

          @Override
          public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            reloadModel();
            preview(nodeId, node, target);
          }
        };
        show(variablePanel, new StringResourceModel("Variable", QuestionnaireTreePanel.this, null), target);
      }
    }
  }

  private String createNode(IQuestionnaireElement element) {
    try {
      StringWriter sw = new StringWriter();
      JsonNode newNode = new JsonNode();
      JsonNodeAttribute attr = new JsonNodeAttribute();
      TreeNode treeNode = new TreeNode(element.getName(), element.getClass());
      attr.setId(addElement(treeNode));
      attr.setClazz("jstree-leaf");
      attr.setRel(ClassUtils.getShortClassName(element.getClass()));
      newNode.setData(getNodeLabel(treeNode));
      newNode.setAttr(attr);
      JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
      new ObjectMapper().writeValue(gen, newNode);
      return sw.toString();
    } catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void preview(final String nodeId, final TreeNode node, AjaxRequestTarget target) {
    editingElement = false;
    @SuppressWarnings("unchecked")
    final IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
    Questionnaire questionnaire = questionnaireModel.getObject();
    final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
    questionnaireFinder.buildQuestionnaireCache();

    if(node.isQuestion()) {
      Question question = questionnaireFinder.findQuestion(node.getName());
      Model<Question> questionModel = new Model<Question>(question);
      QuestionPreviewPanel questionPreviewPanel = new QuestionPreviewPanel(getShownComponentId(), questionModel, questionnaireModel) {

        @Override
        protected void onEdit(@SuppressWarnings("hiding") AjaxRequestTarget target, IModel<Question> model) {
          editQuestion(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      };
      IModel<String> title = new StringResourceModel("ItemPreview", QuestionnaireTreePanel.this, questionModel, new Object[] { new StringResourceModel("Question", QuestionnaireTreePanel.this, null), question.getName() });
      show(questionPreviewPanel, title, target);
    } else if(node.isPage()) {
      Page page = questionnaireFinder.findPage(node.getName());
      Model<Page> pageModel = new Model<Page>(page);
      PagePreviewPanel pagePreviewPanel = new PagePreviewPanel(getShownComponentId(), pageModel, questionnaireModel) {
        @Override
        protected void onEdit(@SuppressWarnings("hiding") AjaxRequestTarget target, IModel<Page> model) {
          editPage(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      };
      IModel<String> title = new StringResourceModel("ItemPreview", QuestionnaireTreePanel.this, pageModel, new Object[] { new StringResourceModel("Page", QuestionnaireTreePanel.this, null), page.getName() });
      show(pagePreviewPanel, title, target);
    } else if(node.isVariable()) {
      @SuppressWarnings({ "unchecked", "rawtypes" })
      IModel<Variable> variableModel = new Model((Serializable) questionnaire.getVariable(node.getName()));
      VariablePreview variablePreview = new VariablePreview(getShownComponentId(), variableModel) {
        @Override
        protected void onEdit(@SuppressWarnings("hiding") AjaxRequestTarget target, IModel<Variable> model) {
          editVariable(nodeId, node, questionnaireModel, target);
        }
      };
      IModel<String> title = new StringResourceModel("ItemPreview", QuestionnaireTreePanel.this, variableModel, new Object[] { new StringResourceModel("Variable", QuestionnaireTreePanel.this, null), node.getName() });
      show(variablePreview, title, target);
    } else {
      // preview not supported
      show(new DefaultRightPanel(getShownComponentId()), new Model<String>(""), target);
    }
  }

  private void editPage(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, QuestionnaireFinder questionnaireFinder, final AjaxRequestTarget target) {
    editingElement = true;
    Page page = questionnaireFinder.findPage(node.getName());
    PagePanel pagePanel = new PagePanel(getShownComponentId(), new Model<Page>(page), questionnaireModel) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") Page page) {
        persist(target);
        node.setName(page.getName());
        // update node name in jsTree
        target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(node) + "');");
        preview(nodeId, node, target);
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    IModel<String> title = new Model<String>(new StringResourceModel("Page", QuestionnaireTreePanel.this, null).getString() + " " + page.getName());
    show(pagePanel, title, target);
  }

  private void editQuestion(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, QuestionnaireFinder questionnaireFinder, final AjaxRequestTarget target) {
    editingElement = true;
    Question question = questionnaireFinder.findQuestion(node.getName());
    EditQuestionPanel questionPanel = new EditQuestionPanel(getShownComponentId(), new Model<Question>(question), questionnaireModel) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") Question question) {
        persist(target);
        node.setName(question.getName());
        QuestionnaireTreePanel.this.setDefaultModelObject(questionnaireModel.getObject());
        // update node name in jsTree
        target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(node) + "');");
        preview(nodeId, node, target);
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    IModel<String> title = new Model<String>(new StringResourceModel("Question", QuestionnaireTreePanel.this, null).getString() + " " + question.getName());
    show(questionPanel, title, target);
  }

  private void editVariable(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, final AjaxRequestTarget target) {
    editingElement = true;
    final Questionnaire questionnaire = questionnaireModel.getObject();
    final Variable variable = questionnaire.getVariable(node.getName());
    @SuppressWarnings({ "unchecked", "rawtypes" })
    VariablePanel variablePanel = new VariablePanel(getShownComponentId(), new Model((Serializable) variable), questionnaireModel) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Variable newVariable) {
        questionnaire.removeVariable(variable);
        questionnaire.addVariable(newVariable);
        persist(target);
        node.setName(newVariable.getName());
        QuestionnaireTreePanel.this.setDefaultModel(questionnaireModel);
        // update node name in jsTree
        target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '[VARIABLE] " + node.getName() + "');");
        preview(nodeId, node, target);
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    IModel<String> title = new Model<String>(new StringResourceModel("Variable", QuestionnaireTreePanel.this, null).getString() + " " + variable.getName());
    show(variablePanel, title, target);
  }

  private void persitQuestionnaire(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject(), localeProperties);
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }

  private String addElement(TreeNode treeNode) {
    String id = ID_PREFIX + elementCounter++;
    elements.put(id, treeNode);
    return id;
  }

  private String getNodeLabel(TreeNode treeNode) {
    return "[" + getShortClassName(treeNode.getClazz()).toUpperCase() + "] " + treeNode.getName();
  }

  private JsonNode populateNode(IQuestionnaireElement element) {

    TreeNode treeNode = new TreeNode(element.getName(), element.getClass());

    JsonNode jsonNode = new JsonNode();
    jsonNode.setData(getNodeLabel(treeNode));
    JsonNodeAttribute nodeAttribute = new JsonNodeAttribute();
    nodeAttribute.setId(addElement(treeNode));
    nodeAttribute.setRel(ClassUtils.getShortClassName(treeNode.getClazz()));
    jsonNode.setAttr(nodeAttribute);

    TreeNodeCollector questionnaireVisitor = new TreeNodeCollector();
    element.accept(questionnaireVisitor);
    if(questionnaireVisitor.getChildren().isEmpty()) {
      nodeAttribute.setClazz("jstree-leaf");
    }
    JsonNode variablesNode = null;
    for(Object child : questionnaireVisitor.getChildren()) {
      if(child instanceof IQuestionnaireElement) {
        jsonNode.getChildren().add(populateNode((IQuestionnaireElement) child));
      } else if(child instanceof Variable) {
        if(variablesNode == null) {
          variablesNode = new JsonNode();
          variablesNode.setData("[VARIABLES]");
          variablesNode.setState("closed");
          JsonNodeAttribute variablesNodeAttribute = new JsonNodeAttribute();
          variablesNodeAttribute.setId(addElement(new TreeNode("", null)));
          variablesNodeAttribute.setRel("Variables");
          variablesNode.setAttr(variablesNodeAttribute);
          jsonNode.getChildren().add(variablesNode);
        }
        Variable variable = (Variable) child;
        JsonNode variableNode = new JsonNode();
        variableNode.setData("[VARIABLE] " + variable.getName());
        JsonNodeAttribute variableNodeAttribute = new JsonNodeAttribute();
        variableNodeAttribute.setId(addElement(new TreeNode(variable.getName(), variable.getClass())));
        variableNodeAttribute.setRel("Variable");
        variableNodeAttribute.setClazz("jstree-leaf");
        variableNode.setAttr(variableNodeAttribute);
        variablesNode.getChildren().add(variableNode);
      }
    }
    return jsonNode;
  }

  private void reloadModel() {
    Questionnaire questionnaire = (Questionnaire) getDefaultModelObject();
    setDefaultModelObject(questionnaireBundleManager.getPersistedBundle(questionnaire.getName()).getQuestionnaire());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private class TreeNodeCollector implements org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor {

    private List children = new ArrayList();

    @Override
    public void visit(Questionnaire questionnaire) {
      children.addAll(questionnaire.getSections());
      children.addAll(questionnaire.getVariables());
    }

    @Override
    public void visit(Section section) {
      children.addAll(section.getSections());
      children.addAll(section.getPages());
    }

    @Override
    public void visit(Page page) {
      children.addAll(page.getQuestions());
    }

    @Override
    public void visit(Question question) {
    }

    @Override
    public void visit(QuestionCategory questionCategory) {
    }

    @Override
    public void visit(Category category) {
    }

    @Override
    public void visit(OpenAnswerDefinition openAnswerDefinition) {
    }

    @Override
    public void visit(Variable variable) {
    }

    public List<?> getChildren() {
      return children;
    }

  }
}
