package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

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
    for(Page page : getPages()) {
      for(Question question : page.getQuestions()) {
        if(question.getName().equals(name)) {
          return question;
        } else {
          Question q = findQuestion(question, name);
          if(q != null) {
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
    for(Question question : parent.getQuestions()) {
      if(question.getName().equals(name)) {
        return question;
      } else {
        Question q = findQuestion(question, name);
        if(q != null) {
          return q;
        }
      }
    }

    return null;
  }

  /**
   * Find all the {@link Category} and associated {@link Question}.
   * @param name
   * @return
   */
  public Map<Category, List<Question>> findCategories(String name) {
    Map<Category, List<Question>> map = new HashMap<Category, List<Question>>();

    for(Page page : getPages()) {
      for(Question question : page.getQuestions()) {
        for(Category category : question.getCategories()) {
          if(category.getName().equals(name)) {
            if(!map.containsKey(category)) {
              List<Question> questions = new ArrayList<Question>();
              questions.add(question);
              map.put(category, questions);
            } else {
              map.get(category).add(question);
            }
          }
        }
        findCategories(question, name, map);
      }
    }

    return map;
  }

  /**
   * Find recursively all the {@link Category} and associated {@link Question}.
   * @param parent
   * @param name
   * @param map
   */
  private void findCategories(Question parent, String name, Map<Category, List<Question>> map) {
    for(Question question : parent.getQuestions()) {
      for(Category category : question.getCategories()) {
        if(category.getName().equals(name)) {
          if(!map.containsKey(category)) {
            List<Question> questions = new ArrayList<Question>();
            questions.add(question);
            map.put(category, questions);
          } else {
            map.get(category).add(question);
          }
        }
      }
      findCategories(question, name, map);
    }
  }

  /**
   * Find the first {@link Category} with the given name.
   * @param name
   * @return null if not found
   */
  public Category findCategory(String name) {
    for(Page page : getPages()) {
      for(Question question : page.getQuestions()) {
        Category c = question.findCategory(name);
        if(c != null) {
          return c;
        }
        c = findCategory(question, name);
        if(c != null) {
          return c;
        }
      }
    }
    return null;
  }

  /**
   * Find recursively in {@link Question} the first {@link Category} with the given name.
   * @param name
   * @return null if not found
   */
  private Category findCategory(Question parent, String name) {
    for(Question question : parent.getQuestions()) {
      Category c = question.findCategory(name);
      if(c != null) {
        return c;
      }
      c = findCategory(question, name);
      if(c != null) {
        return c;
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
    for(Page page : getPages()) {
      if(page.getName().equals(name)) {
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
    for(Section section : getSections()) {
      if(section.getName().equals(name)) {
        return section;
      } else {
        Section s = findSection(section, name);
        if(s != null) {
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
    for(Section section : parent.getSections()) {
      if(section.getName().equals(name)) {
        return section;
      } else {
        Section s = findSection(section, name);
        if(s != null) {
          return s;
        }
      }
    }

    return null;
  }

  /**
   * Look for shared {@link Category}: categories refered by more than one question.
   * @return
   */
  public List<Category> findSharedCategories() {
    List<Category> shared = new ArrayList<Category>();
    Map<Category, List<Question>> map = new HashMap<Category, List<Question>>();
    for(Page page : getPages()) {
      for(Question question : page.getQuestions()) {
        for(Category category : question.getCategories()) {
          if(!map.containsKey(category)) {
            List<Question> questions = new ArrayList<Question>();
            questions.add(question);
            map.put(category, questions);
          } else {
            map.get(category).add(question);
          }
        }
        findCategories(question, map);
      }
    }
    for(Entry<Category, List<Question>> entry : map.entrySet()) {
      if(entry.getValue().size() > 1) {
        shared.add(entry.getKey());
      }
    }
    return shared;
  }

  /**
   * Register recursively the {@link Question} {@link Category} associations.
   * @param parent
   * @param map
   */
  private void findCategories(Question parent, Map<Category, List<Question>> map) {
    for(Question question : parent.getQuestions()) {
      for(Category category : question.getCategories()) {
        if(!map.containsKey(category)) {
          List<Question> questions = new ArrayList<Question>();
          questions.add(question);
          map.put(category, questions);
        } else {
          map.get(category).add(question);
        }
      }
      findCategories(question, map);
    }
  }

  /**
   * Find the first {@link OpenAnswerDefinition} with the given name.
   * @param name
   * @return
   */
  public OpenAnswerDefinition findOpenAnswerDefinition(String name) {
    for(Page page : getPages()) {
      for(Question question : page.getQuestions()) {
        for(Category category : question.getCategories()) {
          if(category.getOpenAnswerDefinition() != null && category.getOpenAnswerDefinition().getName().equals(name)) {
            return category.getOpenAnswerDefinition();
          }
        }
        OpenAnswerDefinition definition = findOpenAnswerDefinition(question, name);
        if(definition != null) {
          return definition;
        } 
      }
    }
    return null;
  }

  /**
   * Find the first {@link OpenAnswerDefinition} with the given name from a {@link Question}.
   * @param parent
   * @param name
   * @return
   */
  private OpenAnswerDefinition findOpenAnswerDefinition(Question parent, String name) {
    for(Question question : parent.getQuestions()) {
      for(Category category : question.getCategories()) {
        if(category.getOpenAnswerDefinition() != null && category.getOpenAnswerDefinition().getName().equals(name)) {
          return category.getOpenAnswerDefinition();
        }
      }
      OpenAnswerDefinition definition = findOpenAnswerDefinition(question, name);
      if(definition != null) {
        return definition;
      }
    }
    return null;
  }

  //
  // ILocalizable
  //
  private static final String[] PROPERTIES = { "label", "description", "labelNext", "imageNext", "labelPrevious", "imagePrevious", "labelStart", "labelFinish", "labelInterrupt", "labelResume", "labelCancel" };

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
