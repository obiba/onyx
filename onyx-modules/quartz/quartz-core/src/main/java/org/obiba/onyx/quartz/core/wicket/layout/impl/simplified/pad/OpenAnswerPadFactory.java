/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad;

import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.util.data.DataType;

/**
 * 
 */
public class OpenAnswerPadFactory {

  public static AbstractOpenAnswerDefinitionPanel create(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel, ModalWindow padWindow) {
    DataType type = ((QuestionCategory) questionCategoryModel.getObject()).getOpenAnswerDefinition().getDataType();
    if(type.equals(DataType.INTEGER) || type.equals(DataType.DECIMAL)) {
      return new NumericPad(id, questionModel, questionCategoryModel, openAnswerDefinitionModel, padWindow);
    } else {
      throw new UnsupportedOperationException("Pad for type " + type + " not supported yet.");
    }
  }

}
