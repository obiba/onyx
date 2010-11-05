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

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;

/**
 * List of questions.
 */
public class SingleDocumentQuestionListPanel extends Panel {

  private static final long serialVersionUID = 1L;

  public SingleDocumentQuestionListPanel(String id, IDataProvider<Question> dataProvider) {
    super(id);
    add(new DataView<Question>("questions", dataProvider) {
      private static final long serialVersionUID = -3937116965166887192L;

      @Override
      protected void populateItem(Item<Question> item) {
        if(item.getModel().getObject().getUIFactoryName().contains(DropDownQuestionPanelFactory.class.getSimpleName())) {
          item.add(new DropDownQuestionPanel("questionPanel", item.getModel()));
        } else
          item.add(new SingleDocumentQuestionPanel("questionPanel", item.getModel()));
      }
    });
  }
}
