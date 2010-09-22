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
import java.util.Locale;
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
import org.obiba.onyx.quartz.editor.form.QuestionnaireElementWebPage;
import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;
import org.obiba.onyx.quartz.editor.page.PagePropertiesPanel;
import org.obiba.onyx.quartz.editor.question.QuestionPropertiesPanel;
import org.obiba.onyx.quartz.editor.section.SectionPropertiesPanel;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class QuestionnaireTreePanel extends Panel {

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private static final String ID_PREFIX = "element_";

  protected Map<String, IQuestionnaireElement> elements = new HashMap<String, IQuestionnaireElement>();

  private int elementCounter;

  private AbstractDefaultAjaxBehavior moveBehavior;

  private Label moveCallback;

  private AbstractDefaultAjaxBehavior editBehavior;

  private Label editCallback;

  private AbstractDefaultAjaxBehavior deleteBehavior;

  private Label deleteCallback;

  private AbstractDefaultAjaxBehavior addChildBehavior;

  private Label addChildCallback;

  protected String treeId;

  protected WebMarkupContainer treeContainer;

  protected ModalWindow elementWindow;

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model) {
    super(id, model);

    elementWindow = new ModalWindow("elementWindow");
    elementWindow.setCssClassName("onyx");
    elementWindow.setResizable(true);
    elementWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true;
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
      if(element instanceof Section) {
        Section section = (Section) element;
        if(newParent instanceof Questionnaire) {
          if(section.getParentSection() == null) {
            ((Questionnaire) newParent).removeSection(section);
          } else {
            section.getParentSection().removeSection(section);
          }
          ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).addSection(section, position);

        } else if(newParent instanceof Section) {
          if(section.getParentSection() == null) {
            ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).removeSection(section);
          } else {
            section.getParentSection().removeSection(section);
          }
          ((Section) newParent).addSection(section, position);
        }
      } else if(element instanceof Page) {
        Page page = (Page) element;
        if(newParent instanceof Questionnaire) {
          if(page.getSection() == null) {
            ((Questionnaire) newParent).removePage(page);
          } else {
            page.getSection().removePage(page);
          }
          ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).addPage(page, position);

        } else if(newParent instanceof Section) {
          if(page.getSection() == null) {
            ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).removePage(page);
          } else {
            page.getSection().removePage(page);
          }
          ((Section) newParent).addPage(page, position);
        }
      } else if(element instanceof Question) {
        Question question = (Question) element;
        question.getPage().removeQuestion(question);
        ((Page) newParent).addQuestion(question, position);
      }
    }
  }

  protected class EditBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(final AjaxRequestTarget respondTarget) {
      final String nodeId = RequestCycle.get().getRequest().getParameter("nodeId");
      log.info("Edit " + nodeId);
      final IQuestionnaireElement element = elements.get(nodeId);
      if(element instanceof Section) {
        elementWindow.setTitle(new StringResourceModel("Section", QuestionnaireTreePanel.this, null));
        elementWindow.setPageCreator(new ModalWindow.PageCreator() {

          @Override
          public org.apache.wicket.Page createPage() {
            return new QuestionnaireElementWebPage(new SectionPropertiesPanel("content", new Model<Section>((Section) element), ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()), elementWindow) {

              @Override
              public void onSave(AjaxRequestTarget target, Section section) {
                super.onSave(target, section);
                // update node name in jsTree
                target.appendJavascript("$('#" + treeId + "').jstree('rename_node'," + nodeId + ", '" + section.getName() + "');");
              }
            });
          }
        });
      } else if(element instanceof Page) {
        elementWindow.setTitle(new StringResourceModel("Page", QuestionnaireTreePanel.this, null));
        elementWindow.setPageCreator(new ModalWindow.PageCreator() {

          @Override
          public org.apache.wicket.Page createPage() {
            return new QuestionnaireElementWebPage(new PagePropertiesPanel("content", new Model<Page>((Page) element), ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()), elementWindow) {
              @Override
              public void onSave(AjaxRequestTarget target, Page page) {
                super.onSave(target, page);
                // update node name in jsTree
                target.appendJavascript("$('#" + treeId + "').jstree('rename_node', #" + nodeId + ", '" + page.getName() + "');");
              }
            });

          }
        });
      } else if(element instanceof Question) {
        elementWindow.setTitle(new StringResourceModel("Question", QuestionnaireTreePanel.this, null));
        elementWindow.setPageCreator(new ModalWindow.PageCreator() {

          @Override
          public org.apache.wicket.Page createPage() {

            return new QuestionnaireElementWebPage(new QuestionPropertiesPanel("content", new Model<Question>((Question) element), ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()), elementWindow) {
              @Override
              public void onSave(AjaxRequestTarget target, Question question) {
                super.onSave(target, question);
                // update node name in jsTree
                target.appendJavascript("$('#" + treeId + "').jstree('rename_node', $('#" + nodeId + "'), '" + question.getName() + "');");
              }
            });
          }
        });
      }
      elementWindow.show(respondTarget);
    }
  }

  protected class DeleteBehavior extends AbstractDefaultAjaxBehavior {
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
  }

  protected class AddChildBehavior extends AbstractDefaultAjaxBehavior {
    @Override
    protected void respond(final AjaxRequestTarget respondTarget) {
      Request request = RequestCycle.get().getRequest();
      String nodeId = request.getParameter("nodeId");
      String type = request.getParameter("type");
      log.info("Add " + type + " to " + nodeId);
      final IQuestionnaireElement element = elements.get(nodeId);
      if((element instanceof Questionnaire || element instanceof Section) && "section".equals(type)) {
        elementWindow.setTitle(new StringResourceModel("Section", QuestionnaireTreePanel.this, null));
        elementWindow.setPageCreator(new ModalWindow.PageCreator() {

          @Override
          public org.apache.wicket.Page createPage() {
            return new QuestionnaireElementWebPage(new SectionPropertiesPanel("content", new Model<Section>(new Section(null)), ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()), elementWindow) {
              @Override
              public void onSave(AjaxRequestTarget target, Section section) {
                super.onSave(target, section);
                if(element instanceof Questionnaire) {
                  ((Questionnaire) element).addSection(section);
                } else if(element instanceof Section) {
                  ((Section) element).addSection(section);
                }
                target.addComponent(treeContainer);
              }
            });
          }
        });
      } else if((element instanceof Questionnaire || element instanceof Section) && "page".equals(type)) {
        elementWindow.setTitle(new StringResourceModel("Page", QuestionnaireTreePanel.this, null));
        elementWindow.setPageCreator(new ModalWindow.PageCreator() {

          @Override
          public org.apache.wicket.Page createPage() {
            return new QuestionnaireElementWebPage(new PagePropertiesPanel("content", new Model<Page>(new Page(null)), ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()), elementWindow) {
              @Override
              public void onSave(AjaxRequestTarget target, Page page) {
                super.onSave(target, page);
                if(element instanceof Questionnaire) {
                  ((Questionnaire) element).addPage(page);
                } else if(element instanceof Section) {
                  ((Section) element).addPage(page);
                }
                target.addComponent(treeContainer);
              }
            });
          }
        });
      } else if(element instanceof Page && "question".equals(type)) {
        elementWindow.setTitle(new StringResourceModel("Question", QuestionnaireTreePanel.this, null));
        elementWindow.setPageCreator(new ModalWindow.PageCreator() {

          @Override
          public org.apache.wicket.Page createPage() {
            Question newQuestion = new Question(null);
            List<LocaleProperties> listLocaleProperties = new ArrayList<LocaleProperties>();
            for(Locale locale : ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()).getLocales()) {
              listLocaleProperties.add(new LocaleProperties(locale, newQuestion));
            }
            return new QuestionnaireElementWebPage(new QuestionPropertiesPanel("content", new Model<Question>(newQuestion), ((Questionnaire) QuestionnaireTreePanel.this.getDefaultModelObject()), elementWindow) {
              @Override
              public void onSave(AjaxRequestTarget target, Question question) {
                super.onSave(target, question);
                ((Page) element).addQuestion(question);
                // easier to reload tree than creating new node in JS
                target.addComponent(treeContainer);
              }
            });
          }
        });
      }
      elementWindow.show(respondTarget);
    }
  }

  protected String addElement(IQuestionnaireElement element) {
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
