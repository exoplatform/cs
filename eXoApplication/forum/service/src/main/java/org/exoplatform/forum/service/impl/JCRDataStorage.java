/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.commons.utils.ObjectPageList;
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
public class JCRDataStorage implements DataStorage {
  private RepositoryService  repositoryService_ ; 
  private JCRRegistryService jcrRegistryService_ ;
  
  public JCRDataStorage(RepositoryService  repositoryService, 
                        JCRRegistryService jcrRegistryService)throws Exception {
    repositoryService_ = repositoryService ;
    jcrRegistryService_ = jcrRegistryService ;
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
      cat.setPath(cateNode.getProperty("exo:path").getString()) ;
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
    cat.setOwner(catNode.getProperty("exo:owner").getString()) ;
    cat.setPath(catNode.getProperty("exo:path").getString()) ;
    cat.setCategoryName(catNode.getProperty("exo:name").getString()) ;
    cat.setCategoryOrder(catNode.getProperty("exo:categoryOrder").getLong()) ;
    cat.setCreatedDate(catNode.getProperty("exo:createdDate").getDate().getTime()) ;
    cat.setDescription(catNode.getProperty("exo:description").getString()) ;
    cat.setModifiedBy(catNode.getProperty("exo:modifiedBy").getString()) ;
    cat.setModifiedDate(catNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
    return cat ;
  }

  public void createCategory(Category category) throws Exception {
  	Node forumHomeNode = getForumHomeNode() ;
  	Node newCategory = forumHomeNode.addNode(category.getId(), "exo:forumCategory") ;
  	newCategory.setProperty("exo:id", category.getId()) ;
  	newCategory.setProperty("exo:owner", category.getOwner()) ;
  	newCategory.setProperty("exo:path", newCategory.getPath()) ;
  	newCategory.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
  	newCategory.setProperty("exo:modifiedBy", category.getModifiedBy()) ;
  	newCategory.setProperty("exo:modifiedDate", GregorianCalendar.getInstance()) ;
  	newCategory.setProperty("exo:name", category.getCategoryName()) ;
  	newCategory.setProperty("exo:description", category.getDescription()) ;
  	newCategory.setProperty("exo:categoryOrder", category.getCategoryOrder()) ;
  	
  	forumHomeNode.save() ;
  	forumHomeNode.getSession().save() ;    
  }
  
  public void updateCategory(Category category) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    if(forumHomeNode.hasNode(category.getId())){
      Node catNode = forumHomeNode.getNode(category.getId()) ;
      catNode.setProperty("exo:name", category.getCategoryName()) ;
      catNode.setProperty("exo:categoryOrder", category.getCategoryOrder()) ;
      catNode.setProperty("exo:description", category.getDescription()) ;
      catNode.setProperty("exo:modifiedBy", category.getModifiedBy()) ;
      catNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance()) ;
    }
    forumHomeNode.save() ;
    forumHomeNode.getSession().save() ;
  }
  
  public Category removeCategory(String categoryId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    Category category = new Category ();
    if(forumHomeNode.hasNode(categoryId)){
      category = getCategory(categoryId);
      forumHomeNode.getNode(categoryId).remove() ;
      forumHomeNode.save() ;
      forumHomeNode.getSession().save() ;
      return category;
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
				forum = getForum(forumNode);
		    Forums.add(forum);
		  }
		  return Forums;
		}
    return null;
  }

  public Forum getForum(String categoryId, String forumId) throws Exception {
    Node forumHomeNode = getForumHomeNode();
    if(forumHomeNode.hasNode(categoryId)) {
      Node catNode = forumHomeNode.getNode(categoryId) ;
      Node forumNode = catNode.getNode(forumId);
      Forum forum = new Forum();
      forum = getForum(forumNode);
      return forum;
    }
    return null;
  }

  public void createForum(String categoryId, Forum forum) throws Exception {
		Node forumHomeNode = getForumHomeNode() ;
		if(forumHomeNode.hasNode(categoryId)) {
		  Node catNode = forumHomeNode.getNode(categoryId) ;
		  Node newForum = catNode.addNode(forum.getId(), "exo:forum");
		  
		  newForum.setProperty("exo:id", forum.getId());
		  newForum.setProperty("exo:owner", forum.getOwner());
		  newForum.setProperty("exo:path", newForum.getPath());
		  newForum.setProperty("exo:name", forum.getForumName());
		  newForum.setProperty("exo:forumOrder", forum.getForumOrder());
		  newForum.setProperty("exo:createdDate", GregorianCalendar.getInstance());
		  newForum.setProperty("exo:modifiedBy", forum.getModifiedBy());
		  newForum.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
		  newForum.setProperty("exo:lastPostBy", forum.getLastPostBy());
		  newForum.setProperty("exo:lastPostDate", GregorianCalendar.getInstance());
		  newForum.setProperty("exo:description", forum.getDescription());
		  newForum.setProperty("exo:postCount", 0);
		  newForum.setProperty("exo:topicCount", 0);
		  
		  newForum.setProperty("exo:isNotifyWhenAddTopic", forum.getIsNotifyWhenAddTopic());
		  newForum.setProperty("exo:isNotifyWhenAddPost", forum.getIsNotifyWhenAddPost());
		  newForum.setProperty("exo:isModerateTopic", forum.getIsModerateTopic());
		  newForum.setProperty("exo:isModeratePost", forum.getIsModeratePost());
		  newForum.setProperty("exo:isClosed", forum.getIsClosed());
		  newForum.setProperty("exo:isLock", forum.getIsLock());
		  
		  newForum.setProperty("exo:viewForumRole", forum.getViewForumRole());
		  newForum.setProperty("exo:createTopicRole", forum.getCreateTopicRole());
		  newForum.setProperty("exo:replyTopicRole", forum.getReplyTopicRole());
		  newForum.setProperty("exo:moderators", forum.getModerators());
		  
		  forumHomeNode.save() ;
		  forumHomeNode.getSession().save() ;
		}
  }
  
  private Forum getForum(Node forumNode) throws Exception {
		Forum forum = new Forum();
		forum.setId(forumNode.getProperty("exo:id").getString());
    forum.setOwner(forumNode.getProperty("exo:owner").getString());
    forum.setPath(forumNode.getProperty("exo:path").getString());
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

    forum.setIsNotifyWhenAddTopic(forumNode.getProperty("exo:isNotifyWhenAddTopic").getBoolean());
    forum.setIsNotifyWhenAddPost(forumNode.getProperty("exo:isNotifyWhenAddPost").getBoolean());
    forum.setIsModerateTopic(forumNode.getProperty("exo:isModerateTopic").getBoolean());
    forum.setIsModeratePost(forumNode.getProperty("exo:isModeratePost").getBoolean());
    forum.setIsClosed(forumNode.getProperty("exo:isClosed").getBoolean());
    forum.setIsLock(forumNode.getProperty("exo:isLock").getBoolean());
    
    forum.setViewForumRole(ValuesToStrings(forumNode.getProperty("exo:viewForumRole").getValues()));
    forum.setCreateTopicRole(ValuesToStrings(forumNode.getProperty("exo:createTopicRole").getValues()));
    forum.setReplyTopicRole(ValuesToStrings(forumNode.getProperty("exo:replyTopicRole").getValues()));
    forum.setModerators(ValuesToStrings(forumNode.getProperty("exo:moderators").getValues()));
    return forum;
  }

  public void updateForum(String categoryId, Forum newForum) throws Exception {
  	Node forumHomeNode = getForumHomeNode();
    if(forumHomeNode.hasNode(categoryId)) {
		  Node catNode = forumHomeNode.getNode(categoryId) ;
		  if(catNode.hasNode(newForum.getId())) {
				Node forumNode = catNode.getNode(newForum.getId());
				
				forumNode.setProperty("exo:name", newForum.getForumName());
				forumNode.setProperty("exo:forumOrder", newForum.getForumOrder());
				forumNode.setProperty("exo:modifiedBy", newForum.getModifiedBy());
				forumNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
				forumNode.setProperty("exo:description", newForum.getDescription());
				
				forumNode.setProperty("exo:isNotifyWhenAddTopic", newForum.getIsNotifyWhenAddTopic());
				forumNode.setProperty("exo:isNotifyWhenAddPost", newForum.getIsNotifyWhenAddPost());
				forumNode.setProperty("exo:isModerateTopic", newForum.getIsModerateTopic());
				forumNode.setProperty("exo:isModeratePost", newForum.getIsModeratePost());
				forumNode.setProperty("exo:isClosed", newForum.getIsClosed());
				forumNode.setProperty("exo:isLock", newForum.getIsLock());
		
				forumNode.setProperty("exo:viewForumRole", newForum.getViewForumRole());
				forumNode.setProperty("exo:createTopicRole", newForum.getCreateTopicRole());
				forumNode.setProperty("exo:replyTopicRole", newForum.getReplyTopicRole());
				forumNode.setProperty("exo:moderators", newForum.getModerators());
			
		  }
		  forumHomeNode.save() ;
		  forumHomeNode.getSession().save() ; 
    }
  }

  public Forum removeForum(String categoryId, String forumId) throws Exception {
		Node forumHomeNode = getForumHomeNode();
		Forum forum = new Forum();
		if(forumHomeNode.hasNode(categoryId)) {
		  Node catNode = forumHomeNode.getNode(categoryId) ;
		  forum = getForum(categoryId, forumId);
		  catNode.getNode(forumId).remove();
		  forumHomeNode.save() ;
		  forumHomeNode.getSession().save() ;
		  return forum;
		}
		return null;
  }
  
  public void moveForum(String forumPath, String destCategoryPath)throws Exception {
  	Node forumHomeNode = getForumHomeNode();
  	forumHomeNode.getSession().getWorkspace().move(forumPath, destCategoryPath);
  	forumHomeNode.save() ;
  	forumHomeNode.getSession().save() ;
  }
  
  
  public PageList getTopics(String categoryId, String forumId) throws Exception {
    Node forumHomeNode = getForumHomeNode();
    if(forumHomeNode.hasNode(categoryId)) {
  	  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  Node forumNode = CategoryNode.getNode(forumId);
		  NodeIterator iter = forumNode.getNodes();
		  List <Topic> topics = new ArrayList<Topic>();
		  Topic topic;
		  while (iter.hasNext()) {
	      Node topicNode = iter.nextNode();
	      topic = getTopicNode(topicNode);
	      topics.add(topic);
      }
		  PageList pagelist = new ObjectPageList(topics, 10);
		  return pagelist;
    }
	  return null;
  }
  
	public Topic getTopic(String categoryId, String forumId, String topicId) throws Exception {
    Node forumHomeNode = getForumHomeNode();
    if(forumHomeNode.hasNode(categoryId)) {
  	  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  Node forumNode = CategoryNode.getNode(forumId);
		  Node topicNode = forumNode.getNode(topicId);
		  Topic topicNew = new Topic();
		  topicNew = getTopicNode(topicNode);
			// setViewCount for Topic
			long newViewCount = topicNode.getProperty("exo:viewCount").getLong() + 1;
			topicNode.setProperty("exo:viewCount", newViewCount);
			
		  return topicNew;
    }
    return null;
  }

  private Topic getTopicNode(Node topicNode) throws Exception {
	  Topic topicNew = new Topic();
	  
	  topicNew.setId(topicNode.getProperty("exo:id").getString());
	  topicNew.setOwner(topicNode.getProperty("exo:owner").getString());
	  topicNew.setPath(topicNode.getProperty("exo:path").getString());
	  topicNew.setTopicName(topicNode.getProperty("exo:name").getString());
	  topicNew.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime());
	  topicNew.setModifiedBy(topicNode.getProperty("exo:modifiedBy").getString());
	  topicNew.setModifiedDate(topicNode.getProperty("exo:modifiedDate").getDate().getTime());
	  topicNew.setLastPostBy(topicNode.getProperty("exo:lastPostBy").getString());
	  topicNew.setLastPostDate(topicNode.getProperty("exo:lastPostDate").getDate().getTime());
	  topicNew.setDescription(topicNode.getProperty("exo:description").getString());
	  topicNew.setPostCount(topicNode.getProperty("exo:postCount").getLong());
	  topicNew.setViewCount(topicNode.getProperty("exo:viewCount").getLong());
	  topicNew.setIcon(topicNode.getProperty("exo:icon").getString());
	  
	  topicNew.setIsNotifyWhenAddPost(topicNode.getProperty("exo:isNotifyWhenAddPost").getBoolean());
	  topicNew.setIsModeratePost(topicNode.getProperty("exo:isModeratePost").getBoolean());
	  topicNew.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean());
	  topicNew.setIsLock(topicNode.getProperty("exo:isLock").getBoolean());
	  topicNew.setIsApproved(topicNode.getProperty("exo:isApproved").getBoolean());	  
	  return topicNew;
  }

  public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception {
	  TopicView topicview = new TopicView();
	  topicview.setTopicView(getTopic(categoryId, forumId, topicId));
	  topicview.setPostsView(getPosts(categoryId, forumId, topicId));
  	return topicview;
  }
  
 
  public void createTopic(String categoryId, String forumId, Topic topic) throws Exception {
		Node forumHomeNode = getForumHomeNode();
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  if(CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.addNode(topic.getId(), "exo:topic");
				
				topicNode.setProperty("exo:id", topic.getId());
				topicNode.setProperty("exo:owner", topic.getOwner());
				topicNode.setProperty("exo:path", topicNode.getPath());
				topicNode.setProperty("exo:name", topic.getTopicName());
		    topicNode.setProperty("exo:createdDate", GregorianCalendar.getInstance());
		    topicNode.setProperty("exo:modifiedBy", topic.getModifiedBy());
		    topicNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
		    topicNode.setProperty("exo:lastPostBy", topic.getLastPostBy());
		    topicNode.setProperty("exo:lastPostDate", GregorianCalendar.getInstance());
		    topicNode.setProperty("exo:description", topic.getDescription());
		    topicNode.setProperty("exo:postCount", 0);
		    topicNode.setProperty("exo:viewCount", 1);
		    topicNode.setProperty("exo:icon", topic.getIcon());
		    
		    topicNode.setProperty("exo:isModeratePost", topic.getIsModeratePost());
		    topicNode.setProperty("exo:isNotifyWhenAddPost", topic.getIsNotifyWhenAddPost());
		    topicNode.setProperty("exo:isClosed", topic.getIsClosed());
		    topicNode.setProperty("exo:isLock", topic.getIsLock());
		    topicNode.setProperty("exo:isApproved", topic.getIsApproved());
			  // setTopicCount for Forum
		    long newTopicCount = forumNode.getProperty("exo:topicCount").getLong() + 1;
			  forumNode.setProperty("exo:topicCount", newTopicCount );
		    
		    forumHomeNode.save() ;
		    forumHomeNode.getSession().save() ;
		    // createPost first
//		    GregorianCalendar calendar = new GregorianCalendar() ;
//				String id = String.valueOf(calendar.getTimeInMillis());
//		    Post post = new Post();
//		    post.setId(id);
//				post.setOwner(topic.getOwner());
//				post.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime());
//				post.setModifiedBy(topic.getModifiedBy());
//				post.setModifiedDate(topicNode.getProperty("exo:createdDate").getDate().getTime());
//				post.setSubject(topic.getTopicName());
//				post.setMessage(topic.getDescription());
//				post.setRemoteAddr("");
//				post.setIcon(topic.getIcon());
//				post.setIsApproved(false);
//				
//				createPost(categoryId, forumId, topic.getId(), post);
		  }
		}
  }
  
  public void updateTopic(String categoryId, String forumId, Topic newTopic) throws Exception {
  	Node forumHomeNode = getForumHomeNode();
    if(forumHomeNode.hasNode(categoryId)) {
  	  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  Node forumNode = CategoryNode.getNode(forumId);
		  Node topicNode = forumNode.getNode(newTopic.getId());
		  
		  topicNode.setProperty("exo:owner", newTopic.getOwner());
		  topicNode.setProperty("exo:name", newTopic.getTopicName());
		  topicNode.setProperty("exo:modifiedBy", newTopic.getModifiedBy());
		  topicNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
		  topicNode.setProperty("exo:description", newTopic.getDescription());
		  topicNode.setProperty("exo:icon", newTopic.getIcon());
		  
		  topicNode.setProperty("exo:isModeratePost", newTopic.getIsModeratePost());
	    topicNode.setProperty("exo:isNotifyWhenAddPost", newTopic.getIsNotifyWhenAddPost());
	    topicNode.setProperty("exo:isClosed", newTopic.getIsClosed());
	    topicNode.setProperty("exo:isLock", newTopic.getIsLock());
	    topicNode.setProperty("exo:isApproved", newTopic.getIsApproved());
	    
    }
    forumHomeNode.save() ;
    forumHomeNode.getSession().save() ;
  } 
  
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
		Node forumHomeNode = getForumHomeNode();
		Topic topic = new Topic();
	  if(forumHomeNode.hasNode(categoryId)) {
	  	Node CategoryNode = forumHomeNode.getNode(categoryId);
		  Node forumNode = CategoryNode.getNode(forumId);
		  topic = getTopic(categoryId, forumId, topicId);
		  // setTopicCount for Forum
		  long newTopicCount = forumNode.getProperty("exo:topicCount").getLong() - 1;
		  forumNode.setProperty("exo:topicCount", newTopicCount );
		  // setPostCount for Forum
		  long newPostCount = forumNode.getProperty("exo:postCount").getLong() - topic.getPostCount();
		  forumNode.setProperty("exo:postCount", newPostCount );
		  
		  forumNode.getNode(topicId).remove();
		  forumHomeNode.save() ;
		  forumHomeNode.getSession().save() ;
		  return topic;
	  }
    return null;
  }
  
  public void moveTopic(String  topicPath, String destForumPath) throws Exception {
  	Node forumHomeNode = getForumHomeNode();
  	forumHomeNode.getSession().getWorkspace().move(topicPath, destForumPath);
  	forumHomeNode.save() ;
  	forumHomeNode.getSession().save() ;
  }
  
  
  public List<Post> getPosts(String categoryId, String forumId, String topicId) throws Exception {
    Node forumHomeNode = getForumHomeNode();
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  if(CategoryNode.hasNode(forumId)) {
			Node forumNode = CategoryNode.getNode(forumId);
			if(forumNode.hasNode(topicId)) {
				Node topicNode = forumNode.getNode(topicId);
				NodeIterator iter = topicNode.getNodes(); 
				List<Post> Posts = new ArrayList<Post>();
				Post postNew;
				while(iter.hasNext()) {
				  Node postNode = iter.nextNode();
				  postNew = getPost(postNode);
				  Posts.add(postNew);
				}
				return Posts;
			  }
		  }
		}
    return null;
  }
  
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
		Node forumHomeNode = getForumHomeNode();
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  if(CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				if(!topicNode.hasNode(postId)) return null;
				Node postNode = topicNode.getNode(postId);
				Post postNew = new Post();
				postNew = getPost(postNode);
				return postNew;
		  }
		}
    return null;
  }

  private Post getPost(Node postNode) throws Exception {
		Post postNew = new Post();
		postNew.setId(postNode.getProperty("exo:id").getString());
		postNew.setOwner(postNode.getProperty("exo:owner").getString());
		postNew.setPath(postNode.getProperty("exo:path").getString());
		postNew.setCreatedDate(postNode.getProperty("exo:createdDate").getDate().getTime());
		postNew.setModifiedBy(postNode.getProperty("exo:modifiedBy").getString());
		postNew.setModifiedDate(postNode.getProperty("exo:modifiedDate").getDate().getTime());
		postNew.setSubject(postNode.getProperty("exo:subject").getString());
		postNew.setMessage(postNode.getProperty("exo:message").getString());
		postNew.setRemoteAddr(postNode.getProperty("exo:remoteAddr").getString());
		postNew.setIcon(postNode.getProperty("exo:icon").getString());
		postNew.setIsApproved(postNode.getProperty("exo:isApproved").getBoolean());
		return postNew;
  }
  
  public void createPost(String categoryId, String forumId, String topicId, Post post) throws Exception {
		Node forumHomeNode = getForumHomeNode();
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  if(CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				Node postNode = topicNode.addNode(post.getId(), "exo:post");
				
				postNode.setProperty("exo:id", post.getId());
				postNode.setProperty("exo:owner", post.getOwner());
				postNode.setProperty("exo:path", postNode.getPath());
				postNode.setProperty("exo:createdDate", GregorianCalendar.getInstance());
				postNode.setProperty("exo:modifiedBy", post.getModifiedBy());
				postNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
				postNode.setProperty("exo:subject", post.getSubject());
				postNode.setProperty("exo:message", post.getMessage());
				postNode.setProperty("exo:remoteAddr", post.getRemoteAddr());
				postNode.setProperty("exo:icon", post.getIcon());
				postNode.setProperty("exo:isApproved", post.getIsApproved());
		    // setPostCount for Topic
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong() + 1;
				topicNode.setProperty("exo:postCount", topicPostCount );
				// setPostCount for Forum
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong() + 1;
				forumNode.setProperty("exo:postCount", forumPostCount );
				
		    forumHomeNode.save() ;
		    forumHomeNode.getSession().save() ;
		    
		  }
		}
  }
  
  public void updatePost(String categoryId, String forumId, String topicId, Post newPost) throws Exception {
    Node forumHomeNode = getForumHomeNode();
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  if(CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				Node postNode = topicNode.getNode(newPost.getId());
				
				postNode.setProperty("exo:modifiedBy", newPost.getModifiedBy());
				postNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance());
				postNode.setProperty("exo:subject", newPost.getSubject());
				postNode.setProperty("exo:message", newPost.getMessage());
				postNode.setProperty("exo:remoteAddr", newPost.getRemoteAddr());
				postNode.setProperty("exo:icon", newPost.getIcon());
				postNode.setProperty("exo:isApproved", newPost.getIsApproved());
		  }
		  forumHomeNode.save() ;
		  forumHomeNode.getSession().save() ;
		}
  }
  
  public Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    Node forumHomeNode = getForumHomeNode();
    Post post = new Post();
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId);
		  if(CategoryNode.hasNode(forumId)) {
			  post = getPost(categoryId, forumId, topicId, postId);
				Node forumNode = CategoryNode.getNode(forumId);
				Node topicNode = forumNode.getNode(topicId);
				topicNode.getNode(postId).remove();
				// setPostCount for Topic
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong() - 1;
				topicNode.setProperty("exo:postCount", topicPostCount );
				// setPostCount for Forum
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong() - 1;
				forumNode.setProperty("exo:postCount", forumPostCount );

				forumHomeNode.save() ;
				forumHomeNode.getSession().save() ;
				return post;
		  }
		}
		return null;
  }
  
  public void movePost(String postPath, String destTopicPaths) throws Exception {
  	Node forumHomeNode = getForumHomeNode();
  	forumHomeNode.getSession().getWorkspace().move(postPath, destTopicPaths);
  	forumHomeNode.save() ;
  	forumHomeNode.getSession().save() ;
  }
  
  
  private String [] ValuesToStrings (Value[] Val) {
		String[] Str = new String[(int) Val.length];
		for(int i = 0; i < Val.length; ++i) {
		  Str[i] = Val[i].toString();
		}
		return Str;
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
