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
package org.exoplatform.mail.service.test;

import java.util.Calendar;
import org.exoplatform.commons.utils.ISO8601;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 22, 2010
 */
public class TestMessageFilter extends BaseMailTestCase {

  public TestMessageFilter() throws Exception {
    super();
  }

  public void testGetStatement() throws Exception {
    MessageFilter filter = new MessageFilter("message filter");
    filter.setAccountPath("/users/root");
    StringBuilder sb = new StringBuilder("/jcr:root/users/root//element(*,exo:message)");
    assertEquals(sb.toString().trim(), filter.getStatement().trim());
    
    filter.setText("hanoi");
    assertTrue(filter.getStatement().trim().contains("jcr:contains(., 'hanoi')"));
    filter.setExcludeFolders(new String[] {"Draft"});
    assertTrue(filter.getStatement().trim().contains("@exo:folders!='Draft'"));
    filter.setFolder(new String[] {"inbox"});
    assertTrue(filter.getStatement().trim().contains("fn:upper-case(@exo:folders)='INBOX'"));
    filter.setTag(new String[] {"Gmail"});
    assertTrue(filter.getStatement().trim().contains("fn:upper-case(@exo:tags)='GMAIL'"));
    filter.setFrom("John");
    assertTrue(filter.getStatement().trim().contains("jcr:like(fn:upper-case(@exo:from), '%JOHN%'"));
    filter.setTo("Mary");
    assertTrue(filter.getStatement().trim().contains("jcr:like(fn:upper-case(@exo:to), '%MARY%'"));
    filter.setSubject("Test");
    assertTrue(filter.getStatement().trim().contains("jcr:contains(@exo:subject, 'Test'"));
    filter.setBody("Body");
    assertTrue(filter.getStatement().trim().contains("jcr:contains(@exo:body, '" + Utils.encodeJCRTextSearch("Body") + "')"));
    
    filter.setPriority(1);
    assertTrue(filter.getStatement().trim().contains("@exo:priority = 1"));
    filter.setHasAttach(true);
    assertTrue(filter.getStatement().trim().contains("@exo:hasAttach = 'true'"));
    filter.setHasStar(true);
    assertTrue(filter.getStatement().trim().contains("@exo:star = 'true'"));
    
    Calendar calendar = Calendar.getInstance();
    filter.setFromDate(calendar);
    assertTrue(filter.getStatement().trim().contains("@exo:receivedDate >= xs:dateTime('" + ISO8601.format(calendar) + "')"));
    
    filter.setToDate(calendar);
    assertTrue(filter.getStatement().trim().contains("@exo:receivedDate <= xs:dateTime('" + ISO8601.format(calendar) + "')"));
    
    // guard from NPE
    filter.setFrom(null);
    filter.setTo(null);
    filter.setOrderBy(null);
    filter.setExcludeFolders(new String[] {(String) null});
    filter.setFolder(new String[] {(String) null});
    filter.setTag(new String[] {(String) null});
    filter.setSubject(null);
    filter.setBody(null);
    filter.setFromDate(null);
    filter.setToDate(null);
    filter.getStatement();
    
    
    
  }
}
