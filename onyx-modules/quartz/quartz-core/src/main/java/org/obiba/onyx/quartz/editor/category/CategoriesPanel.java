/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.category;

import static org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator.ROW_COUNT_KEY;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.value.ValueMap;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.ListToGridPermutator;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class CategoriesPanel extends Panel {

  protected static final String SINGLE_COLUMN_LAYOUT = "singleColumnLayout";

  protected static final String GRID_LAYOUT = "gridLayout";

  // private final transient Logger logger = LoggerFactory.getLogger(getClass());

  protected RadioGroup<String> layout;

  protected TextField<Integer> nbRowsField;

  public CategoriesPanel(String id, final IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel, final IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);

    Question question = model.getObject().getElement();

    // radio group without default selection
    String layoutValue = null;
    ValueMap uiArgumentsValueMap = question.getUIArgumentsValueMap();
    Integer nbRows = ListToGridPermutator.DEFAULT_ROW_COUNT;
    if(uiArgumentsValueMap != null && uiArgumentsValueMap.containsKey(ROW_COUNT_KEY)) {
      layoutValue = Integer.parseInt((String) uiArgumentsValueMap.get(ROW_COUNT_KEY)) == question.getCategories().size() ? SINGLE_COLUMN_LAYOUT : GRID_LAYOUT;
      nbRows = uiArgumentsValueMap.getInt(ROW_COUNT_KEY);
    }

    layout = new RadioGroup<String>("layout", new Model<String>(uiArgumentsValueMap == null ? null : layoutValue));
    layout.setLabel(new ResourceModel("Layout"));
    layout.setRequired(true);
    add(layout);

    Radio<String> singleColumnLayout = new Radio<String>(SINGLE_COLUMN_LAYOUT, new Model<String>(SINGLE_COLUMN_LAYOUT));
    singleColumnLayout.setLabel(new ResourceModel("Layout.single"));
    layout.add(singleColumnLayout);
    layout.add(new SimpleFormComponentLabel("singleColumnLayoutLabel", singleColumnLayout));

    Radio<String> gridLayout = new Radio<String>(GRID_LAYOUT, new Model<String>(GRID_LAYOUT));
    gridLayout.setLabel(new ResourceModel("Layout.grid"));
    layout.add(gridLayout);
    layout.add(new SimpleFormComponentLabel("gridLayoutLabel", gridLayout));

    nbRowsField = new TextField<Integer>("nbRows", new Model<Integer>(nbRows), Integer.class);
    gridLayout.setLabel(new ResourceModel("NbRows"));
    add(nbRowsField);

    add(new CategoryListPanel("categories", model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow));
  }

  public abstract void onSave(AjaxRequestTarget target);
}
