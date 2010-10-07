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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ClassUtils;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasPage;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasQuestion;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasSection;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.editor.page.PagePropertiesPanel;
import org.obiba.onyx.quartz.editor.question.QuestionPropertiesPanel;
import org.obiba.onyx.quartz.editor.section.SectionPropertiesPanel;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class QuestionnaireTreePanel extends Panel {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private static final String ID_PREFIX = "element_";

  private Map<String, IQuestionnaireElement> elements = new HashMap<String, IQuestionnaireElement>();

  // Map<Element, ParentElement>
  private Map<IQuestionnaireElement, IQuestionnaireElement> elementsParent = new HashMap<IQuestionnaireElement, IQuestionnaireElement>();

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

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model) {
    super(id, model);

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);
    add(feedbackWindow);

    elementWindow = new ModalWindow("elementWindow");
    elementWindow.setCssClassName("onyx");
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

    Questionnaire questionnaire = model.getObject();
    treeId = "tree_" + questionnaire.getName();

    treeContainer = new WebMarkupContainer("treeContainer");
    treeContainer.setMarkupId(treeId);
    treeContainer.setOutputMarkupId(true);

    add(treeContainer);

    List<IQuestionnaireElement> root = new ArrayList<IQuestionnaireElement>();
    root.add(questionnaire);
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
    moveCallback.setDefaultModelObject("Wicket.QTree.moveNode = function(nodeId, newParentId, newPosition) {\n" + //
    "  wicketAjaxGet('" + moveBehavior.getCallbackUrl(true) + "&nodeId='+ nodeId +'&newParentId='+ newParentId +'&newPosition='+ newPosition, function() { }, function() { alert('Cannot communicate with server...'); });" + //
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
      // String previousParentId = request.getParameter("previousParentId");
      String newParentId = request.getParameter("newParentId");
      int position = Integer.parseInt(request.getParameter("newPosition"));

      IQuestionnaireElement element = elements.get(nodeId);
      // IQuestionnaireElement previousParent = elements.get(previousParentId);
      IQuestionnaireElement newParent = elements.get(newParentId);
      Questionnaire questionnaire = (Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject();
      if(element instanceof Section && newParent instanceof IHasSection) {
        Section section = (Section) element;
        IHasSection newHasSectionParent = (IHasSection) newParent;
        for(Section existing : newHasSectionParent.getSections()) {
          if(existing.getName().equalsIgnoreCase(section.getName())) {
            error(new StringResourceModel("Section.AlreadyExistForParent", QuestionnaireTreePanel.this, null).getObject());
            feedbackWindow.setContent(feedbackPanel);
            feedbackWindow.show(target);
            break;
          }
        }
        if(!hasErrorMessage()) {
          if(section.getParentSection() == null) {
            questionnaire.removeSection(section);
          } else {
            section.getParentSection().removeSection(section);
          }
          newHasSectionParent.addSection(section, Math.min(newHasSectionParent.getSections().size(), position));
        }
      } else if(element instanceof Page && newParent instanceof IHasPage) {
        Page page = (Page) element;
        IHasPage newHasPageParent = (IHasPage) newParent;
        for(Page existing : newHasPageParent.getPages()) {
          if(existing.getName().equalsIgnoreCase(page.getName())) {
            error(new StringResourceModel("Page.AlreadyExistForParent", QuestionnaireTreePanel.this, null).getObject());
            feedbackWindow.setContent(feedbackPanel);
            feedbackWindow.show(target);
            break;
          }
        }
        if(!hasErrorMessage()) {
          if(page.getSection() == null) {
            questionnaire.removePage(page);
          } else {
            page.getSection().removePage(page);
          }
          newHasPageParent.addPage(page, Math.min(newHasPageParent.getPages().size(), position));
        }
      } else if(element instanceof Question && newParent instanceof IHasQuestion) {
        Question question = (Question) element;
        IHasQuestion newHasQuestionParent = (IHasQuestion) newParent;
        for(Question existing : newHasQuestionParent.getQuestions()) {
          if(existing.getName().equalsIgnoreCase(question.getName())) {
            error(new StringResourceModel("Question.AlreadyExistForParent", QuestionnaireTreePanel.this, null).getObject());
            feedbackWindow.setContent(feedbackPanel);
            feedbackWindow.show(target);
            break;
          }
        }
        if(!hasErrorMessage()) {
          if(question.getParentQuestion() == null) {
            question.getPage().removeQuestion(question);
          } else {
            question.getParentQuestion().removeQuestion(question);
          }
          newHasQuestionParent.addQuestion(question, Math.min(position, newHasQuestionParent.getQuestions().size()));
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
    @SuppressWarnings("unchecked")
    @Override
    protected void respond(final AjaxRequestTarget respondTarget) {
      final String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      log.info("Edit " + nodeId);
      final IQuestionnaireElement element = elements.get(nodeId);
      if(element instanceof Questionnaire) {
        elementWindow.setTitle(new ResourceModel("Questionnaire"));
        elementWindow.setContent(new QuestionnairePropertiesPanel("content", new Model<Questionnaire>((Questionnaire) element), elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Questionnaire questionnaire) {
            super.onSave(target, questionnaire);
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + questionnaire.getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      }
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      if(element instanceof Section) {
        elementWindow.setTitle(new ResourceModel("Section"));
        elementWindow.setContent(new SectionPropertiesPanel("content", new Model<Section>((Section) element), new Model<IHasSection>((IHasSection) elementsParent.get(element)), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Section section) {
            persist(target);
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + section.getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof Page) {
        elementWindow.setTitle(new ResourceModel("Page"));
        elementWindow.setContent(new PagePropertiesPanel("content", new Model<Page>((Page) element), new Model<Section>((Section) elementsParent.get(element)), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Page page) {
            persist(target);
            // update node name in jsTree
            target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + page.getName() + "');");
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof Question) {
        elementWindow.setTitle(new ResourceModel("Question"));
        elementWindow.setContent(new QuestionPropertiesPanel("content", new Model<Question>((Question) element), new Model<IHasQuestion>((IHasQuestion) elementsParent.get(element)), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Question question) {
            super.onSave(target, question);
            persist(target);
            target.addComponent(treeContainer);
          }
        });
        elementWindow.show(respondTarget);
      }
    }
  }

  protected class DeleteBehavior extends AbstractDefaultAjaxBehavior {
    @SuppressWarnings("unchecked")
    @Override
    protected void respond(AjaxRequestTarget target) {
      String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      log.info("Delete " + nodeId);
      IQuestionnaireElement element = elements.get(nodeId);
      Questionnaire questionnaire = (Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject();
      if(element instanceof Section) {
        Section section = (Section) element;
        Section parentSection = section.getParentSection();
        if(parentSection == null) {
          // remove from questionnaire
          questionnaire.removeSection(section);
        } else {
          // remove from parentSection
          parentSection.removeSection(section);
        }
      } else if(element instanceof Page) {
        Page page = (Page) element;
        Section parentSection = page.getSection();
        // remove from parentSection
        parentSection.removePage(page);
      } else if(element instanceof Question) {
        Question question = (Question) element;
        if(question.getParentQuestion() == null) {
          question.getPage().removeQuestion(question);
        } else {
          question.getParentQuestion().removeQuestion(question);
        }
      }
      // TODO temporary
      new QuestionnairePropertiesPanel("content", (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel(), elementWindow).persist(target);
      // remove node from jsTree
      target.appendJavascript("$('#" + treeId + "').jstree('delete_node', $('#" + nodeId + "'));");
    }
  }

  protected class AddChildBehavior extends AbstractDefaultAjaxBehavior {
    @SuppressWarnings("unchecked")
    @Override
    protected void respond(final AjaxRequestTarget respondTarget) {
      Request request = RequestCycle.get().getRequest();
      String nodeId = request.getParameter("nodeId");
      String type = request.getParameter("type");
      log.info("Add " + type + " to " + nodeId);
      final IQuestionnaireElement element = elements.get(nodeId);
      IModel<Questionnaire> questionnaireModel = (IModel<Questionnaire>) QuestionnaireTreePanel.this.getDefaultModel();
      if(element instanceof IHasSection && "section".equals(type)) {
        elementWindow.setTitle(new StringResourceModel("Section", QuestionnaireTreePanel.this, null));
        elementWindow.setContent(new SectionPropertiesPanel("content", new Model<Section>(new Section(null)), new Model<IHasSection>((IHasSection) element), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Section section) {
            ((IHasSection) element).addSection(section);
            persist(target);
            target.addComponent(treeContainer);
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof Section && "page".equals(type)) {
        elementWindow.setTitle(new StringResourceModel("Page", QuestionnaireTreePanel.this, null));
        elementWindow.setContent(new PagePropertiesPanel("content", new Model<Page>(new Page(null)), new Model<Section>((Section) element), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Page page) {
            ((Section) element).addPage(page);
            persist(target);
            target.addComponent(treeContainer);
          }
        });
        elementWindow.show(respondTarget);
      } else if(element instanceof IHasQuestion && "question".equals(type)) {
        elementWindow.setTitle(new StringResourceModel("Question", QuestionnaireTreePanel.this, null));
        elementWindow.setContent(new QuestionPropertiesPanel("content", new Model<Question>(new Question(null)), new Model<IHasQuestion>((IHasQuestion) element), questionnaireModel, elementWindow) {
          @Override
          public void onSave(AjaxRequestTarget target, Question question) {
            super.onSave(target, question);
            ((IHasQuestion) element).addQuestion(question);
            persist(target);
            target.addComponent(treeContainer);
          }
        });
        elementWindow.show(respondTarget);
      }
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

    public List<IQuestionnaireElement> getChildren() {
      return children;
    }

  }

  public class ListFragment extends Fragment {

    public ListFragment(String id, List<IQuestionnaireElement> list) {
      super(id, "listFragment", QuestionnaireTreePanel.this);

      add(new ListView<IQuestionnaireElement>("item", list) {
        @Override
        protected void populateItem(ListItem<IQuestionnaireElement> item) {
          item.setOutputMarkupId(true);

          IQuestionnaireElement element = item.getModelObject();
          QVisitor questionnaireVisitor = new QVisitor(new ArrayList<IQuestionnaireElement>());
          element.accept(questionnaireVisitor);
          for(IQuestionnaireElement child : questionnaireVisitor.getChildren()) {
            elementsParent.put(child, element);
          }

          item.add(new Label("itemTitle", "[" + getShortClassName(element.getClass()).toUpperCase() + "] " + element.getName()));
          item.add(new SimpleAttributeModifier("id", addElement(element)));
          item.add(new SimpleAttributeModifier("name", element.getName()));
          item.add(new AttributeAppender("class", new Model<String>(questionnaireVisitor.getChildren().isEmpty() ? "jstree-leaf" : "jstree-open"), " "));
          item.add(new AttributeAppender("rel", new Model<String>(ClassUtils.getShortClassName(element.getClass())), " "));
          if(questionnaireVisitor.getChildren().isEmpty()) {
            item.add(new WebMarkupContainer("children"));
          } else {
            item.add(new ListFragment("children", questionnaireVisitor.getChildren()));
          }
        }
      });
    }
  }
}
