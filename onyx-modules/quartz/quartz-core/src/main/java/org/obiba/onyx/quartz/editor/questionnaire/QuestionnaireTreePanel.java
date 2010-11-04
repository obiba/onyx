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
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.page.PagePanel;
import org.obiba.onyx.quartz.editor.question.EditQuestionPanel;
import org.obiba.onyx.quartz.editor.questionnaire.Node.NodeAttribute;
import org.obiba.onyx.quartz.editor.section.SectionPanel;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public abstract class QuestionnaireTreePanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  private static final String ID_PREFIX = "element_";

  @SpringBean
  private transient QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private transient LocalePropertiesUtils localePropertiesUtils;

  @SpringBean
  private transient QuestionnaireBundleManager questionnaireBundleManager;

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

  private final WebMarkupContainer tree;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final LocaleProperties localeProperties;

  private final EditionPanel editionPanel;

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model, EditionPanel editionPanel) {
    super(id, model);
    this.editionPanel = editionPanel;

    final Questionnaire questionnaire = model.getObject();
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
  }

  protected class MoveBehavior extends AbstractDefaultAjaxBehavior {

    @Override
    protected void respond(AjaxRequestTarget target) {
      Request request = RequestCycle.get().getRequest();
      String nodeId = request.getParameter("nodeId");
      String previousParentId = trimToNull(request.getParameter("previousParentId"));
      String newParentId = trimToNull(request.getParameter("newParentId"));
      int position = Integer.parseInt(request.getParameter("newPosition"));
      boolean sameParent = StringUtils.equals(previousParentId, newParentId);

      IQuestionnaireElement element = elements.get(nodeId);
      IQuestionnaireElement newParent = elements.get(newParentId);

      @SuppressWarnings("unchecked")
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      final Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(questionnaireModel.getObject().getName()).getQuestionnaire();
      questionnaireModel.setObject(questionnaire);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof Section && newParent instanceof IHasSection) {
        Section updatedElement = questionnaireFinder.findSection(element.getName());

        final IHasSection updatedNewParent;
        if(newParent instanceof Section) {
          updatedNewParent = questionnaireFinder.findSection(newParent.getName());
        } else {
          updatedNewParent = ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject());
        }

        if(!sameParent) {
          for(Section existing : updatedNewParent.getSections()) {
            if(existing.getName().equalsIgnoreCase(updatedElement.getName())) {
              error(new StringResourceModel("Section.AlreadyExistForParent", QuestionnaireTreePanel.this, null).getObject());
              feedbackWindow.setContent(feedbackPanel);
              feedbackWindow.show(target);
              break;
            }
          }
        }
        if(!hasErrorMessage()) {
          if(updatedElement.getParentSection() == null) {
            questionnaire.removeSection(updatedElement);
          } else {
            updatedElement.getParentSection().removeSection(updatedElement);
          }
          if(updatedNewParent instanceof Questionnaire) {
            updatedElement.setParentSection(null);
          }
          updatedNewParent.addSection(updatedElement, Math.min(updatedNewParent.getSections().size(), position));
          persitQuestionnaire(target);
        }
      } else if(element instanceof Page && newParent instanceof Section) {
        Page updatedElement = questionnaireFinder.findPage(element.getName());
        Section updatedNewParent = questionnaireFinder.findSection(newParent.getName());
        if(!sameParent) {
          for(Page existing : updatedNewParent.getPages()) {
            if(existing.getName().equalsIgnoreCase(updatedElement.getName())) {
              error(new StringResourceModel("Page.AlreadyExistForParent", QuestionnaireTreePanel.this, null).getObject());
              feedbackWindow.setContent(feedbackPanel);
              feedbackWindow.show(target);
              break;
            }
          }
        }
        if(!hasErrorMessage()) {
          if(updatedElement.getSection() == null) {
            questionnaire.removePage(updatedElement);
          } else {
            updatedElement.getSection().removePage(updatedElement);
          }
          updatedNewParent.addPage(updatedElement, Math.min(updatedNewParent.getPages().size(), position));
          persitQuestionnaire(target);
        }
      } else if(element instanceof Question && newParent instanceof IHasQuestion) {
        Question updatedElement = questionnaireFinder.findQuestion(element.getName());

        IHasQuestion updatedNewParent;
        if(newParent instanceof Page) {
          updatedNewParent = questionnaireFinder.findPage(newParent.getName());
        } else {
          updatedNewParent = questionnaireFinder.findQuestion(newParent.getName());
        }

        if(!sameParent) {
          for(Question existing : updatedNewParent.getQuestions()) {
            if(existing.getName().equalsIgnoreCase(updatedElement.getName())) {
              error(new StringResourceModel("Question.AlreadyExistForParent", QuestionnaireTreePanel.this, null).getObject());
              feedbackWindow.setContent(feedbackPanel);
              feedbackWindow.show(target);
              break;
            }
          }
        }
        if(updatedElement.hasCategories() && newParent instanceof Question) {
          error(new StringResourceModel("Question.ImpossibleMoveCategories", QuestionnaireTreePanel.this, null).getObject());
        }
        if(!hasErrorMessage()) {
          if(updatedElement.getParentQuestion() == null) {
            updatedElement.getPage().removeQuestion(updatedElement);
          } else {
            updatedElement.getParentQuestion().removeQuestion(updatedElement);
          }
          if(updatedNewParent instanceof Page) {
            updatedElement.setParentQuestion(null);
          } else if(updatedNewParent instanceof Question) {
            updatedElement.setPage(null);
          }
          updatedNewParent.addQuestion(updatedElement, Math.min(position, updatedNewParent.getQuestions().size()));
          persitQuestionnaire(target);
        }
      }

      if(hasErrorMessage()) {
        // target.appendJavascript("Wicket.QTree.refreshTree('" + treeId + "');");
        target.addComponent(tree);
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    }
  }

  protected class EditBehavior extends AbstractDefaultAjaxBehavior {

    @Override
    protected void respond(final AjaxRequestTarget target) {
      final String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      log.info("Edit " + nodeId);

      @SuppressWarnings("unchecked")
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      IQuestionnaireElement element = elements.get(nodeId);
      final Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(questionnaireModel.getObject().getName()).getQuestionnaire();
      questionnaireModel.setObject(questionnaire);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof Questionnaire) {
        QuestionnairePanel questionnairePanel = new QuestionnairePanel(getShownComponentId(), new Model<Questionnaire>(questionnaire), false) {
          @Override
          public void onSave(AjaxRequestTarget target1, Questionnaire savedQuestionnaire) {
            persist(target1);
            elements.put(nodeId, savedQuestionnaire);
            // update node name in jsTree
            target1.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(savedQuestionnaire) + "');");
            show(new WebMarkupContainer(getShownComponentId()), new Model<String>(""), target1);
          }
        };
        show(questionnairePanel, new StringResourceModel("Questionnaire", QuestionnaireTreePanel.this, null), target);

      }

      if(element instanceof Section) {
        Section updatedElement = questionnaireFinder.findSection(element.getName());
        SectionPanel sectionPanel = new SectionPanel(getShownComponentId(), new Model<Section>(updatedElement), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Section section) {
            persist(target1);
            elements.put(nodeId, section);
            // update node name in jsTree
            target1.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(section) + "');");
            show(new WebMarkupContainer(getShownComponentId()), new Model<String>(""), target1);
          }
        };
        show(sectionPanel, new StringResourceModel("Section", QuestionnaireTreePanel.this, null), target);

      } else if(element instanceof Page) {
        Page updatedElement = questionnaireFinder.findPage(element.getName());
        PagePanel pagePanel = new PagePanel(getShownComponentId(), new Model<Page>(updatedElement), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Page editedPage) {
            persist(target1);
            elements.put(nodeId, editedPage);
            // update node name in jsTree
            target1.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(editedPage) + "');");
            show(new WebMarkupContainer(getShownComponentId()), new Model<String>(""), target1);
          }
        };
        show(pagePanel, new StringResourceModel("Page", QuestionnaireTreePanel.this, null), target);

      } else if(element instanceof Question) {
        Question updatedElement = questionnaireFinder.findQuestion(element.getName());
        EditQuestionPanel questionPanel = new EditQuestionPanel(getShownComponentId(), new Model<Question>(updatedElement), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Question question) {
            persist(target1);
            elements.put(nodeId, question);
            QuestionnaireTreePanel.this.setDefaultModelObject(questionnaire);
            target1.addComponent(tree);
            // update node name in jsTree
            target1.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('rename_node', $('#" + nodeId + "'), '" + getNodeLabel(question) + "');");
            show(new WebMarkupContainer(getShownComponentId()), new Model<String>(""), target1);
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
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      IQuestionnaireElement element = elements.get(nodeId);
      Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(questionnaireModel.getObject().getName()).getQuestionnaire();
      questionnaireModel.setObject(questionnaire);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof Section) {
        Section updatedElement = questionnaireFinder.findSection(element.getName());
        Section parentSection = updatedElement.getParentSection();
        if(parentSection == null) {
          questionnaire.removeSection(updatedElement);
        } else {
          parentSection.removeSection(updatedElement);
        }
      } else if(element instanceof Page) {
        Page updatedElement = questionnaireFinder.findPage(element.getName());
        Section parentSection = updatedElement.getSection();
        parentSection.removePage(updatedElement);
      } else if(element instanceof Question) {
        Question updatedElement = questionnaireFinder.findQuestion(element.getName());
        if(updatedElement.getParentQuestion() == null) {
          updatedElement.getPage().removeQuestion(updatedElement);
        } else {
          updatedElement.getParentQuestion().removeQuestion(updatedElement);
        }
      }
      persitQuestionnaire(target);
      // remove node from jsTree
      target.appendJavascript("$('#" + tree.getMarkupId(true) + "').jstree('delete_node', $('#" + nodeId + "'));");
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
      IQuestionnaireElement element = elements.get(nodeId);
      final Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(questionnaireModel.getObject().getName()).getQuestionnaire();
      questionnaireModel.setObject(questionnaire);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof IHasSection && "section".equals(type)) {
        final IHasSection updatedElement;
        if(element instanceof Section) {
          updatedElement = questionnaireFinder.findSection(element.getName());
        } else {
          updatedElement = questionnaire;
        }

        SectionPanel sectionPanel = new SectionPanel(getShownComponentId(), new Model<Section>(new Section(null)), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Section section) {
            updatedElement.addSection(section);
            persist(target1);
            QuestionnaireTreePanel.this.setDefaultModelObject(questionnaire);
            target1.addComponent(tree);
            show(new WebMarkupContainer(getShownComponentId()), new Model<String>(""), target1);
          }
        };
        show(sectionPanel, new StringResourceModel("Section", QuestionnaireTreePanel.this, null), target);

      } else if(element instanceof Section && "page".equals(type)) {
        final Section updatedElement = questionnaireFinder.findSection(element.getName());
        PagePanel pagePanel = new PagePanel(getShownComponentId(), new Model<Page>(new Page(null)), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Page editedPage) {
            updatedElement.addPage(editedPage);
            questionnaire.addPage(editedPage);
            persist(target1);
            QuestionnaireTreePanel.this.setDefaultModelObject(questionnaire);
            target1.addComponent(tree);
            show(new WebMarkupContainer(getShownComponentId()), new Model<String>(""), target1);
          }
        };
        show(pagePanel, new StringResourceModel("Page", QuestionnaireTreePanel.this, null), target);

      } else if(element instanceof IHasQuestion && "question".equals(type)) {
        final IHasQuestion updatedElement;
        if(element instanceof Page) {
          updatedElement = questionnaireFinder.findPage(element.getName());
        } else {
          updatedElement = questionnaireFinder.findQuestion(element.getName());
        }

        EditQuestionPanel questionPanel = new EditQuestionPanel(getShownComponentId(), new Model<Question>(new Question(null)), questionnaireModel) {
          @Override
          public void onSave(AjaxRequestTarget target1, Question question) {
            updatedElement.addQuestion(question);
            persist(target1);
            QuestionnaireTreePanel.this.setDefaultModelObject(questionnaire);
            target1.addComponent(tree);
            show(new WebMarkupContainer(getShownComponentId()), new Model<String>(""), target1);
          }
        };
        show(questionPanel, new StringResourceModel("Question", QuestionnaireTreePanel.this, null), target);
      }
    }
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

  public class QVisitor implements org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor {

    private List<IQuestionnaireElement> children;

    public QVisitor(List<IQuestionnaireElement> children) {
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
      QuestionType questionType = question.getType();
      if(questionType != QuestionType.ARRAY_CHECKBOX && questionType != QuestionType.ARRAY_RADIO) {
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

    public List<IQuestionnaireElement> getChildren() {
      return children;
    }

    @Override
    public void visit(Variable variable) {
    }

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

    QVisitor questionnaireVisitor = new QVisitor(new ArrayList<IQuestionnaireElement>());
    element.accept(questionnaireVisitor);
    for(IQuestionnaireElement child : questionnaireVisitor.getChildren()) {
      node.getChildren().add(populateNode(child));
    }
    return node;
  }

}
