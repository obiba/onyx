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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.OddEvenListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.util.ListModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.springframework.util.ClassUtils;

public class QuestionnaireElementListPanel<T extends IQuestionnaireElement> extends Panel {

  private static final long serialVersionUID = 1L;

  public QuestionnaireElementListPanel(String id, ListModel<T> model, Class<T> clazz) {
    super(id, model);
    createComponent(model, clazz);
  }

  private void createComponent(ListModel<T> model, Class<T> clazz) {

    add(new Label("labelTitle", ClassUtils.getShortName(clazz)));

    ListView<T> listView = new ListView<T>("listQuestionnaireElement", model) {

      private static final long serialVersionUID = 1L;

      @Override
      protected ListItem<T> newItem(int index) {
        // FIXME don't work no odd or even class added
        return new OddEvenListItem<T>(index, getListItemModel(getModel(), index));
      }

      @Override
      protected void populateItem(ListItem<T> item) {
        item.add(new Label("labelQuestionnaireElement", item.getModelObject().getName()));
      }
    };
    add(listView);
  }
}
