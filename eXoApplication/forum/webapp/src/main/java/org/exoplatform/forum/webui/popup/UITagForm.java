/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Dec 12, 2007 11:34:56 AM 
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UITagForm.gtmpl",
    events = {
      @EventConfig(listeners = UITagForm.AddTagActionListener.class), 
      @EventConfig(listeners = UITagForm.EditTagActionListener.class),
      @EventConfig(listeners = UITagForm.DeleteActionListener.class),
      @EventConfig(listeners = UITagForm.SelectedActionListener.class),
      @EventConfig(listeners = UITagForm.SaveActionListener.class),
      @EventConfig(listeners = UITagForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UITagForm extends UIForm implements UIPopupComponent {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	@SuppressWarnings("unused")
  private String IdSelected = "";
	private String topicPath = "";
	private String tagId[] = new String[] {} ;
	public UITagForm() throws Exception {
  }

	public void activate() throws Exception {
	  // TODO Auto-generated method stub
  }

	public void deActivate() throws Exception {
	  // TODO Auto-generated method stub
  }
	
	public void setTopicPathAndTagId(String topicPath, String[] tagId) {
	  this.topicPath = topicPath ;
	  this.tagId = tagId ;
  }
	
	@SuppressWarnings("unused")
  private boolean getSelected(String tagId) throws Exception {
		if(this.IdSelected.equals(tagId)) return true ;
		return false ;
	}
	
	@SuppressWarnings("unused")
  private List<Tag> getAllTag() throws Exception {
		List<Tag> tags = forumService.getTags(ForumUtils.getSystemProvider());
		List<Tag> tags_ = new ArrayList<Tag>() ;
		boolean isUpdate = true ;
		for (Tag tag : tags) {
	    String tagId = tag.getId() ;
	    for(String str : this.tagId) {
	    	if(tagId.equals(str)) {
	    		isUpdate = false ;
	    		break ;
	    	}
	    }
	    if(isUpdate) tags_.add(tag) ;
	    isUpdate = true ;
    }
		if(tags_.size() > 0 && this.IdSelected.length() == 0) this.IdSelected = tags.get(0).getId() ;
		return tags_ ;
	}
	
	static  public class AddTagActionListener extends EventListener<UITagForm> {
		public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      uiChildPopup.activate(UIAddTagForm.class, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static  public class EditTagActionListener extends EventListener<UITagForm> {
		public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
      uiChildPopup.activate(UIAddTagForm.class, 400) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static  public class DeleteActionListener extends EventListener<UITagForm> {
		public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			uiForm.forumService.removeTag(ForumUtils.getSystemProvider(), uiForm.IdSelected);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
		}
	}

	static  public class SelectedActionListener extends EventListener<UITagForm> {
		public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			uiForm.IdSelected = event.getRequestContext().getRequestParameter(OBJECTID);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getParent()) ;
		}
	}

	static  public class SaveActionListener extends EventListener<UITagForm> {
		public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			if(uiForm.IdSelected != null && uiForm.IdSelected.length() > 0 && uiForm.topicPath != null && uiForm.topicPath.length() > 0) {
				uiForm.forumService.addTopicInTag(ForumUtils.getSystemProvider(), uiForm.IdSelected, uiForm.topicPath);
			}
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UITagForm> {
		public void execute(Event<UITagForm> event) throws Exception {
			UITagForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
