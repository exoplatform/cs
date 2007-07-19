/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class ForumServiceImpl implements ForumService{
  private JCRDataStorage storage_ ;
  
  public ForumServiceImpl(RepositoryService  repositoryService, 
                          JCRRegistryService jcrRegistryService)throws Exception {
    storage_ = new JCRDataStorage(repositoryService, jcrRegistryService) ;
  }
  public void createCategory(Category category) throws Exception {
    storage_.createCategory(category);
  }
  
  public Category getCategory(String categoryId) throws Exception {
    return storage_.getCategory(categoryId);
  }
  
  public List<Category> getCategories() throws Exception {
    return storage_.getCategories();
  }
  
  public Category removeCategory(String categoryId) throws Exception {
    return storage_.removeCategory(categoryId) ;
  }
  
  public void updateCategory(Category category) throws Exception {
    storage_.updateCategory(category) ;    
  }
  
  public void createForum(String categoryId, Forum forum) throws Exception {
    storage_.createForum(categoryId, forum);
  }
  
  public void moveForum(String forumPath, String destCategoryPath) throws Exception {
    storage_.moveForum(forumPath, destCategoryPath);
  }
  
  public Forum getForum(String categoryId, String forumId) throws Exception {
    return storage_.getForum(categoryId, forumId);
  }
  
  public List<Forum> getForums(String categoryId) throws Exception {
    return storage_.getForums(categoryId);
  }
  
  public void updateForum(String categoryId, Forum newForum) throws Exception {
	storage_.updateForum(categoryId, newForum);
  }
  
  public Forum removeForum(String categoryId, String forumId) throws Exception {
	return storage_.removeForum(categoryId, forumId);
  }
  
  public void createTopic(String categoryId, String forumId, Topic topic) throws Exception {
	 storage_.createTopic(categoryId, forumId, topic);
  }
  
  public Topic getTopic(String categoryId, String forumId, String topicId) throws Exception {
	return storage_.getTopic(categoryId, forumId, topicId);
  }
  
  public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception {
	  // TODO Auto-generated method stub
	  return null;
  }
  
  public PageList getTopics(String categoryId, String forumId) throws Exception {
	  // TODO Auto-generated method stub
	  return null;
  }
  
  public void moveTopic(String  topicPath, String destForumPath) throws Exception {
	storage_.moveTopic(topicPath, destForumPath);
  }
  
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
	return storage_.removeTopic(categoryId, forumId, topicId);
  }
  
  public void updateTopic(String categoryId, String forumId, Topic newTopic) throws Exception {
	storage_.updateTopic(categoryId, forumId, newTopic);
  }

  public void createPost(String categoryId, String forumId, String topicId, Post post) throws Exception {
	storage_.createPost(categoryId, forumId, topicId, post);
  }
  
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.getPost(categoryId, forumId, topicId, postId);
  }
  
  public List<Post> getPosts(String categoryId, String forumId, String topicId) throws Exception {
    return storage_.getPosts(categoryId, forumId, topicId);
  }
  
  public void movePost(String postPath, String destTopicPaths) throws Exception {
    storage_.movePost(postPath, destTopicPaths);
  }
  
  public Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.removePost(categoryId, forumId, topicId, postId);
  }
  
  public void updatePost(String categoryId, String forumId, String topicId, Post newPost) throws Exception {
    storage_.updatePost(categoryId, forumId, topicId, newPost);
  }
  
}
