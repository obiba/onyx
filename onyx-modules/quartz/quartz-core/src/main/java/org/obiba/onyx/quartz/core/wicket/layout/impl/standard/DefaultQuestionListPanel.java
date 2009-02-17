/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.standard;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.QuestionPanelFactoryRegistry;

/**
 * List of questions.
 */
public class DefaultQuestionListPanel extends Panel {

  private static final long serialVersionUID = 1L;

  @SpringBean
  private QuestionPanelFactoryRegistry questionPanelFactoryRegistry;

  /**
   * @param id
   * @param dataProvider
   */
  public DefaultQuestionListPanel(String id, IDataProvider dataProvider) {
    super(id);
    add(new DataView("questions", dataProvider) {
      private static final long serialVersionUID = -3937116965166887192L;

      @Override
      protected void populateItem(Item item) {
        Question question = (Question) item.getModelObject();

        QuestionPanel panel = questionPanelFactoryRegistry.get(question.getUIFactoryName()).createPanel("question", item.getModel());
        item.add(panel);
      }
    }.setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance()));
  }

}
