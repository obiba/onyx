package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * 
 */
public class NoDragBehavior extends AbstractBehavior {
  @Override
  public void onComponentTag(Component component, ComponentTag tag) {
    super.onComponentTag(component, tag);
    // prevent from drag
    tag.getAttributes().put("onmousedown", "if (event.preventDefault) {event.preventDefault();}return false;");
  }
}