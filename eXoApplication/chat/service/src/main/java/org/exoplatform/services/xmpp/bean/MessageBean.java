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

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class MessageBean {

  /**
   * 
   */
  private String id;

  /**
   * 
   */
  private String from;

  /**
   * 
   */
  private String to;

  /**
   * 
   */
  private String type;

  /**
   * 
   */
  private String body;

  /**
   * 
   */
  private String dateSend;

  /**
   * 
   */
  public MessageBean() {

  }

  /**
   * @param id the id
   * @param from the from
   * @param to the to
   * @param type the type
   * @param body the body
   */
  public MessageBean(String id, String from, String to, String type, String body) {
    this.id = id;
    this.from = from;
    this.to = to;
    this.type = type;
    this.body = body;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
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
  public String getDateSend() {
    return dateSend;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
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
  public void setDateSend(String dateSend) {
    this.dateSend = dateSend;
  }

}
