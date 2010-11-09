/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.IHasSection;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;

/**
 *
 */
public class TreeNode implements Serializable {

  private static final long serialVersionUID = 1L;

  private String name;

  private Class<?> clazz;

  public TreeNode(String name, Class<?> clazz) {
    this.name = name;
    this.clazz = clazz;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Class<?> getClazz() {
    return clazz;
  }

  public void setClazz(Class<?> clazz) {
    this.clazz = clazz;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null || !(obj instanceof TreeNode)) return false;
    if(this == obj) return true;
    TreeNode node = (TreeNode) obj;
    return new EqualsBuilder().appendSuper(super.equals(obj)).append(name, node.name).append(clazz, node.clazz).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(name).append(clazz).toHashCode();
  }

  public boolean isQuestionnaire() {
    return Questionnaire.class.equals(getClazz());
  }

  public boolean isSection() {
    return Section.class.equals(getClazz());
  }

  public boolean isHasSection() {
    return IHasSection.class.isAssignableFrom(getClazz());
  }

  public boolean isPage() {
    return Page.class.equals(getClazz());
  }

  public boolean isQuestion() {
    return Question.class.equals(getClazz());
  }

}
