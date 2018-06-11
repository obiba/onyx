/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument;

import java.util.Iterator;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.obiba.magma.Variable;
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

    String label = model.getObject().getName() + " " + model.getObject().getVersion();

    add(new Label("title", label));

    add(new Label("questionnaire", label));

    add(new DataView<Section>("sections", new AllSectionsProvider(model)) {
      @Override
      protected void populateItem(Item<Section> item) {
        item.add(new SingleDocumentSectionPanel("sectionPanel", item.getModel()));
      }
    });

    add(new DataView<Variable>("variables", new VariablesProvider(model)) {
      @Override
      protected void populateItem(Item<Variable> item) {
        item.add(new SingleDocumentVariablePanel("variablePanel", item.getModel()));
      }
    });

  }

  private class VariablesProvider implements IDataProvider<Variable> {

    private static final long serialVersionUID = -1757316578083924986L;

    private IModel<Questionnaire> model;

    private VariablesProvider(IModel<Questionnaire> model) {
      this.model = model;
    }

    @Override
    public Iterator<? extends Variable> iterator(int first, int count) {
      return getVariables().subList(first, first + count).iterator();
    }

    @Override
    public IModel<Variable> model(Variable object) {
      return new VariableModel(object);
    }

    @Override
    public int size() {
      return getVariables().size();
    }

    @Override
    public void detach() {
      if(model != null) {
        model.detach();
      }
    }

    private List<Variable> getVariables() {
      return model.getObject().getSortedVariables();
    }

  }

  private class VariableModel implements IModel<Variable> {

    private static final long serialVersionUID = -1757316578083924986L;

    private Variable object;

    private VariableModel(Variable object) {
      this.object = object;
    }

    @Override
    public Variable getObject() {
      return object;
    }

    @Override
    public void setObject(Variable object) {
      this.object = object;
    }

    @Override
    public void detach() {

    }

  }
}
