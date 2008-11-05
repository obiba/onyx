/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.array;

import java.util.ArrayList;
import java.util.List;

class RowProvider extends AbstractDataListProvider<Row> {

  private List<Row> rows = new ArrayList<Row>();

  public RowProvider(int count) {
    for(int i = 0; i < count; i++) {
      String index = new Integer(i + 1).toString();
      getDataList().add(new Row(index, "label" + index, "description" + index));
    }
  }

  @Override
  public List<Row> getDataList() {
    return rows;
  }
}