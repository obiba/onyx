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

import java.io.File;
import java.io.InputStream;
import java.security.cert.Certificate;

/**
 * 
 */
public interface IPublicKeyStore {
  public void setCertificate(String name, String certString) throws KeyStoreRuntimeException;

  public void setCertificate(String name, File certFile) throws KeyStoreRuntimeException;

  public void setCertificate(String name, InputStream certStream) throws KeyStoreRuntimeException;

  public void setCertificate(String name, Certificate cert) throws KeyStoreRuntimeException;
}
