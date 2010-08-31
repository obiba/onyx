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

public class LocalePredicateFactory {

  public static LocalePredicateFinder newLocalePredicateFinder(Locale locale) {
    return new LocalePredicateFinder(locale);
  }

  public static LocalePredicateMinusFilter newLocalePredicateFilter(List<Locale> locales) {
    return new LocalePredicateMinusFilter(locales);
  }

  public static LocalePredicateTabRemover newLocalePredicateTabRemover(List<Locale> locales) {
    return new LocalePredicateTabRemover(locales);
  }
}
