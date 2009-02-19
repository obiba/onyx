/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyNamingStrategy;

/**
 * 
 */
public class DefaultPropertyKeyNamingStrategy implements IPropertyKeyNamingStrategy {

  public String getPropertyKey(IQuestionnaireElement localizable, String property) {
    return localizable.getClass().getSimpleName() + "." + localizable.getName() + "." + property;
  }

}
