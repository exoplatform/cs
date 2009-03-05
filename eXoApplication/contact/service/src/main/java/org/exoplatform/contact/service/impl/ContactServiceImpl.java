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
package org.exoplatform.contact.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactImportExport;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Jul 11, 2007
 */
public class ContactServiceImpl implements ContactService {

	final private static String VCARD = "x-vcard".intern();

	private JCRDataStorage storage_;
	private Map<String, ContactImportExport> contactImportExport_ = new HashMap<String, ContactImportExport>();

	private static final Log log = ExoLogger
			.getLogger(ContactServiceImpl.class);

	public ContactServiceImpl(NodeHierarchyCreator nodeHierarchyCreator)
			throws Exception {
		storage_ = new JCRDataStorage(nodeHierarchyCreator);

		contactImportExport_.put(VCARD, new VCardImportExport(storage_));
	}

	public List<Contact> getAllContact(SessionProvider sProvider,
			String username) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getAllContact(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Contact getPersonalContact(String userId) throws Exception {
		return storage_.getPersonalContact(userId);
	}

	/*
	 * public ContactPageList getContactPageListByTag(String username,
	 * ContactFilter filter) throws Exception { return
	 * storage_.getContactPageListByTag(username, filter); }
	 */

	public ContactPageList getContactPageListByGroup(SessionProvider sProvider,
			String username, String groupId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getContactPageListByGroup(sProvider, username,
					groupId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ContactPageList getContactPageListByGroupSys(String username,
			String groupId) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getContactPageListByGroup(sProvider, username,
					groupId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ContactPageList getContactPageListByGroup(SessionProvider sProvider,
			String username, ContactFilter filter, String type)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getContactPageListByGroup(sProvider, username,
					filter, type);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Map<String, String> searchEmails(SessionProvider sProvider,
			String username, ContactFilter filter) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.searchEmails(sProvider, username, filter);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<String> getAllEmailAddressByGroup(SessionProvider sProvider,
			String username, String groupId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getAllEmailAddressByGroup(sProvider, username,
					groupId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Contact getContact(SessionProvider sProvider, String username,
			String contactId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getContact(sProvider, username, contactId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveContact(SessionProvider sProvider, String username,
			Contact contact, boolean isNew) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.saveContact(sProvider, username, contact, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveContactSys(String username, Contact contact, boolean isNew)
			throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveContact(sProvider, username, contact, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Contact> removeContacts(SessionProvider sProvider,
			String username, List<String> contactIds) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.removeContacts(sProvider, username, contactIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Contact> removeContactsSys(
			String username, List<String> contactIds) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.removeContacts(sProvider, username, contactIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	public void moveContacts(SessionProvider sProvider, String username,
			List<Contact> contacts, String addressType) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.moveContacts(sProvider, username, contacts, addressType);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void addGroupToPersonalContact(String userId, String groupId)
			throws Exception {
		storage_.addGroupToPersonalContact(userId, groupId);
	}

	public List<ContactGroup> getGroups(SessionProvider sProvider,
			String username) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getGroups(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ContactGroup getGroup(SessionProvider sProvider, String username,
			String groupId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getGroup(sProvider, username, groupId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveGroup(SessionProvider sProvider, String username,
			ContactGroup group, boolean isNew) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.saveGroup(sProvider, username, group, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveGroupSys(String username,
			ContactGroup group, boolean isNew) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.saveGroup(sProvider, username, group, isNew);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	
	public ContactGroup removeGroup(SessionProvider sProvider, String username,
			String groupId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.removeGroup(sProvider, username, groupId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	/*
	 * public List<GroupContactData> getPublicContacts(SessionProvider
	 * sProvider, String[] groupIds) throws Exception { return
	 * storage_.getPublicContacts(sProvider, groupIds); }
	 */

	/*
	 * public List<Contact> shareContacts(SessionProvider sProvider, String
	 * username, List<String> contactIds, String[] groupIds) throws Exception {
	 * return storage_.shareContacts(sProvider, username, contactIds, groupIds)
	 * ; }
	 */

	public void removeUserShareContact(SessionProvider sProvider,
			String username, String contactId, String removedUser)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removeUserShareContact(sProvider, username, contactId,
					removedUser);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void shareContact(SessionProvider sProvider, String username,
			String[] contactIds, List<String> receiveUsers) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_
					.shareContact(sProvider, username, contactIds, receiveUsers);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void shareContactSys( String username,
			String[] contactIds, List<String> receiveUsers) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_
					.shareContact(sProvider, username, contactIds, receiveUsers);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	
	public DataPageList getSharedContacts(String username) throws Exception {
		return storage_.getSharedContacts(username);
	}

	public void shareAddressBook(SessionProvider sProvider, String username,
			String addressBookId, List<String> receiverUsers) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.shareAddressBook(sProvider, username, addressBookId,
					receiverUsers);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void removeUserShareAddressBook(SessionProvider sProvider,
			String username, String addressBookId, String removedUser)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.removeUserShareAddressBook(sProvider, username,
					addressBookId, removedUser);
		} finally {
			closeSessionProvider(sProvider);
		}
	}
	
	public void removeUserShareAddressBookSys(
			String username, String addressBookId, String removedUser)
			throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removeUserShareAddressBook(sProvider, username,
					addressBookId, removedUser);
		} finally {
			closeSessionProvider(sProvider);
		}
	}
	
	public List<SharedAddressBook> getSharedAddressBooks(
			SessionProvider sProvider, String username) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getSharedAddressBooks(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ContactPageList getSharedContactsByAddressBook(
			SessionProvider sProvider, String username,
			SharedAddressBook addressBook) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getSharedContactsByAddressBook(sProvider, username,
					addressBook);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ContactPageList getSharedContactsByAddressBookSys(String username,
			SharedAddressBook addressBook) throws Exception {

		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getSharedContactsByAddressBook(sProvider, username,
					addressBook);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	
	/*
	 * public void removeSharedAddressBook(SessionProvider sProvider, String
	 * username, String addressBookId) throws Exception {
	 * storage_.removeSharedAddressBook(sProvider, username, addressBookId) ; }
	 */

	public void removeSharedContact(SessionProvider sProvider, String username,
			String addressBookId, String contactId) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removeSharedContact(sProvider, username, addressBookId,
					contactId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void saveContactToSharedAddressBook(String username,
			String addressBookId, Contact contact, boolean isNew)
			throws Exception {
		storage_.saveContactToSharedAddressBook(username, addressBookId,
				contact, isNew);
	}

	public Contact getSharedContactAddressBook(String username, String contactId)
			throws Exception {
		return storage_.getSharedContactAddressBook(username, contactId);
	}

	public void saveSharedContact(String username, Contact contact)
			throws Exception {
		storage_.saveSharedContact(username, contact);
	}

	public Contact getSharedContact(SessionProvider sProvider, String username,
			String contactId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getSharedContact(sProvider, username, contactId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}
	
	public Contact getSharedContactSys(String username,
			String contactId) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getSharedContact(sProvider, username, contactId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	

	public Contact getPublicContact(String contactId) throws Exception {
		return storage_.getPublicContact(contactId);
	}

	public List<GroupContactData> getPublicContacts(SessionProvider sProvider,
			String[] groupIds) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPublicContacts(sProvider, groupIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<String> getPublicAddressBookContacts(SessionProvider sProvider,
			String[] groupIds) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPublicAddressBookContacts(sProvider, groupIds);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	/*
	 * public Contact removePublicContact(SessionProvider sProvider, String
	 * contactId) throws Exception { return
	 * storage_.removePublicContact(sProvider, contactId); }
	 */
	/*
	 * public void savePublicContact(Contact contact, boolean isNew) throws
	 * Exception { storage_.savePublicContact(contact, isNew); }
	 */
	public Tag getTag(SessionProvider sProvider, String username, String tagName)
			throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getTag(sProvider, username, tagName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public List<Tag> getTags(SessionProvider sProvider, String username)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getTags(sProvider, username);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public DataPageList getContactPageListByTag(SessionProvider sProvider,
			String username, String tagName) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getContactPageListByTag(sProvider, username,
					tagName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public DataPageList getContactPageListByTagSys(
			String username, String tagName) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getContactPageListByTag(sProvider, username,
					tagName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	
	public void addTag(SessionProvider sProvider, String username,
			List<String> contactIds, List<Tag> tags) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.addTag(sProvider, username, contactIds, tags);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void addTag(SessionProvider sProvider, String username,
			List<String> contactIds, String tagId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.addTag(sProvider, username, contactIds, tagId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public Tag removeTag(SessionProvider sProvider, String username,
			String tagName) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.removeTag(sProvider, username, tagName);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void updateTag(SessionProvider sProvider, String username, Tag tag)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.updateTag(sProvider, username, tag);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void removeContactTag(SessionProvider sProvider, String username,
			List<String> contactIds, List<String> tags) throws Exception {
		try {
			sProvider = resetSystemProvider(sProvider);
			storage_.removeContactTag(sProvider, username, contactIds, tags);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ContactPageList getPublicContactsByAddressBook(
			SessionProvider sProvider, String groupId) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.getPublicContactsByAddressBook(sProvider, groupId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ContactPageList getPublicContactsByAddressBookSys( String groupId) throws Exception {

		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.getPublicContactsByAddressBook(sProvider, groupId);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	
	
	public void pasteAddressBook(SessionProvider sProvider, String username,
			String srcAddress, String srcType, String destAddress,
			String destType) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.pasteAddressBook(sProvider, username, srcAddress, srcType,
					destAddress, destType);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public void pasteContacts(SessionProvider sProvider, String username,
			String destAddress, String destType, List<Contact> contacts)
			throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			storage_.pasteContacts(sProvider, username, destAddress, destType,
					contacts);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public ContactImportExport getContactImportExports(String type) {
		return contactImportExport_.get(type);
	}

	public String[] getImportExportType() throws Exception {
		return contactImportExport_.keySet().toArray(new String[] {});
	}

	public DataPageList searchContact(SessionProvider sProvider,
			String username, ContactFilter filter) throws Exception {
		try {
			sProvider = resetProvider(sProvider);
			return storage_.searchContact(sProvider, username, filter);
		} finally {
			closeSessionProvider(sProvider);
		}
	}

	public DataPageList searchContactSys(
			String username, ContactFilter filter) throws Exception {
		SessionProvider sProvider = null;
		try {
			sProvider = resetSystemProvider(sProvider);
			return storage_.searchContact(sProvider, username, filter);
		} finally {
			closeSessionProvider(sProvider);
		}
	}	

	public ContactGroup getSharedGroup(String username, String groupId)
			throws Exception {
		return storage_.getSharedGroup(username, groupId);
	}

	public List<String> getAllEmailBySharedGroup(String username,
			String addressBookId) throws Exception {
		return storage_.getAllEmailBySharedGroup(username, addressBookId);
	}

	public List<String> getAllEmailByPublicGroup(String username, String groupId)
			throws Exception {
		return storage_.getAllEmailByPublicGroup(username, groupId);
	}

	/**
	 * close and create a new SessionProvider
	 * 
	 * @param provider
	 * @return
	 */
	private SessionProvider resetProvider(SessionProvider provider) {
		closeSessionProvider(provider);
		return createSessionProvider();
	}

	private SessionProvider resetSystemProvider(SessionProvider provider) {
		closeSessionProvider(provider);
		return createSystemProvider();
	}

	private SessionProvider createSystemProvider() {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		SessionProviderService service = (SessionProviderService) container
				.getComponentInstanceOfType(SessionProviderService.class);
		return service.getSystemSessionProvider(null);
	}

	/**
	 * Create a session provider for current context. The method first try to
	 * get a normal session provider, then attempts to create a system provider
	 * if the first one was not available.
	 * 
	 * @return a SessionProvider initialized by current SessionProviderService
	 * @see SessionProviderService#getSessionProvider(null)
	 */
	private SessionProvider createSessionProvider() {
		ExoContainer container = ExoContainerContext.getCurrentContainer();
		SessionProviderService service = (SessionProviderService) container
				.getComponentInstanceOfType(SessionProviderService.class);
		SessionProvider provider = service.getSessionProvider(null);
		if (provider == null) {
			log
					.info("No user session provider was available, trying to use a system session provider");
			provider = service.getSystemSessionProvider(null);
		}
		return provider;
	}

	/**
	 * Safely closes JCR session provider. Call this method in finally to clean
	 * any provider initialized by createSessionProvider()
	 * 
	 * @param sessionProvider
	 *            the sessionProvider to close
	 * @see SessionProvider#close();
	 */
	private void closeSessionProvider(SessionProvider sessionProvider) {
		if (sessionProvider != null) {
			sessionProvider.close();
		}
	}

}
