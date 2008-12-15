package org.obiba.onyx.quartz.core.engine.questionnaire.answer;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Holds the modifiers to be applied to Calendar time.
 * 
 * @see Calendar#add(int, int)
 */
public class DateModifier implements Serializable {

  private static final long serialVersionUID = 1L;

  private int field;

  private int amount;

  public DateModifier(int field, int amount) {
    super();
    this.field = field;
    this.amount = amount;
  }

  public int getField() {
    return field;
  }

  public int getAmount() {
    return amount;
  }

}