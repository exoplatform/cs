/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mail.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.mail.util.MailSSLSocketFactory;

/**
 * Created by The eXo Platform SAS
 * Author : Nguyen Van Hoang
 *          hoang.nguyen@exoplatform.com
 * Nov 15, 2010  
 */
public class ExoMailTrustManager implements X509TrustManager {

  private X509TrustManager         trustManager;

  public static final String       PATH_CERTS_FILE      = System.getProperty("java.home") + File.separatorChar + "lib" + File.separatorChar + "security";

  private String                   pathCertFile;

  private char[]                   pwdCertFile          = null;

  private Map<String, Certificate> temporaryCerts       = new HashMap<String, Certificate>();                                                             // <alias, cert>

  private static final Log         log                  = ExoLogger.getLogger("cs.mail.service");

  private KeyStore                 keystore;

  private String                   hostname;

  private String                   protocolName;

  private boolean                  isStoreCertPermanent = false;

  private File                     ksfile;

  public ExoMailTrustManager(String relPath, boolean isStorePermanent, String host, String sslProtocol) throws GeneralSecurityException {
    this(relPath, isStorePermanent);
    this.hostname = host;
    if (sslProtocol != null)
      this.protocolName = sslProtocol;
  }

  public ExoMailTrustManager() throws GeneralSecurityException {
    this(null, true);
  }

  /**
   * @param {@link String} the path name of cert file
   * @param {@link Boolean} whether is store the certs permanent?
   * @throws GeneralSecurityException
   **/
  public ExoMailTrustManager(String pathCertFile, boolean storePermanent) throws GeneralSecurityException {
    this.isStoreCertPermanent = storePermanent;
    File temFile = null;
    boolean isLoged = false;
    try {
      if (pathCertFile != null)
        temFile = new File(pathCertFile);
    } catch (Exception e) {
    }// do nothing

    if (pathCertFile == null || temFile == null || (!temFile.isDirectory() && !temFile.isFile()))
      this.pathCertFile = PATH_CERTS_FILE;
    else
      this.pathCertFile = pathCertFile;
    if (temFile != null)
      temFile.delete();
    try {
      refreshTrustManager();
    } catch (Exception e) {
      if (!isLoged)
        log.warn("Cannot refresh trustmanager. The certs that presented by server mail are not stored.\n", e);
      isLoged = true;
    }
    if (isLoged)
      throw new GeneralSecurityException();
  }

  public X509Certificate[] getAcceptedIssuers() {
    if (trustManager == null || !(trustManager instanceof X509TrustManager))
      return new X509Certificate[0];
    return trustManager.getAcceptedIssuers();
  }

  /**
   * Check the certs presented by server whether is trusted or not? If not, they will stored.
   * */
  public void checkServerTrusted(X509Certificate[] certs, String authType) throws CertificateException {
    if (trustManager != null)
      try {
        trustManager.checkServerTrusted(certs, authType);
      } catch (CertificateException ce) {
        updateCerts(certs, this.isStoreCertPermanent);
        trustManager.checkServerTrusted(certs, authType);
      }
    ;
  }

