/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Session;

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
	
	public JCRPageList getPageList() { return pagePosts; }
	public void setPageList(JCRPageList pagePosts) {this.pagePosts = pagePosts; }
	
//	public List<Post> getAllPost(Session session) throws Exception {
//	  JCRPageList  pageList = this.pagePosts;
//	  List<Post> posts = new ArrayList<Post>();
//	  int t = 1, j = 0;
//	  long k = pageList.getPageSize();
//	  for (int i = 0; i < pageList.getAvailable(); i++) {
//	  	if(k*t <= i){ ++t; j = 0;}
//	  	posts.add((Post)pageList.getPage(t, session).get(j));
//	  	++j;
//		}
//		return posts;
//	}
	
}
