/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.List;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface DataStorage {
  
   public List<Category> getCategories() throws Exception;
   public Category getCategory(String categoryId) throws Exception;
   public void saveCategory(Category category, boolean isNew)throws Exception;
   public Category removeCategory(String categoryId)throws Exception;  
   
   public List<Forum> getForums(String categoryId)throws Exception;
   public Forum getForum(String categoryId, String forumId)throws Exception;  
   public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception;
   public Forum removeForum(String categoryId, String forumId)throws Exception;
   public void moveForum(String forumId, String forumPath, String destCategoryPath) throws Exception;
   
   public JCRPageList getPageTopic(String categoryId, String forumId) throws Exception;
   public List<Topic> getTopics(String categoryId, String forumId) throws Exception;
   public Topic getTopic(String categoryId, String forumId, String topicId, boolean viewTopic) throws Exception;
   public Topic getTopicByPath(String topicPath) throws Exception;
   public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception;
   public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew) throws Exception;
   public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception;
   public void moveTopic(String topicId, String  topicPath, String destForumPath) throws Exception;
   
   public JCRPageList getPosts(String categoryId, String forumId, String topicId)throws Exception;
   public Post getPost(String categoryId, String forumId, String topicId, String postId)throws Exception;
   public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew)throws Exception;
   public Post removePost(String categoryId, String forumId, String topicId, String postId)throws Exception;
   public void movePost(String postId, String postPath, String destTopicPath) throws Exception ;

   public Poll getPoll(String categoryId, String forumId, String topicId)throws Exception;
   public void savePoll(String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote)throws Exception;
   public Poll removePoll(String categoryId, String forumId, String topicId)throws Exception;

   public Object getObjectByPath(String path) throws Exception ;
   public List getPage(long page, JCRPageList pageList) throws Exception ;
   public List<ForumLinkData> getAllLink() throws Exception ;
}
