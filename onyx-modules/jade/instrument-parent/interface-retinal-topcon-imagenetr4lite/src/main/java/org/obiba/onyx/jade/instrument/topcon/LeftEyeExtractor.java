/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.topcon;

public class LeftEyeExtractor extends EyeExtractor {

  public static String name = "LEFT_EYE_VENDOR";

  @Override
  public String getName() {
    return name;
  }

  @Override
  public int getEyeTypeIntValue() {
    return 1;
  }

}
