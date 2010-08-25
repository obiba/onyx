/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale;

import java.util.Locale;

import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 *
 */
public class LocalePropertiesTab extends AbstractTab {

  private static final long serialVersionUID = 1L;

  private Locale locale;

  private LocalePropertiesPanel localePropertiesPanel;

  public LocalePropertiesTab(Locale locale) {
    super(new Model<String>(locale.getDisplayLanguage(Session.get().getLocale())));
    this.locale = locale;
  }

  @Override
  public Panel getPanel(String panelId) {
    if(localePropertiesPanel == null) {
      localePropertiesPanel = new LocalePropertiesPanel(panelId, locale);
    }
    return localePropertiesPanel;
  }

  public Locale getLocale() {
    return locale;
  }
}
