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

package org.exoplatform.services.xmpp.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.packet.DiscoverItems.Item;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.xmpp.bean.FieldBean;
import org.exoplatform.services.xmpp.bean.FormBean;
import org.exoplatform.services.xmpp.ext.transport.AIMTransport;
import org.exoplatform.services.xmpp.ext.transport.GtalkTransport;
import org.exoplatform.services.xmpp.ext.transport.ICQTransport;
import org.exoplatform.services.xmpp.ext.transport.MSNTransport;
import org.exoplatform.services.xmpp.ext.transport.Transport;
import org.exoplatform.services.xmpp.ext.transport.XMPPTransport;
import org.exoplatform.services.xmpp.ext.transport.YahooTransport;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class XMPPConnectionUtils {

  private static final Log LOGGER = LogFactory.getLog("ConnectionUtils");

  /**
   * Discovery Server items.
   * 
   * @param connection - XMPPConnection>.
   * @param entityId - item entity ID.
   * @return - DiscoveryItems.
   */
  public static DiscoverItems getDiscoverItems(XMPPConnection connection, String entityId) {
    ServiceDiscoveryManager serviceDiscoveryManager = new ServiceDiscoveryManager(connection);
    try {
      return serviceDiscoveryManager.discoverItems(entityId);
    } catch (XMPPException e) {
      LOGGER.error("Can't get discovery items for entityId: " + entityId + " : " + e);
      return new DiscoverItems();
    }
  }

  /**
   * Get list of supported transports for connection.
   * 
   * @param connection - XMPPConnection.
   * @return - <code>List</code> of supported transports.
   */
  public static List<Transport> getSupportedTransports(XMPPConnection connection) {
    DiscoverItems discoverItems = getDiscoverItems(connection, connection.getServiceName());
    Iterator<Item> iterator = discoverItems.getItems();
    List<Transport> transports = new ArrayList<Transport>();
    String serviceName = connection.getServiceName();
    while (iterator.hasNext()) {
      String entityName = iterator.next().getEntityID();
      if (entityName.startsWith(Transport.Type.YAHOO)) {
        transports.add(new YahooTransport(serviceName));
      } else if (entityName.startsWith(Transport.Type.ICQ)) {
        transports.add(new ICQTransport(serviceName));
      } else if (entityName.startsWith(Transport.Type.XMPP)) {
        transports.add(new XMPPTransport(serviceName));
      } else if (entityName.startsWith(Transport.Type.AIM)) {
        transports.add(new AIMTransport(serviceName));
      } else if (entityName.startsWith(Transport.Type.GTALK)) {
        transports.add(new GtalkTransport(serviceName));
      } else if (entityName.startsWith(Transport.Type.MSN)) {
        transports.add(new MSNTransport(serviceName));
      }
    }
    return transports;
  }

  /**
   * Register user for use transport. Transport registered by
   * <code>String serviceName</code>.<br/>
   * <code>Transport.getServiceName().</code>
   * 
   * @param connection - XMPPConnection.
   * @param serviceName - service name.
   * @param username - username for remote IM service (Yahoo, ICQ, etc).
   * @param password - password for remote IM service (Yahoo, ICQ, etc).
   * @throws XMPPException - XMPPException.
   */
  public static void registerUser(XMPPConnection connection,
                                  String serviceName,
                                  String username,
                                  String password) throws XMPPException {

    Registration registration = new Registration();
    registration.setType(IQ.Type.SET);
    registration.setTo(serviceName);
    registration.addExtension(new TransportRegisterExtension());

    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put("username", username);
    attributes.put("password", password);
    registration.setAttributes(attributes);

    PacketCollector collector = connection.createPacketCollector(new PacketIDFilter(registration.getPacketID()));
    connection.sendPacket(registration);

    IQ response = (IQ) collector.nextResult(2 * SmackConfiguration.getPacketReplyTimeout());

    collector.cancel();
    if (response == null) {
      throw new XMPPException("Server timed out!");
    }
    if (response.getType() == IQ.Type.ERROR) {
      throw new XMPPException("Error registering user!", response.getError());
    }
  }

  /**
   * Unregister user for using transport. Transport identified by
   * <code>String serviceName</code>.<br/>
   * <code>Transport.getServiceName()</code>
   * 
   * @param connection - XMPPConnection.
   * @param serviceName - serviceName.
   * @throws XMPPException - XMPPException.
   */
  public static void unregisterUser(XMPPConnection connection, String serviceName) throws XMPPException {

    Registration registration = new Registration();
    registration.setType(IQ.Type.SET);
    registration.setTo(serviceName);
    Map<String, String> map = new HashMap<String, String>();
    map.put("remove", "");
    registration.setAttributes(map);

    PacketCollector collector = connection.createPacketCollector(new PacketIDFilter(registration.getPacketID()));
    connection.sendPacket(registration);

    IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
    collector.cancel();
    if (response == null) {
      throw new XMPPException("Server timed out!");
    }
    if (response.getType() == IQ.Type.ERROR) {
      throw new XMPPException("Error registering user!", response.getError());
    }
  }

  /**
   * Checks if the user is registered with transport. Transport identified by
   * <code>String serviceName</code>.<br/>
   * <code>Transport.getServiceName()</code>
   * 
   * @param connection - XMPPConnection.
   * @param serviceName - serviceName.
   * @return true if the user is registered with the transport.
   */
  public static boolean isRegistered(XMPPConnection connection, String serviceName) {
    if (!connection.isConnected()) {
      return false;
    }
    ServiceDiscoveryManager discoveryManager = new ServiceDiscoveryManager(connection);
    try {
      DiscoverInfo info = discoveryManager.discoverInfo(serviceName);
      return info.containsFeature("jabber:iq:registered");
    } catch (XMPPException e) {
      LOGGER.error("Error: " + e);
    }
    return false;
  }

  /**
   * @param fullJID
   * @return
   */
  public static String getAddress(String fullJID) {
    return fullJID.split("/")[0];
  }

  /**
   * @param address
   * @return
   */
  public static String getName(String address) {
    return address.split("@")[0];
  }

  /*
   * RegisterExtention.
   */
  static class TransportRegisterExtension implements PacketExtension {

    public String getElementName() {
      return "x";
    }

    public String getNamespace() {
      return "jabber:iq:gateway:register";
    }

    public String toXML() {
      StringBuffer sb = new StringBuffer();
      sb.append("<")
        .append(getElementName())
        .append(" xmlns=\"")
        .append(getNamespace())
        .append("\"/>");
      return sb.toString();
    }
  }
  
  
  
}
