/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

package org.exoplatform.services.xmpp.connection.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.xmpp.connection.XMPPSession;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;
import org.exoplatform.services.xmpp.userinfo.UserInfoService;
import org.exoplatform.ws.frameworks.cometd.transport.ContinuationServiceDelegate;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class XMPPMessenger {

  /**
   * Logger.
   */
  protected static final Log             LOGGER                     = ExoLogger.getLogger("XMPPMessenger");

  /**
   * 
   */
  private static final String            PORT                       = "port";

  /**
   * 
   */
  private static final String            HOST                       = "host";

  /**
   * 
   */
  private static final String            DEFAULT_HOST               = "127.0.0.1";

  /**
   * 
   */
  private static final int               DEFAULT_PORT               = 5222;

  /**
   * 
   */
  private static final int               DEFAULT_DISCONNECT_TIME    = 10000;

  /**
   * 
   */
  private static final long              DEFAULT_CLOSE_SESSION_TIME = 10 * DEFAULT_DISCONNECT_TIME;

  /**
   * 
   */
  private static int                     disconnect_time;

  /**
   * 
   */
  private static long                    close_session_time;

  /**
   * 
   */
  private static int                     file_send_timeout;

  static {
    Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
  }

  /**
   * 
   */
  private static ConnectionConfiguration connectionConfiguration    = new ConnectionConfiguration(DEFAULT_HOST,
                                                                                                  DEFAULT_PORT);

  /**
   * 
   */
  private Map<String, XMPPSession>       sessions_                  = new HashMap<String, XMPPSession>();

  // TODO add XMPPSession cleaner. This class must search closed session in
  // sessions map and remove it.

  /**
   * @param params the initParms
   */
  public XMPPMessenger(InitParams params) {
    PropertiesParam pparams = params.getPropertiesParam("openfire-connection-conf");
    if (pparams != null) {
      String host = (pparams.getProperty(HOST) != null) ? pparams.getProperty(HOST) : DEFAULT_HOST;
      int port = (pparams.getProperty(PORT) != null) ? Integer.valueOf(pparams.getProperty(PORT))
                                                    : DEFAULT_PORT;
      connectionConfiguration = new ConnectionConfiguration(host, port);
    } else {
      LOGGER.info("Connection params to XMPP server is null." + "Use default configuration.");
    }
    pparams = params.getPropertiesParam("alive-checker-conf");
    if (pparams != null) {
      disconnect_time = (pparams.getProperty("disconnect") != null) ? Integer.parseInt(pparams.getProperty("disconnect"))
                                                                   : DEFAULT_DISCONNECT_TIME;
      close_session_time = (pparams.getProperty("close") != null) ? Long.parseLong(pparams.getProperty("close"))
                                                                 : DEFAULT_CLOSE_SESSION_TIME;
    } else {
      LOGGER.info("Connection params to alive-checker-conf server is null."
          + "Use default configuration.");
    }
    pparams = params.getPropertiesParam("send-file");
    if (pparams != null) {
      file_send_timeout = (pparams.getProperty("timeout") != null) ? Integer.parseInt(pparams.getProperty("timeout"))
                                                                  : OutgoingFileTransfer.getResponseTimeout();
    } else {
      LOGGER.info("Connection params to alive-checker-conf server is null."
          + "Use default configuration.");
    }
    OutgoingFileTransfer.setResponseTimeout(file_send_timeout);
  }

  /**
   * @return default configuration for XMPP connection.
   */
  public static ConnectionConfiguration getConnectionConfiguration() {
    return connectionConfiguration;
  }

  /**
   * @return the close session timeout
   */
  public static long getCloseSessionTime() {
    return close_session_time;
  }

  /**
   * @return the disconnect timeout
   */
  public static int getDisconnectTime() {
    return disconnect_time;
  }

  /**
   * Connect to XMPP server.
   * 
   * @param username the user name.
   * @param password the password.
   * @throws XMPPException the XMPPException
   */
  public synchronized void login(String username,
                                 String password,
                                 UserInfoService userInfoService,
                                 ContinuationServiceDelegate delegate,
                                 HistoryImpl history,
                                 ResourceBundle rb) throws XMPPException {
    try {
      XMPPSession session = sessions_.get(username);
      if (session != null && session.getConnection().isConnected()
          && session.getConnection().isAuthenticated()) {
        if (LOGGER.isDebugEnabled())
          LOGGER.warn("Client has one opened session!");
        // throw new XMPPException("Client has one opened session!");
      } else {
        XMPPSessionImpl sessionImpl = new XMPPSessionImpl(username,
                                                          password,
                                                          userInfoService,
                                                          delegate,
                                                          history,
                                                          rb);
        sessions_.put(username, sessionImpl);
      }
    } catch (XMPPException e) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.error("Can't create XMPP session for user '" + username + "'. Exception: " + e);
        e.printStackTrace();
      }
      throw new XMPPException("Error login!");
    }

  }

  /**
   * Close XMPP connection for given user and remove this session from the
   * sessions list.
   * 
   * @param username the user name.
   */
  public synchronized void logout(String username) {
    XMPPSession session = sessions_.remove(username);
    if (session != null)
      session.close();
  }

  /**
   * Get XMPP session for given user.
   * 
   * @param username the user name.
   * @return the XMPPSession or null if user is not registered on the server.
   */
  public XMPPSession getSession(String username) {
    if (LOGGER.isDebugEnabled())
      LOGGER.info("Get session for User '" + username + "'.");
    XMPPSession session = sessions_.get(username);
    if (session == null && LOGGER.isDebugEnabled())
      LOGGER.warn("User '" + username + "' is not registered on the server.");
    return session;
  }

}
