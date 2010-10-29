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
import java.util.List;
import java.util.Map;

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
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
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
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.quartz.editor.section.SectionPanel;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

@SuppressWarnings("serial")
public class QuestionnaireTreePanel extends Panel {

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

  private final String treeId;

  private final WebMarkupContainer treeContainer;

  private final ModalWindow elementWindow;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private ListModel<IQuestionnaireElement> root;

  private final LocaleProperties localeProperties;

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model) {
    super(id, model);

    localeProperties = localePropertiesUtils.load(model.getObject());

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

      @SuppressWarnings("unchecked")
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      IQuestionnaireElement element = elements.get(nodeId);
      final Questionnaire questionnaire = questionnaireBundleManager.getPersistedBundle(questionnaireModel.getObject().getName()).getQuestionnaire();
      questionnaireModel.setObject(questionnaire);
      QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaire);

      if(element instanceof Questionnaire) {
        elementWindow.setTitle(new ResourceModel("Questionnaire"));
        elementWindow.setContent(new QuestionnairePanel("content", new Model<Questionnaire>(questionnaire), elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Questionnaire savedQuestionnaire) {
            persist(target);
            elements.put(nodeId, savedQuestionnaire);
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + savedQuestionnaire.getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      }

      if(element instanceof Section) {
        Section updatedElement = questionnaireFinder.findSection(element.getName());
        elementWindow.setTitle(new ResourceModel("Section"));
        elementWindow.setContent(new SectionPanel("content", new Model<Section>(updatedElement), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Section section) {
            persist(target);
            elements.put(nodeId, section);
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + section.getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof Page) {
        Page updatedElement = questionnaireFinder.findPage(element.getName());
        elementWindow.setTitle(new ResourceModel("Page"));

        elementWindow.setContent(new PagePanel("content", new Model<Page>(updatedElement), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Page editedPage) {
            persist(target);
            elements.put(nodeId, editedPage);
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + editedPage.getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof Question) {
        Question updatedElement = questionnaireFinder.findQuestion(element.getName());
        elementWindow.setTitle(new ResourceModel("Question"));

        elementWindow.setContent(new EditQuestionPanel("content", new Model<Question>(updatedElement), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, EditedQuestion editedQuestion) {
            persist(target);
            elements.put(nodeId, editedQuestion.getElement());
            root.setObject(Lists.newArrayList((IQuestionnaireElement) questionnaire));
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
      target.appendJavascript("$('#" + treeId + "').jstree('delete_node', $('#" + nodeId + "'));");
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

        elementWindow.setTitle(new StringResourceModel("Section", QuestionnaireTreePanel.this, null));
        elementWindow.setContent(new SectionPanel("content", new Model<Section>(new Section(null)), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, Section section) {
            updatedElement.addSection(section);
            persist(target1);
            root.setObject(Lists.newArrayList((IQuestionnaireElement) questionnaire));
            target1.addComponent(treeContainer);
          }
        });
        elementWindow.show(target);
      } else if(element instanceof Section && "page".equals(type)) {
        final Section updatedElement = questionnaireFinder.findSection(element.getName());
        elementWindow.setTitle(new StringResourceModel("Page", QuestionnaireTreePanel.this, null));

        elementWindow.setContent(new PagePanel("content", new Model<Page>(new Page(null)), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, Page editedPage) {
            updatedElement.addPage(editedPage);
            questionnaire.addPage(editedPage);
            persist(target1);
            root.setObject(Lists.newArrayList((IQuestionnaireElement) questionnaire));
            target1.addComponent(treeContainer);
          }
        });
        elementWindow.show(target);
      } else if(element instanceof IHasQuestion && "question".equals(type)) {
        final IHasQuestion updatedElement;
        if(element instanceof Page) {
          updatedElement = questionnaireFinder.findPage(element.getName());
        } else {
          updatedElement = questionnaireFinder.findQuestion(element.getName());
        }
        elementWindow.setTitle(new StringResourceModel("Question", QuestionnaireTreePanel.this, null));

        elementWindow.setContent(new EditQuestionPanel("content", new Model<Question>(new Question(null)), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, EditedQuestion editedQuestion) {
            updatedElement.addQuestion(editedQuestion.getElement());
            persist(target1);
            root.setObject(Lists.newArrayList((IQuestionnaireElement) questionnaire));
            target1.addComponent(treeContainer);
          }
        });
        elementWindow.show(target);
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
      try {
        if(question.getType() != QuestionType.ARRAY_CHECKBOX && question.getType() != QuestionType.ARRAY_RADIO) {
          children.addAll(question.getQuestions());
        }
      } catch(Exception e) {
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

    public ListModel<IQuestionnaireElement> getChildren() {
      return new ListModel<IQuestionnaireElement>(children);
    }

    @Override
    public void visit(Variable variable) {
      // TODO Auto-generated method stub

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

          String label = "[" + getShortClassName(element.getClass()).toUpperCase() + "] " + element.getName();
          if(element instanceof Question) {
            try {
              label += (" " + ((Question) element).getType());
            } catch(Exception e) {

            }
          }
          QVisitor questionnaireVisitor = new QVisitor(new ArrayList<IQuestionnaireElement>());
          element.accept(questionnaireVisitor);

          item.add(new Label("itemTitle", label));
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
