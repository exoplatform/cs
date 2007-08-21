/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.webui.UICalendarPortlet;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.util.IdGenerator;
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
    //template = "app:/templates/calendar/webui/UICalendarCategoryForm.gtmpl",
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UICalendarCategoryForm.SaveActionListener.class),
      @EventConfig(listeners = UICalendarCategoryForm.CancelActionListener.class)
    }
)
public class UICalendarCategoryForm extends UIForm implements UIPopupComponent{
  final static String CATEGORY_NAME = "categoryName" ;
  final static String DESCRIPTION = "description" ;
  private boolean isAddNew = true ;
  public UICalendarCategoryForm() {
    addUIFormInput(new UIFormStringInput(CATEGORY_NAME, null)) ;
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
  }
  
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  
  static  public class SaveActionListener extends EventListener<UICalendarCategoryForm> {
    public void execute(Event<UICalendarCategoryForm> event) throws Exception {
      UICalendarCategoryForm uiForm = event.getSource() ;
      String categoryName = uiForm.getUIStringInput(CATEGORY_NAME).getValue() ;
      String description = uiForm.getUIFormTextAreaInput(DESCRIPTION).getValue() ;
      CalendarService calendarService = (CalendarService)PortalContainer.getComponent(CalendarService.class) ;
      CalendarCategory category = new CalendarCategory() ;
      category.setId("category" + IdGenerator.generate()) ;
      category.setName(categoryName) ;
      category.setDescription(description) ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      calendarService.saveCalendarCategory(username, category, uiForm.isAddNew) ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
  static  public class CancelActionListener extends EventListener<UICalendarCategoryForm> {
    public void execute(Event<UICalendarCategoryForm> event) throws Exception {
      UICalendarCategoryForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      calendarPortlet.cancelAction() ;
    }
  }
  
}
