/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.test;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ComponentTesterUtils {

  private static final Logger log = LoggerFactory.getLogger(ComponentTesterUtils.class);

  /**
   * Find the first child of given class, and with model equals to the given model.
   * @param parent
   * @param clazz
   * @param model
   * @return
   */
  public static Component findChild(WebMarkupContainer parent, final Class clazz, final IModel model) {
    if(parent == null) {
      throw new IllegalArgumentException("parent cannot be null.");
    }

    return (Component) parent.visitChildren(new Component.IVisitor() {

      public Object component(Component component) {
        if(clazz.isAssignableFrom(component.getClass()) && component.getModel().equals(model)) {
          log.debug("child.path: {}", component.getPath());
          return component;
        }
        return CONTINUE_TRAVERSAL;
      }

    });

  }

  /**
   * Find the first child of given class, and with model object equals to the given localizable.
   * @param parent
   * @param clazz
   * @param localizable
   * @return
   */
  public static Component findChild(WebMarkupContainer parent, final Class clazz, final IQuestionnaireElement localizable) {
    if(parent == null) {
      throw new IllegalArgumentException("parent cannot be null.");
    }

    return (Component) parent.visitChildren(new Component.IVisitor() {

      public Object component(Component component) {
        if(clazz.isAssignableFrom(component.getClass())) {
          if(component.getModelObject() != null && localizable.getClass().isAssignableFrom(component.getModelObject().getClass()) && localizable.getName().equals(((IQuestionnaireElement) component.getModelObject()).getName())) {
            log.info("child.{}.path: {}", localizable.getName(), component.getPath());
            return component;
          }
        }
        return CONTINUE_TRAVERSAL;
      }

    });

  }

  public static String extractPath(Component component, String from) {

    String path = null;
    for(String p : component.getPath().split(":")) {
      if(path == null) {
        if(p.equals(from)) {
          path = from;
        }
      } else {
        path += ":" + p;
      }
    }

    log.info("extractPath={}", path);
    return path;
  }

  /**
   * Get all children of the given class.
   * @param parent
   * @param clazz
   * @return
   */
  public static List<Component> findChildren(WebMarkupContainer parent, final Class clazz) {
    if(parent == null) {
      throw new IllegalArgumentException("parent cannot be null.");
    }

    final List<Component> children = new ArrayList<Component>();

    parent.visitChildren(new Component.IVisitor() {

      public Object component(Component component) {
        if(clazz.isAssignableFrom(component.getClass())) {
          log.info("children.path: {}", component.getPath());
          children.add(component);
          return CONTINUE_TRAVERSAL;
        }
        return CONTINUE_TRAVERSAL;
      }

    });

    return children;
  }

}
