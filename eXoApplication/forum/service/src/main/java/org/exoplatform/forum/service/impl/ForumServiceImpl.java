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
  
  public void removeCategory(String categoryId) throws Exception {
    storage_.removeCategory(categoryId) ;
  }
  
  public void updateCategory(Category category) throws Exception {
    storage_.updateCategory(category) ;    
  }
  
  public void createForum(String categoryId, Forum forum) throws Exception {
    // TODO Auto-generated method stub
  }
  public void createPost(String categoryId, String forumId, String topicId, Post post) throws Exception {
    // TODO Auto-generated method stub
  }
  public void createTopic(String categoryId, String forumId, Topic topic) throws Exception {
    // TODO Auto-generated method stub
  }
  
  public void moveForum(String srcCategoryId, String forumId, String destCategoryId)throws Exception {
    
  }
  public Forum getForum(String categoryId, String forumId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<Forum> getForums(String categoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public List<Post> getPosts(String categoryId, String forumId, String topicId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void movePost(String srcTopicId, String postId, String destTopicId) throws Exception {
    
  }
  
  public Topic getTopic(String categoryId, String forumId, String topicId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public PageList getTopics(String categoryId, String forumId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void moveTopic(String srcForumId, String topicId, String destForumId) throws Exception {
    
  }
  
  public void removeForum(String categoryId, String forumId) throws Exception {
    // TODO Auto-generated method stub
  }
  public void removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    // TODO Auto-generated method stub
  }
  public void removeTopic(String categoryId, String forumId, String topicId) throws Exception {
    // TODO Auto-generated method stub
  }
  
  public void updateForum(String categoryId, Forum newForum) throws Exception {
    // TODO Auto-generated method stub
  }
  public void updatePost(String categoryId, String forumId, String topicId, Post newPost) throws Exception {
    // TODO Auto-generated method stub
  }
  public void updateTopic(String categoryId, String forumId, Topic newTopic) throws Exception {
    // TODO Auto-generated method stub
  }
  
}
