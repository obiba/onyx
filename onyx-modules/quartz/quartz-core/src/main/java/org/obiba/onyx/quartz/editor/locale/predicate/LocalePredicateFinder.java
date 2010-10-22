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

import java.util.Locale;

import org.obiba.onyx.quartz.editor.locale.model.LocaleProperties2;

import com.google.common.base.Predicate;

/**
 * Predicate which find LocaleProperties where locale is equals with given locale argument
 */
public class LocalePredicateFinder implements Predicate<LocaleProperties2> {

  private Locale locale;

  public LocalePredicateFinder(Locale locale) {
    this.locale = locale;
  }

  @Override
  public boolean apply(LocaleProperties2 input) {
    return input.getLocale().equals(locale);
  }
}