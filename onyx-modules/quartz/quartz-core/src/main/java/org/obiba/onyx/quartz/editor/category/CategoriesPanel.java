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

import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.question.EditedQuestion;
import org.obiba.onyx.quartz.editor.question.EditedQuestion.Layout;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;

/**
 *
 */
@SuppressWarnings("serial")
public class CategoriesPanel extends Panel {

  // private final transient Logger log = LoggerFactory.getLogger(getClass());

  public CategoriesPanel(String id, final IModel<EditedQuestion> model, final IModel<Questionnaire> questionnaireModel, final IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);

    RadioGroup<Layout> layout = new RadioGroup<Layout>("layout", new PropertyModel<Layout>(model, "layout"));
    layout.setLabel(new ResourceModel("Layout"));
    layout.setRequired(true);
    add(layout);

    Radio<Layout> singleColumnLayout = new Radio<Layout>("singleColumnLayout", new Model<Layout>(Layout.SINGLE_COLUMN));
    singleColumnLayout.setLabel(new ResourceModel("Layout.single"));
    layout.add(singleColumnLayout);
    layout.add(new SimpleFormComponentLabel("singleColumnLayoutLabel", singleColumnLayout));

    Radio<Layout> gridLayout = new Radio<Layout>("gridLayout", new Model<Layout>(Layout.GRID));
    gridLayout.setLabel(new ResourceModel("Layout.grid"));
    layout.add(gridLayout);
    layout.add(new SimpleFormComponentLabel("gridLayoutLabel", gridLayout));

    TextField<Integer> nbRowsField = new TextField<Integer>("nbRows", new PropertyModel<Integer>(model, "nbRows")) {
      @Override
      public boolean isRequired() {
        return model.getObject().getLayout() == Layout.GRID;
      }
    };
    gridLayout.setLabel(new ResourceModel("NbRows"));
    add(nbRowsField);

    add(new CategoryListPanel("categories", model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow));
  }

}
