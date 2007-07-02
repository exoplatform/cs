/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service ;

import java.util.Date;
/**
 * March 2, 2007  
 */
public class Forum {
  private String id_;
  private String owner_;
  private int forumOrder_;
  private Date createdDate_;
  private String modifiedBy_;
  private Date modifiedDate_;
  private String lastPostBy_;
  private Date lastPostDate_;
  private String name_;
  private String description_;
  private int postCount_;
  private int topicCount_;

  private String[] viewForumRole_;
  private String[] createTopicRole_;
  private String[] replyTopicRole_;
  private String[] moderators_;
  
  public Forum() {}
  
  public void setId(String id){id_ = id;}
  public String getId(){return id_;}
  
  /**
   * This method should:
   * Calculate the category id  base on the forum id
   * @return The category id
   */
  public String getCategoryId(){return null;}
  
  public void setOwner(String owner){owner_ = owner;}
  public String getOwner(){return owner_;}
  
  public void setForumOrder(int forumOrder){forumOrder_ = forumOrder;}
  public int getForumOrder(){return forumOrder_;}
  
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
  
  public void setForumName(String forumName){name_ = forumName;}
  public String getForumName(){return name_;}
  
  public void setDescription(String description){description_ = description;}
  public String getDescription(){return description_;}
  
  public void setPostCount(int postCount){postCount_ = postCount;}
  public int getPostCount(){return postCount_;}
  
  public void setTopicCount(int topicCount){topicCount_ = topicCount;}
  public int getTopicCount(){return topicCount_;}
  
  public void setViewForumRole(String[] viewForumRole){ viewForumRole_ = viewForumRole;}
  public String[] getViewForumRole(){return viewForumRole_;}
  
  public void setCreateTopicRole(String[] createTopicRole){createTopicRole_ = createTopicRole;}
  public String[] getCreateTopicRole(){return createTopicRole_;}
  
  public void setReplyTopicRole(String[] replyTopicRole){replyTopicRole_ = replyTopicRole;}
  public String[] getReplyTopicRole(){return replyTopicRole_;}
  
  public void setModerators(String[] moderators){moderators_ = moderators;}
  public String[] getModerators(){return moderators_;}  
}
