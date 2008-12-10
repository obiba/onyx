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

import org.apache.wicket.model.IModel;

/**
 * Question categories are presented in a dropdown.
 */
public class DropDownQuestionPanel extends BaseQuestionPanel {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public DropDownQuestionPanel(String id, IModel questionModel) {
    super(id, questionModel);
    setOutputMarkupId(true);
  }

  @Override
  protected void setContent(String id) {
    add(new DropDownQuestionCategoriesPanel(id, getModel()));

  }

}
