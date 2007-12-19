/**
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
 **/
package org.exoplatform.calendar.webui;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.SessionsUtils;
import org.exoplatform.calendar.service.EventPageList;
import org.exoplatform.calendar.service.EventQuery;
import org.exoplatform.calendar.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.calendar.webui.popup.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UISearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UISearchForm.SearchActionListener.class),
      @EventConfig(listeners = UISearchForm.AdvancedSearchActionListener.class)
    }
)
public class UISearchForm extends UIForm {
  final static  private String FIELD_SEARCHVALUE = "inputValue" ;

  public UISearchForm() {
    addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
  }

  static  public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String text = uiForm.getUIStringInput(UISearchForm.FIELD_SEARCHVALUE).getValue() ;
      if(text == null || text.length() == 0) {
        uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.no-text-to-search", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      EventQuery eventQuery = new EventQuery() ;
      eventQuery.setText(text) ;
      System.out.println("\n\n eventQuery " + eventQuery.getQueryStatement());
      String username = CalendarUtils.getCurrentUser() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UICalendarViewContainer calendarViewContainer = 
        calendarPortlet.findFirstComponentOfType(UICalendarViewContainer.class) ;
      UIListView uiListView = calendarViewContainer.findFirstComponentOfType(UIListView.class) ;
      EventPageList resultPageList = 
        CalendarUtils.getCalendarService().searchEvent(SessionsUtils.getSessionProvider(), username, eventQuery, uiListView.getPublicCalendars()) ;
      calendarViewContainer.setRenderedChild(UICalendarViewContainer.LIST_VIEW) ;
      uiListView.update(resultPageList) ;
      uiListView.setViewType(UIListView.TYPE_BOTH) ;
      uiListView.setDisplaySearchResult(true) ;
      uiListView.setSelectedEvent(null) ;
      calendarViewContainer.findFirstComponentOfType(UIPreview.class).setEvent(null) ;
      UIActionBar uiActionBar = calendarPortlet.findFirstComponentOfType(UIActionBar.class) ;
      uiActionBar.setCurrentView(UICalendarViewContainer.LIST_VIEW) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActionBar) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(calendarViewContainer) ;
    }
  }
  static  public class AdvancedSearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource() ;
      UICalendarPortlet calendarPortlet = uiForm.getAncestorOfType(UICalendarPortlet.class) ;
      UIPopupAction popupAction = calendarPortlet.getChild(UIPopupAction.class) ;
      popupAction.activate(UIAdvancedSearchForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
