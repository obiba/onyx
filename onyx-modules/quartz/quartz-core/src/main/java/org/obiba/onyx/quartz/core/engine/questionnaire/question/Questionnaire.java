package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.runtime.Version;

public class Questionnaire implements Serializable, ILocalizable {

  private static final long serialVersionUID = -9079010396321478385L;

  private String name;

  private String version;

  private List<Locale> locales;

  private List<Section> sections;

  private List<Page> pages;

  public Questionnaire(String name, String version) {
    this.name = name;
    setVersion(version);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    // throws a IllegalArgumentException if not valid format
    new Version(version);
    this.version = version;
  }

  public List<Locale> getLocales() {
    return locales != null ? locales : (locales = new ArrayList<Locale>());
  }

  public void addLocale(Locale locale) {
    if(locale != null) {
      getLocales().add(locale);
    }
  }

  public List<Section> getSections() {
    return sections != null ? sections : (sections = new ArrayList<Section>());
  }

  public void addSection(Section section) {
    if(section != null) {
      getSections().add(section);
    }
  }

  public List<Page> getPages() {
    return pages != null ? pages : (pages = new ArrayList<Page>());
  }

  public void addPage(Page page) {
    if(page != null) {
      getPages().add(page);
    }
  }

  //
  // ILocalizable
  //

  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

}
