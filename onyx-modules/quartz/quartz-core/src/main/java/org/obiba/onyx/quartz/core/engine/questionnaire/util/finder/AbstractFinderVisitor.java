package org.obiba.onyx.quartz.core.engine.questionnaire.util.finder;

import java.util.ArrayList;
import java.util.List;

import org.obiba.onyx.quartz.core.engine.questionnaire.ILocalizable;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.IWalkerVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFinderVisitor<T extends ILocalizable> implements IWalkerVisitor {

  @SuppressWarnings("unused")
  private final Logger log = LoggerFactory.getLogger(getClass());

  private String name;

  private boolean stopAtFirst;

  private List<T> elements;

  protected AbstractFinderVisitor(String name) {
    this(name, true);
  }

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

  public T getFirstElement() {
    if(elements.size() > 0) return elements.get(0);
    else
      return null;
  }

  public List<T> getElements() {
    return elements;
  }

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
