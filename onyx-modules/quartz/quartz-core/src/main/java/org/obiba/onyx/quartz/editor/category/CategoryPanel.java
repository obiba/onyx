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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.editor.openAnswerDefinition.OpenAnswerWindow;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableList;
import org.obiba.onyx.quartz.editor.widget.sortable.SortableListCallback;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.springframework.util.CollectionUtils;

@SuppressWarnings("serial")
public class CategoryPanel extends Panel {

  // @SpringBean
  // private LocalePropertiesUtils localePropertiesUtils;

  private ModalWindow openAnswerWindow;

  // private ListModel<LocaleProperties> localeProperties;

  private SortableList<OpenAnswerDefinition> openAnswerDefinitionList;

  public CategoryPanel(String id, IModel<QuestionCategory> model, final IModel<Questionnaire> questionnaireModel) {
    super(id);

    IModel<EditedQuestionCategory> editedModel = new Model<EditedQuestionCategory>(new EditedQuestionCategory(model.getObject()));
    setDefaultModel(editedModel);

    final Question question = model.getObject().getQuestion();
    final Category category = editedModel.getObject().getElement().getCategory();

    openAnswerWindow = new ModalWindow("openAnswerWindow");
    openAnswerWindow.setCssClassName("onyx");
    openAnswerWindow.setInitialWidth(900);
    openAnswerWindow.setInitialHeight(500);
    openAnswerWindow.setResizable(true);
    openAnswerWindow.setTitle(new ResourceModel("OpenAnswerDefinition"));
    add(openAnswerWindow);

    // localeProperties = new ListModel<LocaleProperties>(localePropertiesUtils.loadLocaleProperties(model,
    // questionnaireModel));

    TextField<String> name = new TextField<String>("name", new PropertyModel<String>(editedModel, "element.category.name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    add(name);
    add(new SimpleFormComponentLabel("nameLabel", name));

    TextField<String> variable = new TextField<String>("variable", new MapModel<String>(new PropertyModel<Map<String, String>>(model, "category.variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    variable.add(new StringValidator.MaximumLengthValidator(20));
    add(variable);
    add(new SimpleFormComponentLabel("variableLabel", variable));

    CheckBox escapeCheckBox = new CheckBox("escape", new PropertyModel<Boolean>(editedModel, "element.category.escape"));
    escapeCheckBox.setLabel(new ResourceModel("EscapeOrMissing"));
    add(escapeCheckBox);
    add(new SimpleFormComponentLabel("escapeLabel", escapeCheckBox));

    CheckBox noAnswerCheckBox = new CheckBox("noAnswer", new PropertyModel<Boolean>(editedModel, "element.category.noAnswer"));
    noAnswerCheckBox.setLabel(new ResourceModel("NoAnswer"));
    add(noAnswerCheckBox);
    add(new SimpleFormComponentLabel("noAnswerLabel", noAnswerCheckBox));

    // add(new LocalesPropertiesAjaxTabbedPanel("localesProperties", new
    // PropertyModel<QuestionCategory>(getModel(), "element"), localePropertiesModelQuestionCategory));

    LoadableDetachableModel<List<OpenAnswerDefinition>> openAnswerModel = new LoadableDetachableModel<List<OpenAnswerDefinition>>() {

      @Override
      protected List<OpenAnswerDefinition> load() {
        List<OpenAnswerDefinition> list = new ArrayList<OpenAnswerDefinition>();
        if(category.getOpenAnswerDefinition() != null) {
          if(CollectionUtils.isEmpty(category.getOpenAnswerDefinition().getOpenAnswerDefinitions())) {
            list.add(category.getOpenAnswerDefinition());
          } else {
            list.addAll(category.getOpenAnswerDefinition().getOpenAnswerDefinitions());
          }
        }
        return list;
      }
    };

    openAnswerDefinitionList = new SortableList<OpenAnswerDefinition>("openAnswerDefinitionList", openAnswerModel) {

      @Override
      public Component getItemTitle(@SuppressWarnings("hiding") String id, OpenAnswerDefinition openAnswer) {
        return new Label(id, openAnswer.getName());
      }

      @Override
      public void editItem(OpenAnswerDefinition openAnswer, AjaxRequestTarget target) {
        openAnswerWindow.setContent(new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(openAnswer), new Model<Question>(question), questionnaireModel, openAnswerWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, OpenAnswerDefinition openAnswer1) {
            super.onSave(target1, openAnswer1);
            refreshList(target1);
          }
        });
        openAnswerWindow.show(target);
      }

      @Override
      public void deleteItem(OpenAnswerDefinition openAnswerToRemove, AjaxRequestTarget target) {
        OpenAnswerDefinition currentOpenAnswerDefinition = category.getOpenAnswerDefinition();
        if(!CollectionUtils.isEmpty(currentOpenAnswerDefinition.getOpenAnswerDefinitions())) {
          currentOpenAnswerDefinition.removeOpenAnswerDefinition(openAnswerToRemove);
          if(currentOpenAnswerDefinition.getOpenAnswerDefinitions().size() == 1) {
            OpenAnswerDefinition next = currentOpenAnswerDefinition.getOpenAnswerDefinitions().iterator().next();
            next.setParentOpenAnswerDefinition(null);
            category.setOpenAnswerDefinition(next);
          }
        } else {
          category.setOpenAnswerDefinition(null);
        }
        refreshList(target);
      }

      @Override
      @SuppressWarnings({ "rawtypes", "unchecked" })
      public SortableList<OpenAnswerDefinition>.Button[] getButtons() {
        SortableList<OpenAnswerDefinition>.Button addButton = new SortableList.Button(new ResourceModel("AddOpenAnswerDefinition")) {

          @Override
          public void callback(AjaxRequestTarget target) {
            openAnswerWindow.setContent(new OpenAnswerWindow("content", new Model<OpenAnswerDefinition>(new OpenAnswerDefinition()), new Model<Question>(question), questionnaireModel, openAnswerWindow) {
              @Override
              public void onSave(AjaxRequestTarget target1, OpenAnswerDefinition openAnswer) {
                super.onSave(target1, openAnswer);
                OpenAnswerDefinition currentOpenAnswer = category.getOpenAnswerDefinition();
                if(currentOpenAnswer == null) {
                  category.setOpenAnswerDefinition(openAnswer);
                } else {
                  if(!CollectionUtils.isEmpty(currentOpenAnswer.getOpenAnswerDefinitions())) {
                    currentOpenAnswer.addOpenAnswerDefinition(openAnswer);
                  } else {
                    OpenAnswerDefinition newOpenAnswer = new OpenAnswerDefinition();
                    newOpenAnswer.setName("whatNamePutHere");
                    newOpenAnswer.addOpenAnswerDefinition(currentOpenAnswer);
                    newOpenAnswer.addOpenAnswerDefinition(openAnswer);
                    category.setOpenAnswerDefinition(newOpenAnswer);
                  }
                }
                refreshList(target1);
              }
            });
            openAnswerWindow.show(target);
          }
        };
        return new SortableList.Button[] { addButton };
      }
    };

    add(openAnswerDefinitionList);

  }

  /**
   * 
   * @param target
   * @param questionCategory
   */
  public void onSave(AjaxRequestTarget target, final QuestionCategory questionCategory) {
    // editedQuestionCategory.setLocalePropertiesWithNamingStrategy(localeProperties.getObject());

    openAnswerDefinitionList.save(target, new SortableListCallback<OpenAnswerDefinition>() {

      @Override
      public void onSave(List<OpenAnswerDefinition> orderedItems, AjaxRequestTarget target1) {
        OpenAnswerDefinition currentOpenAnswer = questionCategory.getOpenAnswerDefinition();
        if(currentOpenAnswer != null && !CollectionUtils.isEmpty(currentOpenAnswer.getOpenAnswerDefinitions())) {
          currentOpenAnswer.getOpenAnswerDefinitions().clear();
          for(OpenAnswerDefinition openAnswerDefinition : orderedItems) {
            currentOpenAnswer.addOpenAnswerDefinition(openAnswerDefinition);
          }
        }
      }
    });
  }

}
