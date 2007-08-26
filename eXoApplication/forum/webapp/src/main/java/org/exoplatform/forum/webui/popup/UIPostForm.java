/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIPostForm.gtmpl",
    events = {
      @EventConfig(listeners = UIPostForm.PreviewPost.class), 
      @EventConfig(listeners = UIPostForm.SubmitPost.class), 
      @EventConfig(listeners = UIPostForm.CancelAction.class)
    }
)
public class UIPostForm extends UIForm implements UIPopupComponent {
  public static final String FIELD_POSTTITLE_INPUT = "PostTitle" ;
  public static final String FIELD_MESSENGER_TEXTAREA = "Messenger" ;
  
  private String categoryId; 
  private String forumId ;
  public UIPostForm() throws Exception {
    UIFormStringInput postTitle = new UIFormStringInput(FIELD_POSTTITLE_INPUT, FIELD_POSTTITLE_INPUT, null);
    postTitle.addValidator(EmptyNameValidator.class) ;
    UIFormTextAreaInput messenger = new UIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA, FIELD_MESSENGER_TEXTAREA, null);
    
    addUIFormInput(postTitle);
    addUIFormInput(messenger);
  }
  
  public void setTopicIds(String categoryId, String forumId) {
    this.categoryId = categoryId ;
    this.forumId = forumId ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    //System.out.println("\n\n description: sfdsf\n\n");
  }
  
  public String[] getActionsTopic() throws Exception {
    return (new String [] {"PreviewPost", "SubmitPost", "CancelAction"});
  }
  
  static  public class PreviewPost extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
      UIPostForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
  
  static  public class SubmitPost extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
      UIPostForm uiForm = event.getSource() ;
      String postTitle = uiForm.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue().trim();
      String messenger = uiForm.getUIFormTextAreaInput(FIELD_MESSENGER_TEXTAREA).getValue() ;
      
      String userName = Util.getPortalRequestContext().getRemoteUser() ;
  
    }
  }
  
  static  public class CancelAction extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
      UIPostForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}
