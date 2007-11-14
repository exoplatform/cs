/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service ;

import java.util.Date;
import java.util.List;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * March 2, 2007  
 */
public class Topic { 
  private String id;
  private String owner;
  private String path ;
  private Date createdDate;
  private String modifiedBy;
  private Date modifiedDate;
  private String lastPostBy;
  private Date lastPostDate;
  private String name;
  private String description;
  private long postCount = 0;  
	private long viewCount = 0;
	private String icon ;
  private long attachmentFirstPost = 0 ;
	
  private boolean isModeratePost = false ;
  private boolean isNotifyWhenAddPost = false ;  
  private boolean isClosed = false ;
  private boolean isLock = false ;
  private boolean isApproved = false ;
  private boolean isSticky = false ;
  private boolean isPoll = false ;
  
  private String[] canView;
  private String[] canPost;
  private String[] userVoteRating ;
  private Double voteRating = 0.0;
  private List<ForumAttachment> attachments ;
  
  public Topic(){ 
    id = "topic" + IdGenerator.generate();
    canView = new String [] {} ;
    canPost = new String [] {} ;
    userVoteRating = new String [] {} ;
  }
  
  public String getOwner(){return owner;} 
  public void setOwner(String owner){this.owner = owner;}
  
  public String getPath() {return path; }
  public void setPath( String path) { this.path = path;}
  
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
  
  public String getTopicName(){return name;}
  public void setTopicName(String topic){this.name = topic;}
  
  public String getDescription(){return description;}
  public void setDescription(String description){this.description = description;}
  
  public long getPostCount(){return postCount;}
  public void setPostCount(long postCount){this.postCount = postCount;}

  public long getViewCount(){return viewCount;}
  public void setViewCount(long viewCount){this.viewCount = viewCount;}

  public boolean getIsModeratePost() { return isModeratePost;}
  public void setIsModeratePost(boolean isModeratePost) { this.isModeratePost = isModeratePost;}

  public boolean getIsNotifyWhenAddPost() { return isNotifyWhenAddPost;}
  public void setIsNotifyWhenAddPost(boolean isNotifyWhenAddPost) { this.isNotifyWhenAddPost = isNotifyWhenAddPost;}
  
  public boolean getIsClosed() { return isClosed;}
  public void setIsClosed(boolean isClosed) { this.isClosed = isClosed;}
  
  public boolean getIsLock() { return isLock;}
  public void setIsLock(boolean isLock) { this.isLock = isLock;}
  
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  
  public String getIcon() { return icon;}
  public void setIcon( String icon) { this.icon = icon;}

  public long getAttachmentFirstPost() { return attachmentFirstPost;}
  public void setAttachmentFirstPost( long attachmentFirstPost) { this.attachmentFirstPost = attachmentFirstPost;}
  
  public boolean getIsApproved() { return isApproved;}
  public void setIsApproved(boolean isApproved) { this.isApproved = isApproved;}

  public boolean getIsSticky() { return isSticky;}
  public void setIsSticky(boolean isSticky) { this.isSticky = isSticky;}

  public String[] getCanView(){return canView;}
  public void setCanView(String[] canView){this.canView = canView;}
  
  public String[] getCanPost(){return canPost;}
  public void setCanPost(String[] canPost){this.canPost = canPost;}

  public boolean getIsPoll(){return isPoll;}
  public void setIsPoll(boolean isPoll){this.isPoll = isPoll;}
  
  public String[] getUserVoteRating(){return userVoteRating;}
  public void setUserVoteRating(String[] userVoteRating){this.userVoteRating = userVoteRating;}

  public Double getVoteRating(){return voteRating;}
  public void setVoteRating(Double voteRating){this.voteRating = voteRating;}
  
  public void setAttachments( List<ForumAttachment> attachments) { this.attachments = attachments;}
  public List<ForumAttachment> getAttachments() { return this.attachments;}
  
  public String getForumId() { return null ; }

}
