/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.validation.IValidatable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;

public class ValidationUtils {
  public static boolean mustValidate(IQuestionnaireElement element, IValidatable<String> validatable) {
    return !StringUtils.trimToEmpty(element.getName()).equals(StringUtils.trimToEmpty(validatable.getValue()));
  }
}
