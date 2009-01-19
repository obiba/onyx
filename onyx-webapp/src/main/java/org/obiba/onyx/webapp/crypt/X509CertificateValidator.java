/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.crypt;

import java.security.cert.CertificateException;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.obiba.onyx.crypt.CryptUtil;

/**
 * 
 */
public class X509CertificateValidator implements IValidator {

  private static final long serialVersionUID = 1L;

  public X509CertificateValidator() {
  }

  public void validate(IValidatable validatable) {
    String certString = (String) validatable.getValue();
    try {
      CryptUtil.parseCertificate(certString);
    } catch(CertificateException e) {
      ValidationError error = new ValidationError();
      error.addMessageKey("InvalidCertificate");
      error.setVariable("certificateException", e);
      validatable.error(error);
    }
  }

}
