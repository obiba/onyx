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

/**
 * Add some more properties for touchscreen questionnaire layout.
 */
public class SimplifiedUIPropertyKeyProviderImpl extends DefaultPropertyKeyProviderImpl {

  public SimplifiedUIPropertyKeyProviderImpl() {
    super();
    getQuestionnaireProperties().add("clearAll");
    getQuestionnaireProperties().add("or");
    getQuestionnaireProperties().add("clickHere");
    getQuestionnaireProperties().add("ok");
    getQuestionnaireProperties().add("cancel");
    getQuestionnaireProperties().add("reset");
    getCategoryProperties().add("description");
    getCategoryProperties().add("imageSelected");
    getCategoryProperties().add("imageDeselected");
  }

}
