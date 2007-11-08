/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service ;

import java.util.Date;

import org.exoplatform.services.jcr.util.IdGenerator;
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
  private String lastTopicPath;
  private String name;
  private String description;
  private long postCount;
  private long topicCount;
  
  private String[] notifyWhenAddTopic ;
  private String[] notifyWhenAddPost ;
  private boolean isModerateTopic = false ;
  private boolean isModeratePost = false ;
  private boolean isClosed = false ;
  private boolean isLock = false ;

  private String[] viewForumRole;
  private String[] createTopicRole;
  private String[] moderators;
  
  private String[] replyTopicRole;
  
  
  public Forum() {
    id = "forum" + IdGenerator.generate() ;
  }
  
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
  
  public String getLastTopicPath(){return lastTopicPath;}
  public void setLastTopicPath(String lastTopicPath){this.lastTopicPath = lastTopicPath;}
  
  public String getForumName(){return name;}
  public void setForumName(String forumName){this.name = forumName;}
  
  public String getDescription(){return description;}
  public void setDescription(String description){this.description = description;}
  
  public long getPostCount(){return postCount;}
  public void setPostCount(long postCount){this.postCount = postCount;}
  
  public long getTopicCount(){return topicCount;}
  public void setTopicCount(long topicCount){this.topicCount = topicCount;}
  
  public String[] getNotifyWhenAddTopic() { return notifyWhenAddTopic;	}
  public void setNotifyWhenAddTopic(String[] notifyWhenAddTopic) {this.notifyWhenAddTopic = notifyWhenAddTopic;}
  
  public String[] getNotifyWhenAddPost() {return notifyWhenAddPost; }
  public void setNotifyWhenAddPost(String[] notifyWhenAddPost) { this.notifyWhenAddPost = notifyWhenAddPost;}
  
  public boolean getIsModerateTopic() { return isModerateTopic;}
  public void setIsModerateTopic(boolean isModerateTopic) { this.isModerateTopic = isModerateTopic;}
 
  public boolean getIsModeratePost() { return isModeratePost;}
  public void setIsModeratePost(boolean isModeratePost) { this.isModeratePost = isModeratePost;}
  
  public boolean getIsClosed() { return isClosed;}
  public void setIsClosed(boolean isClosed) { this.isClosed = isClosed;}
  
  public boolean getIsLock() { return isLock;}
  public void setIsLock(boolean isLock) { this.isLock = isLock;}
  
  public String[] getViewForumRole(){return viewForumRole;}
  public void setViewForumRole(String[] viewForumRole){this.viewForumRole = viewForumRole;}
  
  public String[] getCreateTopicRole(){return createTopicRole;}
  public void setCreateTopicRole(String[] createTopicRole){this.createTopicRole = createTopicRole;}
  
  public String[] getReplyTopicRole(){return replyTopicRole;}
  public void setReplyTopicRole(String[] replyTopicRole){this.replyTopicRole = replyTopicRole;}
  
  public String[] getModerators(){return moderators;}  
  public void setModerators(String[] moderators){this.moderators = moderators;}
}
