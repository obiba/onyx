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

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.IVisitor;

public class Category implements Serializable, ILocalizable {

  private static final long serialVersionUID = -1722883141794376906L;

  private String name;

  private OpenAnswerDefinition openAnswerDefinition;

  public Category(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public OpenAnswerDefinition getOpenAnswerDefinition() {
    return openAnswerDefinition;
  }

  public void setOpenAnswerDefinition(OpenAnswerDefinition openAnswerDefinition) {
    this.openAnswerDefinition = openAnswerDefinition;
  }

  public void accept(IVisitor visitor) {
    visitor.visit(this);
  }

  public boolean hasDataSource() {
    if(getOpenAnswerDefinition() != null) {
      if(getOpenAnswerDefinition().getDataSource() != null) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return getName();
  }

  public OpenAnswerDefinition findOpenAnswerDefinition(String name) {
    if(getOpenAnswerDefinition() == null) return null;
    if(getOpenAnswerDefinition().getName().equals(name)) return getOpenAnswerDefinition();

    return getOpenAnswerDefinition().findOpenAnswerDefinition(name);
  }
}
