/*
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
package org.exoplatform.mail.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class Message extends MessageHeader {
  private String path ;
  private String from ;
  private String to ;
  private String cc ;
  private String bcc ;
  private String body ;
  private String subject ;
  private String replyTo ;
  private Date sendDate ;
  private Date receivedDate ;
  private String contentType;
  private boolean isUnread = true ;
  private long size ;
  private boolean hasStar = false;
  private ServerConfiguration serverConfiguration;
  
  private String[] folders ;
  private String[] tags ;
  
  private Map<String, String> properties = new HashMap<String, String>() ;
  private List<Attachment> attachments ;
  
  public Message() {super() ;}
  
  public String getPath() {return path ; }
  public void setPath(String s) { path = s ; } 
  
  public String getMessageTo() { return to ; }
  public void setMessageTo(String s) { to = s ; }
  
  public String getMessageCc() { return cc ; }
  public void setMessageCc(String s) { cc = s ; }
  
  public String getMessageBcc() { return bcc ; }
  public void setMessageBcc(String s) { bcc = s ; }
  
  public String getSubject() { return subject ; }
  public void setSubject(String s) { subject = s ; }
  
  public String getMessageBody() { return body ; }
  public void   setMessageBody(String s) { body =  s ; }
  
  public void setUnread(boolean b) { isUnread = b ; }
  public boolean isUnread() { return isUnread ; }
  
  public Date getSendDate() { return sendDate ; }
  public void setSendDate(Date d) { sendDate = d ; }
  
  public Date getReceivedDate() { return receivedDate ; }
  public void setReceivedDate(Date d) { receivedDate = d ; }
  
  public String getContentType() { return contentType; }
  public void setContentType(String contentType) { this.contentType = contentType; }
  
  public String[] getFolders() { return folders ; }
  public void setFolders(String[] folders) { this.folders = folders ; }
  
  public String[] getTags() { return tags ; }
  public void setTags(String[] tags) { this.tags = tags ; }
  
  public List<Attachment> getAttachments() { return attachments ; }
  public void setAttachements(List<Attachment> attachments) { this.attachments = attachments ; }
  
  public Message cloneMessage() { return null ; }
  
  public String getFrom() { return from ; }
  public void setFrom(String from) { this.from = from ; } 
  
  public String getReplyTo() { return replyTo; }
  public void setReplyTo(String replyTo) { this.replyTo = replyTo; }
  
  public long getSize() { return size; }
  public void setSize(long size) { this.size = size; }
  
  public boolean hasStar() { return hasStar; }
  public void setHasStar(boolean star) { hasStar = star; }
  
  public ServerConfiguration getServerConfiguration() { return serverConfiguration ; }
  public void setServerConfiguration(ServerConfiguration s) { serverConfiguration  = s; }
  
  public void setProperties(String key, String value) {
    if (properties == null) properties = new HashMap<String, String>();
    properties.put(key, value) ;
  }
  public Map<String, String> getProperties() { return properties ; }
}
