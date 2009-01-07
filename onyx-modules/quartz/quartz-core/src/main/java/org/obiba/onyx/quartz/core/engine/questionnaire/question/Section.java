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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;

public class Section implements Serializable, IQuestionnaireElement {

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
      page.setSection(this);
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

  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  @Override
  public String toString() {
    return getName();
  }
}
