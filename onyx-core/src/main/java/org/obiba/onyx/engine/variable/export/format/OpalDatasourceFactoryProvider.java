/*******************************************************************************
 * Copyright 2012(c) OBiBa. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.engine.variable.export.format;

import java.io.File;
import java.net.Socket;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedKeyManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.magma.DatasourceFactory;
import org.obiba.magma.ValueTable;
import org.obiba.magma.crypt.KeyProvider;
import org.obiba.onyx.crypt.OnyxKeyStore;
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination;
import org.obiba.onyx.engine.variable.export.OnyxDataExportDestination.Format;
import org.obiba.opal.rest.client.magma.OpalJavaClient;
import org.obiba.opal.rest.client.magma.RestDatasource;

public class OpalDatasourceFactoryProvider implements DatasourceFactoryProvider {

  private OnyxKeyStore onyxKeyStore;

  @Override
  public Format getFormat() {
    return Format.OPAL;
  }

  @Override
  public DatasourceFactory getDatasourceFactory(final OnyxDataExportDestination destination, File outputDir, KeyProvider provider, Iterable<ValueTable> tables) {

    try {
      final OpalJavaClient opalJavaClient = new OpalJavaClient(destination.getOptions().getOpalUri(), destination.getOptions().getUsername(), destination.getOptions().getPassword());
      return new AbstractDatasourceFactory() {
        @Override
        public Datasource internalCreate() {
          return new RestDatasource(destination.getName(), opalJavaClient, destination.getOptions().getOpalDatasource());
        }
      };
    } catch(URISyntaxException e) {
      throw new RuntimeException(e);
    }

  }

  // This is currently unused because the OpalJavaClient doesn't support authentication through certificates.
  private SSLContext makeSslContext(OnyxDataExportDestination destination) {
    try {
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(new X509KeyManager[] { new OnyxKeyManager() }, new TrustManager[] { new OnyxTrustManager(onyxKeyStore.getCertificate(destination.getName())) }, new SecureRandom());
      return sslContext;
    } catch(NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch(KeyManagementException e) {
      throw new RuntimeException(e);
    }
  }

  private class OnyxKeyManager extends X509ExtendedKeyManager {

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
      return null;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
      return null;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
      return null;
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
      return null;
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
      return null;
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
      return null;
    }
  }

  private class OnyxTrustManager implements X509TrustManager {

    private final Certificate cert;

    public OnyxTrustManager(Certificate certificate) {
      this.cert = certificate;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      throw new CertificateException();
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
      for(X509Certificate x509Cert : chain) {
        try {
          x509Cert.verify(cert.getPublicKey());
          // If verify succeeds, it doesn't throw an Exception
          return;
        } catch(GeneralSecurityException e) {
          // Ignore
        }
      }
      throw new CertificateException("no trusted certificates");
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }

  }
}
