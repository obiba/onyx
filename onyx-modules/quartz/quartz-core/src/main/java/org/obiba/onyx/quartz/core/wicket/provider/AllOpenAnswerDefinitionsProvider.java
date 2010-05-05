/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;

public class AllOpenAnswerDefinitionsProvider extends AbstractQuestionnaireElementProvider<OpenAnswerDefinition, QuestionCategory> {

  private static final long serialVersionUID = 929587284672308157L;

  public AllOpenAnswerDefinitionsProvider(IModel<QuestionCategory> model) {
    super(model);
  }

  @Override
  protected List<OpenAnswerDefinition> getElementList() {
    List<OpenAnswerDefinition> openAnswerDefinitions = new ArrayList<OpenAnswerDefinition>();

    if(getProviderElement().getOpenAnswerDefinition() != null) {
      openAnswerDefinitions.addAll(getChildOpenAnswerDefinition(getProviderElement().getOpenAnswerDefinition()));
    }

    return openAnswerDefinitions;
  }

  private List<OpenAnswerDefinition> getChildOpenAnswerDefinition(OpenAnswerDefinition parentOpenAnswerdefinition) {
    if(parentOpenAnswerdefinition.getOpenAnswerDefinitions().isEmpty()) {
      return Arrays.asList(new OpenAnswerDefinition[] { parentOpenAnswerdefinition });
    } else {
      return parentOpenAnswerdefinition.getOpenAnswerDefinitions();
    }

  }
}
