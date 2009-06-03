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

import java.util.Comparator;

/**
 * A comparison function used to sort {@code Action} objects in ascending order according to their time stamp.
 */
public class ActionAscendingComparator implements Comparator<Action> {

  public int compare(Action o1, Action o2) {
    return o1.getDateTime().compareTo(o2.getDateTime());
  }

}
