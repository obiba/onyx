/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractMultipleOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;

/**
 * UI for OpenAnswer having other open answer children.
 */
public class MultipleDefaultOpenAnswerDefinitionPanel extends AbstractMultipleOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public MultipleDefaultOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
  }

  @SuppressWarnings("serial")
  @Override
  protected AbstractOpenAnswerDefinitionPanel newOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    return new DefaultOpenAnswerDefinitionPanel(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);
    // {
    //
    // // @Override
    // // public void onSelect(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, IModel
    // // openAnswerDefinitionModel) {
    // // MultipleDefaultOpenAnswerDefinitionPanel.this.onSelect(target, questionModel, questionCategoryModel,
    // // openAnswerDefinitionModel);
    // // }
    //
    // @Override
    // public void onSubmit(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    // MultipleDefaultOpenAnswerDefinitionPanel.this.onSubmit(target, questionModel, questionCategoryModel);
    // }
    //
    // @Override
    // public void onError(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel) {
    // MultipleDefaultOpenAnswerDefinitionPanel.this.onError(target, questionModel, questionCategoryModel);
    // }
    //
    // };
  }
}
