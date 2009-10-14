/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.behavior;

public enum KeyPressed {

  Enter(13), Backspace(8), PageUp(33), PageDown(34), SpaceBar(32), ArrowUp(38), ArrowDown(40), ArrowRight(39), ArrowLeft(37);

  private int keycode;

  KeyPressed(int keycode) {
    this.keycode = keycode;
  }

  public int code() {
    return keycode;
  }

}
