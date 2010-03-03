/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.webservice.cs.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.calendar.service.Calendar;
import org.exoplatform.calendar.service.CalendarCategory;
import org.exoplatform.calendar.service.CalendarService;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.webservice.cs.calendar.CalendarWebservice;

/**
 * Created by The eXo Platform SARL Author : Volodymyr Krasnikov
 * volodymyr.krasnikov@exoplatform.com.ua
 */

public class TestWebservice extends AbstractResourceTest {

  CalendarWebservice calendarWebservice;
  CalendarService calendarService;

  static final String             baseURI = "";

  public void setUp() throws Exception {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    super.setUp();
    calendarWebservice = (CalendarWebservice) container.getComponentInstanceOfType(CalendarWebservice.class);
    calendarService = (MockCalendarService) container.getComponentInstanceOfType(MockCalendarService.class);
    registry(calendarWebservice);
    //registry(calendarService);
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

   

  public void testCheckPublicRss() throws Exception {

    start();
    UserHandler hUser = orgService.getUserHandler();
 
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
     
    String username = "root";

    h.putSingle("username", username);
    
    //Create calendar
    
    CalendarCategory calCategory = new CalendarCategory() ;
    calCategory.setName("categoryName") ;
    calCategory.setDescription("Description") ;
    //assertNotNull(calendarService);
    //calendarService.saveCalendarCategory("root", calCategory, true) ;

    //create/get calendar in private folder
    Calendar cal = new Calendar() ;
    cal.setName("myCalendar") ;
    cal.setDescription("Desscription") ;
    cal.setCategoryId(calCategory.getId()) ;
    cal.setPublic(true) ;
    
    String extURI = "/cs/calendar/rss/" + username + "/" + cal.getId() + "/0";
    
    cal.setPublicUrl(extURI);
    System.out.println("\n\n extURI " + extURI);
    calendarService.saveUserCalendar(username, cal, true);
    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();

    ContainerResponse response = service("GET", extURI, baseURI, h, null, writer);
     
    response = service("GET", cal.getPublicUrl(), baseURI, h, null, writer);
    
    assertNotNull(response);
    //assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
    //assertEquals(HTTPStatus.NO_CONTENT, response.getStatus());
    //assertEquals(HTTPStatus.INTERNAL_ERROR, response.getStatus());
    assertEquals(HTTPStatus.OK, response.getStatus());
    //assertEquals(MediaType.APPLICATION_XML, response.getContentType());
     
    stop();
  }


}
