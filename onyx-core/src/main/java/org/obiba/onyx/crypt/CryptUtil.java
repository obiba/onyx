/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.crypt;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

/**
 * 
 */
public final class CryptUtil {

  public static Certificate parseCertificate(String cert) throws CertificateException {
    return parseCertificate(cert, "X.509");
  }

  public static Certificate parseCertificate(String cert, String certType) throws CertificateException {
    CertificateFactory cf = CertificateFactory.getInstance(certType);
    try {
      return cf.generateCertificate(new ByteArrayInputStream(cert.getBytes("US-ASCII")));
    } catch(UnsupportedEncodingException e) {
      // This should never happen. Java VMs support US-ASCII
      throw new RuntimeException(e);
    }
  }
}
