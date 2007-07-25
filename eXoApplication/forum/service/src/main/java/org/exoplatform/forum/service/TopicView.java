/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.util.List;

import sun.awt.RepaintArea;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jul 2, 2007  
 * Editer by Vu Duy Tu
 * 				tu.duy@exoplatform.com
 * July 20, 2007
 */
public class TopicView {
  private Topic topic ;
  private JCRPageList pagePosts ;
  
  public TopicView() {}
  
  public Topic getTopic() { return topic;  }
	public void setTopicView(Topic topic) {	this.topic = topic; }
	
	public JCRPageList getTopicView() { return pagePosts; }
	public void setPostsView(JCRPageList pagePosts) {this.pagePosts = pagePosts; }
	
}
