/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale.predicate;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.obiba.onyx.quartz.editor.locale.ui.LocalePropertiesTab;

import com.google.common.base.Predicate;

public class LocalePredicateTabRemover implements Predicate<ITab> {

  private List<Locale> locales;

  public LocalePredicateTabRemover(List<Locale> locales) {
    this.locales = locales;
  }

  @Override
  public boolean apply(ITab input) {
    return !locales.contains(((LocalePropertiesTab) input).getLocale());
  }
}
