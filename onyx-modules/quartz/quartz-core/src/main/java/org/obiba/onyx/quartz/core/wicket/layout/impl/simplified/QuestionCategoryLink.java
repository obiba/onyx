/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategoryLinkSelectionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.QuestionCategorySelectionBehavior;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.wicket.link.AjaxImageLink;

/**
 * A link for selecting a question category, without open answers.
 */
public class QuestionCategoryLink extends AbstractQuestionCategoryLinkSelectionPanel {
  //
  // Constants
  //

  private static final long serialVersionUID = 1L;

  //
  // Constructors
  //

  public QuestionCategoryLink(String id, IModel questionCategoryModel, IModel labelModel, IModel descriptionModel) {
    this(id, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion()), questionCategoryModel, labelModel, descriptionModel);
  }

  public QuestionCategoryLink(String id, IModel questionCategoryModel, IModel labelModel) {
    this(id, new QuestionnaireModel(((QuestionCategory) questionCategoryModel.getObject()).getQuestion()), questionCategoryModel, labelModel, null);
  }

  @SuppressWarnings("serial")
  public QuestionCategoryLink(String id, IModel questionModel, IModel questionCategoryModel, IModel labelModel, IModel descriptionModel) {
    super(id, questionModel, questionCategoryModel, labelModel, descriptionModel);
  }

  //
  // AbstractQuestionCategoryLinkSelectionPanel Methods
  //

  protected void addLinkComponent(IModel labelModel, IModel descriptionModel) {
    AjaxImageLink link = new AjaxImageLink("link", labelModel, descriptionModel) {

      private static final long serialVersionUID = 1L;

      @Override
      public void onClick(AjaxRequestTarget target) {
        QuestionCategoryLink.this.handleSelectionEvent(target);
      }

    };
    link.add(new QuestionCategorySelectionBehavior());
    link.getLink().add(new AbstractBehavior() {
      @Override
      public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        // prevent from drag
        tag.getAttributes().put("onmousedown", "if (event.preventDefault) {event.preventDefault();}return false;");
      }
    });
    add(link);
  }
}
