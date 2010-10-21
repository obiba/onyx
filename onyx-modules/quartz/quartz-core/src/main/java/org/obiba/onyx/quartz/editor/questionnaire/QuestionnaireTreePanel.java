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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.JavascriptPackageResource;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.page.EditedPage;
import org.obiba.onyx.quartz.editor.page.PagePropertiesPanel;
import org.obiba.onyx.quartz.editor.question.EditQuestionPanel;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.quartz.editor.question.QuestionPropertiesPanel;
import org.obiba.onyx.quartz.editor.section.EditedSection;
import org.obiba.onyx.quartz.editor.section.SectionPropertiesPanel;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class QuestionnaireTreePanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  private static final String ID_PREFIX = "element_";

  private Map<String, IQuestionnaireElement> elements = new HashMap<String, IQuestionnaireElement>();

  @SpringBean
  private QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private QuestionnaireBundleManager questionnaireBundleManager;

  private int elementCounter;

  private final AbstractDefaultAjaxBehavior moveBehavior;

  private final Label moveCallback;

  private final AbstractDefaultAjaxBehavior editBehavior;

  private final Label editCallback;

  private final AbstractDefaultAjaxBehavior deleteBehavior;

  private final Label deleteCallback;

  private final AbstractDefaultAjaxBehavior addChildBehavior;

  private final Label addChildCallback;

  private final String treeId;

  private final WebMarkupContainer treeContainer;

  private final ModalWindow elementWindow;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private ListModel<IQuestionnaireElement> root;

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model) {
    super(id, new Model<EditedQuestionnaire>(new EditedQuestionnaire(model.getObject())));

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    elementWindow = new ModalWindow("elementWindow");
    elementWindow.setCssClassName("onyx");
    elementWindow.setInitialWidth(1000);
    elementWindow.setInitialHeight(600);
    elementWindow.setResizable(true);
    elementWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true;
      }
    });

    Form<Void> form = new Form<Void>("form");
    form.add(elementWindow);
    add(form);

    add(JavascriptPackageResource.getHeaderContribution(QuestionnaireTreePanel.class, "QuestionnaireTreePanel.js"));

    treeId = "tree_" + model.getObject().getName();

    treeContainer = new WebMarkupContainer("treeContainer");
    treeContainer.setMarkupId(treeId);
    treeContainer.setOutputMarkupId(true);

    add(treeContainer);

    root = new ListModel<IQuestionnaireElement>();
    root.setObject(Lists.newArrayList((IQuestionnaireElement) model.getObject()));
    ListFragment listFragment = new ListFragment("tree", root);
    listFragment.add(new JsTreeBehavior());
    treeContainer.add(listFragment);

    treeContainer.add(new AbstractBehavior() {
      @Override
      public void renderHead(IHeaderResponse response) {
        response.renderOnLoadJavascript("Wicket.QTree.buildTree('" + treeId + "')");
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

      EditedQuestionnaire editedQuestionnaire = (EditedQuestionnaire) QuestionnaireTreePanel.this.getDefaultModelObject();
      Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(editedQuestionnaire.getElement().getName()).getQuestionnaire();
      editedQuestionnaire.setElement(questionnaire);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof Section && newParent instanceof IHasSection) {
        Section updatedElement = questionnaireFinder.findSection(element.getName());

        final IHasSection updatedNewParent;
        if(newParent instanceof Section) {
          updatedNewParent = questionnaireFinder.findSection(newParent.getName());
        } else {
          updatedNewParent = ((EditedQuestionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).getElement();
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
          persit();
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
          persit();
        }
      } else if(element instanceof Question && newParent instanceof IHasQuestion) {
        Question updatedElement = questionnaireFinder.findQuestion(element.getName());

        IHasQuestion updatedNewParent;
        if(newParent instanceof Page) {
          updatedNewParent = questionnaireFinder.findPage(element.getName());
        } else {
          updatedNewParent = questionnaireFinder.findQuestion(element.getName());
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
          persit();
        }
      }

      if(hasErrorMessage()) {
        // target.appendJavascript("Wicket.QTree.refreshTree('" + treeId + "');");
        target.addComponent(treeContainer);
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    }
  }

  protected class EditBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(final AjaxRequestTarget respondTarget) {
      final String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      log.info("Edit " + nodeId);
      IQuestionnaireElement element = elements.get(nodeId);
      EditedQuestionnaire editedQuestionnaire = (EditedQuestionnaire) QuestionnaireTreePanel.this.getDefaultModelObject();
      Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(editedQuestionnaire.getElement().getName()).getQuestionnaire();
      editedQuestionnaire.setElement(questionnaire);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);
      if(element instanceof Questionnaire) {
        elementWindow.setTitle(new ResourceModel("Questionnaire"));
        elementWindow.setContent(new QuestionnairePropertiesPanel("content", new Model<EditedQuestionnaire>(new EditedQuestionnaire(questionnaire)), elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, EditedQuestionnaire onSaveEditedQuestionnaire) {
            super.onSave(target, onSaveEditedQuestionnaire);
            elements.put(nodeId, onSaveEditedQuestionnaire.getElement());
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + onSaveEditedQuestionnaire.getElement().getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      }

      IModel<Questionnaire> questionnaireModel = new PropertyModel<Questionnaire>(QuestionnaireTreePanel.this.getDefaultModel(), "element");
      if(element instanceof Section) {
        Section updatedElement = questionnaireFinder.findSection(element.getName());
        elementWindow.setTitle(new ResourceModel("Section"));
        Set<String> unavailableNames = new HashSet<String>();
        for(Section section : questionnaireModel.getObject().getSections()) {
          unavailableNames.add(section.getName());
        }
        unavailableNames.remove(updatedElement.getName());

        elementWindow.setContent(new SectionPropertiesPanel("content", new Model<Section>(updatedElement), unavailableNames, questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, EditedSection editedSection) {
            super.onSave(target, editedSection);
            persist(target);
            elements.put(nodeId, editedSection.getElement());
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + editedSection.getElement().getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof Page) {
        Page updatedElement = questionnaireFinder.findPage(element.getName());
        elementWindow.setTitle(new ResourceModel("Page"));

        Set<String> unavailableNames = new HashSet<String>();
        for(Page page : updatedElement.getSection().getPages()) {
          unavailableNames.add(page.getName());
        }
        unavailableNames.remove(updatedElement.getName());

        elementWindow.setContent(new PagePropertiesPanel("content", new Model<Page>(updatedElement), unavailableNames, questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, EditedPage editedPage) {
            super.onSave(target, editedPage);
            persist(target);
            elements.put(nodeId, editedPage.getElement());
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + editedPage.getElement().getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof Question) {
        Question updatedElement = questionnaireFinder.findQuestion(element.getName());
        elementWindow.setTitle(new ResourceModel("Question"));

        Set<String> unavailableNames = new HashSet<String>();
        for(Question question : updatedElement.getPage() != null ? updatedElement.getPage().getQuestions() : updatedElement.getParentQuestion().getQuestions()) {
          unavailableNames.add(question.getName());
        }
        unavailableNames.remove(updatedElement.getName());

        elementWindow.setContent(new QuestionPropertiesPanel("content", new Model<Question>(updatedElement), unavailableNames, questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, EditedQuestion editedQuestion) {
            super.onSave(target, editedQuestion);
            persist(target);
            elements.put(nodeId, editedQuestion.getElement());
            target.addComponent(treeContainer);
          }
        });
        elementWindow.show(respondTarget);
      }
    }
  }

  protected class DeleteBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(AjaxRequestTarget target) {
      String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      log.info("Delete " + nodeId);

      IQuestionnaireElement element = elements.get(nodeId);
      EditedQuestionnaire editedQuestionnaire = (EditedQuestionnaire) QuestionnaireTreePanel.this.getDefaultModelObject();
      Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(editedQuestionnaire.getElement().getName()).getQuestionnaire();
      editedQuestionnaire.setElement(questionnaire);
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
        ((EditedQuestionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).getElement().removePage(updatedElement);
      } else if(element instanceof Question) {
        Question updatedElement = questionnaireFinder.findQuestion(element.getName());
        if(updatedElement.getParentQuestion() == null) {
          updatedElement.getPage().removeQuestion(updatedElement);
        } else {
          updatedElement.getParentQuestion().removeQuestion(updatedElement);
        }
      }
      persit();
      // remove node from jsTree
      target.appendJavascript("$('#" + treeId + "').jstree('delete_node', $('#" + nodeId + "'));");
    }
  }

  protected class AddChildBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(final AjaxRequestTarget respondTarget) {
      Request request = RequestCycle.get().getRequest();
      final String nodeId = request.getParameter("nodeId");
      String type = request.getParameter("type");
      log.info("Add " + type + " to " + nodeId);

      IModel<Questionnaire> questionnaireModel = new PropertyModel<Questionnaire>(QuestionnaireTreePanel.this.getDefaultModel(), "element");
      IQuestionnaireElement element = elements.get(nodeId);
      final Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(questionnaireModel.getObject().getName()).getQuestionnaire();
      questionnaireModel.setObject(questionnaire);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof IHasSection && "section".equals(type)) {
        final IHasSection updatedElement;
        if(element instanceof Section) {
          updatedElement = questionnaireFinder.findSection(element.getName());
        } else {
          updatedElement = ((EditedQuestionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).getElement();
        }

        Set<String> unavailableNames = new HashSet<String>();
        for(Section section : updatedElement.getSections()) {
          unavailableNames.add(section.getName());
        }
        elementWindow.setTitle(new StringResourceModel("Section", QuestionnaireTreePanel.this, null));
        elementWindow.setContent(new SectionPropertiesPanel("content", new Model<Section>(new Section(null)), unavailableNames, questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, EditedSection editedSection) {
            super.onSave(target, editedSection);
            updatedElement.addSection(editedSection.getElement());
            persist(target);
            root.setObject(Lists.newArrayList((IQuestionnaireElement) questionnaire));
            target.addComponent(treeContainer);
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof Section && "page".equals(type)) {
        final Section updatedElement = questionnaireFinder.findSection(element.getName());
        elementWindow.setTitle(new StringResourceModel("Page", QuestionnaireTreePanel.this, null));

        Set<String> unavailableNames = new HashSet<String>();
        for(Page page : updatedElement.getPages()) {
          unavailableNames.add(page.getName());
        }

        elementWindow.setContent(new PagePropertiesPanel("content", new Model<Page>(new Page(null)), unavailableNames, questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, EditedPage editedPage) {
            super.onSave(target, editedPage);
            updatedElement.addPage(editedPage.getElement());
            ((EditedQuestionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).getElement().addPage(editedPage.getElement());
            persist(target);
            root.setObject(Lists.newArrayList((IQuestionnaireElement) questionnaire));
            target.addComponent(treeContainer);
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof IHasQuestion && "question".equals(type)) {
        IHasQuestion updatedElement;
        if(element instanceof Page) {
          updatedElement = questionnaireFinder.findPage(element.getName());
        } else {
          updatedElement = questionnaireFinder.findQuestion(element.getName());
        }
        elementWindow.setTitle(new StringResourceModel("Question", QuestionnaireTreePanel.this, null));

        Set<String> unavailableNames = new HashSet<String>();
        for(Question question : updatedElement.getQuestions()) {
          unavailableNames.add(question.getName());
        }

        elementWindow.setContent(new EditQuestionPanel("content", new Model<Question>(new Question(null)), new Model<IHasQuestion>(updatedElement), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, EditedQuestion editedQuestion) {
            super.onSave(target, editedQuestion);
            // TODO persist question
            // ((IHasQuestion) element).addQuestion(editedQuestion.getElement());
            // persist(target);
            // target.addComponent(treeContainer);
          }
        });
        elementWindow.show(respondTarget);
      }
    }
  }

  protected void persit() {
    try {
      EditedQuestionnaire defaultModelObject = (EditedQuestionnaire) QuestionnaireTreePanel.this.getDefaultModelObject();
      QuestionnaireBuilder builder = questionnairePersistenceUtils.createBuilder(defaultModelObject.getElement());
      questionnairePersistenceUtils.persist(defaultModelObject, builder);
    } catch(Exception e) {
      log.error("Cannot persist questionnaire", e);
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
      children.addAll(question.getQuestions());
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

    public ListModel<IQuestionnaireElement> getChildren() {
      return new ListModel<IQuestionnaireElement>(children);
    }

  }

  public class ListFragment extends Fragment {

    public ListFragment(String id, ListModel<IQuestionnaireElement> root) {
      super(id, "listFragment", QuestionnaireTreePanel.this);

      add(new ListView<IQuestionnaireElement>("item", root) {
        @Override
        protected void populateItem(ListItem<IQuestionnaireElement> item) {
          item.setOutputMarkupId(true);

          IQuestionnaireElement element = item.getModelObject();
          QVisitor questionnaireVisitor = new QVisitor(new ArrayList<IQuestionnaireElement>());
          element.accept(questionnaireVisitor);

          item.add(new Label("itemTitle", "[" + getShortClassName(element.getClass()).toUpperCase() + "] " + element.getName()));
          item.add(new SimpleAttributeModifier("id", addElement(element)));
          item.add(new SimpleAttributeModifier("name", element.getName()));
          item.add(new AttributeAppender("class", new Model<String>(questionnaireVisitor.getChildren().getObject().isEmpty() ? "jstree-leaf" : "jstree-open"), " "));
          item.add(new AttributeAppender("rel", new Model<String>(ClassUtils.getShortClassName(element.getClass())), " "));
          if(questionnaireVisitor.getChildren().getObject().isEmpty()) {
            item.add(new WebMarkupContainer("children"));
          } else {
            item.add(new ListFragment("children", questionnaireVisitor.getChildren()));
          }
        }
      });
    }
  }
}
