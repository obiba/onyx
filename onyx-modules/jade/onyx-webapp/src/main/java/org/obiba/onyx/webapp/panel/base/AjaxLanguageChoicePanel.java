package org.obiba.onyx.webapp.panel.base;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

/**
 * A panel with a language selector. Ajax callback when a language is selected.
 * @author ymarcon
 * 
 */
public abstract class AjaxLanguageChoicePanel extends Panel {

  private Locale selectedLanguage;

  /**
   * Default constructor with FRENCH and ENGLISH to be selected.
   * @param id
   * @param header
   */
  public AjaxLanguageChoicePanel(String id, IModel header) {
    this(id, header, Arrays.asList(new Locale[] { Locale.FRENCH, Locale.ENGLISH }));
  }

  /**
   * Constructor with a given language list.
   * @param id
   * @param header
   * @param languages
   */
  public AjaxLanguageChoicePanel(String id, IModel header, List<Locale> languages) {
    super(id);
    init(header, languages);
  }

  private void init(IModel header, List<Locale> languages) {
    add(new Label("localeHeader", header));
    DropDownChoice ddcLocale = new DropDownChoice("localeSelect", new PropertyModel(this, "selectedLanguage"), languages, new IChoiceRenderer() {

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

    ddcLocale.add(new AjaxFormComponentUpdatingBehavior("onchange") {

      private static final long serialVersionUID = 1L;

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        onLanguageUpdate(getSelectedLanguage(), target);
      }

    });
    add(ddcLocale);
  }

  /**
   * Method on language selection ajax event.
   * @param language
   * @param target
   */
  protected abstract void onLanguageUpdate(Locale language, AjaxRequestTarget target);

  /**
   * @return the selectedLanguage
   */
  public Locale getSelectedLanguage() {
    return selectedLanguage;
  }

  /**
   * @param selectedLanguage the selectedLanguage to set
   */
  public void setSelectedLanguage(Locale selectedLanguage) {
    this.selectedLanguage = selectedLanguage;
  }

}
