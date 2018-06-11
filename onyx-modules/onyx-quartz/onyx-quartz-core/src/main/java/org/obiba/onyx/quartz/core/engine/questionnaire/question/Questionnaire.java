/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.QuestionnaireCache;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyNamingStrategy;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.DefaultPropertyKeyProviderImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.impl.SimplifiedUIPropertyKeyProviderImpl;
import org.obiba.runtime.Version;

public class Questionnaire implements IHasSection {

  private static final long serialVersionUID = -9079010396321478385L;

  public static final String STANDARD_UI = "standard";

  public static final String SIMPLIFIED_UI = "simplified";

  private String name;

  private String version;

  private List<Locale> locales;

  private List<Section> sections;

  private List<Page> pages;

  private List<Variable> variables;

  private String uiType;

  /** Indicates if question conditions where converted to Magma variables */
  private boolean convertedToMagmaVariables;

  private Boolean commentable;

  private transient QuestionnaireCache questionnaireCache;

  public Questionnaire(String name, String version) {
    this.name = name;
    setVersion(version);
  }

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getVersion() {
    return version;
  }

  @SuppressWarnings("unused")
  public void setVersion(String version) {
    // throws a IllegalArgumentException if not valid format
    new Version(version);
    this.version = version;
  }

  public List<Locale> getLocales() {
    return locales != null ? locales : (locales = new ArrayList<Locale>());
  }

  public void setLocales(List<Locale> locales) {
    this.locales = locales;
  }

  public void addLocale(Locale locale) {
    if(locale != null) {
      getLocales().add(locale);
    }
  }

  @Override
  public List<Section> getSections() {
    return sections != null ? sections : (sections = new ArrayList<Section>());
  }

  @Override
  public void addSection(Section section) {
    if(section != null) {
      getSections().add(section);
    }
  }

  @Override
  public void addSection(Section section, int index) {
    if(section != null) {
      getSections().add(index, section);
    }
  }

  @Override
  public void removeSection(Section section) {
    if(section != null) {
      getSections().remove(section);
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

  public void addPage(Page page, int index) {
    if(page != null) {
      getPages().add(index, page);
    }
  }

  public void removePage(Page page) {
    if(page != null) {
      getPages().remove(page);
      if(page.getSection() != null) {
        page.getSection().removePage(page);
      }
    }
  }

  public List<Variable> getSortedVariables() {
    Collections.sort(getVariables(), new Comparator<Variable>() {

      @Override
      public int compare(Variable o1, Variable o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return variables;
  }

  public List<Variable> getVariables() {
    return variables != null ? variables : (variables = new ArrayList<Variable>());
  }

  public void addVariable(Variable variable) {
    if(variable != null) {
      getVariables().add(variable);
    }
  }

  public void removeVariable(Variable variable) {
    if(variable != null) {
      getVariables().remove(variable);
    }
  }

  public boolean hasVariable(@SuppressWarnings("hiding") String name) {
    for(Variable variable : getVariables()) {
      if(variable.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  public Variable getVariable(@SuppressWarnings("hiding") String name) throws IllegalArgumentException {
    for(Variable variable : getVariables()) {
      if(variable.getName().equals(name)) {
        return variable;
      }
    }
    throw new IllegalArgumentException("No such variable in questionnaire '" + this.name + "': " + name);
  }

  public String getUiType() {
    return uiType;
  }

  public void setUiType(String uiType) {
    this.uiType = uiType;
  }

  /**
   * @return true if question conditions where converted to Magma variables
   */
  public boolean isConvertedToMagmaVariables() {
    return convertedToMagmaVariables;
  }

  public void setConvertedToMagmaVariables(boolean convertedToMagmaVariables) {
    this.convertedToMagmaVariables = convertedToMagmaVariables;
  }

  //
  // Cache
  //
  public QuestionnaireCache getQuestionnaireCache() {
    return questionnaireCache;
  }

  public void setQuestionnaireCache(QuestionnaireCache questionnaireCache) {
    this.questionnaireCache = questionnaireCache;
  }

  //
  // ILocalizable
  //
  @Override
  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return getName();
  }

  public IPropertyKeyProvider getPropertyKeyProvider() {
    if(Questionnaire.SIMPLIFIED_UI.equals(getUiType())) {
      SimplifiedUIPropertyKeyProviderImpl provider = new SimplifiedUIPropertyKeyProviderImpl();
      provider.setPropertyKeyNamingStrategy(new DefaultPropertyKeyNamingStrategy());
      return provider;
    }
    DefaultPropertyKeyProviderImpl provider = new DefaultPropertyKeyProviderImpl();
    provider.setPropertyKeyNamingStrategy(new DefaultPropertyKeyNamingStrategy());
    return provider;
  }

  public boolean isCommentable() {
    return commentable == null ? true : commentable;
  }

  public void setCommentable(boolean commentable) {
    this.commentable = commentable;
  }

}
