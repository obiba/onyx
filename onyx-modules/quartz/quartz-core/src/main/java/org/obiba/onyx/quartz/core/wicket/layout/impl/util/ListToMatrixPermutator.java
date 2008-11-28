/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Permute list elements so that it can be displayed in a matrix style: reading is by column, writing is by row. Matrix
 * has a fixed count of rows, columns count is set accordingly.
 */
public class ListToMatrixPermutator<T> implements IDataListPermutator<T> {

  private static final long serialVersionUID = 1L;

  private static final Logger log = LoggerFactory.getLogger(ListToMatrixPermutator.class);

  public static int DEFAULT_ROW_COUNT = 5;

  private List<T> lineList;

  private List<T> matrixList;

  private Map<Integer, Integer[]> indexMap = new HashMap<Integer, Integer[]>();

  private int rowCount;

  private int columnCount;

  public ListToMatrixPermutator() {
    this(DEFAULT_ROW_COUNT);
  }

  public ListToMatrixPermutator(int rowCount) {
    super();
    this.rowCount = rowCount;
  }

  protected void setRowCount(int rowCount) {
    this.rowCount = rowCount;
  }

  public int getRowCount() {
    return rowCount;
  }

  public int getColumnCount() {
    return columnCount;
  }

  public List<T> getMatrixList() {
    return matrixList;
  }

  public Map<Integer, Integer[]> getIndexMap() {
    return indexMap;
  }

  public int getRow(int index) {
    return indexMap.get(index)[0];
  }

  public int getColumn(int index) {
    return indexMap.get(index)[1];
  }

  private void permuteInternal() {
    columnCount = lineList.size() / rowCount;
    // add a column for the remaining items
    if(lineList.size() > rowCount * columnCount) {
      columnCount++;
    }

    // do permutation
    matrixList = new ArrayList<T>();
    for(int i = 0; i < rowCount; i++) {
      for(int j = 0; j < columnCount; j++) {
        int lineIndex = i + j * rowCount;
        log.debug("({}, {}) lineIndex={} lineList.size={} matrixList.size={}", new Object[] { i, j, lineIndex, lineList.size(), matrixList.size() });

        if(lineIndex < lineList.size()) {
          indexMap.put(lineIndex, new Integer[] { i, j });
          log.debug("       adding index={}", lineIndex);
          matrixList.add(lineList.get(lineIndex));
        } else {
          // fill remaining rows with null
          matrixList.add(null);
        }
      }
    }
    while(matrixList.size() > 0 && matrixList.get(matrixList.size() - 1) == null) {
      matrixList.remove(matrixList.size() - 1);
    }
  }

  public List<T> permute(List<T> list) {
    this.lineList = list;
    permuteInternal();
    return matrixList;
  }

}
