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

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractMultipleOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;

/**
 * UI for OpenAnswer having other open answer children.
 */
public class MultipleSimplifiedOpenAnswerDefinitionPanel extends AbstractMultipleOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public MultipleSimplifiedOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
  }

  @SuppressWarnings("serial")
  @Override
  protected AbstractOpenAnswerDefinitionPanel newOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    return new SimplifiedOpenAnswerDefinitionPanel(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);
  }
}
