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
package org.exoplatform.forum.webui;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatFunction;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.popup.UIAddTagForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/UITopicsTag.gtmpl",
		events = {
				@EventConfig(listeners = UITopicsTag.OpenTopicActionListener.class ),
				@EventConfig(listeners = UITopicsTag.EditTagActionListener.class ),
				@EventConfig(listeners = UITopicsTag.OpenTopicsTagActionListener.class ),
				@EventConfig(listeners = UITopicsTag.RemoveTopicActionListener.class ),
				@EventConfig(listeners = UITopicsTag.RemoveTagActionListener.class )
		}
)

public class UITopicsTag extends UIForm {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String tagId = "" ;
	private JCRPageList listTopic ;
	private List<Topic> topics ;
	private long page = 1 ;
	private Tag tag ;
	private boolean isUpdateTag = true ;
	//private List <JCRPageList> listPageListPost = new ArrayList<JCRPageList>() ;
	public UITopicsTag() throws Exception {
		//addChild(UIForumPageIterator.class, null, null) ;
	}
	
	public void setIdTag(String tagId) {
		this.tagId = tagId ;
		this.isUpdateTag = true ;
  }
	
	@SuppressWarnings("unused")
  private JCRPageList getListTopicTag() throws Exception {
		this.listTopic = forumService.getTopicsByTag(ForumUtils.getSystemProvider(), this.tagId) ;
		return this.listTopic ;
	}

	@SuppressWarnings({ "unchecked", "unused" })
  private List<Topic> getTopicsTag() throws Exception {
		this.listTopic = forumService.getTopicsByTag(ForumUtils.getSystemProvider(), this.tagId) ;
		this.topics = forumService.getPage(page, this.listTopic, ForumUtils.getSystemProvider()) ;
		for(Topic topic : this.topics) {
			if(getUIFormCheckBoxInput(topic.getId()) != null) {
				getUIFormCheckBoxInput(topic.getId()).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(topic.getId(), topic.getId(), false) );
			}
		}
		return this.topics ;
	}
	
	@SuppressWarnings("unused")
  private Tag getTagById() throws Exception {
		if(this.isUpdateTag) {
			this.tag = forumService.getTag(ForumUtils.getSystemProvider(), this.tagId) ;
			this.isUpdateTag = false ;
		}
		return this.tag ;
	}

	@SuppressWarnings("unused")
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		return ForumFormatFunction.getStarNumber(voteRating) ;
	}
	
	@SuppressWarnings("unused")
	private String getStringCleanHtmlCode(String sms) {
		return ForumFormatFunction.getStringCleanHtmlCode(sms);
	}
	
	@SuppressWarnings("unused")
	private List<Tag> getTagsByTopic(String[] tagIds) throws Exception {
		String []ids = new String[tagIds.length-1] ; 
		int t = 0;
		for (String string : tagIds) {
	    if(!string.equals(this.tagId)){
	    	ids[t] = string ;
	    	++t;
	    }
    }
		return this.forumService.getTagsByTopic(ForumUtils.getSystemProvider(), ids);
	}
	
	private Topic getTopic(String topicId) throws Exception {
		List<Topic> listTopic = this.topics ;
		for (Topic topic : listTopic) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
	static public class OpenTopicActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag uiTopicsTag = event.getSource();
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Topic topic = uiTopicsTag.getTopic(topicId);
			String []temp = topic.getPath().split("/") ;
			UIForumPortlet forumPortlet = uiTopicsTag.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiTopicDetail.setUpdateContainer(temp[temp.length-3], temp[temp.length-2], topic, 1) ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(temp[temp.length-3], temp[temp.length-2], topic) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption(temp[temp.length-3] + "/" + temp[temp.length-2] + "");
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenTopicsTagActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(tagId) ;
			topicsTag.setIdTag(tagId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class EditTagActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
      UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
      UIAddTagForm addTagForm = popupAction.createUIComponent(UIAddTagForm.class, null, null) ;
      addTagForm.setUpdateTag(topicsTag.tag);
      addTagForm.setIsTopicTag(true) ;
			popupAction.activate(addTagForm, 410, 263) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class RemoveTopicActionListener extends EventListener<UITopicsTag> {
		@SuppressWarnings("unchecked")
    public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			List<UIComponent> children = topicsTag.getChildren() ;
			boolean hasCheck = false ;
			String topicPath = "" ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						topicPath = topicsTag.getTopic(child.getName()).getPath() ;
						topicsTag.forumService.removeTopicInTag(ForumUtils.getSystemProvider(), 
								topicsTag.tagId,topicPath ) ;
						hasCheck = true ;
					}
				}
			}
			if(!hasCheck) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheck", 
						args, ApplicationMessage.WARNING)) ;
			}else {
				topicsTag.isUpdateTag = true ;
			}
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class RemoveTagActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
			topicsTag.forumService.removeTag(ForumUtils.getSystemProvider(), topicsTag.tagId) ;
			forumPortlet.updateIsRendered(1) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			categoryContainer.updateIsRender(true) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath("ForumService") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

}
