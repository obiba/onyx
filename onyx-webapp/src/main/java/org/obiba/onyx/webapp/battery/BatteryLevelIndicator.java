/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.battery;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Native;
import com.sun.jna.Platform;

@SuppressWarnings("serial")
public class BatteryLevelIndicator extends Panel {

  private static final Logger log = LoggerFactory.getLogger(BatteryLevelIndicator.class);

  private static final Kernel32 kernel32;

  static {
    if(Platform.isWindows()) {
      kernel32 = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class);
    } else {
      kernel32 = null;
    }
  }

  public BatteryLevelIndicator(String id) {
    super(id);

    // Only supported under Windows
    if(Platform.isWindows()) {
      add(new Label("percentLeft", new PropertyModel<String>(this, "percentLeft")));
      add(new WebMarkupContainer("power").add(new AttributeModifier("style", true, new PropertyModel<String>(this, "percentLeft")) {
        @Override
        protected String newValue(String currentValue, String replacementValue) {
          return currentValue.replaceFirst("width:\\d+%", "width:" + replacementValue);
        }
      }));
    } else {
      log.info("Battery Level Indicator is disabled: only supported under Windows");
      add(new Label("percentLeft", ""));
      add(new Label("power", ""));
    }
  }

  @Override
  public boolean isVisible() {
    return Platform.isWindows() && readBatteryStatus().BatteryFlag < (byte) 127;
  }

  public String getPercentLeft() {
    byte percent = readBatteryStatus().BatteryLifePercent;
    if(percent > 100) percent = 100;
    return percent + "%";
  }

  private Kernel32.SYSTEM_POWER_STATUS readBatteryStatus() {
    Kernel32.SYSTEM_POWER_STATUS status = new Kernel32.SYSTEM_POWER_STATUS();
    kernel32.GetSystemPowerStatus(status);
    log.debug("Battery status: {}", status);
    log.debug("Battery flag: {}", status.BatteryFlag);
    return status;
  }
}
