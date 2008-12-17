/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.core.engine.questionnaire.condition;

import org.obiba.onyx.util.data.Data;

/**
 * Comparision result between two Data.
 */
public enum ComparisionOperator {
  eq {
    @Override
    public boolean compare(Data left, Data right) {
      return left.compareTo(right) == 0;
    }
  },
  ne {
    @Override
    public boolean compare(Data left, Data right) {
      return left.compareTo(right) != 0;
    }
  },
  lt {
    @Override
    public boolean compare(Data left, Data right) {
      return left.compareTo(right) < 0;
    }
  },
  le {
    @Override
    public boolean compare(Data left, Data right) {
      return left.compareTo(right) <= 0;
    }
  },
  gt {
    @Override
    public boolean compare(Data left, Data right) {
      return left.compareTo(right) > 0;
    }
  },
  ge {
    @Override
    public boolean compare(Data left, Data right) {
      return left.compareTo(right) >= 0;
    }
  };

  /**
   * Compares two data.
   * @param left
   * @param right
   * @return if result is positive for the operator
   */
  public abstract boolean compare(Data left, Data right);
}
