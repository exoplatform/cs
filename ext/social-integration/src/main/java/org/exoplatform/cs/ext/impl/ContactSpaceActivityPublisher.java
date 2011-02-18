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
package org.exoplatform.cs.ext.impl;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.impl.ContactEventListener;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 30, 2010  
 */
public class ContactSpaceActivityPublisher extends ContactEventListener{
  private Log LOG = ExoLogger.getLogger(ContactSpaceActivityPublisher.class);
  public static final String CONTACT_APP_ID = "cs-contact:spaces".intern();
  public static final String FULL_NAME_KEY = "FullName".intern();
  public static final String EMAIL_KEY = "EmailContact".intern();
  public static final String JOB_TITLE_KEY = "JobTitle".intern();
  public static final String PHONE_KEY = "Phone".intern();
  public static final String ACTIVITY_TYPE = "ActivityType".intern();
  public static final String CONTACT_ADD = "ContactAdd".intern();
  public static final String CONTACT_UPDATE = "ContactUpdate".intern();
  
  @Override
  public void saveContact(String username, Contact contact) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager") ;
      String addrBookId = contact.getAddressBook()[0];
      if (addrBookId == null || addrBookId.indexOf(ContactDataInitialize.ADDRESSBOOK_ID_PREFIX) < 0) {
        return;
      }
      
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(contact.getFullName());
      activity.setBody("");
      
      Map<String, String> params = new HashMap<String, String>();
      params.put(EMAIL_KEY, contact.getEmailAddress() != null ? contact.getEmailAddress() : "");
      params.put(FULL_NAME_KEY, contact.getFullName());
      params.put(JOB_TITLE_KEY, contact.getJobTitle() != null ? contact.getJobTitle() : "");
      params.put(PHONE_KEY, contact.getMobilePhone() != null ? contact.getMobilePhone() : "");
      params.put(ACTIVITY_TYPE, CONTACT_ADD);
      activity.setTemplateParams(params);
      activity.setType(CONTACT_APP_ID);
      
      
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
      String spaceId = addrBookId.split(ContactDataInitialize.ADDRESSBOOK_ID_PREFIX)[1]; 
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceId, false);
      activityM.recordActivity(spaceIdentity, activity);
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when contact added " +e.getMessage());
    }
    
  }

  @Override
  public void updateContact(String username, Contact contact) {
    try {
      Class.forName("org.exoplatform.social.core.manager.IdentityManager") ;
      String addrBookId = contact.getAddressBook()[0];
      if (addrBookId == null || addrBookId.indexOf(ContactDataInitialize.ADDRESSBOOK_ID_PREFIX) < 0) {
        return;
      }
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle(contact.getFullName());
      activity.setBody("");
      
      Map<String, String> params = new HashMap<String, String>();
      params.put(EMAIL_KEY, contact.getEmailAddress() != null ? contact.getEmailAddress() : "");
      params.put(FULL_NAME_KEY, contact.getFullName());
      params.put(JOB_TITLE_KEY, contact.getJobTitle() != null ? contact.getJobTitle() : "");
      params.put(PHONE_KEY, contact.getMobilePhone() != null ? contact.getMobilePhone() : "");
      params.put(ACTIVITY_TYPE, CONTACT_UPDATE);
      activity.setTemplateParams(params);
      activity.setType(CONTACT_APP_ID);
      
      
      IdentityManager indentityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class); 
      ActivityManager activityM = (ActivityManager) PortalContainer.getInstance().getComponentInstanceOfType(ActivityManager.class);
      String spaceId = addrBookId.split(ContactDataInitialize.ADDRESSBOOK_ID_PREFIX)[1]; 
      Identity spaceIdentity = indentityM.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceId, false);
      activityM.recordActivity(spaceIdentity, activity);
    } catch (Exception e) {
      LOG.error("Can not record Activity for space when contact updated " +e.getMessage());
    }
    
  }

}
