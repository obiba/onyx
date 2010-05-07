/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.obiba.magma.type.BooleanType;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireUniqueVariableNameResolver;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;

public class SingleDocumentQuestionDetailsPanel extends QuestionPanel {

  private static final long serialVersionUID = 1L;

  public SingleDocumentQuestionDetailsPanel(String id, IModel<Question> questionModel) {
    super(id, questionModel);

    Question question = (Question) getDefaultModelObject();
    QuestionnaireUniqueVariableNameResolver variableNameResolver = new QuestionnaireUniqueVariableNameResolver();
    add(new Label("label", variableNameResolver.variableName(question)));
    if(question.isBoilerPlate() || question.hasSubQuestions()) {
      add(new Label("type", "[" + BooleanType.get().getName() + "]"));
    } else {
      add(new Label("type", "[" + TextType.get().getName() + "]"));
    }

    add(new SingleDocumentQuestionContentPanel("content", questionModel));
  }
}
