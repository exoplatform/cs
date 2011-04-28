package org.exoplatform.mail.service.impl;

import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ExoMailSSLSocketFactory extends SSLSocketFactory{
  
  private SSLSocketFactory factory;
  
  private TrustManager[] trustManager = new TrustManager[]{new ExoMailTrustManager()};
  
  private static String protocol = "TLS";
  
  public ExoMailSSLSocketFactory(String protocol){
    if(protocol != null) ExoMailSSLSocketFactory.protocol = protocol;
    try {
      SSLContext sslcontext = SSLContext.getInstance(ExoMailSSLSocketFactory.protocol);
      sslcontext.init(null, trustManager, null);//new java.security.SecureRandom()
      factory = (SSLSocketFactory)sslcontext.getSocketFactory();
  } catch(Exception ex) {}

  }
  
  public static SocketFactory getDefault() {
      return new ExoMailSSLSocketFactory(ExoMailSSLSocketFactory.protocol);
  }

  public SSLSocketFactory getSSLSocketFactory(){
    return factory;
  }
  
  public Socket createSocket() throws IOException {
    return factory.createSocket();
  }

  public Socket createSocket(Socket socket, String s, int i, boolean flag)
      throws IOException {
    return factory.createSocket(socket, s, i, flag);
  }

  public Socket createSocket(InetAddress inaddr, int i,
      InetAddress inaddr1, int j) throws IOException {
    return factory.createSocket(inaddr, i, inaddr1, j);
  }

  public Socket createSocket(InetAddress inaddr, int i)
      throws IOException {
    return factory.createSocket(inaddr, i);
  }

  public Socket createSocket(String s, int i, InetAddress inaddr, int j)
      throws IOException {
    return factory.createSocket(s, i, inaddr, j);
  }

  public Socket createSocket(String s, int i) throws IOException {
    return factory.createSocket(s, i);
  }

  public String[] getDefaultCipherSuites() {
    return factory.getDefaultCipherSuites();
  }

  public String[] getSupportedCipherSuites() {
    return factory.getSupportedCipherSuites();
  }
  
  /** Implement X509TrustManager*/
  class ExoMailTrustManager implements X509TrustManager{

    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
    
  }
} 
