/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 * Edited by Vu Duy Tu
 *          tu.duy@exoplatform.com
 * July 16, 2007 
 */
public class JCRDataStorage implements DataStorage{
  private RepositoryService  repositoryService_ ; 
  private JCRRegistryService jcrRegistryService_ ;
  
  public JCRDataStorage(RepositoryService  repositoryService, 
                        JCRRegistryService jcrRegistryService)throws Exception {
    repositoryService_ = repositoryService ;
    jcrRegistryService_ = jcrRegistryService ;
  }
  
  public void createCategory(Category category) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    Node newCategory = forumHomeNode.addNode(category.getId(), "exo:forumCategory") ;
    GregorianCalendar calendar = new GregorianCalendar() ;
    newCategory.setProperty("exo:id", String.valueOf(calendar.getTimeInMillis())) ;
    newCategory.setProperty("exo:owner", category.getOwner()) ;
    newCategory.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
    newCategory.setProperty("exo:modifiedBy", category.getModifiedBy()) ;
    newCategory.setProperty("exo:modifiedDate", GregorianCalendar.getInstance()) ;
    newCategory.setProperty("exo:name", category.getCategoryName()) ;
    newCategory.setProperty("exo:description", category.getDescription()) ;
    newCategory.setProperty("exo:categoryOrder", category.getCategoryOrder()) ;
    forumHomeNode.save() ;
    forumHomeNode.getSession().save() ;    
  }
  
  public List<Category> getCategories() throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    NodeIterator iter = forumHomeNode.getNodes() ;
    List<Category> categories = new ArrayList<Category>() ;
    Category cat ;
    while(iter.hasNext()) {
      Node cateNode = iter.nextNode() ;
      cat = new Category() ;
      cat.setId(cateNode.getProperty("exo:id").getString()) ;
      cat.setOwner(cateNode.getProperty("exo:owner").getString()) ;
      cat.setCategoryName(cateNode.getProperty("exo:name").getString()) ;
      cat.setCategoryOrder(cateNode.getProperty("exo:categoryOrder").getLong()) ;
      cat.setCreatedDate(cateNode.getProperty("exo:createdDate").getDate().getTime()) ;
      cat.setDescription(cateNode.getProperty("exo:description").getString()) ;
      cat.setModifiedBy(cateNode.getProperty("exo:modifiedBy").getString()) ;
      cat.setModifiedDate(cateNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
      categories.add(cat) ;
    }
    return categories ;
  }
  
  public Category getCategory(String categoryId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    if(!forumHomeNode.hasNode(categoryId)) return null;
    Node catNode = forumHomeNode.getNode(categoryId) ;
    Category cat = new Category() ;
    cat.setId(categoryId) ;
    cat.setCategoryName(catNode.getProperty("exo:name").getString()) ;
    cat.setCategoryOrder(catNode.getProperty("exo:categoryOrder").getLong()) ;
    cat.setCreatedDate(catNode.getProperty("exo:createdDate").getDate().getTime()) ;
    cat.setDescription(catNode.getProperty("exo:description").getString()) ;
    cat.setModifiedBy(catNode.getProperty("exo:modifiedBy").getString()) ;
    cat.setModifiedDate(catNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
    cat.setOwner(catNode.getProperty("exo:owner").getString()) ;
    return cat ;
  }

  public void removeCategory(String categoryId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    if(forumHomeNode.hasNode(categoryId)){
      forumHomeNode.getNode(categoryId).remove() ;
    }
    forumHomeNode.save() ;
    forumHomeNode.getSession().save() ;
  }
  
  public void updateCategory(Category category) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    if(forumHomeNode.hasNode(category.getId())){
      Node catNode = forumHomeNode.getNode(category.getId()) ;
      catNode.setProperty("exo:name", category.getCategoryName()) ;
      catNode.setProperty("exo:categoryOrder", category.getCategoryOrder()) ;
      GregorianCalendar cal = new GregorianCalendar() ;
      cal.setTime(category.getCreatedDate()) ;
      catNode.setProperty("exo:createdDate", cal.getInstance()) ;
      catNode.setProperty("exo:description", category.getDescription()) ;
      catNode.setProperty("exo:modifiedBy", category.getModifiedBy()) ;
      cal.setTime(category.getModifiedDate()) ;
      catNode.setProperty("exo:modifiedDate", cal.getInstance()) ;
      catNode.setProperty("exo:owner", category.getOwner()) ;
    }
    forumHomeNode.save() ;
    forumHomeNode.getSession().save() ;
  }
  
  public void createForum(String categoryId, Forum forum) throws Exception {
	Node forumHomeNode = getForumHomeNode() ;
	if(forumHomeNode.hasNode(categoryId)) {
	  Node catNode = forumHomeNode.getNode(categoryId) ;
	  Node newForum = catNode.addNode(forum.getId(), "exo:forum");
	  GregorianCalendar calendar = new GregorianCalendar() ;
	  
	  newForum.setProperty("exo:id", String.valueOf(calendar.getTimeInMillis()));
	  newForum.setProperty("exo:owner", forum.getOwner());
	  newForum.setProperty("exo:name", forum.getForumName());
	  newForum.setProperty("exo:forumOrder", 0);
	  newForum.setProperty("exo:createdDate", GregorianCalendar.getInstance());
	  newForum.setProperty("exo:modifiedBy", forum.getModifiedBy());
	  newForum.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
	  newForum.setProperty("exo:lastPostBy", forum.getLastPostBy());
	  newForum.setProperty("exo:lastPostDate", GregorianCalendar.getInstance());
	  newForum.setProperty("exo:description", forum.getDescription());
	  newForum.setProperty("exo:postCount", 0);
	  newForum.setProperty("exo:topicCount", 0);
	  newForum.setProperty("exo:viewForumRole", new String[] {""});
	  newForum.setProperty("exo:createTopicRole", new String[] {""});
	  newForum.setProperty("exo:replyTopicRole", new String[] {""});
	  newForum.setProperty("exo:moderators", new String[] {""});
	  forumHomeNode.save() ;
	  forumHomeNode.getSession().save() ;
	}
  }
  
  private String [] ValuesToStrings (Value[] Val){
	String[] Str = new String[(int) Val.length];
	for(int i = 0; i < Val.length; ++i) {
	  Str[i] = Val[i].toString();
	}
	return Str;
  }
  
  public Forum getForum(String categoryId, String forumId) throws Exception {
    Node forumHomeNode = getForumHomeNode();
    if(forumHomeNode.hasNode(categoryId)) {
      Node catNode = forumHomeNode.getNode(categoryId) ;
      if(catNode.hasNode(forumId)) {
    	Node forumNode = catNode.getNode(forumId);
	    Forum forum = new Forum();
	    
	    forum.setId(forumId);
	    forum.setOwner(forumNode.getProperty("exo:owner").getString());
	    forum.setForumName(forumNode.getProperty("exo:name").getString());
	    forum.setForumOrder(forumNode.getProperty("exo:forumOrder").getType());
	    forum.setCreatedDate(forumNode.getProperty("exo:createdDate").getDate().getTime());
	    forum.setModifiedBy(forumNode.getProperty("exo:modifiedBy").getString());
	    forum.setModifiedDate(forumNode.getProperty("exo:modifiedDate").getDate().getTime());
	    forum.setLastPostBy(forumNode.getProperty("exo:lastPostBy").getString());
	    forum.setLastPostDate(forumNode.getProperty("exo:lastPostDate").getDate().getTime());
	    forum.setDescription(forumNode.getProperty("exo:description").getString());
	    forum.setPostCount(forumNode.getProperty("exo:postCount").getType());
	    forum.setTopicCount(forumNode.getProperty("exo:topicCount").getType());
	    
	    forum.setViewForumRole(ValuesToStrings(forumNode.getProperty("exo:viewForumRole").getValues()));
	    forum.setCreateTopicRole(ValuesToStrings(forumNode.getProperty("exo:createTopicRole").getValues()));
	    forum.setReplyTopicRole(ValuesToStrings(forumNode.getProperty("exo:replyTopicRole").getValues()));
	    forum.setModerators(ValuesToStrings(forumNode.getProperty("exo:moderators").getValues()));
	    
	    return forum;
      }
    }
    return null;
  }
  
  public List<Forum> getForums(String categoryId) throws Exception {
	Node forumHomeNode = getForumHomeNode();
	if(forumHomeNode.hasNode(categoryId)) {
	  Node catNode = forumHomeNode.getNode(categoryId) ;
	  NodeIterator iter = catNode.getNodes();
	  List<Forum> Forums = new ArrayList<Forum>();
	  Forum forum;
	  while (iter.hasNext()) {
		Node forumNode = iter.nextNode() ;
		forum = new Forum();
		
		forum.setId(forumNode.getProperty("exo:id").getString());
	    forum.setOwner(forumNode.getProperty("exo:owner").getString());
	    forum.setForumName(forumNode.getProperty("exo:name").getString());
	    forum.setForumOrder(forumNode.getProperty("exo:forumOrder").getType());
	    forum.setCreatedDate(forumNode.getProperty("exo:createdDate").getDate().getTime());
	    forum.setModifiedBy(forumNode.getProperty("exo:modifiedBy").getString());
	    forum.setModifiedDate(forumNode.getProperty("exo:modifiedDate").getDate().getTime());
	    forum.setLastPostBy(forumNode.getProperty("exo:lastPostBy").getString());
	    forum.setLastPostDate(forumNode.getProperty("exo:lastPostDate").getDate().getTime());
	    forum.setDescription(forumNode.getProperty("exo:description").getString());
	    forum.setPostCount(forumNode.getProperty("exo:postCount").getType());
	    forum.setTopicCount(forumNode.getProperty("exo:topicCount").getType());
	    forum.setViewForumRole(ValuesToStrings(forumNode.getProperty("exo:viewForumRole").getValues()));
	    forum.setCreateTopicRole(ValuesToStrings(forumNode.getProperty("exo:createTopicRole").getValues()));
	    forum.setReplyTopicRole(ValuesToStrings(forumNode.getProperty("exo:replyTopicRole").getValues()));
	    forum.setModerators(ValuesToStrings(forumNode.getProperty("exo:moderators").getValues()));
	    
	    Forums.add(forum);
	  }
	  return Forums;
	}
    return null;
  }
  
  private Forum getForum(Node forumNode) throws Exception {
	  return null;
  }
  
  public void removeForum(String categoryId, String forumId) throws Exception {
	Node forumHomeNode = getForumHomeNode();
	if(forumHomeNode.hasNode(categoryId)) {
	  Node catNode = forumHomeNode.getNode(categoryId) ;
	  if(catNode.hasNode(forumId)) {
		 catNode.getNode(forumId).remove();
	  }
	  forumHomeNode.save() ;
	  forumHomeNode.getSession().save() ;
	}
  }
  
  public void updateForum(String categoryId, Forum newForum) throws Exception {
	Node forumHomeNode = getForumHomeNode();
    if(forumHomeNode.hasNode(categoryId)) {
	  Node catNode = forumHomeNode.getNode(categoryId) ;
	  if(catNode.hasNode(newForum.getId())) {
		Node forumNode = catNode.getNode(newForum.getId());
		GregorianCalendar calendar = new GregorianCalendar() ;
		
		forumNode.setProperty("exo:name", newForum.getForumName());
		forumNode.setProperty("exo:forumOrder", newForum.getForumOrder());
		forumNode.setProperty("exo:modifiedBy", newForum.getModifiedBy());
		forumNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
		forumNode.setProperty("exo:description", newForum.getDescription());

		forumNode.setProperty("exo:viewForumRole", newForum.getViewForumRole());
		forumNode.setProperty("exo:createTopicRole", newForum.getCreateTopicRole());
		forumNode.setProperty("exo:replyTopicRole", newForum.getReplyTopicRole());
		forumNode.setProperty("exo:moderators", newForum.getModerators());
		  
	  }
	  forumHomeNode.save() ;
	  forumHomeNode.getSession().save() ; 
    }
  }
  
  public void moveForum(String srcCategoryId, String forumId, String destCategoryId) throws Exception {
	Node forumHomeNode = getForumHomeNode();
	if(!forumHomeNode.hasNode(srcCategoryId)||!forumHomeNode.hasNode(destCategoryId)) return;
    Node srcCateNode = forumHomeNode.getNode(srcCategoryId);
    Node destCateNode = forumHomeNode.getNode(destCategoryId);
    if(srcCateNode.hasNode(forumId)) {
      Node forumNode = srcCateNode.getNode(forumId);
      String destAbsPath = destCateNode.getPath() + "/" + forumNode.getName();
      forumHomeNode.getSession().getWorkspace().move(forumNode.getPath(), destAbsPath);
    }
    forumHomeNode.save() ;
	forumHomeNode.getSession().save() ;
  }
  
  public void createTopic(String categoryId, String forumId, Topic topic) throws Exception {
	Node forumHomeNode = getForumHomeNode();
	if(forumHomeNode.hasNode(categoryId)) {
	  Node CategoryNode = forumHomeNode.getNode(categoryId);
	  if(CategoryNode.hasNode(forumId)) {
		Node forumNode = CategoryNode.getNode(forumId);
		Node topicNode = forumNode.addNode(topic.getId(), "exo:topic");
		GregorianCalendar calendar = new GregorianCalendar() ;
		
		topicNode.setProperty("exo:id", String.valueOf(calendar.getTimeInMillis()));
		topicNode.setProperty("exo:owner", topic.getOwner());
		topicNode.setProperty("exo:name", topic.getTopicName());
	    topicNode.setProperty("exo:createdDate", GregorianCalendar.getInstance());
	    topicNode.setProperty("exo:modifiedBy", topic.getModifiedBy());
	    topicNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
	    topicNode.setProperty("exo:lastPostBy", topic.getLastPostBy());
	    topicNode.setProperty("exo:lastPostDate", GregorianCalendar.getInstance());
	    topicNode.setProperty("exo:description", topic.getDescription());
	    topicNode.setProperty("exo:postCount", 0);
	    
	    forumHomeNode.save() ;
	    forumHomeNode.getSession().save() ;
	  }
	}
  }
  
  public Topic getTopic(String categoryId, String forumId, String topicId) throws Exception {
    Node forumHomeNode = getForumHomeNode();
    if(forumHomeNode.hasNode(categoryId)) {
  	  Node CategoryNode = forumHomeNode.getNode(categoryId);
  	  if(CategoryNode.hasNode(forumId)) {
  		Node forumNode = CategoryNode.getNode(forumId);
  		if(forumNode.hasNode(topicId)) {
  		  Node topicNode = forumNode.getNode(topicId);
  		  Topic topicNew = new Topic();
  		  
  		  topicNew.setId(topicId);
  		  topicNew.setOwner(topicNode.getProperty("exo:owner").getString());
  		  topicNew.setTopicName(topicNode.getProperty("exo:name").getString());
  		  topicNew.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime());
  		  topicNew.setModifiedBy(topicNode.getProperty("exo:modifiedBy").getString());
  		  topicNew.setModifiedDate(topicNode.getProperty("exo:modifiedDate").getDate().getTime());
  		  topicNew.setLastPostBy(topicNode.getProperty("exo:lastPostBy").getString());
  		  topicNew.setLastPostDate(topicNode.getProperty("exo:lastPostDate").getDate().getTime());
  		  topicNew.setDescription(topicNode.getProperty("exo:description").getString());
  		  topicNew.setPostCount(topicNode.getProperty("exo:postCount").getType());
  		  
  		  return topicNew;
  		}
  	  }
    }
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
  
  public void removeTopic(String categoryId, String forumId, String topicId) throws Exception {
	  // TODO Auto-generated method stub
  }
  
  public void updateTopic(String categoryId, String forumId, Topic newTopic) throws Exception {
	  // TODO Auto-generated method stub
  } 
  
  public void moveTopic(String srcForumId, String topicId, String destForumId) throws Exception {
	  // TODO Auto-generated method stub
	  
  }
  
  public void createPost(String categoryId, String forumId, String topicId, Post post) throws Exception {
    // TODO Auto-generated method stub
  }
  
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public List<Post> getPosts(String categoryId, String forumId, String topicId) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }
  
  public void removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    // TODO Auto-generated method stub
  }
  
  public void updatePost(String categoryId, String forumId, String topicId, Post newPost) throws Exception {
    // TODO Auto-generated method stub
  }
  
  public void movePost(String srcTopicId, String postId, String destTopicId) throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  
  private Node getForumHomeNode() throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("ForumService") ;
    Session session = getJCRSession() ;
    jcrRegistryService_.createServiceRegistry(serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, serviceRegistry.getName()) ;
  }
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }

}
