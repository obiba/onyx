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
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.editor.question.QuestionPropertiesPanel;
import org.obiba.onyx.quartz.editor.section.SectionPropertiesPanel;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("serial")
public class QuestionnaireTreePanel extends Panel {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private static final String ID_PREFIX = "element_";

  private Map<String, IQuestionnaireElement> elements = new HashMap<String, IQuestionnaireElement>();

  private int elementCounter;

  private AbstractDefaultAjaxBehavior moveBehavior;

  private Label moveCallback;

  private AbstractDefaultAjaxBehavior editBehavior;

  private Label editCallback;

  private AbstractDefaultAjaxBehavior deleteBehavior;

  private Label deleteCallback;

  private AbstractDefaultAjaxBehavior addChildBehavior;

  private Label addChildCallback;

  private String treeId;

  private WebMarkupContainer treeContainer;

  private ModalWindow elementWindow;

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model) {
    super(id, model);

    elementWindow = new ModalWindow("elementWindow");
    elementWindow.setCssClassName("onyx");
    elementWindow.setInitialWidth(1000);
    elementWindow.setInitialHeight(600);
    elementWindow.setResizable(true);
    elementWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true; // same as cancel
      }
    });
    add(elementWindow);

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

    add(new AbstractBehavior() {
      @Override
      public void renderHead(IHeaderResponse response) {
        response.renderOnLoadJavascript("Wicket.QTree.buildTree('" + treeId + "')");
      }
    });

    add(moveBehavior = new AbstractDefaultAjaxBehavior() {
      @Override
      protected void respond(AjaxRequestTarget target) {
        Request request = RequestCycle.get().getRequest();
        String nodeId = request.getParameter("nodeId");
        String previousParentId = request.getParameter("previousParentId");
        String newParentId = request.getParameter("newParentId");
        int position = Integer.parseInt(request.getParameter("newPosition"));

        IQuestionnaireElement element = elements.get(nodeId);
        IQuestionnaireElement previousParent = elements.get(previousParentId);
        IQuestionnaireElement newParent = elements.get(newParentId);
        if(element instanceof Section) {
          Section section = (Section) element;
          if(previousParent instanceof Questionnaire) {
            ((Questionnaire) previousParent).removeSection(section);
          } else if(previousParent instanceof Section) {
            ((Section) previousParent).removeSection(section);
          }
          if(newParent instanceof Questionnaire) {
            ((Questionnaire) newParent).addSection(section, position);
          } else if(newParent instanceof Section) {
            ((Section) newParent).addSection(section, position);
          }
        } else if(element instanceof Page) {
          Page page = (Page) element;
          if(previousParent instanceof Questionnaire) {
            ((Questionnaire) previousParent).removePage(page);
          } else if(previousParent instanceof Section) {
            ((Section) previousParent).removePage(page);
          }
          if(newParent instanceof Questionnaire) {
            ((Questionnaire) newParent).addPage(page, position);
          } else if(newParent instanceof Section) {
            ((Section) newParent).addPage(page, position);
          }
        } else if(element instanceof Question) {
          Question question = (Question) element;
          ((Page) previousParent).removeQuestion(question);
          ((Page) newParent).addQuestion(question, position);
        }
      }
    });
    moveCallback = new Label("moveCallback", "");
    moveCallback.setOutputMarkupId(true);
    moveCallback.setEscapeModelStrings(false);
    add(moveCallback);

    add(editBehavior = new AbstractDefaultAjaxBehavior() {
      @Override
      protected void respond(final AjaxRequestTarget target) {
        final String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
        log.info("Edit " + nodeId);
        IQuestionnaireElement element = elements.get(nodeId);
        if(element instanceof Section) {
          elementWindow.setTitle(new StringResourceModel("Section", QuestionnaireTreePanel.this, null));
          elementWindow.setContent(new SectionPropertiesPanel("content", new Model<Section>((Section) element), elementWindow) {
            @Override
            public void onSave(AjaxRequestTarget target1, Section section) {
              super.onSave(target, section);
              // update node name in jsTree
              target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + section.getName() + "');");
            }
          });
          elementWindow.show(target);

        } else if(element instanceof Page) {
          elementWindow.setTitle(new StringResourceModel("Page", QuestionnaireTreePanel.this, null));
          // TODO show PagePropertiesPanel
        } else if(element instanceof Question) {
          elementWindow.setTitle(new StringResourceModel("Question", QuestionnaireTreePanel.this, null));
          elementWindow.setContent(new QuestionPropertiesPanel("content", new Model<Question>((Question) element), elementWindow) {
            @Override
            public void onSave(AjaxRequestTarget target1, Question question) {
              super.onSave(target, question);
              // update node name in jsTree
              target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + question.getName() + "');");
            }
          });
          elementWindow.show(target);
        }
      }
    });
    editCallback = new Label("editCallback", "");
    editCallback.setOutputMarkupId(true);
    editCallback.setEscapeModelStrings(false);
    add(editCallback);

    add(deleteBehavior = new AbstractDefaultAjaxBehavior() {
      @Override
      protected void respond(AjaxRequestTarget target) {
        String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
        log.info("Delete " + nodeId);
        IQuestionnaireElement element = elements.get(nodeId);
        if(element instanceof Section) {
          Section section = (Section) element;
          Section parentSection = section.getParentSection();
          if(parentSection == null) {
            // remove from questionnaire
            ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).removeSection(section);
          } else {
            // remove from parentSection
            parentSection.removeSection(section);
          }
        } else if(element instanceof Page) {
          Page page = (Page) element;
          Section parentSection = page.getSection();
          if(parentSection == null) {
            // remove from questionnaire
            ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).removePage(page);
          } else {
            // remove from parentSection
            parentSection.removePage(page);
          }
        } else if(element instanceof Question) {
          Question question = (Question) element;
          Page page = question.getPage();
          if(page != null) {
            page.removeQuestion(question);
          }
        }

        // remove node from jsTree
        target.appendJavascript("$('#" + treeId + "').jstree('delete_node', $('#" + nodeId + "'));");
      }
    });
    deleteCallback = new Label("deleteCallback", "");
    deleteCallback.setOutputMarkupId(true);
    deleteCallback.setEscapeModelStrings(false);
    add(deleteCallback);

    add(addChildBehavior = new AbstractDefaultAjaxBehavior() {
      @Override
      protected void respond(final AjaxRequestTarget target) {
        Request request = RequestCycle.get().getRequest();
        String nodeId = request.getParameter("nodeId");
        String type = request.getParameter("type");
        log.info("Add " + type + " to " + nodeId);
        final IQuestionnaireElement element = elements.get(nodeId);
        if((element instanceof Questionnaire || element instanceof Section) && "section".equals(type)) {
          elementWindow.setTitle(new StringResourceModel("Section", QuestionnaireTreePanel.this, null));
          // TODO show SectionPropertiesPanel
        } else if((element instanceof Questionnaire || element instanceof Section) && "page".equals(type)) {
          elementWindow.setTitle(new StringResourceModel("Page", QuestionnaireTreePanel.this, null));
          // TODO show PagePropertiesPanel
        } else if(element instanceof Page && "question".equals(type)) {
          elementWindow.setTitle(new StringResourceModel("Question", QuestionnaireTreePanel.this, null));
          elementWindow.setContent(new QuestionPropertiesPanel("content", new Model<Question>(new Question("")), elementWindow) {
            @Override
            public void onSave(AjaxRequestTarget target1, Question question) {
              ((Page) element).addQuestion(question);
              // easier to reload tree than creating new node in JS
              target.addComponent(treeContainer);
            }
          });
        }
      }
    });
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

  private String addElement(IQuestionnaireElement element) {
    String id = ID_PREFIX + elementCounter++;
    elements.put(id, element);
    return id;
  }

  public class ListFragment extends Fragment {

    public ListFragment(String id, List<IQuestionnaireElement> list) {
      super(id, "listFragment", QuestionnaireTreePanel.this);

      add(new ListView<IQuestionnaireElement>("item", list) {
        @Override
        protected void populateItem(ListItem<IQuestionnaireElement> item) {
          item.setOutputMarkupId(true);

          IQuestionnaireElement element = item.getModelObject();
          item.add(new Label("itemTitle", element.getName()));

          List<IQuestionnaireElement> children = new ArrayList<IQuestionnaireElement>();
          if(element instanceof Questionnaire) {
            Questionnaire questionnaire = (Questionnaire) element;
            children.addAll(questionnaire.getSections());
            children.addAll(questionnaire.getPages());
          } else if(element instanceof Section) {
            children.addAll(((Section) element).getPages());
          } else if(element instanceof Page) {
            children.addAll(((Page) element).getQuestions());
          } else if(element instanceof Question) {
            children.addAll(((Question) element).getQuestionCategories());
          } else if(element instanceof QuestionCategory) {
            QuestionCategory questionCategory = (QuestionCategory) element;
            Category category = questionCategory.getCategory();
            if(category != null) children.add(category);
            OpenAnswerDefinition openAnswerDefinition = questionCategory.getOpenAnswerDefinition();
            if(openAnswerDefinition != null) children.add(openAnswerDefinition);
          }

          item.add(new SimpleAttributeModifier("id", addElement(element)));
          item.add(new SimpleAttributeModifier("name", element.getName()));
          item.add(new AttributeAppender("class", new Model<String>(children.isEmpty() ? "jstree-leaf" : "jstree-open"), " "));
          item.add(new AttributeAppender("rel", new Model<String>(ClassUtils.getShortClassName(element.getClass())), " "));
          if(children.isEmpty()) {
            item.add(new WebMarkupContainer("children"));
          } else {
            item.add(new ListFragment("children", children));
          }
        }
      });

    }
  }

}
