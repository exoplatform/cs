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
  private String id;
  private String owner;
  private String path ;
  private int forumOrder;
  private Date createdDate;
  private String modifiedBy;
  private Date modifiedDate;
  private String lastPostBy;
  private Date lastPostDate;
  private String name;
  private String description;
  private int postCount;
  private int topicCount;
  
  private boolean isNotifyWhenAddTopic = false ;
  private boolean isNotifyWhenAddPost = false ;
  private boolean isModerateTopic = false ;
  private boolean isModeratePost = false ;
  private boolean isClosed = false ;
  private boolean isLock = false ;
  

  private String[] viewForumRole;
  private String[] createTopicRole;
  private String[] moderators;
  private String[] replyTopicRole;
  
  
  public Forum() {}
  
  public String getId(){return id;}
  public void setId(String id){this.id = id;}
  
  /**
   * This method should:
   * Calculate the category id  base on the forum id
   * @return The category id
   */
  public String getCategoryId(){return null;}
  
  public String getOwner(){return owner;}
  public void setOwner(String owner){this.owner = owner;}
  
  public String getPath() {return path; }
  public void setPath( String path) { this.path = path;}
  
  public int getForumOrder(){return forumOrder;}
  public void setForumOrder(int forumOrder){this.forumOrder = forumOrder;}
  
  public Date getCreatedDate(){return createdDate;}
  public void setCreatedDate(Date createdDate){this.createdDate = createdDate;}
  
  public String getModifiedBy(){return modifiedBy;}
  public void setModifiedBy(String modifiedBy){this.modifiedBy = modifiedBy;}
  
  public Date getModifiedDate(){return modifiedDate;}
  public void setModifiedDate(Date modifiedDate){this.modifiedDate = modifiedDate;}
  
  public String getLastPostBy(){return lastPostBy;}
  public void setLastPostBy(String lastPostBy){this.lastPostBy = lastPostBy;}
  
  public Date getLastPostDate(){return lastPostDate;}
  public void setLastPostDate(Date lastPostDate){this.lastPostDate = lastPostDate;}
  
  public String getForumName(){return name;}
  public void setForumName(String forumName){this.name = forumName;}
  
  public String getDescription(){return description;}
  public void setDescription(String description){this.description = description;}
  
  public int getPostCount(){return postCount;}
  public void setPostCount(int postCount){this.postCount = postCount;}
  
  public int getTopicCount(){return topicCount;}
  public void setTopicCount(int topicCount){this.topicCount = topicCount;}
  
  public String[] getViewForumRole(){return viewForumRole;}
  public void setViewForumRole(String[] viewForumRole){this.viewForumRole = viewForumRole;}
  
  public String[] getCreateTopicRole(){return createTopicRole;}
  public void setCreateTopicRole(String[] createTopicRole){this.createTopicRole = createTopicRole;}
  
  public String[] getReplyTopicRole(){return replyTopicRole;}
  public void setReplyTopicRole(String[] replyTopicRole){this.replyTopicRole = replyTopicRole;}
  
  public String[] getModerators(){return moderators;}  
  public void setModerators(String[] moderators){this.moderators = moderators;}
}
