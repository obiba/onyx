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

import static org.apache.commons.lang.ClassUtils.getShortClassName;
import static org.apache.commons.lang.StringUtils.trimToNull;

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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasQuestion;
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
import org.obiba.onyx.quartz.editor.questionnaire.Node.NodeAttribute;
import org.obiba.onyx.quartz.editor.section.SectionPanel;
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

  /**
   * Mapping of HTML id and QuestionnaireElement. <br>
   * These IQuestionnaireElement are not the same instances as the QuestionnaireTreePanel.getModelObject() because this
   * model is reloaded on each save/cancel but this map is populated only on the JStree load.
   */
  private final Map<String, IQuestionnaireElement> elements = new HashMap<String, IQuestionnaireElement>();

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
          String elementId = requestCycle.getRequest().getParameter("id");
          IQuestionnaireElement element = elements.get(elementId);
          StringWriter sw = new StringWriter();
          Node rootNode = populateNode(element == null ? (IQuestionnaireElement) QuestionnaireTreePanel.this.getDefaultModelObject() : element);
          JsonGenerator gen = new JsonFactory().createJsonGenerator(sw);
          new ObjectMapper().writeValue(gen, element != null ? rootNode.getChildren() : rootNode);
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

      IQuestionnaireElement element = elements.get(nodeId);
      IQuestionnaireElement newParent = elements.get(newParentId);

      @SuppressWarnings("unchecked")
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      final Questionnaire questionnaire = questionnaireModel.getObject();
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof Section && newParent instanceof IHasSection) {
        Section section = questionnaireFinder.findSection(element.getName());
        final IHasSection newHasSection;
        if(newParent instanceof Section) {
          newHasSection = questionnaireFinder.findSection(newParent.getName());
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
      } else if(element instanceof Page && newParent instanceof Section) {
        Page page = questionnaireFinder.findPage(element.getName());
        Section newParentSection = questionnaireFinder.findSection(newParent.getName());
        page.getSection().removePage(page);
        newParentSection.addPage(page, Math.min(newParentSection.getPages().size(), position));
        persitQuestionnaire(target);
      } else if(element instanceof Question && newParent instanceof IHasQuestion) {
        Question question = questionnaireFinder.findQuestion(element.getName());
        Page newParentPage = questionnaireFinder.findPage(newParent.getName());
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

      final IQuestionnaireElement element = elements.get(nodeId);
      final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
      editingElement = true;
      if(element instanceof Questionnaire) {
        QuestionnairePanel questionnairePanel = new QuestionnairePanel(getShownComponentId(), new Model<Questionnaire>(questionnaire), false) {
          @Override
          public void onSave(AjaxRequestTarget target1, @SuppressWarnings("hiding") Questionnaire questionnaire) {
            persist(target1);
            elements.put(nodeId, questionnaire);
            // update node name in jsTree
            target1.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(questionnaire) + "');");
            hidePanel(target1);
          }

          @Override
          public void onCancel(AjaxRequestTarget target1) {
            reloadModel();
            hidePanel(target1);
          }
        };
        show(questionnairePanel, new StringResourceModel("Questionnaire", QuestionnaireTreePanel.this, null), target);
      }
      if(element instanceof Section) {
        Section section = questionnaireFinder.findSection(element.getName());
        SectionPanel sectionPanel = new SectionPanel(getShownComponentId(), new Model<Section>(section), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, @SuppressWarnings("hiding") Section section) {
            persist(target1);
            elements.put(nodeId, section);
            // update node name in jsTree
            target1.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(section) + "');");
            hidePanel(target1);
          }

          @Override
          public void onCancel(AjaxRequestTarget target1) {
            reloadModel();
            hidePanel(target1);
          }
        };
        show(sectionPanel, new StringResourceModel("Section", QuestionnaireTreePanel.this, null), target);
      } else if(element instanceof Page) {
        Page page = questionnaireFinder.findPage(element.getName());
        PagePanel pagePanel = new PagePanel(getShownComponentId(), new Model<Page>(page), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, @SuppressWarnings("hiding") Page page) {
            persist(target1);
            elements.put(nodeId, page);
            // update node name in jsTree
            target1.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(page) + "');");

            // show preview
            show(new PagePreviewPanel(getShownComponentId(), new Model<Page>(page), questionnaireModel), new StringResourceModel("Preview", QuestionnaireTreePanel.this, null, page.getName()), target1);
            editingElement = false;
          }

          @Override
          public void onCancel(AjaxRequestTarget target1) {
            reloadModel();
            hidePanel(target1);
          }
        };
        show(pagePanel, new StringResourceModel("Page", QuestionnaireTreePanel.this, null), target);
      } else if(element instanceof Question) {
        Question question = questionnaireFinder.findQuestion(element.getName());
        EditQuestionPanel questionPanel = new EditQuestionPanel(getShownComponentId(), new Model<Question>(question), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, @SuppressWarnings("hiding") Question question) {
            persist(target1);
            elements.put(nodeId, question);
            QuestionnaireTreePanel.this.setDefaultModelObject(questionnaire);
            // update node name in jsTree
            target1.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(question) + "');");

            // show preview
            show(new QuestionPreviewPanel(getShownComponentId(), new Model<Question>(question), questionnaireModel), new StringResourceModel("Preview", QuestionnaireTreePanel.this, null, question.getName()), target1);
            editingElement = false;
          }

          @Override
          public void onCancel(AjaxRequestTarget target1) {
            reloadModel();
            hidePanel(target1);
          }
        };
        show(questionPanel, new StringResourceModel("Question", QuestionnaireTreePanel.this, null), target);
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
      IQuestionnaireElement element = elements.get(nodeId);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof Section) {
        Section section = questionnaireFinder.findSection(element.getName());
        Section parentSection = section.getParentSection();
        if(parentSection == null) {
          questionnaire.removeSection(section);
        } else {
          parentSection.removeSection(section);
        }
      } else if(element instanceof Page) {
        Page page = questionnaireFinder.findPage(element.getName());
        page.getSection().removePage(page);
      } else if(element instanceof Question) {
        Question question = questionnaireFinder.findQuestion(element.getName());
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
      final IQuestionnaireElement element = elements.get(nodeId);

      // TODO hide context menu
      if(editingElement) {
        editingConfirmationDialog.setYesButtonCallback(new OnYesCallback() {
          @Override
          public void onYesButtonClicked(@SuppressWarnings("hiding") AjaxRequestTarget target) {
            // cancel current editing and reload model
            reloadModel();
            preview(target, element);
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
        preview(target, element);
      }
    }

    private void preview(final AjaxRequestTarget target, IQuestionnaireElement element) {
      editingElement = false;
      @SuppressWarnings("unchecked")
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
      if(element instanceof Question) {
        Question question = questionnaireFinder.findQuestion(element.getName());
        QuestionPreviewPanel questionPreviewPanel = new QuestionPreviewPanel(getShownComponentId(), new Model<Question>(question), questionnaireModel);
        show(questionPreviewPanel, new StringResourceModel("Preview", QuestionnaireTreePanel.this, null, question.getName()), target);
      } else if(element instanceof Page) {
        Page page = questionnaireFinder.findPage(element.getName());
        PagePreviewPanel pagePreviewPanel = new PagePreviewPanel(getShownComponentId(), new Model<Page>(page), questionnaireModel);
        show(pagePreviewPanel, new StringResourceModel("Preview", QuestionnaireTreePanel.this, null, page.getName()), target);
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
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      final Questionnaire questionnaire = questionnaireModel.getObject();
      final IQuestionnaireElement element = elements.get(nodeId);
      final QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      editingElement = true;
      if(element instanceof IHasSection && "section".equals(type)) {
        SectionPanel sectionPanel = new SectionPanel(getShownComponentId(), new Model<Section>(new Section(null)), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Section section) {
            final IHasSection hasSection = element instanceof Section ? questionnaireFinder.findSection(element.getName()) : questionnaire;
            hasSection.addSection(section);
            persist(target1);
            hidePanel(target1);
            // TODO just add node to JSTree and do not reload JSTree and add node to element map
            target1.addComponent(tree);
          }

          @Override
          public void onCancel(AjaxRequestTarget target1) {
            reloadModel();
            hidePanel(target1);
          }
        };
        show(sectionPanel, new StringResourceModel("Section", QuestionnaireTreePanel.this, null), target);

      } else if(element instanceof Section && "page".equals(type)) {
        PagePanel pagePanel = new PagePanel(getShownComponentId(), new Model<Page>(new Page(null)), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Page editedPage) {
            questionnaireFinder.findSection(element.getName()).addPage(editedPage);
            questionnaire.addPage(editedPage);
            persist(target1);
            hidePanel(target1);
            // TODO just add node to JSTree and do not reload JSTree and add node to element map
            target1.addComponent(tree);
          }

          @Override
          public void onCancel(AjaxRequestTarget target1) {
            reloadModel();
            hidePanel(target1);
          }
        };
        show(pagePanel, new StringResourceModel("Page", QuestionnaireTreePanel.this, null), target);

      } else if(element instanceof Page && "question".equals(type)) {
        EditQuestionPanel questionPanel = new EditQuestionPanel(getShownComponentId(), new Model<Question>(new Question(null)), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Question question) {
            questionnaireFinder.findPage(element.getName()).addQuestion(question);
            persist(target1);
            hidePanel(target1);
            // TODO just add node to JSTree and do not reload JSTree and add node to element map
            target1.addComponent(tree);
          }

          @Override
          public void onCancel(AjaxRequestTarget target1) {
            reloadModel();
            hidePanel(target1);
          }
        };
        show(questionPanel, new StringResourceModel("Question", QuestionnaireTreePanel.this, null), target);
      }
    }
  }

  private void hidePanel(AjaxRequestTarget target1) {
    show(new WebMarkupContainer(getShownComponentId()), new Model<String>(""), target1);
    editingElement = false;
  }

  protected void persitQuestionnaire(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject(), localeProperties);
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }

  protected String addElement(IQuestionnaireElement element) {
    String id = ID_PREFIX + elementCounter++;
    elements.put(id, element);
    return id;
  }

  private String getNodeLabel(IQuestionnaireElement element) {
    return "[" + getShortClassName(element.getClass()).toUpperCase() + "] " + element.getName();
  }

  private Node populateNode(IQuestionnaireElement element) {
    Node node = new Node();
    node.setData(getNodeLabel(element));
    NodeAttribute nodeAttribute = new NodeAttribute();
    nodeAttribute.setId(addElement(element));
    nodeAttribute.setRel(ClassUtils.getShortClassName(element.getClass()));

    node.setAttr(nodeAttribute);

    TreeNodeCollector questionnaireVisitor = new TreeNodeCollector(new ArrayList<IQuestionnaireElement>());
    element.accept(questionnaireVisitor);
    if(questionnaireVisitor.getChildren().isEmpty()) {
      nodeAttribute.setClazz("jstree-leaf");
    }
    for(IQuestionnaireElement child : questionnaireVisitor.getChildren()) {
      node.getChildren().add(populateNode(child));
    }
    return node;
  }

  private void reloadModel() {
    Questionnaire questionnaire = (Questionnaire) getDefaultModelObject();
    setDefaultModelObject(questionnaireBundleManager.getPersistedBundle(questionnaire.getName()).getQuestionnaire());
  }

  private class TreeNodeCollector implements org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor {

    private List<IQuestionnaireElement> children;

    public TreeNodeCollector(List<IQuestionnaireElement> children) {
      this.children = children;
    }

    @Override
    public void visit(Questionnaire questionnaire) {
      children.addAll(questionnaire.getSections());
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

    public List<IQuestionnaireElement> getChildren() {
      return children;
    }

    @Override
    public void visit(Variable variable) {
    }

  }
}
