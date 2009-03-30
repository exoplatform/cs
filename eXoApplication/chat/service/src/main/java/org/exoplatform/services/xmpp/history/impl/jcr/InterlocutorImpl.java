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
package org.exoplatform.services.xmpp.history.impl.jcr;

import org.exoplatform.services.xmpp.history.Interlocutor;
import org.exoplatform.services.xmpp.util.StringUtils;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
@JcrNode(nodeType = "lr:interlocutor")
public class InterlocutorImpl implements Interlocutor {

  /**
   * 
   */
  @JcrName
  private String  hexJid;

  /**
   * 
   */
  @JcrPath
  private String  path;

  /**
   * 
   */
  @JcrProperty(name = "lr:interlocutorjid")
  private String  jid;

  /**
   * 
   */
  @JcrProperty(name = "lr:interlocutorname")
  private String  interlocutorName;

  /**
   * 
   */
  @JcrProperty(name = "lr:interlocutorisRoom")
  private Boolean isRoom;

  /**
   * 
   */
  @JcrProperty(name = "lr:conversationId")
  private String  conversationId;

  /**
   * 
   */
  public InterlocutorImpl() {
   
  }

  /**
   * @param jid the jid
   * @param conversationId the conversation id
   * @param isRoom is group chat
   */
  public InterlocutorImpl(String jid, String conversationId, Boolean isRoom) {
    super();
    this.jid = jid;
    this.conversationId = conversationId;
    this.hexJid = StringUtils.encodeToHex(jid);
    this.interlocutorName = StringUtils.getUsernameFromJID(jid);
    this.isRoom = isRoom;
  }

  /**
   * @return the coversationId
   */
  public String getConversationId() {
    return conversationId;
  }

  /**
   * @return the hexJid
   */
  public String getHexJid() {
    return hexJid;
  }

  
  /**
   * {@inheritDoc}
   */
  public String getJid() {
    return jid;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * {@inheritDoc}
   */
  public String getInterlocutorName() {
    return interlocutorName;
  }

  /**
   * {@inheritDoc}
   */
  public Boolean getIsRoom() {
    return isRoom;
  }

  /**
   * {@inheritDoc}
   */
  public void setIsRoom(Boolean isRoom) {
    this.isRoom = isRoom;
  }

  /**
   * @param coversationId the coversationId to set
   */
  public void setConversationId(String coversationId) {
    this.conversationId = coversationId;
  }

  /**
   * @param hexJid the hexJid to set
   */
  public void setHexJid(String hexJid) {
    this.hexJid = hexJid;
  }

  /**
   * {@inheritDoc}
   */
  public void setJid(String jid) {
    this.jid = jid;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * {@inheritDoc}
   */
  public void setInterlocutorName(String interlocutorName) {
    this.interlocutorName = interlocutorName;
  }

}
