/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.JCRPageList;
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
   public void createCategory(Category category)throws Exception;
   public void updateCategory(Category category)throws Exception;  
   public Category removeCategory(String categoryId)throws Exception;  
   
   public List<Forum> getForums(String categoryId)throws Exception;
   public Forum getForum(String categoryId, String forumId)throws Exception;  
   public void createForum(String categoryId, Forum forum) throws Exception;
   public void updateForum(String categoryId, Forum newForum)throws Exception;
   public Forum removeForum(String categoryId, String forumId)throws Exception;
   public void moveForum(String forumPath, String destCategoryPath) throws Exception;
   
   public JCRPageList getTopics(String categoryId, String forumId) throws Exception;
   public Topic getTopic(String categoryId, String forumId, String topicId) throws Exception;    
   public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception;
   public void createTopic(String categoryId, String forumId, Topic topic) throws Exception;
   public void updateTopic(String categoryId, String forumId, Topic newTopic) throws Exception;  
   public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception;
   public void moveTopic(String  topicPath, String destForumPath) throws Exception;
   
   public List<Post> getPosts(String categoryId, String forumId, String topicId)throws Exception;
   public Post getPost(String categoryId, String forumId, String topicId, String postId)throws Exception;
   public void createPost(String categoryId, String forumId, String topicId, Post post)throws Exception;
   public void updatePost(String categoryId, String forumId, String topicId, Post newPost)throws Exception;
   public Post removePost(String categoryId, String forumId, String topicId, String postId)throws Exception;
   public void movePost(String postPath, String destTopicPaths) throws Exception ;
}
