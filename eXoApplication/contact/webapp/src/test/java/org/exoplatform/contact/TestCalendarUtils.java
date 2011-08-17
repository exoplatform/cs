/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.contact;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 9, 2011  
 */
public class TestCalendarUtils extends TestCase {

  public TestCalendarUtils() {
    super();
  }

  public void testIsValidEmailAddresses() {
    // for value null
    String value = null;
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value is empty
    value = "";
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value is one text
    value = "test";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value is missing extension
    value = "test@";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value is missing extension
    value = "test@test";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value is content strange characters
    value = "test.test@test&.com";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value is content strange characters
    value = "test.test@test.com#";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value is content strange characters
    value = "test.test#ssss@test.com";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value is content strange characters
    value = "testemail[]ssss@test.com.com";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value is content double @
    value = "testemail@test@test.com";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value has last extension very long - more than 6 characters
    value = "testemail@test.comlastlong";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value is good email address.
    value = "test-test@test.com";
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value is good email address.
    value = "test.test@test.com";
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value is good email address.
    value = "testemail@test.com.com.com";
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value has longer fist extension
    value = "jean-christophe.lastname@adressetroplongue.fr";
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value is very longer email address
    value = "jean-christophe.lastname.long.text.more@adressetroplongue.fr";
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value content some good email address
    value = "jean@test.uk, testemail2@kkk.com, emailtest.exo@exo.com";
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value content some good email address and slip by ; and ,
    value = "jean@test.uk; testemail2@kkk.com, emailtest.exo@exo.com; testtest@test.test";
    assertEquals(true, CalendarUtils.isValidEmailAddresses(value));
    // for value content some email address but has email content strange characters.
    value = "jean@test.uk, tes#temail2@kkk.com, emailtest.exo@exo.com; testtest@test.test";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value content some email address but has email missing extension.
    value = "jean@test.uk, root@localhost, emailtest.exo@exo.com; testtest@test.test";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
    // for value content some email address but has email longer extension - more than 6 characters.
    value = "jean@test.uk, root@localhost.localhost, emailtest.exo@exo.com; testtest@test.test";
    assertEquals(false, CalendarUtils.isValidEmailAddresses(value));
  }

}
