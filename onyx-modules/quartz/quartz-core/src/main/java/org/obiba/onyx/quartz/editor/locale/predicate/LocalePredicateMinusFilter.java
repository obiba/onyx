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

import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties;

import com.google.common.base.Predicate;

/**
 * Predicate which keep LocaleProperties where locale in LocaleProperties are not in given locales (Locale list)
 * argument)
 */
public class LocalePredicateMinusFilter implements Predicate<LocaleProperties> {

  private List<Locale> locales;

  public LocalePredicateMinusFilter(List<Locale> locales) {
    this.locales = locales;
  }

  @Override
  public boolean apply(LocaleProperties input) {
    return !locales.contains(input.getLocale());
  }
}