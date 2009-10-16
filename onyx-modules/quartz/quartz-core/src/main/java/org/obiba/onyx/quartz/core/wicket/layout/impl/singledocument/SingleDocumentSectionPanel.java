/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.ReuseIfModelsEqualStrategy;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.wicket.layout.impl.behavior.QuestionnaireStyleBehavior;
import org.obiba.onyx.quartz.core.wicket.provider.AllPagesProvider;
import org.obiba.onyx.quartz.core.wicket.provider.AllQuestionsProvider;
import org.obiba.onyx.quartz.core.wicket.provider.AllSubSectionsProvider;

/**
 */
public class SingleDocumentSectionPanel extends Panel {

  private static final long serialVersionUID = -1757316578083924986L;

  @SuppressWarnings("serial")
  public SingleDocumentSectionPanel(String id, IModel<Section> model) {
    super(id, model);
    add(new Label("sectionLabel", model.getObject().getName()));

    add(new SingleDocumentPageView("pages", new AllPagesProvider(model)));

    add(new DataView<Section>("subsections", new AllSubSectionsProvider(model)) {
      @Override
      protected void populateItem(Item<Section> item) {
        item.add(new SingleDocumentSectionPanel("sectionPanel", item.getModel()));
      }
    });
  }

  /**
   * A repeater for the pages within a section
   */
  private static class SingleDocumentPageView extends DataView<Page> {

    private static final long serialVersionUID = -1757316578083924986L;

    public SingleDocumentPageView(String id, IDataProvider<Page> provider) {
      super(id, provider);
      setOutputMarkupId(true);
      add(new QuestionnaireStyleBehavior());
      setItemReuseStrategy(ReuseIfModelsEqualStrategy.getInstance());
    }

    @Override
    protected void populateItem(Item<Page> item) {
      item.add(new Label("pageLabel", item.getModelObject().getName()));
      item.add(new DataView<Question>("questions", new AllQuestionsProvider(item.getModel())) {

        private static final long serialVersionUID = 1601751763143168458L;

        @Override
        protected void populateItem(Item<Question> item) {
          item.add(new SingleDocumentQuestionPanel("questionPanel", item.getModel()));
          // TODO: Add the question details panel here
          // item.add(new SingleDocumentQuestionDetailsPanel("questionDetails", item.getModel()));
        }
      });
    }
  }
}
