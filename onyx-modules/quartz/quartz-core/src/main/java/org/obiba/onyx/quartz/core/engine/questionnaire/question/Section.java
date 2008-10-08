package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Section implements Serializable, ILocalizable {

  private static final long serialVersionUID = -1624223156473292196L;

  private String name;

  private Section parentSection;

  private List<Page> pages;

  private List<Section> sections;

  public Section(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Page> getPages() {
    return pages != null ? pages : (pages = new ArrayList<Page>());
  }

  public void addPage(Page page) {
    if(page != null) {
      getPages().add(page);
      page.setQuestionnaireSection(this);
    }
  }

  public Section getParentSection() {
    return parentSection;
  }

  public void setParentSection(Section parentSection) {
    this.parentSection = parentSection;
  }

  public List<Section> getSections() {
    return sections != null ? sections : (sections = new ArrayList<Section>());
  }

  public void addSection(Section section) {
    if(section != null) {
      getSections().add(section);
      section.setParentSection(this);
    }
  }

  private static final String[] PROPERTIES = { "label" };

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
