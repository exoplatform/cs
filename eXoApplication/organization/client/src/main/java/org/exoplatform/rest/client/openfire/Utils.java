/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.rest.client.openfire;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jivesoftware.util.JiveGlobals;
import org.w3c.dom.Document;

/**
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */

class Utils {

  static String getBaseURL() {
    String serverBaseURL = JiveGlobals.getXMLProperty("eXo.env.serverBaseURL");
    serverBaseURL = (serverBaseURL != null) ? serverBaseURL : new String("http://localhost:8080/");
    serverBaseURL = serverBaseURL.endsWith("/") ? serverBaseURL : serverBaseURL + "/";
    String restContext = JiveGlobals.getXMLProperty("eXo.env.restContextName");
    restContext = (restContext != null) ? restContext : new String("rest");
    // If "eXo.env.restContextName" system property exists, it will override value from Openfire.xml
    restContext = (System.getProperty("eXo.env.restContextName") != null) ? System.getProperty("eXo.env.restContextName") : restContext;
    return (serverBaseURL + restContext);
  }

  private static UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(JiveGlobals.getXMLProperty("provider.authorizedUser.name"), JiveGlobals.getXMLProperty("provider.authorizedUser.password"));

  static Response doGet(URL url) throws HttpException, IOException {
    HttpClient httpClient = new HttpClient();
    httpClient.getState().setCredentials(new AuthScope(url.getHost(), url.getPort()), usernamePasswordCredentials);
    GetMethod get = new GetMethod(url.toString());
    get.setDoAuthentication(true);
    int status = httpClient.executeMethod(get);
    Document resDoc = null;
    try {
      if (get.getResponseBody().length > 0) {
        // if response has body
        resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(get.getResponseBodyAsStream());
      }
    } catch (Exception e) {
      throw new HttpException("XML parsing error : " + e);
    } finally {
      get.releaseConnection();
    }
    return new Response(status, resDoc);
  }

  static Response doPost(URL url) throws HttpException, IOException {
    HttpClient httpClient = new HttpClient();
    httpClient.getState().setCredentials(new AuthScope(url.getHost(), url.getPort()), usernamePasswordCredentials);
    PostMethod post = new PostMethod(url.toString());
    post.setDoAuthentication(true);
    int status = httpClient.executeMethod(post);
    Document resDoc = null;
    try {
      if (post.getResponseBody().length > 0) {
        // if response has body
        resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(post.getResponseBodyAsStream());
      }
    } catch (Exception e) {
      throw new HttpException("XML parsing error : " + e);
    } finally {
      post.releaseConnection();
    }
    return new Response(status, resDoc);
  }

  static Response doGet(URL url, HashMap<String, String> params) throws HttpException, IOException {
    if (params == null || params.size() == 0)
      return doGet(url);
    HttpClient httpClient = new HttpClient();
    httpClient.getState().setCredentials(new AuthScope(url.getHost(), url.getPort()), usernamePasswordCredentials);
    StringBuffer _url = new StringBuffer(url.toString());
    Set<String> key_set = params.keySet();
    _url.append((key_set.size() > 0) ? "?" : "");
    for (String key : key_set) {
      _url.append(key).append("=").append(params.get(key));
    }
    
    GetMethod get = new GetMethod(_url.toString());
    get.setDoAuthentication(true);
    int status = httpClient.executeMethod(get);
    Document resDoc = null;
    try {
      if (get.getResponseBody().length > 0) {
        // if response has body
        resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(get.getResponseBodyAsStream());
      }
    } catch (Exception e) {
      throw new HttpException("XML parsing error : " + e);
    } finally {
      get.releaseConnection();
    }
    return new Response(status, resDoc);
  }

  static Response doPost(URL url, HashMap<String, String> params) throws HttpException, IOException {
    if (params == null || params.size() == 0)
      return doPost(url);
    HttpClient httpClient = new HttpClient();
    httpClient.getState().setCredentials(new AuthScope(url.getHost(), url.getPort()), usernamePasswordCredentials);
    PostMethod post = new PostMethod(url.toString());
    post.setDoAuthentication(true);
    Set<String> key_set = params.keySet();
    for (String key : key_set) {
      post.setParameter(key, params.get(key));
    }
    int status = httpClient.executeMethod(post);
    Document resDoc = null;
    try {
      if (post.getResponseBody().length > 0) {
        // if response has body
        resDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(post.getResponseBodyAsStream());
      }
    } catch (Exception e) {
      throw new HttpException("XML parsing error : " + e);
    } finally {
      post.releaseConnection();
    }
    return new Response(status, resDoc);
  }

  static Map<String, String> parseQuery(List<String> l) {
    Map<String, String> m = new HashMap<String, String>();
    if (l != null && l.size() > 0) {
      for (String s : l) {
        int eq = s.indexOf('=');
        if (eq <= 0)
          continue;
        String key = s.substring(0, eq).trim();
        String value = s.substring(eq + 1, s.length()).trim();
        m.put(key, value);
      }
    }
    return m;
  }

  static class Response {
    private final int      status_;

    private final Document d_;

    public Response(int status, Document d) {
      status_ = status;
      d_ = d;
    }

    public int getStatus() {
      return status_;
    }

    public Document getResponseDoc() {
      return d_;
    }
  }

}
