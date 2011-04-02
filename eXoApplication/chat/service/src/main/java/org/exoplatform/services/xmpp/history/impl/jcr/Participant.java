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

import java.util.List;

import org.exoplatform.services.xmpp.util.CodingUtils;
import org.jcrom.annotations.JcrChildNode;
import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrNode;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;
import org.jivesoftware.smack.util.StringUtils;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
@JcrNode(nodeType = "lr:participantchat")
public class Participant {

  /**
   * 
   */
  @JcrName
  private String                 hexName;

  /**
   * 
   */
  @JcrPath
  private String                 path;

  /**
   * 
   */
  @JcrProperty(name = "lr:participantchatjid")
  private String                 jid;

  /**
   * 
   */
  @JcrProperty(name = "lr:participantchatusername")
  private String                 username;

  /**
   * 
   */
  @JcrChildNode
  private List<InterlocutorImpl> interlocutorList;

  /**
   * 
   */
  @JcrChildNode
  private List<InterlocutorImpl> groupChatList;

  /**
   * 
   */
  public Participant() {

  }

  /**
   * @param jid the jid
   * @param interlocutorList the interlocutors
   * @param groupChatList the group chats
   */
  public Participant(String jid, List<InterlocutorImpl> interlocutorList, List<InterlocutorImpl> groupChatList) {
    this.jid = jid;
    this.interlocutorList = interlocutorList;
    this.groupChatList = groupChatList;
    this.username = StringUtils.parseName(jid);
    this.hexName = CodingUtils.encodeToHex(this.username);

  }

  /**
   * @param interlocutor the interlocutor
   */
  public void addInterlocutor(InterlocutorImpl interlocutor) {
    interlocutorList.add(interlocutor);
  }

  /**
   * @param interlocutor the interclocutor
   */
  public void addGroupChat(InterlocutorImpl interlocutor) {
    groupChatList.add(interlocutor);
  }

  /**
   * @return the hexJid
   */
  public String getHexName() {
    return hexName;
  }

  /**
   * @param name the jid
   * @return the interlocutor
   */
  public InterlocutorImpl getInterlocutor(String name) {
    if (interlocutorList != null) {
      for (InterlocutorImpl interlocutor : interlocutorList) {
        if (interlocutor.getInterlocutorName().equals(name))
          return interlocutor;
      }
    }
    return null;
  }

  /**
   * @param name the group chat jid
   * @return interlocutor
   */
  public InterlocutorImpl getGroupChat(String name) {
    if (groupChatList != null) {
      for (InterlocutorImpl interlocutor : groupChatList) {
        if (interlocutor.getInterlocutorName().equals(name))
          return interlocutor;
      }
    }
    return null;
  }

  /**
   * @return the interlocutorList
   */
  public List<InterlocutorImpl> getInterlocutorList() {
    return interlocutorList;
  }

  /**
   * @return the jid
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
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @return the group chats
   */
  public List<InterlocutorImpl> getGroupChatList() {
    return groupChatList;
  }

  /**
   * @param hexJid the hexJid to set
   */
  public void setHexName(String hexName) {
    this.hexName = hexName;
  }

  /**
   * @param interlocutorList the interlocutorList to set
   */
  public void setInterlocutorList(List<InterlocutorImpl> interlocutorList) {
    this.interlocutorList = interlocutorList;
  }

  /**
   * @param jid the jid to set
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
   * @param username the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @param groupChatList the group chats to set
   */
  public void setGroupChatList(List<InterlocutorImpl> groupChatList) {
    this.groupChatList = groupChatList;
  }

}
