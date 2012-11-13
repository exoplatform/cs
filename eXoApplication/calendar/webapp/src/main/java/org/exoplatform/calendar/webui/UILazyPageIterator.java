/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.calendar.webui;

import org.exoplatform.commons.serialization.api.annotations.Serialized;
import org.exoplatform.commons.utils.EmptySerializablePageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A customized version of org.exoplatform.webui.core UIPageIterator 
 * in order to show only current page. 
 * Each time next page button is clicked, new data is querying from JCR 
 * 
 */
@ComponentConfig(
  template = "app:/templates/calendar/webui/UILazyPageIterator.gtmpl", 
  events = @EventConfig(listeners = UILazyPageIterator.ShowPageActionListener.class)
)
@Serialized
public class UILazyPageIterator extends UIPageIterator
{   
   /* current page number to be displayed in the UI */
   private int pageShown; 
   
   public UILazyPageIterator()
   {
     // first page is 1
     pageShown = 1;
   }

   public void setPageShown(int pageNumber)
   {
     pageShown = pageNumber;
   }
   
   public int getPageShown()
   {
     return pageShown;
   }
}