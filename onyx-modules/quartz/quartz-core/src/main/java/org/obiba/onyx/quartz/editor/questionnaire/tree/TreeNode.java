/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire.tree;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.wicket.ResourceReference;
import org.obiba.magma.Variable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.editor.QuartzImages;

/**
 *
 */
public class TreeNode implements Serializable {

  private static final long serialVersionUID = 1L;

  public enum NodeType {
    QUESTIONNAIRE(QuartzImages.QUESTIONNAIRE), SECTION(QuartzImages.SECTION), PAGE(QuartzImages.PAGE), QUESTION(QuartzImages.QUESTION), QUESTIONBOILERPLATE(QuartzImages.QUESTION), VARIABLES(QuartzImages.VARIABLES), VARIABLE((QuartzImages.VARIABLE));

    private ResourceReference icon;

    private NodeType(ResourceReference icon) {
      this.icon = icon;
    }

    public ResourceReference getIcon() {
      return icon;
    }

    public static NodeType get(IQuestionnaireElement element) {
      if(element.getClass().isAssignableFrom(Questionnaire.class)) return NodeType.QUESTIONNAIRE;
      if(element.getClass().isAssignableFrom(Section.class)) return NodeType.SECTION;
      if(element.getClass().isAssignableFrom(Page.class)) return NodeType.PAGE;
      if(element.getClass().isAssignableFrom(Question.class)) {
        if(((Question) element).getType() == QuestionType.BOILER_PLATE) {
          return NodeType.QUESTIONBOILERPLATE;
        }
        return NodeType.QUESTION;
      }
      if(element.getClass().isAssignableFrom(Variable.class)) return NodeType.VARIABLE;
      throw new IllegalArgumentException(element + " not supported");
    }
  }

  private String name;

  private NodeType nodeType;

  public TreeNode(String name, NodeType nodeType) {
    this.name = name;
    this.nodeType = nodeType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public NodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(NodeType nodeType) {
    this.nodeType = nodeType;
  }

  @Override
  public boolean equals(Object obj) {
    if(obj == null || !(obj instanceof TreeNode)) return false;
    if(this == obj) return true;
    TreeNode node = (TreeNode) obj;
    return new EqualsBuilder().appendSuper(super.equals(obj)).append(name, node.name).append(nodeType, node.nodeType).isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).append(name).append(nodeType).toHashCode();
  }

  public boolean isQuestionnaire() {
    return nodeType == NodeType.QUESTIONNAIRE;
  }

  public boolean isSection() {
    return nodeType == NodeType.SECTION;
  }

  public boolean isHasSection() {
    return nodeType == NodeType.SECTION || nodeType == NodeType.QUESTIONNAIRE;
  }

  public boolean isPage() {
    return nodeType == NodeType.PAGE;
  }

  public boolean isAnyQuestion() {
    return isNoBoilerPlateQuestion() || isQuestionBoilerPlate();
  }

  public boolean isNoBoilerPlateQuestion() {
    return nodeType == NodeType.QUESTION;
  }

  public boolean isQuestionBoilerPlate() {
    return nodeType == NodeType.QUESTIONBOILERPLATE;
  }

  public boolean isVariable() {
    return nodeType == NodeType.VARIABLE;
  }

  public boolean isVariables() {
    return nodeType == NodeType.VARIABLES;
  }
}
