/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabelsTab extends Panel {

  private static final long serialVersionUID = 1L;

  private final FeedbackPanel feedbackPanel;

  private final Form<?> form;

  // private final IModel qLabelModel;

  private Question question;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  public LabelsTab(String id, Question question, Form<?> form, FeedbackPanel feedbackPanel, IModel labelModel) {
    super(id, new CompoundPropertyModel<Question>(question));
    this.form = form;
    this.feedbackPanel = feedbackPanel;
    // this.qLabelModel = labelModel;
    this.question = question;

    createComponent();
  }

  private void createComponent() {
    DefaultPropertyKeyProviderImpl propProvider = new DefaultPropertyKeyProviderImpl();

    WebMarkupContainer labels = new WebMarkupContainer("labelsItem");
    for(String label : propProvider.getProperties(question)) {
      String key = propProvider.getPropertyKey(question, label);
      log.info("Key : " + key);

      TextField<String> labelInput = new TextField<String>(label, new QuestionnaireStringResourceModel(question, label));
      labelInput.add(new StringValidator.MaximumLengthValidator(20));

      labels.add(labelInput);
    }
    add(labels);

    // add(new TextField<String>("test", new ResourceModel("questionnaire.label", "")));
    // final Question question = (Question) getDefaultModelObject();
    // final IModel qLabelModel = new MessageSourceResolvableStringModel(getDefaultModel());
    //
    // TextField<String> instructions = new TextField<String>("instructions");
    // instructions.add(new StringValidator.MaximumLengthValidator(20));
    // add(instructions);
    //
    // TextField<String> caption = new TextField<String>("caption");
    // caption.add(new StringValidator.MaximumLengthValidator(20));
    // add(caption);
    //
    // TextField<String> help = new TextField<String>("help");
    // help.add(new StringValidator.MaximumLengthValidator(20));
    // add(help);
    //
    // TextField<String> specifications = new TextField<String>("specifications");
    // specifications.add(new StringValidator.MaximumLengthValidator(20));
    // add(specifications);
  }
}
