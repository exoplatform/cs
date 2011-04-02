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
package org.exoplatform.services.xmpp.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class EventsBean {

  /**
   * 
   */
  private String                      eventId;

  /**
   * 
   */
  private List<ContactBean>           roster;

  /**
   * 
   */
  private List<PresenceBean>          presences;

  /**
   * 
   */
  private List<PresenceBean>          subscriptions;

  /**
   * 
   */
  private List<MessageBean>           messages;

  /**
   * 
   */
  private List<MUCPacketBean>         mucEvents;

  /**
   * 
   */
  private List<FileTransferEventBean> fileEvents;

  /**
   * 
   */
  public EventsBean() {
    presences = new ArrayList<PresenceBean>();
    subscriptions = new ArrayList<PresenceBean>();
    messages = new ArrayList<MessageBean>();
    mucEvents = new ArrayList<MUCPacketBean>();
    fileEvents = new ArrayList<FileTransferEventBean>();
  }

  /**
   * @return fileEvents
   */
  public List<FileTransferEventBean> getFileEvents() {
    return fileEvents;
  }

  /**
   * @param fileEvents the fileEvent to set
   */
  public void setFileEvents(List<FileTransferEventBean> fileEvents) {
    this.fileEvents = fileEvents;
  }

  /**
   * @return the events id
   */
  public String getEventId() {
    return eventId;
  }

  /**
   * @return the roster
   */
  public List<ContactBean> getRoster() {
    return roster;
  }

  /**
   * @return the presences
   */
  public List<PresenceBean> getPresences() {
    return presences;
  }

  /**
   * @return the messages
   */
  public List<MessageBean> getMessages() {
    return messages;
  }

  /**
   * @return the mucEvents
   */
  public List<MUCPacketBean> getMucEvents() {
    return mucEvents;
  }

  /**
   * @return the subscriptions
   */
  public List<PresenceBean> getSubscriptions() {
    return subscriptions;
  }

  /**
   * @param eventId the eventsId to set
   */
  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  /**
   * @param roster the roster to set
   */
  public void setRoster(List<ContactBean> roster) {
    this.roster = roster;
  }

  /**
   * @param presences the presences to set
   */
  public void setPresences(List<PresenceBean> presences) {
    this.presences = presences;
  }

  /**
   * @param messages the messages to set
   */
  public void setMessages(List<MessageBean> messages) {
    this.messages = messages;
  }

  /**
   * @param mucEvents the mucEvents to set
   */
  public void setMucEvents(List<MUCPacketBean> mucEvents) {
    this.mucEvents = mucEvents;
  }

  /**
   * @param subscriptions the subscriptions to set
   */
  public void setSubscriptions(List<PresenceBean> subscriptions) {
    this.subscriptions = subscriptions;
  }

  /**
   * @param message the message
   */
  public void addMessage(MessageBean message) {
    this.messages.add(message);
  }

  /**
   * @param subscription the subscription 
   */
  public void addSubscription(PresenceBean subscription) {
    this.subscriptions.add(subscription);
  }

  /**
   * @param presence the presence
   */
  public void addPresence(PresenceBean presence) {
    this.presences.add(presence);
  }

  /**
   * @param muc the mucEvent
   */
  public void addMUCEvent(MUCPacketBean muc) {
    this.mucEvents.add(muc);
  }

  /**
   * @param bean the fileTransferEvent
   */
  public void addFileEvent(FileTransferEventBean bean) {
    this.fileEvents.add(bean);
  }

}
