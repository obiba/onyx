/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.spring.context;

import java.text.MessageFormat;
import java.util.Locale;

import org.obiba.onyx.util.StringReferenceCompatibleMessageFormat;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DelegatingMessageSource;

/**
 * An implementation of {@code DelegatingMessageSource} that formats messages using
 * {@link StringReferenceCompatibleMessageFormat}. All other methods are delegated to the parent message source.
 */
public class StringReferenceFormatingMessageSource extends DelegatingMessageSource {

  public StringReferenceFormatingMessageSource(MessageSource parent) {
    this.setParentMessageSource(parent);
  }

  @Override
  protected MessageFormat createMessageFormat(String msg, Locale locale) {
    return new StringReferenceCompatibleMessageFormat((msg != null ? msg : ""), locale);
  }
}
