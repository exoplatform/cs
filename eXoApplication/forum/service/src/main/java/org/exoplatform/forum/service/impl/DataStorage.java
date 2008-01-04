/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.List;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumOption;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *					tuan.nguyen@exoplatform.com
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
	public void moveTopic(String topicId, String	topicPath, String destForumPath) throws Exception;
	
	public JCRPageList getPosts(String categoryId, String forumId, String topicId)throws Exception;
	public Post getPost(String categoryId, String forumId, String topicId, String postId)throws Exception;
	public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew)throws Exception;
	public Post removePost(String categoryId, String forumId, String topicId, String postId)throws Exception;
	public void movePost(String postId, String postPath, String destTopicPath) throws Exception ;

	public Poll getPoll(String categoryId, String forumId, String topicId)throws Exception;
	public void savePoll(String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote)throws Exception;
	public Poll removePoll(String categoryId, String forumId, String topicId)throws Exception;

	public Object getObjectNameByPath(String path) throws Exception ;
	@SuppressWarnings("unchecked")
	public List getPage(long page, JCRPageList pageList) throws Exception ;
	public List<ForumLinkData> getAllLink() throws Exception ;
	
	public void addTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception ;
	public void removeTopicInTag(SessionProvider sProvider, String tagId, String topicPath) throws Exception ;
	public Tag getTag(SessionProvider sProvider, String tagId) throws Exception ;
	public List<Tag> getTags(SessionProvider sProvider) throws Exception ;
	public List<Tag> getTagsByUser(SessionProvider sProvider, String userName) throws Exception ;
	public List<Tag> getTagsByTopic(SessionProvider sProvider, String[] tagIds) throws Exception ;
	public JCRPageList getTopicsByTag(SessionProvider sProvider, String tagId) throws Exception ;
	public void saveTag(SessionProvider sProvider, Tag newTag, boolean isNew) throws Exception ;
	public void removeTag(SessionProvider sProvider, String tagId) throws Exception ;

	public void saveOption(SessionProvider sProvider, ForumOption newOption) throws Exception ;
	public ForumOption getOption(SessionProvider sProvider, String userName) throws Exception ;
}
