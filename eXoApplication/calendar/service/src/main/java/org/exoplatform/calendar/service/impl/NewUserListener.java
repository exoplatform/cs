/*
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
 */
package org.exoplatform.calendar.service.impl;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.calendar.service.EventCategory;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Nov 23, 2007 3:09:21 PM
 */
public class NewUserListener extends UserEventListener {
  private CalendarService cservice_ ;
  private String[] defaultEventCategories_ ;
  private String defaultCalendarCategory_ ;
  private String[] defaultCalendar_ ;
  public NewUserListener(CalendarService cservice, InitParams params) throws Exception {
  	cservice_ = cservice ;
  	String defaultEventCategories = params.getValueParam("defaultEventCategories").getValue() ;
  	if(defaultEventCategories != null && defaultEventCategories.length() > 0) {
  		defaultEventCategories_ = defaultEventCategories.split(",") ;
  	}
  	
  	defaultCalendarCategory_ = params.getValueParam("defaultCalendarCategory").getValue() ;
  	
  	String defaultCalendar = params.getValueParam("defaultCalendar").getValue() ;
  	if(defaultCalendar != null && defaultCalendar.length() > 0) {
  		defaultCalendar_ = defaultCalendar.split(",") ;
  	}
  }
  
  public void postSave(User user, boolean isNew) throws Exception {
  	SessionProvider sysProvider = SessionProvider.createSystemProvider() ;
  	if(defaultEventCategories_ != null && defaultEventCategories_.length > 0) {
  		for(String evCategory : defaultEventCategories_) {
  			EventCategory eventCategory = new EventCategory() ;
  			eventCategory.setName(evCategory) ;
  	  	cservice_.saveEventCategory(sysProvider, user.getUserName(), eventCategory, null, true) ;
    	}
  	}
  	if(defaultCalendarCategory_ != null && defaultCalendarCategory_.length() > 0) {
			CalendarCategory calCategory = new CalendarCategory() ;
			calCategory.setName(defaultCalendarCategory_) ;  			
	  	cservice_.saveCalendarCategory(sysProvider, user.getUserName(), calCategory, true) ;
	  	if(defaultCalendar_ != null && defaultCalendar_.length > 0) {
	  		for(String calendar : defaultCalendar_) {
	  			Calendar cal = new Calendar() ;
	  			cal.setName(calendar) ;  
	  			cal.setCategoryId(calCategory.getId()) ;
	  	  	cservice_.saveUserCalendar(sysProvider, user.getUserName(), cal, true) ;
	    	}
	  	}
  	}
  	
  }
  
  public void preDelete(User user) throws Exception {
    
  }
}