  public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
    if (trustManager != null)
      trustManager.checkClientTrusted(chain, authType);
  }

  protected void refreshTrustManager() throws Exception {
    KeyStore ks = getKeyStore(this.pathCertFile, null);// set password is null to get all public key
    Set<String> aliases = temporaryCerts.keySet();
    if (aliases != null && aliases.size() > 0)
      for (Iterator<String> it = aliases.iterator(); it.hasNext();) {
        String alias = it.next();
        ks.setCertificateEntry(alias, temporaryCerts.get(alias));
      }
    this.keystore = ks;
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    tmf.init(ks);
    TrustManager tms[] = tmf.getTrustManagers();
    for (int i = 0; i < tms.length; i++) {
      if (tms[i] instanceof X509TrustManager) {
        /*
         * X509TrustManager trustManagerTem = (X509TrustManager)tms[i]; X509Certificate[] x509s = trustManagerTem.getAcceptedIssuers(); for(X509Certificate x509 : x509s){ String host = x509.getSubjectDN().toString().split(",")[0].split("=")[1]; if(this.hostname != null && !this.hostname.equalsIgnoreCase("") && (!host.equalsIgnoreCase(this.hostname) || x509.toString().contains(this.hostname))){
         * trustManager = trustManagerTem; return; } }
         */
        trustManager = (X509TrustManager) tms[i];
        return;
      }
    }
    throw new NoSuchAlgorithmException("There is no X059TrustManager in TrustManager");
  }

  protected void updateCerts(Certificate[] certs, boolean isStoreCertPermanent) {
    String alias = this.hostname;
    try {
      FileOutputStream fos = null;
      X509Certificate x509 = (X509Certificate) certs[0];
      if (Utils.isEmptyField(alias))
        alias = x509.getSubjectDN().toString().split(",")[0].split("=")[1];
      if (Utils.isEmptyField(alias))
        alias = x509.getSubjectX500Principal().toString().split(",")[0].split("=")[1];
      if (Utils.isEmptyField(alias))
        alias = UUID.randomUUID().toString();

      if (isStoreCertPermanent) {
        this.keystore.setCertificateEntry(alias, x509);
        try {
          if (this.ksfile != null)
            fos = new FileOutputStream(this.ksfile);
          else
            fos = new FileOutputStream(ExoMailTrustManager.getCertFile(this.pathCertFile));
          this.keystore.store(fos, this.pwdCertFile);
          fos.close();
        } catch (Exception e) {// if cacerts file is read-only
          File certtem = new File("certtem.cer");
          OutputStream os = new FileOutputStream(certtem);
          os.write(x509.toString().getBytes());
          String command = "keytool -import -alias " + alias + " -trustcacerts -file " + certtem.getPath();
          Runtime.getRuntime().exec(command);
          if (certtem != null)
            certtem.delete();
          os.close();
        }
      } else
        temporaryCerts.put(alias, certs[0]);

      refreshTrustManager();
    } catch (Exception e) {
      log.warn("The cert of " + alias + " is not updated");
    }
  }

  public static File getCertFile(String relPath) throws Exception {
    File suncert = new File(relPath);
    ;
    if (suncert != null && suncert.isDirectory()) {
      suncert = new File(relPath, "jssecacerts");
      if (suncert != null && !suncert.isFile())
        suncert = new File(relPath, "cacerts");
    }
    if (!suncert.canWrite())
      suncert.setWritable(true);
    return suncert;
  }

  /**
   * Get KeyStore from java KeyStory file
   * @return {@link KeyStore}readonly
   * **/
  protected KeyStore getKeyStore(String relPath, char[] password) throws Exception {
    File certfile = ExoMailTrustManager.getCertFile(relPath);
    // if(!certfile.canWrite()) throw new FileNotFoundException("The " +relPath+ " is readonly. You have not write permission with it.");
    this.ksfile = certfile;
    InputStream is = new FileInputStream(certfile);
    KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
    keyStore.load(is, password);
    is.close();
    return keyStore;
  }

  public ExoMailTrustManager getExomailTrustManager() {
    return (ExoMailTrustManager) trustManager;
  }

  /**
   * set trusted host. If hostname is not constructed, set certificate for all hosts*/
  public MailSSLSocketFactory trustHosts() throws Exception {
    MailSSLSocketFactory sslsocket = new MailSSLSocketFactory();
    if (this.protocolName != null && !this.protocolName.equalsIgnoreCase("TLS"))
      sslsocket = new MailSSLSocketFactory(this.protocolName);

    if (this.hostname != null && this.hostname.equalsIgnoreCase(""))
      sslsocket.setTrustedHosts(new String[] { this.hostname });
    else
      sslsocket.setTrustAllHosts(true);
    sslsocket.setTrustManagers(new TrustManager[] { trustManager });

    return sslsocket;
  }

  protected void setProtocolName(String protocolname) {
    this.protocolName = protocolname;
  }

  protected void setHostName(String hn) {
    this.hostname = hn;
  }

  public X509TrustManager getTrustManager() {
    return trustManager;
  }
}
