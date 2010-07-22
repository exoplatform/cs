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

import java.util.Collection;
import java.util.List;

import org.exoplatform.services.xmpp.history.HistoricalMessage;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class InitInfoBean {

  /**
   * 
   */
  private String                  mainServiceName;
  
  /**
   * 
   */
  private Integer                 totalRooms;
  
  /**
   * 
   */
  private Integer                 totalJoinedRooms;

  /**
   * 
   */
  private Collection<String>      mucServicesNames;

  /**
   * 
   */
  private List<String>            searchServicesNames;

  /**
   * 
   */
  private FormBean                form;

  /**
   * 
   */
  private List<ContactBean>       roster;
  
  /**
  *
  */
  private ContactBean       myProfile;

  /**
   * 
   */
  private List<HostedRoomBean>    hostedRooms;

  /**
   * 
   */
  private List<HistoricalMessage> messages;

  /**
   * 
   */
  private List<FullRoomInfoBean>  joinedRooms;
  
  /**
   * @return the mainServiceName
   */
  public String getMainServiceName() {
    return mainServiceName;
  }

  /**
   * @return the mucServiceName
   */
  public Collection<String> getMucServicesNames() {
    return mucServicesNames;
  }

  /**
   * @return the searchServicesNames
   */
  public List<String> getSearchServicesNames() {
    return searchServicesNames;
  }

  /**
   * @return the form
   */
  public FormBean getForm() {
    return form;
  }

  /**
   * @return the roster
   */
  public List<ContactBean> getRoster() {
    return roster;
  }

  /**
   * @return the hostedRoom
   */
  public List<HostedRoomBean> getHostedRooms() {
    return hostedRooms;
  }

  /**
   * @return the messages
   */
  public List<HistoricalMessage> getMessages() {
    return messages;
  }

  /**
   * @param mainServiceName the mainServiceName to set
   */
  public void setMainServiceName(String mainServiceName) {
    this.mainServiceName = mainServiceName;
  }

  /**
   * @param mucServicesNames the nemes of group chat services
   */
  public void setMucServicesNames(Collection<String> mucServicesNames) {
    this.mucServicesNames = mucServicesNames;
  }

  /**
   * @param searchServicesNames the searchServicesNames to set
   */
  public void setSearchServicesNames(List<String> searchServicesNames) {
    this.searchServicesNames = searchServicesNames;
  }

  /**
   * @param form the form to set
   */
  public void setForm(FormBean form) {
    this.form = form;
  }

  /**
   * @param roster the roster to set
   */
  public void setRoster(List<ContactBean> roster) {
    this.roster = roster;
  }
  
  public ContactBean getMyProfile() {
    return myProfile;
  }

  public void setMyProfile(ContactBean myProfile) {
    this.myProfile = myProfile;
  }

  /**
   * @param hostedRooms the hostedRooms to set
   */
  public void setHostedRooms(List<HostedRoomBean> hostedRooms) {
    this.hostedRooms = hostedRooms;
  }

  /**
   * @param messages the messages to set
   */
  public void setMessages(List<HistoricalMessage> messages) {
    this.messages = messages;
  }

  /**
   * @return the joined rooms
   */
  public List<FullRoomInfoBean> getJoinedRooms() {
    return joinedRooms;
  }

  /**
   * @param joinedRooms the joinedRooms to set
   */
  public void setJoinedRooms(List<FullRoomInfoBean> joinedRooms) {
    this.joinedRooms = joinedRooms;
  }

  /**
   * @return the totalRooms
   */
  public Integer getTotalRooms() {
    return totalRooms;
  }

  /**
   * @param totalRooms the totalRooms to set
   */
  public void setTotalRooms(Integer totalRooms) {
    this.totalRooms = totalRooms;
  }

  /**
   * @return the totalJoinedRooms
   */
  public Integer getTotalJoinedRooms() {
    return totalJoinedRooms;
  }

  /**
   * @param totalJoinedRooms the totalJoinedRooms to set
   */
  public void setTotalJoinedRooms(Integer totalJoinedRooms) {
    this.totalJoinedRooms = totalJoinedRooms;
  }

}
