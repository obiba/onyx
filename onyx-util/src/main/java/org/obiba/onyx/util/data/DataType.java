/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.util.data;

public enum DataType {
  BOOLEAN {
    @Override
    public boolean isNumberType() {
      return false;
    }
  },
  DATA {
    @Override
    public boolean isNumberType() {
      return false;
    }
  },
  DATE {
    @Override
    public boolean isNumberType() {
      return false;
    }
  },
  DECIMAL {
    @Override
    public boolean isNumberType() {
      return true;
    }
  },
  INTEGER {
    @Override
    public boolean isNumberType() {
      return true;
    }
  },
  TEXT {
    @Override
    public boolean isNumberType() {
      return false;
    }
  };

  public abstract boolean isNumberType();
}
