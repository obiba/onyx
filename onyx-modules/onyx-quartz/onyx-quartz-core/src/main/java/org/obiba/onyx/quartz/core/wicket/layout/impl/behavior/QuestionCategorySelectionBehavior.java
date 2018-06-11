/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.behavior;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.obiba.onyx.quartz.core.wicket.layout.IQuestionCategorySelectionStateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Behavior that sets the appropriate class tag attribute when parent question category selector is in (un)selected
 * state.
 */
public class QuestionCategorySelectionBehavior extends AbstractBehavior {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(QuestionCategorySelectionBehavior.class);

  private static final String SELECTED_CSS_CLASS = "selected";

  @Override
  public void onComponentTag(Component component, ComponentTag tag) {
    super.onRendered(component);

    // behavior can be attached to the QuestionCategoryImageSelectorPanel or any of its children
    IQuestionCategorySelectionStateHolder selector;
    if(IQuestionCategorySelectionStateHolder.class.isInstance(component)) {
      selector = (IQuestionCategorySelectionStateHolder) component;
    } else {
      selector = component.findParent(IQuestionCategorySelectionStateHolder.class);
    }

    // synchronize the state
    if(selector != null && selector.updateState()) {
      String cssClass = SELECTED_CSS_CLASS;
      if(tag.getAttributes().containsKey("class")) {
        cssClass += " " + tag.getAttributes().getString("class");
      }
      tag.getAttributes().put("class", cssClass);
      log.debug("{}", tag);
    }
  }
}
