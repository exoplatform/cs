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

import java.io.InputStream;
import java.util.LinkedHashMap;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.calendar.webui.popup.UIPopupComponent;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/calendar/webui/UIDefaultPreview.gtmpl",
    events = {
      @EventConfig(listeners = UIPreview.ViewActionListener.class),  
      @EventConfig(listeners = UICalendarView.EditActionListener.class),  
      @EventConfig(listeners = UICalendarView.DeleteActionListener.class)

    }
)
public class UIPreview extends UICalendarView implements UIPopupComponent {
  private CalendarEvent event_ = null ;
  private boolean isShowPopup_ = false ;

  public UIPreview() throws Exception {}

  public String getTemplate(){
    if(event_ == null) return "app:/templates/calendar/webui/UIDefaultPreview.gtmpl" ;
    if(event_.getEventType().equals(CalendarEvent.TYPE_EVENT))
      return "app:/templates/calendar/webui/UIEventPreview.gtmpl" ;
    if(event_.getEventType().equals(CalendarEvent.TYPE_TASK))
      return "app:/templates/calendar/webui/UITaskPreview.gtmpl" ;
    return "app:/templates/calendar/webui/UIDefaultPreview.gtmpl" ;
  }

  public CalendarEvent getEvent(){ return event_ ; }
  public void setEvent(CalendarEvent event) { event_ = event ; }

  public void refresh() throws Exception {
    /*if(event_ != null) {
      CalendarService calService = getApplicationComponent(CalendarService.class) ;
      String username = Util.getPortalRequestContext().getRemoteUser() ;
      CalendarEvent event = null ;
      if(CalendarUtils.PUBLIC_TYPE.equals(event_.getCalType())) {
        event = calService.getGroupEvent(event_.getCalendarId(), event_.getId()) ;
      } else {
        event = calService.getUserEvent(username, event_.getCalendarId(), event_.getId()) ;
      }
      if( event != null) event_ = event ;
    } */
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  public void setShowPopup(boolean isShow) {
    this.isShowPopup_ = isShow;
  }

  public boolean isShowPopup() {
    return isShowPopup_;
  }

  public String getImage(Attachment att) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    InputStream input = att.getInputStream() ;
    dresource = new InputStreamDownloadResource(input, att.getName()) ;
    dresource.setDownloadName(att.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }

  public String getDownloadLink(Attachment att) throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource ;
    InputStream input = att.getInputStream() ;
    dresource = new InputStreamDownloadResource(input, att.getName()) ;
    dresource.setDownloadName(att.getName()) ;
    return dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
  @Override
  LinkedHashMap<String, CalendarEvent> getDataMap() {
    LinkedHashMap<String, CalendarEvent> dataMap = new LinkedHashMap<String, CalendarEvent>() ;
    dataMap.put(event_.getId(), event_) ;
    return dataMap ;
  }
  static  public class ViewActionListener extends EventListener<UIPreview> {
    public void execute(Event<UIPreview> event) throws Exception {
      UIPreview uiView = event.getSource() ;
      System.out.println("\n\n ViewActionListener");
      /* CalendarEvent eventCalendar = null ;
      String username = event.getRequestContext().getRemoteUser() ;
      String calendarId = event.getRequestContext().getRequestParameter(CALENDARID) ;
      String eventId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      try {
        CalendarService calService = uiView.getApplicationComponent(CalendarService.class) ;
        eventCalendar = calService.getUserEvent(username, calendarId, eventId) ;
      } catch (Exception e){
        e.printStackTrace() ;
      }
       */
    }
  }
}
