/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.data;

import java.util.Calendar;

import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.util.data.Data;

/**
 * Holds the modifiers to be applied to Calendar time. The optional amount data source should return an Integer value.
 * 
 * @see Calendar#add(int, int)
 */
public class DateModifier {

  private int field;

  private int amount;

  private IDataSource amountSource;

  public void modify(Calendar calendar, Participant participant) {
    if(amountSource != null) {
      Data data = amountSource.getData(participant);
      if(data != null) {
        Number val = data.getValue();
        if(val != null) {
          calendar.add(field, val.intValue());
        }
      }
    } else {
      calendar.add(field, amount);
    }
  }

  public DateModifier(int field, int amount) {
    this.field = field;
    this.amount = amount;
  }

  public DateModifier(int field, IDataSource amountSource) {
    this.field = field;
    this.amountSource = amountSource;
  }

  public void setField(int field) {
    this.field = field;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public void setAmountSource(IDataSource amountSource) {
    this.amountSource = amountSource;
  }
}
