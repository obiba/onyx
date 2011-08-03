/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.paradox;

import org.obiba.paradox.ParadoxDb.ParadoxDbHeader;

public class ParadoxRecord {

  private final ParadoxDbHeader header;

  private final Object[] values;

  ParadoxRecord(ParadoxDbHeader header) {
    this.header = header;
    this.values = new Object[header.getNumFields()];
  }

  public Object[] getValues() {
    return values;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(int i) {
    return (T) values[i];
  }

  public <T> T getValue(String fieldName) {
    return this.<T> getValue(getFieldIndex(fieldName));
  }

  void setFieldValue(int i, Object value) {
    values[i] = value;
  }

  private int getFieldIndex(String fieldName) {
    int index = header.getFieldNames().indexOf(fieldName);
    if(index < 0) {
      throw new IllegalArgumentException("no such field " + fieldName);
    }
    return index;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(String fieldName : header.getFieldNames()) {
      sb.append(fieldName).append(":").append(getValue(fieldName));
    }
    return sb.toString();
  }
}
