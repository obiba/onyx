/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.wicket.test;

import java.util.Locale;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

public class ExtendedApplicationContextMock extends ApplicationContextMock {

  private static final long serialVersionUID = 1L;

  private static final String MESSAGE_SOURCE_BEAN_NAME = "messageSource";

  private String message;

  /**
   * If a message source bean exists in the context (with name "messageSource"), returns the message resolved with that
   * bean.
   * 
   * Otherwise, the message configured with <code>setMessage</code> is returned.
   * 
   * @param code message key
   * @param args message arguments (for placeholders)
   * @param defaultMessage default message
   * @param locale message locale
   * @return message previously set by <code>setMessage</code>
   */
  public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
    if(containsBean(MESSAGE_SOURCE_BEAN_NAME)) {
      MessageSource messageSource = (MessageSource) this.getBean("messageSource");
      return messageSource.getMessage(code, args, defaultMessage, locale);
    } else {
      return message;
    }
  }

  /**
   * Equivalent to <code>getMessage(code, args, null, locale)</code>.
   * 
   * @param code message key
   * @param args message arguments (for placeholders)
   * @param locale message locale
   * @return message previously set by <code>setMessage</code>
   */
  public String getMessage(String code, Object[] args, Locale locale) {
    return getMessage(code, args, null, locale);
  }

  /**
   * If a message source bean exists in the context (with name "messageSource"), returns the message resolved with that
   * bean.
   * 
   * Otherwise, the message configured with <code>setMessage</code> is returned.
   * 
   * @param resolvable message source resolvable
   * @param locale message locale
   * @return message previously set by <code>setMessage</code>
   */
  public String getMessage(MessageSourceResolvable resolvable, Locale locale) {
    if(containsBean(MESSAGE_SOURCE_BEAN_NAME)) {
      MessageSource messageSource = (MessageSource) this.getBean("messageSource");
      return messageSource.getMessage(resolvable, locale);
    } else {
      return message;
    }
  }

  /**
   * Sets the message returned by <code>getMessage</code> when no message source exists in the context.
   * 
   * @param message the message
   */
  public void setMessage(String message) {
    this.message = message;
  }
}
