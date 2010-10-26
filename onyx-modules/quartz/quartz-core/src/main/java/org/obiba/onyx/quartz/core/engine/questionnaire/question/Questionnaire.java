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
import java.util.List;
import java.util.Locale;

import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.finder.QuestionnaireCache;
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

  private String uiType;

  /** Indicates if question conditions where converted to Magma variables */
  private boolean convertedToVariableConditions;

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
    }
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
  public boolean isConvertedToVariableConditions() {
    return convertedToVariableConditions;
  }

  public void setConvertedToVariableConditions(boolean convertedToVariableConditions) {
    this.convertedToVariableConditions = convertedToVariableConditions;
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

}
