/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.utils;

/**
 *
 */
public class QuestionnaireConverterException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public QuestionnaireConverterException() {
  }

  /**
   * @param arg0
   */
  public QuestionnaireConverterException(String arg0) {
    super(arg0);
  }

  /**
   * @param arg0
   */
  public QuestionnaireConverterException(Throwable arg0) {
    super(arg0);
  }

  /**
   * @param arg0
   * @param arg1
   */
  public QuestionnaireConverterException(String arg0, Throwable arg1) {
    super(arg0, arg1);
  }

}
