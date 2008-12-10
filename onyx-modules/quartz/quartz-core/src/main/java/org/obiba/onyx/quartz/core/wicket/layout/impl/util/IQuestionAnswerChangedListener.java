/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.util;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

/**
 * Listener of question answers.
 */
public interface IQuestionAnswerChangedListener {

  /**
   * Called when answer persistence has changed.
   * @param target
   * @param questionModel
   * @param questionCategoryModel
   */
  public void onQuestionAnswerChanged(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel);

}
