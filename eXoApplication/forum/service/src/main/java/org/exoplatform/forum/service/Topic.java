/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service ;

import java.util.Date;

/**
 * Created by The eXo Platform SARL
 * March 2, 2007  
 */
public class Topic { 
  private String id_;
  private String owner_;
  private Date createdDate_;
  private String modifiedBy_;
  private Date modifiedDate_;
  private String lastPostBy_;
  private Date lastPostDate_;
  private String topic_;
  private String description_;
  private int postCount_;  
  
  public Topic(){ }
  
  public void setOwner(String owner){owner_ = owner;}
  public String getOwner(){return owner_;} 
  
  public void setCreatedDate(Date createdDate){createdDate_ = createdDate;}
  public Date getCreatedDate(){return createdDate_;}
  
  public void setModifiedBy(String modifiedBy){modifiedBy_ = modifiedBy;}
  public String getModifiedBy(){return modifiedBy_;}
    
  public void setModifiedDate(Date modifiedDate){modifiedDate_ = modifiedDate;}
  public Date getModifiedDate(){return modifiedDate_;}
  
  public void setLastPostBy(String lastPostBy){lastPostBy_ = lastPostBy;}
  public String getLastPostBy(){return lastPostBy_;}
    
  public void setLastPostDate(Date lastPostDate){lastPostDate_ = lastPostDate;}
  public Date getLastPostDate(){return lastPostDate_;}
  
  public void setTopic(String topic){topic_ = topic;}
  public String getTopic(){return topic_;}
  
  public void setDescription(String description){description_ = description;}
  public String getDescription(){return description_;}
  
  public void setPostCount(int postCount){postCount_ = postCount;}
  public int getPostCount(){return postCount_;}

  public String getId() { return id_; }
  public void setId(String id) { this.id_ = id; }

  /**
   * This method should calculate the forum id base on the topic id
   * @return
   */
  public String getForumId() { return null ; }

}
