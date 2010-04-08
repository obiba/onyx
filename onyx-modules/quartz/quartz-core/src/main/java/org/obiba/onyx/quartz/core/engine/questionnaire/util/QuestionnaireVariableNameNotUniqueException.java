/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;

/**
 * An questionnaire variable name explicitly set by the user is not unique.
 */
public class QuestionnaireVariableNameNotUniqueException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String variableName;

  private final IQuestionnaireElement duplicate;

  private final IQuestionnaireElement original;

  public QuestionnaireVariableNameNotUniqueException(String variableName, IQuestionnaireElement duplicate, IQuestionnaireElement original) {
    this.variableName = variableName;
    this.duplicate = duplicate;
    this.original = original;
  }

  @Override
  public String getMessage() {
    String duplicateName = duplicate != null ? duplicate.getName() : "null";
    String originalName = original != null ? original.getName() : "null";
    return new StringBuilder().append("The user configured variable name [").append(variableName).append("] is use by both [").append(duplicateName).append("] and [").append(originalName).append("].").toString();
  }
}
