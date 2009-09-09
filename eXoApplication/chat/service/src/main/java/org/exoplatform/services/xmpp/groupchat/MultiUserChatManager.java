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
package org.exoplatform.services.xmpp.groupchat;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class MultiUserChatManager {

  /**
   * Chat map.
   */
  private Map<String, MultiUserChat> chatMap = new HashMap<String, MultiUserChat>();


  /**
   * Add chat to map.
   * 
   * @param chat the chat
   */
  public void addMultiUserChat(MultiUserChat chat) {
    chatMap.put(chat.getRoom(), chat);
  }
  
  /**
   * Remove chat from map.
   * 
   * @param chat the chat
   */
  public void removeMultiUserChat(MultiUserChat chat) {
    if (chatMap.containsKey(chat.getRoom())) {
      chatMap.remove(chat.getRoom());
    }
  }

  /**
   * @param room the room name
   * @return the instance of MultiUserChat 
   */
  public MultiUserChat getMultiUserChat(String room) {
    if (chatMap.containsKey(room)) {
      return chatMap.get(room);
    }
    return null;
  }

  /**
   * @param chat the chat
   * @return true id successfully
   */
  public Boolean updateMultiUserChat(MultiUserChat chat) {
    if (chatMap.containsKey(chat.getRoom())) {
      chatMap.remove(chat.getRoom());
      chatMap.put(chat.getRoom(), chat);
      return true;
    }
    return false;
  }

  /**
   * @return all chats
   */
  public Collection<MultiUserChat> getAll() {
    return chatMap.values();
  }

}
