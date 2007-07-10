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
  private String id_;
  private String owner_;
  private Date createdDate_;
  private String modifiedBy_;
  private Date modifiedDate_;
  private String subject_;
  private String message_;
  private String remoteAddr_;
  private int    attachments_ ;
  
  public Post(){}
  
  public String getId() { return id_; }
  public void setId(String id_) { this.id_ = id_; }
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

  public void setOwner(String owner){owner_ = owner;}
  public String getOwner(){return owner_;}
  
  public void setCreatedDate(Date createdDate){createdDate_ = createdDate;}
  public Date getCreatedDate(){return createdDate_;}
  
  public void setModifiedBy(String modifiedBy){modifiedBy_ = modifiedBy;}
  public String getModifiedBy(){return modifiedBy_;}
  
  public void setModifiedDate(Date modifiedDate){modifiedDate_ = modifiedDate;}
  public Date getModifiedDate(){return modifiedDate_;}
  
  public void setSubject(String subject){subject_ = subject;}
  public String getSubject(){return subject_;}
  
  public void setMessage(String message){message_ = message;}
  public String getMessage(){return message_;}
  
  public void setRemoteAddr(String remoteAddr){remoteAddr_ = remoteAddr;}
  public String getRemoteAddr(){return remoteAddr_;}
  
  public int  getNumberOfAttachment() { return attachments_ ; }
  public void setNumberOfAttachment(int number) { attachments_ = number ;}
}
