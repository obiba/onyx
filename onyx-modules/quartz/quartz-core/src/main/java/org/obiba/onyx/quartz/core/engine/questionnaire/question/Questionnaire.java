package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
  // Find methods
  //
  
  /**
   * Find {@link Question} with the given name in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Question findQuestion(String name) {
    for (Page page : getPages()) {
      for (Question question : page.getQuestions()) {
        if (question.getName().equals(name)) {
          return question;
        }
        else {
          Question q = findQuestion(question, name);
          if (q != null) {
            return q;
          }
        }
      }
    }
    
    return null;
  }
  
  /**
   * Find recursively {@link Question} among the children.
   * @param parent
   * @param name
   * @return null if not found
   */
  private Question findQuestion(Question parent, String name) {
    for (Question question : parent.getQuestions()) {
      if (question.getName().equals(name)) {
        return question;
      }
      else {
        Question q = findQuestion(question, name);
        if (q != null) {
          return q;
        }
      }
    }
    
    return null;
  }
  
  /**
   * Find {@link Page} in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Page findPage(String name) {
    for (Page page : getPages()) {
      if (page.getName().equals(name)) {
        return page;
      }
    }
    
    return null;
  }
  
  
  /**
   * Find {@link Section} in the questionnaire.
   * @param name
   * @return null if not found
   */
  public Section findSection(String name) {
    for (Section section : getSections()) {
      if (section.getName().equals(name)) {
        return section;
      }
      else {
        Section s = findSection(section, name);
        if (s != null) {
          return s;
        }
      }
    }
    
    return null;
  }
  
  /**
   * Find recursively a {@link Section} among the children.
   * @param parent
   * @param name
   * @return null if not found
   */
  private Section findSection(Section parent, String name) {
    for (Section section : parent.getSections()) {
      if (section.getName().equals(name)) {
        return section;
      }
      else {
        Section s = findSection(section, name);
        if (s != null) {
          return s;
        }
      }
    }
    
    return null;
  }
  
  //
  // ILocalizable
  //
  private static final String[] PROPERTIES = { "description", "labelNext", "imageNext", "labelPrevious", "imagePrevious", "labelStart", "labelFinish", "labelInterrupt", "labelResume", "labelCancel" };

  public String getPropertyKey(String property) {
    for(String key : PROPERTIES) {
      if(key.equals(property)) {
        return getClass().getSimpleName() + "." + getName() + "." + property;
      }
    }
    throw new IllegalArgumentException("Invalid property for class " + getClass().getName() + ": " + property);
  }
  
  public String[] getProperties() {
    return PROPERTIES;
  }
}
