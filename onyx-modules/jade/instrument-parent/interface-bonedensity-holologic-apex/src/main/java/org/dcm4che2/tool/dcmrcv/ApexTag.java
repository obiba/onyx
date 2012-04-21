/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.dcm4che2.tool.dcmrcv;

public enum ApexTag {
  PFILENAME(0x00231001), PFILEDATA(0x00231002), PFILELENGTH(0x00231003), RFILEDATA(0x00231004), RFILELENGTH(0x00231005);

  private int value;

  private ApexTag(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

}
