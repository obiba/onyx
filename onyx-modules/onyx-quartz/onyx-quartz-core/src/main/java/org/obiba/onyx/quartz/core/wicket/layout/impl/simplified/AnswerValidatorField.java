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

import org.apache.wicket.markup.html.form.HiddenField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;

/**
 * A hidden field that performs the validation of answers on form submit.
 */
public class AnswerValidatorField extends HiddenField {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor, for validating the given question.
   * @param id
   * @param questionModel
   */
  public AnswerValidatorField(String id, IModel questionModel) {
    super(id, new Model());
    add(new AnswerCountValidator(questionModel));
    setRequired(false);
  }

}
