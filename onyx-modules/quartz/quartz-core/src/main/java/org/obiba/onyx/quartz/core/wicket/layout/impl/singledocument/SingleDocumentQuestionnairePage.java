/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.obiba.magma.MagmaEngine;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.wicket.provider.AllSectionsProvider;

/**
 */
public class SingleDocumentQuestionnairePage extends WebPage {

  private static final long serialVersionUID = -1757316578083924986L;

  @SuppressWarnings("serial")
  public SingleDocumentQuestionnairePage(IModel<Questionnaire> model) {
    super(model);
    new MagmaEngine();

    add(new Label("title", new PropertyModel<String>(model, "name")));

    add(new DataView<Section>("sections", new AllSectionsProvider(model)) {
      @Override
      protected void populateItem(Item<Section> item) {
        item.add(new SingleDocumentSectionPanel("sectionPanel", item.getModel()));
      }
    });

  }
}
