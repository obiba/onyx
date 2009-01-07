/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.util.finder;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.IQuestionnaireElement;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.IWalkerVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for defining a questionnaire element finder visitor.
 * @author Yannick Marcon
 * @see QuestionnaireFinder
 * @see QuestionnaireWalker
 * @param <T> questionnaire elements are search by their name
 */
public abstract class AbstractFinderVisitor<T extends IQuestionnaireElement> implements IWalkerVisitor {

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  /**
   * Localization name.
   */
  private String name;

  /**
   * Do we stop when first element is found ?
   */
  private boolean stopAtFirst;

  /**
   * The list of found elements.
   */
  private List<T> elements;

  /**
   * Constructor, stopping at first element by default.
   * @param name
   */
  protected AbstractFinderVisitor(String name) {
    this(name, true);
  }
  
  /**
   * Constructor.
   * @param name
   * @param stopAtFirst
   */
  protected AbstractFinderVisitor(String name, boolean stopAtFirst) {
    this.name = name;
    this.stopAtFirst = stopAtFirst;
    this.elements = new ArrayList<T>();
  }

  /**
   * The name of the questionnaire element that was looked for.
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * The first element of the found list.
   * @return null if none
   */
  public T getFirstElement() {
    if(elements.size() > 0) return elements.get(0);
    else
      return null;
  }

  /**
   * The list of found elements.
   * @return
   */
  public List<T> getElements() {
    return elements;
  }

  /**
   * Visit the element, deciding if it answer the search criteria.
   * @param element
   * @return true if search criteria is satisfied
   */
  protected boolean visitElement(T element) {
    if(element.getName().equals(name)) {
      elements.add(element);
      return true;
    }
    return false;
  }

  public boolean visiteMore() {
    return !(stopAtFirst && elements.size() > 0);
  }

}
