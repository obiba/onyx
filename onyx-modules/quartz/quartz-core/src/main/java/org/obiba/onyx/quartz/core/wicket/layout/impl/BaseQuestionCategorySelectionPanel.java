/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.IQuestionAnswerChangedListener;
import org.obiba.onyx.wicket.wizard.WizardForm;

/**
 * Base utility methods for sharing some question category selection callbacks.
 */
public abstract class BaseQuestionCategorySelectionPanel extends Panel {

  /**
   * @param id
   * @param model
   */
  public BaseQuestionCategorySelectionPanel(String id, IModel model) {
    super(id, model);
  }

  /**
   * Find the feedback panel associated to the wizard form and update it regarding the ajax target.
   * @param target
   */
  protected void updateFeedbackPanel(AjaxRequestTarget target) {
    WizardForm wizard = (WizardForm) findParent(WizardForm.class);
    if(wizard != null && wizard.getFeedbackPanel() != null) {
      target.addComponent(wizard.getFeedbackPanel());
    }
  }

  /**
   * Find the first parent that will to be warned about a changing question answer.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   * @see IQuestionAnswerChangedListener
   */
  protected void fireQuestionAnswerChanged(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    IQuestionAnswerChangedListener parentListener = (IQuestionAnswerChangedListener) findParent(IQuestionAnswerChangedListener.class);
    if(parentListener != null) {
      parentListener.onQuestionAnswerChanged(target, questionModel, questionCategoryModel);
    }
  }

}
