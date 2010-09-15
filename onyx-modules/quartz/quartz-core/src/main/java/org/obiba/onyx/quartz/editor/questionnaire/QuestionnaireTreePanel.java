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
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.SimpleAttributeModifier;
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
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.editor.widget.jsTree.JsTreeBehavior;

/**
 *
 */
@SuppressWarnings("serial")
public class QuestionnaireTreePanel extends Panel {

  private static final String ID_PREFIX = "element_";

  private Map<String, IQuestionnaireElement> elements = new HashMap<String, IQuestionnaireElement>();

  private int elementCounter;

  private Label moveCallback;

  private AbstractDefaultAjaxBehavior ajaxBehavior;

  public QuestionnaireTreePanel(String id, IModel<Questionnaire> model) {
    super(id, model);

    add(JavascriptPackageResource.getHeaderContribution(QuestionnaireTreePanel.class, "QuestionnaireTreePanel.js"));

    Questionnaire questionnaire = model.getObject();
    final String treeId = "tree_" + questionnaire.getName();

    WebMarkupContainer treeContainer = new WebMarkupContainer("treeContainer");
    treeContainer.setMarkupId(treeId);
    treeContainer.setOutputMarkupId(true);

    add(treeContainer);

    List<IQuestionnaireElement> root = new ArrayList<IQuestionnaireElement>();
    root.add(questionnaire);
    ListFragment listFragment = new ListFragment("tree", root);
    listFragment.add(new JsTreeBehavior());
    treeContainer.add(listFragment);

    ajaxBehavior = new AbstractDefaultAjaxBehavior() {

      @Override
      public void renderHead(IHeaderResponse response) {
        response.renderOnLoadJavascript("Wicket.QTree.buildTree('" + treeId + "')");
      }

      @Override
      protected void respond(AjaxRequestTarget target) {
        Request request = RequestCycle.get().getRequest();
        String nodeId = request.getParameter("nodeId");
        String newParentId = request.getParameter("newParentId");
        String newPosition = request.getParameter("newPosition");
        // TODO move node
      }

    };
    add(ajaxBehavior);

    moveCallback = new Label("moveCallback", "");
    moveCallback.setOutputMarkupId(true);
    moveCallback.setEscapeModelStrings(false);
    add(moveCallback);
  }

  @Override
  protected void onBeforeRender() {
    super.onBeforeRender();
    String script = "Wicket.QTree.moveCallback = function(nodeId, newParentId, newPosition) {\n" + //
    "  wicketAjaxGet('" + ajaxBehavior.getCallbackUrl(true) + "&nodeId='+ nodeId +'&newParentId='+ newParentId +'&newPosition='+ newPosition, function() { alert('success'); }, function() { alert('error'); });" + //
    "\n}";
    moveCallback.setDefaultModelObject(script);
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
