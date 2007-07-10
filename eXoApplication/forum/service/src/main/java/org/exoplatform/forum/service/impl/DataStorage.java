/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
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
   public Category createCategory(Category category)throws Exception;
   public Category removeCategory(String categoryId)throws Exception;  
   public Category updateCategory(Category category)throws Exception;  
   
   public List<Forum> getForums(String categoryId)throws Exception;
   public Forum getForum(String categoryId, String forumId)throws Exception;  
   public Forum createForum(String categoryId, Forum forum) throws Exception;
   public Forum updateForum(String categoryId, Forum newForum)throws Exception;
   public Forum removeForum(String categoryId, String forumId)throws Exception;   
   
   public PageList getTopics(String categoryId, String forumId) throws Exception;
   public Topic getTopic(String categoryId, String forumId, String topicId) throws Exception;    
   public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception;
   public Topic createTopic(String categoryId, String forumId, Topic topic) throws Exception;
   public Topic updateTopic(String categoryId, String forumId, Topic newTopic) throws Exception;  
   public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception;
   
   public List<Post> getPosts(String categoryId, String forumId, String topicId)throws Exception;
   public Post getPost(String categoryId, String forumId, String topicId, String postId)throws Exception;
   public Post createPost(String categoryId, String forumId, String topicId, Post post)throws Exception;
   public Post updatePost(String categoryId, String forumId, String topicId, Post newPost)throws Exception;
   public Post removePost(String categoryId, String forumId, String topicId, String postId)throws Exception;
}
