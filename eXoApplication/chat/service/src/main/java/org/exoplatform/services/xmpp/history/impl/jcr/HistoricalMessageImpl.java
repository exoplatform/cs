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

import java.util.Date;

import org.exoplatform.services.xmpp.history.HistoricalMessage;
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
@JcrNode(nodeType = "lr:historicalmessage")
public class HistoricalMessageImpl implements HistoricalMessage {

  /**
   * 
   */
  @JcrName
  private String  id;

  /**
   * 
   */
  @JcrPath
  private String  path;

  /**
   * 
   */
  @JcrProperty(name = "lr:messagefrom")
  private String  from;

  /**
   * 
   */
  @JcrProperty(name = "lr:messageto")
  private String  to;

  /**
   * 
   */
  @JcrProperty(name = "lr:messagetype")
  private String  type;

  /**
   * 
   */
  @JcrProperty(name = "lr:messagebody")
  private String  body;

  /**
   * 
   */
  @JcrProperty(name = "lr:messagedateSend")
  private Date    dateSend;

  /**
   * 
   */
  @JcrProperty(name = "lr:messagereceive")
  private Boolean receive;

  /**
   * 
   */
  public HistoricalMessageImpl() {
  
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * @return the from
   */
  public String getFrom() {
    return from;
  }

  /**
   * @return the to
   */
  public String getTo() {
    return to;
  }

  /**
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * @return the body
   */
  public String getBody() {
    return body;
  }

  /**
   * @return the dateSend
   */
  public Date getDateSend() {
    return dateSend;
  }

  /**
   * @return the receive
   */
  public Boolean getReceive() {
    return receive;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @param path the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * @param from the from to set
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * @param to the to to set
   */
  public void setTo(String to) {
    this.to = to;
  }

  /**
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * @param body the body to set
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * @param dateSend the dateSend to set
   */
  public void setDateSend(Date dateSend) {
    this.dateSend = dateSend;
  }

  /**
   * @param recieve the receive to set
   */
  public void setReceive(Boolean receive) {
    this.receive = receive;
  }

}
