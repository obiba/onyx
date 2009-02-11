/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified.pad;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractOpenAnswerDefinitionPanel;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class NumericPad extends AbstractOpenAnswerDefinitionPanel implements IPadSelectionListener {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(NumericPad.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  /**
   * @param id
   * @param model
   */
  @SuppressWarnings("serial")
  public NumericPad(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel, final ModalWindow padWindow) {
    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);

    final DataType type = getOpenAnswerDefinition().getDataType();

    add(new PadButton("0", new Model("0")));
    add(new PadButton("1", new Model("1")));
    add(new PadButton("2", new Model("2")));
    add(new PadButton("3", new Model("3")));
    add(new PadButton("4", new Model("4")));
    add(new PadButton("5", new Model("5")));
    add(new PadButton("6", new Model("6")));
    add(new PadButton("7", new Model("7")));
    add(new PadButton("8", new Model("8")));
    add(new PadButton("9", new Model("9")));
    add(new PadButton("dot", new Model(".")).setButtonEnabled(type.equals(DataType.DECIMAL)));
    add(new PadButton("clear", new Model("C")));

    add(new Label("category", new QuestionnaireStringResourceModel(questionCategoryModel, "label")));

    add(new Label("value", new PropertyModel(this, "data.valueAsString")).setOutputMarkupId(true));

    AjaxLink link = new AjaxLink("ok") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        // build final data
        Data data = getData();
        if(data != null && data.getValue() != null && data.getValueAsString().length() > 0) {
          try {
            setData(DataBuilder.build(type, data.getValueAsString()));
            // Validatable validatable = new Validatable(getData()) {
            // @Override
            // public void error(IValidationError error) {
            // // TODO Auto-generated method stub
            // super.error(error);
            // MessageSource source = new MessageSource();
            // String message = error.getErrorMessage(source);
            // NumericPad.this.error(new ValidationErrorFeedback(error, message));
            // }
            // };
            // for(IDataValidator validator : getOpenAnswerDefinition().getValidators()) {
            // validator.validate(validatable);
            // }
          } catch(Exception e) {
            log.warn("Failed parsing as a " + type + ":" + data.getValueAsString(), e);
            setData(null);
          }
        } else {
          setData(null);
        }

        // persist
        if(!getQuestion().isMultiple()) {
          activeQuestionnaireAdministrationService.deleteAnswers(getQuestion());
        } else {
          activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestionCategory());
        }
        if(getData() != null) {
          activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition(), getData());
        }

        // close pad modal window
        padWindow.close(target);
      }

    };
    link.add(new AttributeModifier("value", new Model("Ok")));
    add(link);

    link = new AjaxLink("cancel") {

      @Override
      public void onClick(AjaxRequestTarget target) {
        setData(null);
        if(!getQuestion().isMultiple()) {
          activeQuestionnaireAdministrationService.deleteAnswers(getQuestion());
        } else {
          activeQuestionnaireAdministrationService.deleteAnswer(getQuestion(), getQuestionCategory());
        }
        padWindow.close(target);
      }

    };
    link.add(new AttributeModifier("value", new Model("Cancel")));
    add(link);

  }

  @Override
  public void resetField() {
    setData(null);
  }

  public void onPadSelection(AjaxRequestTarget target, IModel model) {
    String key = (String) model.getObject();
    if(key.equals("C")) {
      setData(null);
    } else {
      Data data = getData();
      if(data == null) {
        setData(DataBuilder.buildText(key));
      } else {
        String val = data.getValueAsString();
        if(key.equals(".") && val.contains(".")) {
          // ignore
        } else {
          setData(DataBuilder.buildText(val + key));
        }
      }
    }

    target.addComponent(get("value"));
  }

}
