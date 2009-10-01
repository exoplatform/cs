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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jcrom.annotations.JcrChildNode;
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

@JcrNode(nodeType = "lr:conversation")
public class Conversation {

  /**
   * 
   */
  @JcrName
  private String                      conversationId;

  /**
   * 
   */
  @JcrPath
  private String                      path;

  /**
   * 
   */
  @JcrProperty(name = "lr:conversationlastActiveDate")
  private Date                        lastActiveDate;

  /**
   * 
   */
  @JcrProperty(name = "lr:conversationstartDate")
  private Date                        startDate;

  /**
   * 
   */
  @JcrChildNode
  private List<HistoricalMessageImpl> messageList;

  /**
   * 
   */
  public Conversation() {
  }

  /**
   * @param conversationId
   * @param room
   * @param lastActiveDate
   * @param startDate
   */
  public Conversation(String conversationId, Date lastActiveDate, Date startDate) {
    this.conversationId = conversationId;
    this.lastActiveDate = lastActiveDate;
    this.startDate = startDate;
    this.messageList = new ArrayList<HistoricalMessageImpl>();
  }

  /**
   * @param message the message 
   */
  public void addMessage(HistoricalMessageImpl message) {
    if(message == null) return;
    boolean hasExist = false;
    if(message.getDateSend() != null)
      for(HistoricalMessageImpl hisMessage: messageList)
      {
        if(message.getDateSend().equals(hisMessage.getDateSend()) && message.getFrom().equals(hisMessage.getFrom())){
          hasExist = true;
          break;
        }
      }
    if(!hasExist)
      messageList.add(message);
  }

  /**
   * @return the conversationId
   */
  public String getConversationId() {
    return conversationId;
  }

  /**
   * @param id the id of message
   * @return the message
   */
  public HistoricalMessageImpl getHistoricalMessage(String id) {
    for (HistoricalMessageImpl message : messageList) {
      if (message.getId().equals(id))
        return message;
    }
    return null;
  }

  /**
   * @return the lastActiveDate
   */
  public Date getLastActiveDate() {
    return lastActiveDate;
  }

  /**
   * @return the messageList
   */
  public List<HistoricalMessageImpl> getMessageList() {
    return messageList;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the startDate
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * @param conversationId the conversationId to set
   */
  public void setConversationId(String conversationId) {
    this.conversationId = conversationId;
  }

  /**
   * @param lastActiveDate the lastActiveDate to set
   */
  public void setLastActiveDate(Date lastActiveDate) {
    this.lastActiveDate = lastActiveDate;
  }

  /**
   * @param messageList the messageList to set
   */
  public void setMessageList(List<HistoricalMessageImpl> messageList) {
    this.messageList = messageList;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * @param startDate the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

}
