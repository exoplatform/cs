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
  public Category createCategory(Category category) throws Exception {
    return storage_.createCategory(category);
  }
  
  public Category getCategory(String categoryId) throws Exception {
    return storage_.getCategory(categoryId);
  }
  
  public List<Category> getCategories() throws Exception {
    return storage_.getCategories();
  }
  
  public Category removeCategory(String categoryId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public Category updateCategory(Category category) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public Forum createForum(String categoryId, Forum forum) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Post createPost(String categoryId, String forumId, String topicId, Post post) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Topic createTopic(String categoryId, String forumId, Topic topic) throws Exception {
    // TODO Auto-generated method stub
    return null;
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
  
  public Forum removeForum(String categoryId, String forumId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public Forum updateForum(String categoryId, Forum newForum) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Post updatePost(String categoryId, String forumId, String topicId, Post newPost) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  public Topic updateTopic(String categoryId, String forumId, Topic newTopic) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  
}
