/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.chatbar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.portlet.PortletRequestContext;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 26, 2010  
 */
public class Utils {
  
  public static final String URL_PATTERN = "((mailto\\:|(news|(ht|f)tp(s?))\\://){1}\\S+)";
  
  public static String getServerBaseUrl() {
    PortletRequestContext portletRequestContext = PortletRequestContext.getCurrentInstance() ;
    String url = portletRequestContext.getRequest().getScheme() + "://" + 
    portletRequestContext.getRequest().getServerName() + ":" +
    String.format("%s",portletRequestContext.getRequest().getServerPort()) 
    + "/" ;
    return url ;
  }
  
  public static String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  
  
  public static boolean isUri(String input) {
    try {
      if (input == null) return false;
      Matcher matcher = Pattern.compile(URL_PATTERN).matcher(input);
      return matcher.find();
    } catch (Exception e) {
      return false;
    }
  }
  
}
