/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.instrument.jtech;

public final class Tracker5Util {

  private static final double ONE_POUND_FORCE_IN_NEWTONS = 4.4482216152605;

  private static final double ONE_POUND_FORCE_IN_KILOGRAMS = 4.4482216152605 / 9.80665;

  public static double asNewtons(int thousanthsPounds) {
    return ((double) thousanthsPounds) * ONE_POUND_FORCE_IN_NEWTONS / 1000d;
  }

  public static double asKg(int thousanthsPounds) {
    return ((double) thousanthsPounds) * ONE_POUND_FORCE_IN_KILOGRAMS / 1000d;
  }
}
