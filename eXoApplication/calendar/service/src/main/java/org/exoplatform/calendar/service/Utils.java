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
package org.exoplatform.calendar.service;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Sep 28, 2007  
 */
public class Utils {
  
  
  public static final String EXO_ID = "exo:id".intern() ;
  public static final String EXO_LABEL = "exo:label".intern() ;
  public static final String EXO_DESCRIPTION = "exo:description".intern() ;
  public static final String EXO_NAME = "exo:name".intern() ;

  
  public static final String EXO_EVEN_TATTACHMENT = "exo:eventAttachment".intern() ;
  public static final String EXO_FILE_NAME = "exo:fileName".intern() ;
  
  //public static final String ATTACHMENT_ID = "exo:attachmentId".intern() ; ;
  public static final String ATTACHMENT_NODE = "attachment".intern() ; ;
  public static final String REMINDERS_NODE = "reminders".intern() ; ;
  
  public static final String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  public static final String NT_FILE = "nt:file".intern() ;
  public static final String NT_RESOURCE = "nt:resource".intern() ;
  
  public static final String JCR_LASTMODIFIED = "jcr:lastModified".intern() ;
  public static final String JCR_CONTENT = "jcr:content".intern() ;
  public static final String JCR_MIMETYPE = "jcr:mimeType".intern() ;
  public static final String JCR_DATA = "jcr:data".intern() ;
  
  public static final String MIMETYPE_TEXTPLAIN = "text/plain".intern() ;
  public static final String ATTACHMENT = "ATTACHMENT".intern();
  public static final String INLINE = "INLINE".intern();
  
  public static GregorianCalendar getInstanceTempCalendar() { 
	  GregorianCalendar  calendar = new GregorianCalendar() ;
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset) ;
		return  calendar;
	}
}
