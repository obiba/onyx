package org.obiba.onyx.quartz.core.wicket.questionnaire;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ActiveInterviewService;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;

public class LanguageSelectorPanel extends Panel {

  private static final long serialVersionUID = 5589767297291614169L;

  private Locale language = null;

  @SpringBean(name = "activeInterviewService")
  private ActiveInterviewService activeInterviewService;

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;
    
  public Locale getLanguage() {
    return this.language;
  }
  
  public void setLanguage(Locale language) {
    this.language = language;
  }

  @SuppressWarnings("serial")
  public LanguageSelectorPanel(String id) {
    super(id);

    add(new Label("participant", activeInterviewService.getParticipant().getFullName()));
    add(new Label("user", activeInterviewService.getInterview().getUser().getFullName()));
    add(new Label("description", activeQuestionnaireAdministrationService.getQuestionnaire().getName()));

    List<Locale> languages = Arrays.asList(Locale.FRENCH, Locale.ENGLISH, Locale.GERMAN);

    DropDownChoice ddcLocale = new DropDownChoice("localeSelect", new PropertyModel(LanguageSelectorPanel.this, "language"), languages, new IChoiceRenderer() {

      private static final long serialVersionUID = -1858115721444491116L;

      public Object getDisplayValue(Object object) {
        Locale lang = (Locale) object;
        return lang.getDisplayLanguage(lang);
      }

      public String getIdValue(Object object, int index) {
        Locale lang = (Locale) object;
        return lang.toString();
      }

    });
    
    ddcLocale.setLabel(new StringResourceModel("Language", LanguageSelectorPanel.this, null));
    ddcLocale.setOutputMarkupId(true);
    add(ddcLocale);
  }
}
