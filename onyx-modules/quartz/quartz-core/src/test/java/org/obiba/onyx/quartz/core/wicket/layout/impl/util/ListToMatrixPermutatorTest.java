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

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class ListToMatrixPermutatorTest {

  static final Logger log = LoggerFactory.getLogger(ListToMatrixPermutatorTest.class);

  @Test
  public void testPermutation1() {
    List<String> line = Arrays.asList(new String[] { "0", "1", "2", "3" });
    permute(line, 1, 5);
  }

  @Test
  public void testPermutation2() {
    List<String> line = Arrays.asList(new String[] { "0", "1", "2", "3", "4", "5", "6", "7" });
    ListToMatrixPermutator<String> permutator = permute(line, 2, 5);
    Assert.assertEquals(2, permutator.getRow(7));
    Assert.assertEquals(1, permutator.getColumn(7));
  }

  @Test
  public void testPermutation3() {
    List<String> line = Arrays.asList(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12" });
    permute(line, 3, 5);
  }

  @Test
  public void testPermutation4() {
    List<String> line = Arrays.asList(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16" });
    ListToMatrixPermutator<String> permutator = permute(line, 4, 5);
    Assert.assertEquals(3, permutator.getRow(13));
    Assert.assertEquals(2, permutator.getColumn(13));
    Assert.assertEquals(1, permutator.getRow(16));
    Assert.assertEquals(4, permutator.getColumnCount());
    Assert.assertEquals(3, permutator.getColumn(16));
  }

  private ListToMatrixPermutator<String> permute(List<String> line, int columnCountExpected, int rowCountExpected) {
    ListToMatrixPermutator<String> permutator = new ListToMatrixPermutator<String>();
    log.info("matrix={}", permutator.permute(line));
    Assert.assertEquals(columnCountExpected, permutator.getColumnCount());
    Assert.assertEquals(rowCountExpected, permutator.getRowCount());
    int k = 0;
    for(int i = 0; i < permutator.getRowCount(); i++) {
      System.out.print("[ ");
      for(int j = 0; j < permutator.getColumnCount(); j++) {
        if(k < permutator.getMatrixList().size()) {
          System.out.print(permutator.getMatrixList().get(k) + " ");
          k++;
        } else {
          System.out.print("- ");
        }
      }
      System.out.println("]");
    }

    return permutator;

  }
}
