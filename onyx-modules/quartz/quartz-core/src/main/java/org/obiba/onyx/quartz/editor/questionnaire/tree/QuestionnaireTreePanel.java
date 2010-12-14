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

import static org.apache.commons.lang.StringUtils.trimToNull;

import java.io.Serializable;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.WordUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.QuartzImages;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.page.PagePanel;
import org.obiba.onyx.quartz.editor.page.PagePreviewPanel;
import org.obiba.onyx.quartz.editor.question.CopyQuestionPanel;
import org.obiba.onyx.quartz.editor.question.EditQuestionPanel;
import org.obiba.onyx.quartz.editor.question.QuestionPreviewPanel;
import org.obiba.onyx.quartz.editor.questionnaire.EditionPanel.MenuItem;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePanel;
import org.obiba.onyx.quartz.editor.questionnaire.tree.JsonNode.Data;
import org.obiba.onyx.quartz.editor.questionnaire.tree.JsonNode.JsonNodeAttribute;
import org.obiba.onyx.quartz.editor.questionnaire.tree.TreeNode.NodeType;
import org.obiba.onyx.quartz.editor.questionnaire.utils.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.section.SectionPanel;
import org.obiba.onyx.quartz.editor.variable.VariablePanel;
import org.obiba.onyx.quartz.editor.variable.VariablePreview;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;
import org.obiba.onyx.wicket.Images;
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

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "Need to be be re-initialized upon deserialization")
  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "SE_BAD_FIELD", justification = "Need to be be re-initialized upon deserialization")
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

  private ModalWindow copyQuestionWindow;

  /**
   * Flag that indicates if user is currently editing an element. It allows us to block action in the tree while
   * edition.
   */
  private boolean editingElement = false;

  private JsonNode variablesNode;

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model) {
    super(id, model);
    final Questionnaire questionnaire = model.getObject();

    editingConfirmationDialog = new ConfirmationDialog("editingConfirm");
    editingConfirmationDialog.setContent(new MultiLineLabel(editingConfirmationDialog.getContentId(), new ResourceModel("CancelChanges")));
    add(editingConfirmationDialog);

    copyQuestionWindow = new ModalWindow("copyQuestionWindow");
    copyQuestionWindow.setCssClassName("onyx");
    copyQuestionWindow.setInitialWidth(700);
    copyQuestionWindow.setInitialHeight(200);
    copyQuestionWindow.setResizable(false);
    add(copyQuestionWindow);

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
          rootNode.setState("open");
          if(variablesNode == null) {
            createVariablesNode();
            variablesNode.getAttr().setClazz("jstree-leaf");
            rootNode.getChildren().add(variablesNode);
          }
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

  private String findNodeId(IQuestionnaireElement element) {
    NodeType elementType = NodeType.get(element.getClass());
    for(Entry<String, TreeNode> entry : elements.entrySet()) {
      TreeNode treeNode = entry.getValue();
      if(elementType == treeNode.getNodeType() && treeNode.getName().equals(element.getName())) {
        return entry.getKey();
      }
    }
    return null;
  }

  public abstract void show(Component component, IModel<String> title, ResourceReference icon, List<MenuItem> menuItems, AjaxRequestTarget target);

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
      questionnaire.setQuestionnaireCache(null);
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
        try {
          persitQuestionnaire(target);
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      } else if(node.isPage() && newNode.isSection()) {
        Page page = questionnaireFinder.findPage(node.getName());
        Section newParentSection = questionnaireFinder.findSection(newNode.getName());
        page.getSection().removePage(page);
        newParentSection.addPage(page, Math.min(newParentSection.getPages().size(), position));
        try {
          persitQuestionnaire(target);
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      } else if(node.isQuestion() && newNode.isPage()) {
        Question question = questionnaireFinder.findQuestion(node.getName());
        Page newParentPage = questionnaireFinder.findPage(newNode.getName());
        question.getPage().removeQuestion(question);
        newParentPage.addQuestion(question, Math.min(position, newParentPage.getQuestions().size()));
        try {
          persitQuestionnaire(target);
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }
    }
  }

  protected class EditBehavior extends AbstractDefaultAjaxBehavior {

    @Override
    protected void respond(final AjaxRequestTarget target) {
      final String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");

      // retrieve updated model
      @SuppressWarnings("unchecked")
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      Questionnaire questionnaire = questionnaireModel.getObject();

      TreeNode node = elements.get(nodeId);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
      questionnaire.setQuestionnaireCache(null);
      if(node.isQuestionnaire()) {
        editQuestionnaire(nodeId, node, questionnaire, target);
      } else if(node.isSection()) {
        editSection(nodeId, node, questionnaireModel, questionnaireFinder, target);
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
      deleteElement(RequestCycle.get().getRequest().getParameter("nodeId"), target);
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
            reloadModel();
            preview(nodeId, node, target);
          }
        });
        editingConfirmationDialog.setNoButtonCallback(new OnNoCallback() {
          @Override
          public void onNoButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
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
      questionnaire.setQuestionnaireCache(null);
      if(node.isHasSection() && "section".equals(type)) {
        addSection(nodeId, node, questionnaireModel, questionnaireFinder, target);
      } else if(node.isSection() && "page".equals(type)) {
        addPage(nodeId, node, questionnaireModel, questionnaireFinder, target);
      } else if(node.isPage() && "question".equals(type)) {
        addQuestion(nodeId, node, questionnaireModel, questionnaireFinder, target);
      } else if("variable".equals(type)) {
        addVariable(nodeId, node, questionnaireModel, target);
      }
    }
  }

  private JsonNode createNode(IQuestionnaireElement element, NodeType nodeType) {
    String type = WordUtils.capitalizeFully(nodeType.name());
    TreeNode treeNode = new TreeNode(element.getName(), nodeType);
    JsonNodeAttribute attr = new JsonNodeAttribute(addElement(treeNode), type, type + " " + element.getName());
    attr.setClazz("jstree-leaf");
    JsonNode newNode = new JsonNode();
    newNode.setData(new Data(treeNode.getName(), RequestCycle.get().urlFor(treeNode.getNodeType().getIcon()).toString()));
    newNode.setAttr(attr);
    return newNode;
  }

  private void preview(final String nodeId, final TreeNode node, AjaxRequestTarget target) {
    editingElement = false;
    @SuppressWarnings("unchecked")
    final IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
    final Questionnaire questionnaire = questionnaireModel.getObject();
    final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
    questionnaire.setQuestionnaireCache(null);
    if(node.isQuestionnaire()) {

      List<MenuItem> menuItems = new ArrayList<MenuItem>();
      menuItems.add(new MenuItem(new StringResourceModel("Edit", QuestionnaireTreePanel.this, null), Images.EDIT) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          editQuestionnaire(nodeId, node, questionnaire, target);
        }
      });
      menuItems.add(new MenuItem(new StringResourceModel("Add.Section", QuestionnaireTreePanel.this, null), QuartzImages.SECTION_ADD) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          addSection(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      });
      IModel<String> title = new StringResourceModel("ItemPreview", QuestionnaireTreePanel.this, questionnaireModel, new Object[] { new StringResourceModel("Questionnaire", QuestionnaireTreePanel.this, null), questionnaire.getName() });
      show(new Label(getShownComponentId(), new StringResourceModel("ItemPreview.NotSupported", QuestionnaireTreePanel.this, null)), title, QuartzImages.QUESTIONNAIRE, menuItems, target);

    } else if(node.isSection()) {

      List<MenuItem> menuItems = new ArrayList<MenuItem>();
      menuItems.add(new MenuItem(new StringResourceModel("Edit", QuestionnaireTreePanel.this, null), Images.EDIT) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          editSection(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      });
      menuItems.add(new MenuItem(new StringResourceModel("Add.Section", QuestionnaireTreePanel.this, null), QuartzImages.SECTION_ADD) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          addSection(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      });
      menuItems.add(new MenuItem(new StringResourceModel("Add.Page", QuestionnaireTreePanel.this, null), QuartzImages.PAGE_ADD) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          addPage(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      });
      menuItems.add(new MenuItem(new StringResourceModel("Delete", QuestionnaireTreePanel.this, null), Images.DELETE) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          deleteElement(nodeId, target);
        }
      });
      IModel<String> title = new StringResourceModel("ItemPreview", QuestionnaireTreePanel.this, new Model<Section>(questionnaireFinder.findSection(node.getName())), new Object[] { new StringResourceModel("Section", QuestionnaireTreePanel.this, null), node.getName() });
      show(new Label(getShownComponentId(), new StringResourceModel("ItemPreview.NotSupported", QuestionnaireTreePanel.this, null)), title, QuartzImages.SECTION, menuItems, target);

    } else if(node.isPage()) {

      Page page = questionnaireFinder.findPage(node.getName());
      Model<Page> pageModel = new Model<Page>(page);
      PagePreviewPanel pagePreviewPanel = new PagePreviewPanel(getShownComponentId(), pageModel, questionnaireModel);
      IModel<String> title = new StringResourceModel("ItemPreview", QuestionnaireTreePanel.this, pageModel, new Object[] { new StringResourceModel("Page", QuestionnaireTreePanel.this, null), page.getName() });
      List<MenuItem> menuItems = new ArrayList<MenuItem>();
      menuItems.add(new MenuItem(new StringResourceModel("Edit", QuestionnaireTreePanel.this, null), Images.EDIT) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          editPage(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      });
      menuItems.add(new MenuItem(new StringResourceModel("Add.Question", QuestionnaireTreePanel.this, null), QuartzImages.QUESTION_ADD) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          addQuestion(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      });
      menuItems.add(new MenuItem(new StringResourceModel("Delete", QuestionnaireTreePanel.this, null), Images.DELETE) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          deleteElement(nodeId, target);
        }
      });
      show(pagePreviewPanel, title, QuartzImages.PAGE, menuItems, target);

    } else if(node.isQuestion()) {

      final Question question = questionnaireFinder.findQuestion(node.getName());
      final Model<Question> questionModel = new Model<Question>(question);
      QuestionPreviewPanel questionPreviewPanel = new QuestionPreviewPanel(getShownComponentId(), questionModel, questionnaireModel);
      IModel<String> title = new StringResourceModel("ItemPreview", QuestionnaireTreePanel.this, questionModel, new Object[] { new StringResourceModel("Question", QuestionnaireTreePanel.this, null), question.getName() });
      List<MenuItem> menuItems = new ArrayList<MenuItem>();
      menuItems.add(new MenuItem(new StringResourceModel("Edit", QuestionnaireTreePanel.this, null), Images.EDIT) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          editQuestion(nodeId, node, questionnaireModel, questionnaireFinder, target);
        }
      });
      menuItems.add(new MenuItem(new StringResourceModel("Copy", QuestionnaireTreePanel.this, null), Images.COPY) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          copyQuestionWindow.setTitle(new StringResourceModel("Question.Copy", QuestionnaireTreePanel.this, null, new Object[] { question.getName() }));
          CopyQuestionPanel copyQuestionPanel = new CopyQuestionPanel("content", questionModel, questionnaireModel, copyQuestionWindow) {
            @Override
            protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Question newQuestion) {
              // add question to tree
              JsonNode jsonNode = createNode(newQuestion, NodeType.QUESTION);
              target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('create_node', $('#" + findNodeId(newQuestion.getPage()) + "'), 'last'," + jsonNode.toString() + ");");
              target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('select_node', $('#" + jsonNode.getAttr().getId() + "'), true);");
            }
          };
          copyQuestionWindow.setContent(copyQuestionPanel);
          copyQuestionWindow.show(target);
        }
      });

      if(question.getType() == QuestionType.BOILER_PLATE) {
        menuItems.add(new MenuItem(new StringResourceModel("Add.Question", QuestionnaireTreePanel.this, null), QuartzImages.QUESTION_ADD) {
          @Override
          public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            addQuestion(nodeId, node, questionnaireModel, questionnaireFinder, target, QuestionType.BOILER_PLATE);
          }
        });
      }
      menuItems.add(new MenuItem(new StringResourceModel("Delete", QuestionnaireTreePanel.this, null), Images.DELETE) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          deleteElement(nodeId, target);
        }
      });
      show(questionPreviewPanel, title, QuartzImages.QUESTION, menuItems, target);

    } else if(node.isVariables()) {

      List<MenuItem> menuItems = new ArrayList<MenuItem>();
      menuItems.add(new MenuItem(new StringResourceModel("Add.Variable", QuestionnaireTreePanel.this, null), QuartzImages.VARIABLE_ADD) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          addVariable(nodeId, node, questionnaireModel, target);
        }
      });
      show(new Label(getShownComponentId(), new StringResourceModel("ItemPreview.NotSupported", QuestionnaireTreePanel.this, null)), new Model<String>("Variables"), QuartzImages.VARIABLES, menuItems, target);

    } else if(node.isVariable()) {

      @SuppressWarnings({ "unchecked", "rawtypes" })
      IModel<Variable> variableModel = new Model((Serializable) questionnaire.getVariable(node.getName()));
      VariablePreview variablePreview = new VariablePreview(getShownComponentId(), variableModel);
      IModel<String> title = new StringResourceModel("ItemPreview", QuestionnaireTreePanel.this, variableModel, new Object[] { new StringResourceModel("Variable", QuestionnaireTreePanel.this, null), node.getName() });
      List<MenuItem> menuItems = new ArrayList<MenuItem>();
      menuItems.add(new MenuItem(new StringResourceModel("Edit", QuestionnaireTreePanel.this, null), Images.EDIT) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          editVariable(nodeId, node, questionnaireModel, target);
        }
      });
      menuItems.add(new MenuItem(new StringResourceModel("Delete", QuestionnaireTreePanel.this, null), Images.DELETE) {
        @Override
        public void onClick(@SuppressWarnings("hiding") AjaxRequestTarget target) {
          deleteElement(nodeId, target);
        }
      });
      show(variablePreview, title, QuartzImages.VARIABLE, menuItems, target);

    }
  }

  private void editQuestionnaire(final String nodeId, final TreeNode node, final Questionnaire questionnaire, final AjaxRequestTarget target) {
    editingElement = true;
    QuestionnairePanel questionnairePanel = new QuestionnairePanel(getShownComponentId(), new Model<Questionnaire>(questionnaire), false) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") Questionnaire questionnaire) {
        // TOD can't do this for the moment
        // QuestionnaireFinder.getInstance(questionnaire).buildQuestionnaireCache();
        // for(Page page : questionnaire.getQuestionnaireCache().getPageCache().values()) {
        // if(Questionnaire.SIMPLIFIED_UI.equals(questionnaire.getUiType())) {
        // page.setUIFactoryName(new SimplifiedPageLayoutFactory().getBeanName());
        // } else {
        // page.setUIFactoryName(new DefaultPageLayoutFactory().getBeanName());
        // }
        // }
        // for(Question question : questionnaire.getQuestionnaireCache().getQuestionCache().values()) {
        // if(Questionnaire.SIMPLIFIED_UI.equals(questionnaire.getUiType())) {
        // question.setUIFactoryName(new SimplifiedQuestionPanelFactory().getBeanName());
        // } else {
        // question.setUIFactoryName(new DefaultQuestionPanelFactory().getBeanName());
        // }
        // }
        node.setName(questionnaire.getName());
        // update node name in jsTree
        target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + node.getName() + "');");
        preview(nodeId, node, target);
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    IModel<String> title = new Model<String>(new StringResourceModel("Questionnaire", QuestionnaireTreePanel.this, null).getString() + " " + questionnaire.getName());
    show(questionnairePanel, title, QuartzImages.QUESTIONNAIRE, null, target);
  }

  private void editSection(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, final QuestionnaireFinder questionnaireFinder, final AjaxRequestTarget target) {
    editingElement = true;
    Section section = questionnaireFinder.findSection(node.getName());
    SectionPanel sectionPanel = new SectionPanel(getShownComponentId(), new Model<Section>(section), questionnaireModel) {
      @Override
      protected void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") Section section) {
        try {
          persist(target);
          node.setName(section.getName());
          // update node name in jsTree
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + node.getName() + "');");
          preview(nodeId, node, target);
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }

      @Override
      protected void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    IModel<String> title = new Model<String>(new StringResourceModel("Section", QuestionnaireTreePanel.this, null).getString() + " " + section.getName());
    show(sectionPanel, title, QuartzImages.SECTION, null, target);
  }

  private void addSection(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, final QuestionnaireFinder questionnaireFinder, final AjaxRequestTarget target) {
    editingElement = true;
    SectionPanel sectionPanel = new SectionPanel(getShownComponentId(), new Model<Section>(new Section(null)), questionnaireModel) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Section section) {
        final IHasSection hasSection = node.isSection() ? questionnaireFinder.findSection(node.getName()) : questionnaireModel.getObject();
        hasSection.addSection(section);
        try {
          persist(target);
          editingElement = false;
          JsonNode jsonNode = createNode(section, NodeType.SECTION);
          String position = hasSection instanceof Questionnaire ? String.valueOf(((Questionnaire) hasSection).getSections().size() - 1) : "last";
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('create_node', $('#" + nodeId + "'), '" + position + "'," + jsonNode.toString() + ");");
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('select_node', $('#" + jsonNode.getAttr().getId() + "'), true);");
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    show(sectionPanel, new StringResourceModel("Section", QuestionnaireTreePanel.this, null), QuartzImages.SECTION, null, target);
  }

  private void editPage(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, QuestionnaireFinder questionnaireFinder, final AjaxRequestTarget target) {
    editingElement = true;
    Page page = questionnaireFinder.findPage(node.getName());
    PagePanel pagePanel = new PagePanel(getShownComponentId(), new Model<Page>(page), questionnaireModel) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") Page page) {
        try {
          persist(target);
          node.setName(page.getName());
          // update node name in jsTree
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + node.getName() + "');");
          preview(nodeId, node, target);
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    IModel<String> title = new Model<String>(new StringResourceModel("Page", QuestionnaireTreePanel.this, null).getString() + " " + page.getName());
    show(pagePanel, title, QuartzImages.PAGE, null, target);
  }

  private void addPage(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, final QuestionnaireFinder questionnaireFinder, final AjaxRequestTarget target) {
    editingElement = true;
    PagePanel pagePanel = new PagePanel(getShownComponentId(), new Model<Page>(new Page(null)), questionnaireModel) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Page page) {
        questionnaireFinder.findSection(node.getName()).addPage(page);
        questionnaireModel.getObject().addPage(page);
        try {
          persist(target);
          editingElement = false;
          JsonNode jsonNode = createNode(page, NodeType.PAGE);
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('create_node', $('#" + nodeId + "'), 'last'," + jsonNode.toString() + ");");
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('select_node', $('#" + jsonNode.getAttr().getId() + "'), true);");
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    show(pagePanel, new StringResourceModel("Page", QuestionnaireTreePanel.this, null), QuartzImages.PAGE, null, target);
  }

  private void editQuestion(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, QuestionnaireFinder questionnaireFinder, final AjaxRequestTarget target) {
    editingElement = true;
    Question question = questionnaireFinder.findQuestion(node.getName());
    EditQuestionPanel questionPanel = new EditQuestionPanel(getShownComponentId(), new Model<Question>(question), questionnaireModel) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, @SuppressWarnings("hiding") Question question) {
        try {
          persist(target);
          node.setName(question.getName());
          QuestionnaireTreePanel.this.setDefaultModelObject(questionnaireModel.getObject());
          // update node name in jsTree
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + node.getName() + "');");
          preview(nodeId, node, target);
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    IModel<String> title = new Model<String>(new StringResourceModel("Question", QuestionnaireTreePanel.this, null).getString() + " " + question.getName());
    show(questionPanel, title, QuartzImages.QUESTION, null, target);
  }

  private void addQuestion(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, final QuestionnaireFinder questionnaireFinder, final AjaxRequestTarget target, final QuestionType... forceAllowedType) {
    editingElement = true;
    EditQuestionPanel questionPanel = new EditQuestionPanel(getShownComponentId(), new Model<Question>(new Question(null)), questionnaireModel, forceAllowedType) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Question question) {
        questionnaireFinder.getQuestionnaire().setQuestionnaireCache(null);
        if(forceAllowedType.length == 0) {
          questionnaireFinder.findPage(node.getName()).addQuestion(question);
        } else {
          questionnaireFinder.findQuestion(node.getName()).addQuestion(question);
        }
        try {
          persist(target);
          editingElement = false;
          JsonNode jsonNode = createNode(question, NodeType.QUESTION);
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('create_node', $('#" + nodeId + "'), 'last'," + jsonNode.toString() + ");");
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('select_node', $('#" + jsonNode.getAttr().getId() + "'), true);");
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    show(questionPanel, new StringResourceModel("Question", QuestionnaireTreePanel.this, null), QuartzImages.QUESTION, null, target);
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
        try {
          persist(target);
          node.setName(newVariable.getName());
          QuestionnaireTreePanel.this.setDefaultModel(questionnaireModel);
          // update node name in jsTree
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + node.getName() + "');");
          preview(nodeId, node, target);
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    IModel<String> title = new Model<String>(new StringResourceModel("Variable", QuestionnaireTreePanel.this, null).getString() + " " + variable.getName());
    show(variablePanel, title, QuartzImages.VARIABLE, null, target);
  }

  private void addVariable(final String nodeId, final TreeNode node, final IModel<Questionnaire> questionnaireModel, final AjaxRequestTarget target) {
    editingElement = true;
    @SuppressWarnings({ "unchecked", "rawtypes" })
    VariablePanel variablePanel = new VariablePanel(getShownComponentId(), new Model(null), questionnaireModel) {
      @Override
      public void onSave(@SuppressWarnings("hiding") AjaxRequestTarget target, Variable variable) {
        questionnaireModel.getObject().addVariable(variable);
        try {
          persist(target);
          editingElement = false;

          TreeNode treeNode = new TreeNode(variable.getName(), NodeType.VARIABLE);
          JsonNodeAttribute attr = new JsonNodeAttribute(addElement(treeNode), "Variable", "Variable " + variable.getName());
          attr.setClazz("jstree-leaf");

          JsonNode newNode = new JsonNode();
          newNode.setData(new Data(treeNode.getName(), RequestCycle.get().urlFor(treeNode.getNodeType().getIcon()).toString()));
          newNode.setAttr(attr);

          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('create_node', $('#" + nodeId + "'), 'last'," + newNode.toString() + ");");
          target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('select_node', $('#" + newNode.getAttr().getId() + "'), true);");
        } catch(Exception e) {
          log.error("Cannot persist questionnaire", e);
        }
      }

      @Override
      public void onCancel(@SuppressWarnings("hiding") AjaxRequestTarget target) {
        reloadModel();
        preview(nodeId, node, target);
      }
    };
    show(variablePanel, new StringResourceModel("Variable", QuestionnaireTreePanel.this, null), QuartzImages.VARIABLE, null, target);
  }

  private void deleteElement(String nodeId, AjaxRequestTarget target) {
    @SuppressWarnings("unchecked")
    final Questionnaire questionnaire = ((IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel()).getObject();
    TreeNode node = elements.get(nodeId);
    QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
    questionnaire.setQuestionnaireCache(null);
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
    try {
      persitQuestionnaire(target);
      elements.remove(nodeId);
      // remove node from jsTree
      target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('delete_node', $('#" + nodeId + "'));");
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
    }
  }

  private void persitQuestionnaire(AjaxRequestTarget target) throws Exception {
    try {
      questionnairePersistenceUtils.persist((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject(), localeProperties);
    } catch(Exception e) {
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
      throw e;
    }
  }

  private String addElement(TreeNode treeNode) {
    String id = ID_PREFIX + elementCounter++;
    elements.put(id, treeNode);
    return id;
  }

  private JsonNode populateNode(IQuestionnaireElement element) {

    TreeNode treeNode = new TreeNode(element.getName(), NodeType.get(element.getClass()));

    JsonNode jsonNode = new JsonNode();
    jsonNode.setData(new Data(treeNode.getName(), RequestCycle.get().urlFor(treeNode.getNodeType().getIcon()).toString()));
    String type = WordUtils.capitalizeFully(treeNode.getNodeType().name());
    JsonNodeAttribute nodeAttribute = new JsonNodeAttribute(addElement(treeNode), type, type + " " + treeNode.getName());
    jsonNode.setAttr(nodeAttribute);

    TreeNodeCollector questionnaireVisitor = new TreeNodeCollector();
    element.accept(questionnaireVisitor);
    if(questionnaireVisitor.getChildren().isEmpty()) {
      nodeAttribute.setClazz("jstree-leaf");
    }
    for(Object child : questionnaireVisitor.getChildren()) {
      if(child instanceof IQuestionnaireElement) {
        jsonNode.getChildren().add(populateNode((IQuestionnaireElement) child));
      } else if(child instanceof Variable) {
        if(variablesNode == null) {
          createVariablesNode();
          jsonNode.getChildren().add(variablesNode);
        }
        Variable variable = (Variable) child;
        JsonNode variableNode = new JsonNode();
        variableNode.setData(new Data(variable.getName(), RequestCycle.get().urlFor(NodeType.VARIABLE.getIcon()).toString()));
        JsonNodeAttribute variableNodeAttribute = new JsonNodeAttribute(addElement(new TreeNode(variable.getName(), NodeType.VARIABLE)), "Variable", "Variable " + variable.getName());
        variableNodeAttribute.setClazz("jstree-leaf");
        variableNode.setAttr(variableNodeAttribute);
        variablesNode.getChildren().add(variableNode);
      }
    }
    return jsonNode;
  }

  private void createVariablesNode() {
    variablesNode = new JsonNode();
    variablesNode.setData(new Data(NodeType.VARIABLES.name(), RequestCycle.get().urlFor(NodeType.VARIABLES.getIcon()).toString()));
    variablesNode.setState("closed");
    variablesNode.setAttr(new JsonNodeAttribute(addElement(new TreeNode("", NodeType.VARIABLES)), "Variables", "Variables"));
  }

  private void reloadModel() {
    Questionnaire questionnaire = (Questionnaire) getDefaultModelObject();
    setDefaultModelObject(questionnaireBundleManager.getPersistedBundle(questionnaire.getName()).getQuestionnaire());
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static class TreeNodeCollector implements org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor {

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
      if(question.getType() == QuestionType.BOILER_PLATE) {
        children.addAll(question.getQuestions());
      }
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
