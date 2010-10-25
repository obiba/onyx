/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor;

import java.io.Serializable;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;

public class EditedElement<T extends IQuestionnaireElement> implements Serializable {

  private static final long serialVersionUID = 1L;

  private T element;

  public EditedElement(T element) {
    this.element = element;
  }

  public T getElement() {
    return element;
  }

  public void setElement(T element) {
    this.element = element;
  }

}
