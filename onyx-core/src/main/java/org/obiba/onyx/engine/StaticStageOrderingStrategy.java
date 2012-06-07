/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * 
 */
public class StaticStageOrderingStrategy implements Comparator<Stage> {

  /**
   * A {@code List} of stage names for ordering purposes.
   */
  private List<String> stageOrdering;

  /**
   * Returns a negative number if the {@code lhs} {@code Stage} should come before the {@code rhs} {@code Stage} in the
   * list of stages. Returns a positive number otherwise. Note that this class never returns zero.
   * 
   */
  public int compare(Stage lhs, Stage rhs) {
    int leftIndex = stageOrdering.indexOf(lhs.getName());
    int rightIndex = stageOrdering.indexOf(rhs.getName());

    // If rhs is not found in the list, make lhs appear first
    if(rightIndex == -1) return Integer.MIN_VALUE;
    // If lhs is not found in the list, make rhs appear first
    if(leftIndex == -1) return Integer.MAX_VALUE;

    // Returns a negative number if lhs is before rhs in the stageOrdering list.
    return leftIndex - rightIndex;
  }

  private void setStageOrdering(List<String> stageOrdering) {
    this.stageOrdering = stageOrdering;
  }

  /**
   * Set the stage ordering using a comma separated list of stage names.
   * @param stageOrdering a comma sparated list of stage names.
   */
  public void setStageOrdering(String stageOrdering) {
    if(stageOrdering != null) {
      String stages[] = stageOrdering.split(",");
      this.setStageOrdering(Arrays.asList(stages));
    }
  }

}
