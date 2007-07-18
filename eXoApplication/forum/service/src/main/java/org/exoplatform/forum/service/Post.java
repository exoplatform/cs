/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service ;

import java.util.Date;
/**
 * Created by The eXo Platform SARL
 * Author : Lai Van Khoi
 *          laivankhoi46pm1@yahoo.com
 * Edited by Nguyen Van Duc
 *           ducnv82@gmail.com
 * March 2, 2007  
 */
public class Post { 
  private String id;
  private String owner;
  private Date createdDate;
  private String modifiedBy;
  private Date modifiedDate;
  private String subject;
  private String message;
  private String remoteAddr;
  private int    attachments ;
  
  public Post(){}
  
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  /**
   * This method should calculate the id of the topic base on the id of the post
   * @return
   */
  public String getTopicId() { return null; }
  /**
   * This method should calculate the id of the forum base on the id of the post
   * @return
   */
  public String getForumId() { return null ;}

  public String getOwner(){return owner;}
  public void setOwner(String owner){this.owner = owner;}
  
  public Date getCreatedDate(){return createdDate;}
  public void setCreatedDate(Date createdDate){this.createdDate = createdDate;}
  
  public String getModifiedBy(){return modifiedBy;}
  public void setModifiedBy(String modifiedBy){this.modifiedBy = modifiedBy;}
  
  public Date getModifiedDate(){return modifiedDate;}
  public void setModifiedDate(Date modifiedDate){this.modifiedDate = modifiedDate;}
  
  public String getSubject(){return subject;}
  public void setSubject(String subject){this.subject = subject;}
  
  public String getMessage(){return message;}
  public void setMessage(String message){this.message = message;}
  
  public String getRemoteAddr(){return remoteAddr;}
  public void setRemoteAddr(String remoteAddr){this.remoteAddr = remoteAddr;}
  
  public int  getNumberOfAttachment() { return attachments ; }
  public void setNumberOfAttachment(int number) { this.attachments = number ;}
}
