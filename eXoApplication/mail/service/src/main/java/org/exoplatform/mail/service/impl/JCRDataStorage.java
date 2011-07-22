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
package org.exoplatform.mail.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.mail.BodyPart;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMultipart;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Attachment;
import org.exoplatform.mail.service.BufferAttachment;
import org.exoplatform.mail.service.DataStorage;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.Info;
import org.exoplatform.mail.service.JCRMessageAttachment;
import org.exoplatform.mail.service.MailSetting;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.MessagePageList;
import org.exoplatform.mail.service.MimeMessageParser;
import org.exoplatform.mail.service.SpamFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * Created by The eXo Platform SARL Author : Tuan Nguyen
 * tuan.nguyen@exoplatform.com Jun 23, 2007
 */
public class JCRDataStorage implements DataStorage {
  private static final Log     logger        = ExoLogger.getLogger("cs.mail.service");

  private NodeHierarchyCreator nodeHierarchyCreator_;

  private RepositoryService    repoService_;

  private static final String  MAIL_SERVICE  = "MailApplication";

  private static final String  MAIL_DELEGATE = "DelegationAccount";

  public JCRDataStorage(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService repoService) {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
    repoService_ = repoService;
  }

  public String getMailHierarchyNode() throws Exception {
    return nodeHierarchyCreator_.getJcrPath("usersPath");
  }

  public Node getMailHomeNode(SessionProvider sProvider, String username) throws Exception {
    if (sProvider == null)
      sProvider = createSystemProvider();
    Node userApp = getNodeByPath(nodeHierarchyCreator_.getUserApplicationNode(sProvider, username).getPath(), sProvider);
    Node mailNode = null;
    try {
      mailNode = userApp.getNode(MAIL_SERVICE);
    } catch (PathNotFoundException e) {
      mailNode = userApp.addNode(MAIL_SERVICE, Utils.NT_UNSTRUCTURED);
      if (userApp.isNew())
        userApp.getSession().save();
      else
        userApp.save();
    }
    return mailNode;
  }

  private Node getDelegationHomeNode() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node publicApp = getNodeByPath(nodeHierarchyCreator_.getPublicApplicationNode(sProvider).getPath(), sProvider);
    Node node = null;
    try {
      node = publicApp.getNode(MAIL_DELEGATE);
    } catch (PathNotFoundException e) {
      node = publicApp.addNode(MAIL_DELEGATE, NT_UNSTRUCTURED);
      publicApp.getSession().save();
    } finally {
      sProvider.close();
    }
    return node;
  }

  public void delegateAccount(String username, String reciver, String accountId) throws Exception {

    Node tempNode = null;
    SessionProvider sProvider = createSessionProvider();
    try {
      Node accountNode = getMailHomeNode(sProvider, username).getNode(accountId);
      Value[] values = {};
      if (accountNode.isNodeType(EXO_SHARED_MIXIN))
        values = accountNode.getProperty(EXO_SHARED_ID).getValues();
      else
        accountNode.addMixin(EXO_SHARED_MIXIN);
      List<Value> valueList = new ArrayList<Value>();
      for (Value v : values) {
        valueList.add(v);
      }
      try {
        tempNode = getDelegationHomeNode().getNode(reciver);
      } catch (PathNotFoundException e) {
        tempNode = getDelegationHomeNode().addNode(reciver, NT_UNSTRUCTURED);
        if (tempNode.canAddMixin(MIX_REFERENCEABLE))
          tempNode.addMixin(MIX_REFERENCEABLE);
        tempNode.getSession().save();
      }
      // Check delegated or not
      boolean isExist = false;
      isExist = false;
      for (Value v : values) {
        Node refNode = tempNode.getSession().getNodeByUUID(v.getString());
        if (refNode.getPath().equals(tempNode.getPath())) {
          isExist = true;
          break;
        }
      }
      if (!isExist) {
        Value newValue = accountNode.getSession().getValueFactory().createValue(tempNode);
        valueList.add(newValue);
      }
      if (valueList.size() > 0) {
        accountNode.setProperty(EXO_SHARED_ID, valueList.toArray(new Value[valueList.size()]));
        accountNode.getSession().save();
        getDelegationHomeNode().getSession().save();
      }
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug(" Delegate account error :  " + e.getMessage());

    } finally {
      sProvider.close();
    }

  }

  public List<Account> getDelegateAccounts(String userId) throws Exception {
    List<Account> accList = new ArrayList<Account>();
    if (getDelegationHomeNode().hasNode(userId)) {
      SessionProvider sProvider = createSessionProvider();
      try {
        Node tempNode = getDelegationHomeNode().getNode(userId);
        PropertyIterator iter = tempNode.getReferences();
        while (iter.hasNext()) {
          Node accountNode = iter.nextProperty().getParent();
          Account acc = getAccount(sProvider, accountNode);
          acc.setDelegateFrom(accountNode.getParent().getParent().getParent().getName());
          accList.add(acc);
        }
      } catch (Exception e) {
        if (logger.isDebugEnabled())
          logger.debug(e.getMessage());
      } finally {
        sProvider.close();
      }
    }
    return accList;
  }

  public void removeDelegateAccount(String userId, String accountId) throws Exception {
    if (getDelegationHomeNode().hasNode(userId)) {
      Node tempNode = getDelegationHomeNode().getNode(userId);
      String uuid = tempNode.getProperty(JCR_UUID).getString();
      PropertyIterator iter = tempNode.getReferences();
      while (iter.hasNext()) {
        List<Value> newValues = new ArrayList<Value>();
        Node accountNode = iter.nextProperty().getParent();
        if (!accountNode.getProperty(Utils.EXO_ID).getString().equals(accountId))
          continue;
        Value[] values = accountNode.getProperty(EXO_SHARED_ID).getValues();
        for (Value value : values) {
          if (!value.getString().equals(uuid)) {
            newValues.add(value);
          }
        }
        accountNode.setProperty(EXO_SHARED_ID, newValues.toArray(new Value[newValues.size()]));
        accountNode.save();
      }
    }
  }

  public Account getAccountById(String username, String id) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node mailHome = getMailHomeNode(sProvider, username);
      if (mailHome.hasNode(id)) {
        return getAccount(sProvider, mailHome.getNode(id));
      } else {
        return null;
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public List<Account> getAccounts(String username) throws Exception {
    SessionProvider sProvider = null;
    List<Account> accounts = new ArrayList<Account>();
    try {
      sProvider = createSessionProvider();
      Node homeNode = getMailHomeNode(sProvider, username);
      if (homeNode == null)
        return accounts;
      NodeIterator it = homeNode.getNodes();
      while (it.hasNext()) {
        Node node = it.nextNode();
        if (node.isNodeType("exo:account"))
          accounts.add(getAccount(sProvider, node));
      }
      return accounts;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public Account getAccount(SessionProvider sProvider, Node accountNode) throws Exception {
    Account account = new Account();
    account.setId(accountNode.getProperty(Utils.EXO_ID).getString());
    try {
      account.setLabel(accountNode.getProperty(Utils.EXO_LABEL).getString());
    } catch (Exception e) {
    }
    try {
      account.setUserDisplayName(accountNode.getProperty(Utils.EXO_USERDISPLAYNAME).getString());
    } catch (Exception e) {
    }
    try {
      account.setEmailAddress(accountNode.getProperty(Utils.EXO_EMAILADDRESS).getString());
    } catch (Exception e) {
    }
    try {
      account.setEmailReplyAddress(accountNode.getProperty(Utils.EXO_REPLYEMAIL).getString());
    } catch (Exception e) {
    }
    try {
      account.setSignature(accountNode.getProperty(Utils.EXO_SIGNATURE).getString());
    } catch (Exception e) {
    }
    try {
      account.setDescription(accountNode.getProperty(Utils.EXO_DESCRIPTION).getString());
    } catch (Exception e) {
    }
    try {
      account.setCheckedAuto(accountNode.getProperty(Utils.EXO_CHECKMAILAUTO).getBoolean());
    } catch (Exception e) {
    }
    try {
      account.setIsSavePassword(accountNode.getProperty(Utils.EXO_IS_SAVE_PASSWORD).getBoolean());
    } catch (Exception e) {
    }
    try {
      account.setEmptyTrashWhenExit(accountNode.getProperty(Utils.EXO_EMPTYTRASH).getBoolean());
    } catch (Exception e) {
    }
    try {
      account.setPlaceSignature(accountNode.getProperty(Utils.EXO_PLACESIGNATURE).getString());
    } catch (Exception e) {
    }
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTimeInMillis(accountNode.getProperty(Utils.EXO_LAST_CHECKED_TIME).getLong());
      account.setLastCheckedDate(cal.getTime());
    } catch (Exception e) {
      account.setLastCheckedDate(null);
    }
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTimeInMillis(accountNode.getProperty(Utils.EXO_LAST_START_CHECKING_TIME).getLong());
      account.setLastStartCheckingTime(cal.getTime());
    } catch (Exception e) {
      account.setLastStartCheckingTime(null);
    }
    try {
      account.setCheckAll(accountNode.getProperty(Utils.EXO_CHECK_ALL).getBoolean());
    } catch (Exception e) {
    }
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTimeInMillis(accountNode.getProperty(Utils.EXO_CHECK_FROM_DATE).getLong());
      account.setCheckFromDate(cal.getTime());
    } catch (Exception e) {
    }

    try {
      Value[] properties = accountNode.getProperty(Utils.EXO_SERVERPROPERTIES).getValues();
      for (int i = 0; i < properties.length; i++) {
        String property = properties[i].getString();
        int index = property.indexOf('=');
        if (index != -1)
          account.setServerProperty(property.substring(0, index), property.substring(index + 1));
      }
    } catch (Exception e) {
    }

    try {
      Value[] properties = accountNode.getProperty(Utils.EXO_SMTPSERVERPROPERTIES).getValues();
      for (int i = 0; i < properties.length; i++) {
        String property = properties[i].getString();
        int index = property.indexOf('=');
        if (index != -1)
          account.setSmtpServerProperty(property.substring(0, index), property.substring(index + 1));
      }
    } catch (Exception e) {
    }
    try {
      account.setSecureAuthsIncoming(accountNode.getProperty(Utils.EXO_SECURE_AUTHS_INCOMING).getString());
      account.setSecureAuthsOutgoing(accountNode.getProperty(Utils.EXO_SECURE_AUTHS_OUTGOING).getString());
      account.setAuthMechsIncoming(accountNode.getProperty(Utils.EXO_AUTH_MECHS_INCOMING).getString());
      account.setAuthMechsOutgoing(accountNode.getProperty(Utils.EXO_AUTH_MECHS_OUTGOING).getString());
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug("Not all options of " + account.getLabel() + " get completely.", e);
    }

    if (accountNode.hasProperty(Utils.EXO_PERMISSIONS)) {
      Value[] values = accountNode.getProperty(Utils.EXO_PERMISSIONS).getValues();
      account.setPermissions(valuesToMap(values));
    }

    return account;
  }

  private Map<String, String> valuesToMap(Value[] val) throws Exception {
    Map<String, String> map = new HashMap<String, String>();
    try {
      for (Value v : val) {
        map.put(v.getString().split(":")[0], v.getString().split(":")[1]);
      }
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug("no permission");
    }
    return map;
  }

  private String[] mapToStrings(Map<String, String> map) throws Exception {
    StringBuilder s = new StringBuilder();
    try {
      for (String key : map.keySet()) {
        s.append(key).append(":").append(map.get(key)).append("/");
      }
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug("permission empty");
    }
    return s.toString().split("/");
  }

  public Message getMessageById(String username, String accountId, String msgId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node accountNode = getMailHomeNode(sProvider, username).getNode(accountId);
      StringBuffer queryString = new StringBuffer("/jcr:root" + accountNode.getPath() + "//element(*,exo:message)[@exo:id='").append(msgId).append("']");
      QueryImpl queryImpl = createXPathQuery(sProvider, username, accountId, queryString.toString());
      queryImpl.setOffset(0);
      queryImpl.setLimit(1);
      QueryResult result = queryImpl.execute();
      NodeIterator it = result.getNodes();
      Node node = null;
      if (it.hasNext())
        node = it.nextNode();
      Message msg = getMessage(node);
      return msg;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public QueryImpl createXPathQuery(SessionProvider sProvider, String username, String accountId, String xpath) throws Exception {
    Session sess = getSession(sProvider);
    QueryManager queryManager = sess.getWorkspace().getQueryManager();
    return (QueryImpl) queryManager.createQuery(xpath, Query.XPATH);
  }

  public MailSetting getMailSetting(String username) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node homeNode = getMailHomeNode(sProvider, username);
      Node settingNode = null;
      if (homeNode.hasNode(Utils.KEY_MAIL_SETTING))
        settingNode = homeNode.getNode(Utils.KEY_MAIL_SETTING);
      MailSetting setting = new MailSetting();
      if (settingNode != null) {
        try {
          setting.setNumberMsgPerPage((settingNode.getProperty(Utils.EXO_NUMBER_MSG_PER_PAGE).getLong()));
        } catch (Exception e) {
        }
        try {
          setting.setPeriodCheckAuto((settingNode.getProperty(Utils.EXO_PERIOD_CHECKMAIL_AUTO).getLong()));
        } catch (Exception e) {
        }
        try {
          setting.setDefaultAccount((settingNode.getProperty(Utils.EXO_DEFAULT_ACCOUNT).getString()));
        } catch (Exception e) {
        }
        try {
          setting.setUseWysiwyg(settingNode.getProperty(Utils.EXO_USE_WYSIWYG).getBoolean());
        } catch (Exception e) {
        }
        try {
          setting.setFormatAsOriginal((settingNode.getProperty(Utils.EXO_FORMAT_AS_ORIGINAL).getBoolean()));
        } catch (Exception e) {
        }
        try {
          setting.setReplyWithAttach(settingNode.getProperty(Utils.EXO_REPLY_WITH_ATTACH).getBoolean());
        } catch (Exception e) {
        }
        try {
          setting.setForwardWithAtt(settingNode.getProperty(Utils.EXO_FORWARD_WITH_ATTACH).getBoolean());
        } catch (Exception e) {
        }
        try {
          setting.setPrefixMessageWith((settingNode.getProperty(Utils.EXO_PREFIX_MESSAGE_WITH).getString()));
        } catch (Exception e) {
        }
        try {
          setting.setSaveMessageInSent((settingNode.getProperty(Utils.EXO_SAVE_SENT_MESSAGE).getBoolean()));
        } catch (Exception e) {
        }
        try {
          setting.setLayout((settingNode.getProperty(Utils.EXO_LAYOUT).getLong()));
        } catch (Exception e) {
        }
        try {
          setting.setSendReturnReceipt((settingNode.getProperty(Utils.EXO_RETURN_RECEIPT).getLong()));
        } catch (Exception e) {
        }
      }
      return setting;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public MessagePageList getMessagePageList(String username, MessageFilter filter) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node homeMsg = getMessageHome(sProvider, username, filter.getAccountId());
      filter.setAccountPath(homeMsg.getPath());
      long pageSize = getMailSetting(username).getNumberMsgPerPage();
      return new MessagePageList(pageSize, filter.getStatement(), filter.hasStructure());
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public List<Message> getMessages(String username, MessageFilter filter) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node homeMsg = getMessageHome(sProvider, username, filter.getAccountId());
      filter.setAccountPath(homeMsg.getPath());
      QueryManager qm = getSession(sProvider).getWorkspace().getQueryManager();
      String queryString = filter.getStatement();
      Query query = qm.createQuery(queryString, Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      List<Message> strList = new ArrayList<Message>();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        strList.add(getMessage(node));
      }
      return strList;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public Message getMessage(Node messageNode) throws Exception {
    return Utils.getMessage(messageNode);
  }

  public void removeAccount(String username, String accountId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node homeNode = getMailHomeNode(sProvider, username);
      if (homeNode.hasNode(accountId)) {
        homeNode.getNode(accountId).remove();
        homeNode.getSession().save();
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void removeMessage(String username, String accountId, Message message) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node msgStoreNode = getDateStoreNode(sProvider, username, accountId, message.getReceivedDate());
      try {
        Node node = msgStoreNode.getNode(message.getId());
        if (node != null) {
          node = moveReference(accountId, node);
          NodeType[] nts = node.getMixinNodeTypes();
          for (int i = 0; i < nts.length; i++) {
            node.removeMixin(nts[i].getName());
          }
          node.remove();
          msgStoreNode.save();
        }
      } catch (PathNotFoundException e) {
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void removeMessages(String username, String accountId, List<Message> messages, boolean moveReference) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      for (Message message : messages) {
        Node msgStoreNode = getDateStoreNode(sProvider, username, accountId, message.getReceivedDate());
        try {
          Node node = msgStoreNode.getNode(message.getId());
          if (node != null) {
            if (moveReference)
              node = moveReference(accountId, node);
            NodeType[] nts = node.getMixinNodeTypes();
            // TODO should use for each
            for (int i = 0; i < nts.length; i++) {
              node.removeMixin(nts[i].getName());
            }
            node.remove();
          }
        } catch (PathNotFoundException e) {
        }
        msgStoreNode.save();
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void moveMessages(String username, String accountId, List<Message> msgList, String currentFolderId, String destFolderId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node messageHome = getMessageHome(sProvider, username, accountId);
      Node currentFolderNode = getFolderNodeById(sProvider, username, accountId, currentFolderId);
      Node destFolderNode = getFolderNodeById(sProvider, username, accountId, destFolderId);
      int inUnreadNumber = 0, deUnreadNumber = 0, inTotalMessage = 0, deTotalMessage = 0;
      for (Message msg : msgList) {
        try {
          Node msgNode = (Node) messageHome.getSession().getItem(msg.getPath());
          boolean isUnread = msgNode.getProperty(Utils.EXO_ISUNREAD).getBoolean();
          String sentFolderId = Utils.generateFID(accountId, Utils.FD_SENT, false);
          Value[] propFolders = msgNode.getProperty(Utils.MSG_FOLDERS).getValues();
          boolean moveReference = true;
          String[] folderIds = new String[propFolders.length];
          if (propFolders.length == 1) {
            if (destFolderId.equals(sentFolderId)) {
              folderIds[0] = sentFolderId;
              if (!propFolders[0].getString().equals(sentFolderId)) {
                if (isUnread) {
                  inUnreadNumber++;
                  deUnreadNumber++;
                }
                deTotalMessage++;
                inTotalMessage++;
              } else {
                if (isUnread) {
                  inUnreadNumber++;
                }
                inTotalMessage++;
              }
              moveReference = false;
            } else {
              folderIds[0] = destFolderId;
              if (propFolders[0].getString().equals(sentFolderId)) {
                if (isUnread) {
                  inUnreadNumber++;
                }
                inTotalMessage++;
              } else if (!currentFolderId.equals(destFolderId)) {
                if (isUnread) {
                  inUnreadNumber++;
                  deUnreadNumber++;
                }
                deTotalMessage++;
                inTotalMessage++;
              }
            }
          } else {
            for (int i = 0; i < propFolders.length; i++) {
              String folderId = propFolders[i].getString();
              if (currentFolderId.equals(folderId))
                folderIds[i] = destFolderId;
              else
                folderIds[i] = folderId;
            }
            if (isUnread) {
              inUnreadNumber++;
              deUnreadNumber++;
            }
            deTotalMessage++;
            inTotalMessage++;
          }

          msgNode.setProperty(Utils.MSG_FOLDERS, folderIds);
          msgNode.setProperty(Utils.EXO_UID, msg.getUID());
          if (moveReference)
            msgNode = moveReference(accountId, msgNode);
          msgNode.save();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      try {
        if (currentFolderNode != null)
          currentFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (currentFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() - deUnreadNumber));
        if (destFolderNode != null)
          destFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (destFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() + inUnreadNumber));
      } catch (Exception e) {
        e.printStackTrace();
      }

      try {
        if (currentFolderNode != null)
          currentFolderNode.setProperty(Utils.EXO_TOTALMESSAGE, (currentFolderNode.getProperty(Utils.EXO_TOTALMESSAGE).getLong() - deTotalMessage));
        if (destFolderNode != null)
          destFolderNode.setProperty(Utils.EXO_TOTALMESSAGE, (destFolderNode.getProperty(Utils.EXO_TOTALMESSAGE).getLong() + inTotalMessage));
      } catch (Exception e) {
        e.printStackTrace();
      }
      if (currentFolderNode != null)
        currentFolderNode.save();
      if (destFolderNode != null)
        destFolderNode.save();
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void moveMessage(String username, String accountId, Message msg, String currentFolderId, String destFolderId, boolean updateReference) throws Exception {
    List<Message> msgList = new ArrayList<Message>();
    msgList.add(msg);
    moveMessages(username, accountId, msgList, currentFolderId, destFolderId, updateReference);
  }

  public void moveMessages(String username, String accountId, List<Message> msgList, String currentFolderId, String destFolderId, boolean updateReference) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node messageHome = getMessageHome(sProvider, username, accountId);
      Node currentFolderNode = getFolderNodeById(sProvider, username, accountId, currentFolderId);
      Node destFolderNode = getFolderNodeById(sProvider, username, accountId, destFolderId);
      int inUnreadNumber = 0, inTotalMessage = 0;
      Value[] propFolders;
      String[] folderIds;
      String folderId;
      Node msgNode;
      for (Message msg : msgList) {
        msgNode = (Node) messageHome.getSession().getItem(msg.getPath());
        if (updateReference)
          msgNode = moveReference(accountId, msgNode);
        try {
          Boolean isUnread = msgNode.getProperty(Utils.EXO_ISUNREAD).getBoolean();
          propFolders = msgNode.getProperty(Utils.MSG_FOLDERS).getValues();
          folderIds = new String[propFolders.length];
          for (int i = 0; i < propFolders.length; i++) {
            folderId = propFolders[i].getString();
            if (currentFolderId.equals(folderId))
              folderIds[i] = destFolderId;
            else
              folderIds[i] = folderId;
          }
          msgNode.setProperty(Utils.MSG_FOLDERS, folderIds);
          msgNode.setProperty(Utils.EXO_UID, msg.getUID());
          if (isUnread)
            inUnreadNumber++;
          inTotalMessage++;

          msgNode.save();
        } catch (Exception e) {
        }
      }

      try {
        if (currentFolderNode != null)
          currentFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (currentFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() - inUnreadNumber));
        if (destFolderNode != null)
          destFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (destFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() + inUnreadNumber));
      } catch (Exception e) {
      }

      try {
        if (currentFolderNode != null)
          currentFolderNode.setProperty(Utils.EXO_TOTALMESSAGE, (currentFolderNode.getProperty(Utils.EXO_TOTALMESSAGE).getLong() - inTotalMessage));
        if (destFolderNode != null)
          destFolderNode.setProperty(Utils.EXO_TOTALMESSAGE, (destFolderNode.getProperty(Utils.EXO_TOTALMESSAGE).getLong() + inTotalMessage));
      } catch (Exception e) {
      }
      if (currentFolderNode != null)
        currentFolderNode.save();
      if (destFolderNode != null)
        destFolderNode.save();
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void saveAccount(String username, Account account, boolean isNew) throws Exception {
    SessionProvider sysProvider = null;
    try {
      sysProvider = createSessionProvider();
      // creates or updates an account, depending on the isNew flag
      Node mailHome = getMailHomeNode(sysProvider, username);
      Node newAccount = null;
      String accId = account.getId();
      if (isNew) { // creates the node
        newAccount = mailHome.addNode(accId, Utils.EXO_ACCOUNT);
        mailHome.save();
        newAccount.setProperty(Utils.EXO_ID, accId);
      } else { // gets the specified account
        try {
          newAccount = mailHome.getNode(accId);
        } catch (Exception e) {
          return;
        }
      }
      if (newAccount != null) {
        // add some properties
        newAccount.setProperty(Utils.EXO_LABEL, account.getLabel());
        newAccount.setProperty(Utils.EXO_USERDISPLAYNAME, account.getUserDisplayName());
        newAccount.setProperty(Utils.EXO_EMAILADDRESS, account.getEmailAddress());
        newAccount.setProperty(Utils.EXO_REPLYEMAIL, account.getEmailReplyAddress());
        newAccount.setProperty(Utils.EXO_SIGNATURE, account.getSignature());
        newAccount.setProperty(Utils.EXO_DESCRIPTION, account.getDescription());
        newAccount.setProperty(Utils.EXO_CHECKMAILAUTO, account.checkedAuto());
        newAccount.setProperty(Utils.EXO_IS_SAVE_PASSWORD, account.isSavePassword());
        newAccount.setProperty(Utils.EXO_EMPTYTRASH, account.isEmptyTrashWhenExit());
        newAccount.setProperty(Utils.EXO_PLACESIGNATURE, account.getPlaceSignature());
        if (account.getLastCheckedDate() != null)
          newAccount.setProperty(Utils.EXO_LAST_CHECKED_TIME, account.getLastCheckedDate().getTime());
        else
          newAccount.setProperty(Utils.EXO_LAST_CHECKED_TIME, (Value) null);

        if (account.getLastStartCheckingTime() != null)
          newAccount.setProperty(Utils.EXO_LAST_START_CHECKING_TIME, account.getLastStartCheckingTime().getTime());
        else
          newAccount.setProperty(Utils.EXO_LAST_START_CHECKING_TIME, (Value) null);

        newAccount.setProperty(Utils.EXO_CHECK_ALL, account.isCheckAll());
        if (account.getCheckFromDate() != null)
          newAccount.setProperty(Utils.EXO_CHECK_FROM_DATE, account.getCheckFromDate().getTime());
        else
          newAccount.setProperty(Utils.EXO_CHECK_FROM_DATE, (Value) null);
        Iterator<String> it = account.getServerProperties().keySet().iterator();
        ArrayList<String> values = new ArrayList<String>(account.getServerProperties().size());
        while (it.hasNext()) {
          String key = it.next().toString();
          values.add(key + "=" + account.getServerProperties().get(key));
        }
        newAccount.setProperty(Utils.EXO_SERVERPROPERTIES, values.toArray(new String[account.getServerProperties().size()]));

        if (account.getSmtpServerProperties() != null) {
          it = account.getSmtpServerProperties().keySet().iterator();
          values = new ArrayList<String>(account.getSmtpServerProperties().size());
          while (it.hasNext()) {
            String key = it.next().toString();
            values.add(key + "=" + account.getSmtpServerProperties().get(key));
          }
          newAccount.setProperty(Utils.EXO_SMTPSERVERPROPERTIES, values.toArray(new String[account.getSmtpServerProperties().size()]));
        }
        if (account.isIncomingSsl()) {
          newAccount.setProperty(Utils.EXO_SECURE_AUTHS_INCOMING, account.getSecureAuthsIncoming());
          newAccount.setProperty(Utils.EXO_AUTH_MECHS_INCOMING, account.getAuthMechsIncoming());
        }
        if (account.isOutgoingSsl()) {
          newAccount.setProperty(Utils.EXO_SECURE_AUTHS_OUTGOING, account.getSecureAuthsOutgoing());
          newAccount.setProperty(Utils.EXO_AUTH_MECHS_OUTGOING, account.getAuthMechsOutgoing());
        }
        if (account.getPermissions() != null) {
          newAccount.setProperty(Utils.EXO_PERMISSIONS, mapToStrings(account.getPermissions()));
        }
        if (isNew)
          mailHome.getSession().save();
        else
          mailHome.save();
      }
    } catch (Exception e) {
      if (logger.isDebugEnabled())
        logger.debug(e);
    } finally {
      closeSessionProvider(sysProvider);
    }
  }

  public void saveMailSetting(String username, MailSetting newSetting) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node mailHome = getMailHomeNode(sProvider, username);
      Node settingNode = null;
      try {
        settingNode = mailHome.getNode(Utils.KEY_MAIL_SETTING);
      } catch (PathNotFoundException e) {
        settingNode = mailHome.addNode(Utils.KEY_MAIL_SETTING, Utils.EXO_MAIL_SETTING);
        mailHome.save();
      }

      if (settingNode != null) {
        settingNode.setProperty(Utils.EXO_NUMBER_MSG_PER_PAGE, newSetting.getNumberMsgPerPage());
        settingNode.setProperty(Utils.EXO_PERIOD_CHECKMAIL_AUTO, newSetting.getPeriodCheckAuto());
        settingNode.setProperty(Utils.EXO_DEFAULT_ACCOUNT, newSetting.getDefaultAccount());
        settingNode.setProperty(Utils.EXO_FORMAT_AS_ORIGINAL, newSetting.formatAsOriginal());
        settingNode.setProperty(Utils.EXO_USE_WYSIWYG, newSetting.useWysiwyg());
        settingNode.setProperty(Utils.EXO_REPLY_WITH_ATTACH, newSetting.replyWithAttach());
        settingNode.setProperty(Utils.EXO_FORWARD_WITH_ATTACH, newSetting.forwardWithAtt());
        settingNode.setProperty(Utils.EXO_PREFIX_MESSAGE_WITH, newSetting.getPrefixMessageWith());
        settingNode.setProperty(Utils.EXO_SAVE_SENT_MESSAGE, newSetting.saveMessageInSent());
        settingNode.setProperty(Utils.EXO_LAYOUT, newSetting.getLayout());
        settingNode.setProperty(Utils.EXO_RETURN_RECEIPT, newSetting.getSendReturnReceipt());
        // saves change
        settingNode.save();
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void saveMessage(String username, String accountId, String targetMsgPath, Message message, boolean isNew) throws Exception {
    Node msgNode = saveMessage(username, accountId, message, isNew);
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      if (targetMsgPath != null && !targetMsgPath.equals("")) {
        Node mailHome = getMailHomeNode(sProvider, username);
        Node targetNode = (Node) mailHome.getSession().getItem(targetMsgPath);
        createReference(msgNode, targetNode);
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public Node saveMessage(String username, String accountId, Message message, boolean isNew) throws Exception {
    SessionProvider sProvider = null;
    Node nodeMsg = null;
    try {
      sProvider = createSessionProvider();
      Node mailHome = getMailHomeNode(sProvider, username);
      Node homeMsg = getDateStoreNode(sProvider, username, accountId, message.getReceivedDate());

      if (isNew) { // creates the node
        nodeMsg = homeMsg.addNode(message.getId(), Utils.EXO_MESSAGE);
        homeMsg.save();
      } else { // gets the specified message
        nodeMsg = (Node) mailHome.getSession().getItem(message.getPath());
      }
      if (nodeMsg != null) {
        String from = "", to = "", cc = "", bcc = "";
        nodeMsg.setProperty(Utils.EXO_ID, message.getId());
        nodeMsg.setProperty(Utils.EXO_UID, message.getUID());
        nodeMsg.setProperty(Utils.EXO_IN_REPLY_TO_HEADER, message.getInReplyToHeader());
        nodeMsg.setProperty(Utils.EXO_ACCOUNT, accountId);
        nodeMsg.setProperty(Utils.EXO_PATH, message.getPath());
        if (!Utils.isEmptyField(message.getFrom()))
          from = message.getFrom().replaceAll("\"", "");
        nodeMsg.setProperty(Utils.EXO_FROM, from);
        if (!Utils.isEmptyField(message.getMessageTo()))
          to = message.getMessageTo().replaceAll("\"", "");
        nodeMsg.setProperty(Utils.EXO_TO, to);
        nodeMsg.setProperty(Utils.EXO_SUBJECT, message.getSubject());
        if (!Utils.isEmptyField(message.getMessageCc()))
          cc = message.getMessageCc().replaceAll("\"", "");
        nodeMsg.setProperty(Utils.EXO_CC, cc);
        if (!Utils.isEmptyField(message.getMessageBcc()))
          bcc = message.getMessageBcc().replaceAll("\"", "");
        nodeMsg.setProperty(Utils.EXO_BCC, bcc);
        nodeMsg.setProperty(Utils.EXO_BODY, message.getMessageBody());
        nodeMsg.setProperty(Utils.EXO_REPLYTO, message.getReplyTo());
        nodeMsg.setProperty(Utils.EXO_SIZE, message.getSize());
        nodeMsg.setProperty(Utils.EXO_STAR, message.hasStar());
        nodeMsg.setProperty(Utils.EXO_PRIORITY, message.getPriority());
        nodeMsg.setProperty(Utils.EXO_ISUNREAD, message.isUnread());
        nodeMsg.setProperty(Utils.EXO_IS_ROOT, message.isRootConversation());
        nodeMsg.setProperty(Utils.EXO_CONTENT_TYPE, message.getContentType());
        nodeMsg.setProperty(Utils.ATT_IS_LOADED_PROPERLY, message.attIsLoadedProperly());
        nodeMsg.setProperty(Utils.IS_RETURN_RECEIPT, message.isReturnReceipt());
        if (message.getSendDate() != null)
          nodeMsg.setProperty(Utils.EXO_SENDDATE, message.getSendDate().getTime());
        if (message.getReceivedDate() != null) {
          nodeMsg.setProperty(Utils.EXO_RECEIVEDDATE, message.getReceivedDate().getTime());
          nodeMsg.setProperty(Utils.EXO_LAST_UPDATE_TIME, message.getReceivedDate().getTime());
        }
        String[] tags = message.getTags();
        nodeMsg.setProperty(Utils.EXO_TAGS, tags);
        String[] folders = message.getFolders();
        nodeMsg.setProperty(Utils.MSG_FOLDERS, folders);
        Iterator<String> ith = message.getHeaders().keySet().iterator();
        ArrayList<String> values = new ArrayList<String>(message.getHeaders().size());
        while (ith.hasNext()) {
          String key = ith.next().toString();
          values.add(key + "=" + message.getHeaders().get(key));
        }
        nodeMsg.setProperty(Utils.MSG_HEADERS, values.toArray(new String[message.getHeaders().size()]));

        List<Attachment> attachments = message.getAttachments();
        if (attachments != null && attachments.size() > 0) {
          Iterator<Attachment> it = attachments.iterator();
          boolean makeNewAtt = isNew;
          while (it.hasNext()) {
            Attachment file = it.next();
            Node nodeFile = null;
            Session session = mailHome.getSession();
            try {
              if (!isNew)
                nodeFile = (Node) session.getItem(file.getId());
            } catch (Exception e) {
              makeNewAtt = true;
            }

            if (makeNewAtt) {
              Node attHome = null;
              try {
                attHome = nodeMsg.getNode(Utils.KEY_ATTACHMENT);
              } catch (Exception pne) {
                attHome = nodeMsg.addNode(Utils.KEY_ATTACHMENT, Utils.NT_UNSTRUCTURED);
              }
              nodeFile = attHome.addNode("Attachment" + IdGenerator.generate(), Utils.EXO_MAIL_ATTACHMENT);
              nodeFile.setProperty(Utils.EXO_ATT_NAME, file.getName());
            }

            Node nodeContent = null;
            if (!nodeFile.hasNode(Utils.JCR_CONTENT)) {
              nodeContent = nodeFile.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
            } else {
              nodeContent = nodeFile.getNode(Utils.JCR_CONTENT);
            }
            nodeContent.setProperty(Utils.JCR_MIMETYPE, file.getMimeType());
            nodeContent.setProperty(Utils.JCR_DATA, file.getInputStream());
            nodeContent.setProperty(Utils.JCR_LASTMODIFIED, Calendar.getInstance().getTimeInMillis());
            /*
             * nodeFile.setProperty(Utils.ATT_IS_SHOWN_IN_BODY, false); nodeMsg.setProperty(Utils.EXO_HASATTACH, true);
             */
            nodeFile.setProperty(Utils.ATT_IS_SHOWN_IN_BODY, file.isShownInBody());
            nodeMsg.setProperty(Utils.EXO_HASATTACH, !file.isShownInBody());
          }
        }

        if (nodeMsg.canAddMixin("mix:referenceable"))
          nodeMsg.addMixin("mix:referenceable");
        nodeMsg.setProperty(Utils.EXO_SUBJECT, message.getSubject());
        nodeMsg.setProperty(Utils.IS_LOADED, message.isLoaded());
        nodeMsg.save();
      }

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      closeSessionProvider(sProvider);
    }
    return nodeMsg;
  }

  public boolean saveMessage(String username, String accId, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, boolean saveTotal, String currentUserName) throws Exception {
    return saveMessage(username, accId, msg, folderIds, tagList, spamFilter, null, null, saveTotal, currentUserName);
  }

  // Saving a message without UID
  public boolean saveMessage(String username, String accId, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, Info infoObj, ContinuationService continuation, boolean saveTotal, String currentUserName) throws Exception {
    return saveMessage(username, accId, null, msg, folderIds, tagList, spamFilter, infoObj, continuation, saveTotal, currentUserName);
  }

  public boolean saveMessage(String username, String accId, long msgUID, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, Info infoObj, ContinuationService continuation, boolean saveTotal, String currentUserName) throws Exception {
    long[] messageUID = { msgUID };
    return saveMessage(username, accId, messageUID, msg, folderIds, tagList, spamFilter, infoObj, continuation, saveTotal, currentUserName);
  }

  /**
   *return true if the message has not attachment, false if else
   **/
  @Deprecated
  private boolean checkHasAttachment(javax.mail.Message message) {
    try {
      Object obj = message.getContent();
      if (obj instanceof Multipart) {
        Multipart multipart = (Multipart) obj;
        int partCount = multipart.getCount();
        for (int i = 0; i < partCount; i++) {
          BodyPart part = multipart.getBodyPart(i);
          String disposition = part.getDisposition();
          if (disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT) && !hasContentId(part))
            return true;
        }
      }
    } catch (IOException e) {
      logger.debug("IOException: " + e.getMessage());
    } catch (MessagingException e) {
      logger.debug("MessagingException: " + e.getMessage());
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public boolean saveMessage(String username, String accId, long[] msgUID, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, Info infoObj, ContinuationService continuation, boolean saveTotal, String currentUserName) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      long t1, t2, t4;
      String from;

      String msgId = MimeMessageParser.getMessageId(msg);
      logger.debug("MessageId = " + msgId);
      Calendar gc = MimeMessageParser.getReceivedDate(msg);
      boolean isReadMessage = MimeMessageParser.isSeenMessage(msg);
      boolean isReturnReceipt = MimeMessageParser.requestReturnReceipt(msg);
      String inReplyToHeader = MimeMessageParser.getInReplyToHeader(msg);

      Node msgHomeNode = getDateStoreNode(sProvider, username, accId, gc.getTime());

      t1 = System.currentTimeMillis();
      if (msgHomeNode == null)
        return false;
      try {
        Node msgNode = msgHomeNode.getNode(msgId);
        logger.debug("Check duplicate ......................................");
        String folderId;
        for (int i = 0; i < folderIds.length; i++) {
          folderId = folderIds[i];
          t1 = System.currentTimeMillis();
          byte checkDuplicate = checkDuplicateStatus(sProvider, username, msgHomeNode, accId, msgNode, folderId);

          if (checkDuplicate == Utils.MAIL_DUPLICATE_IN_OTHER_FOLDER) {
            // there is a duplicate but in another folder
            return false;
          }

          if (checkDuplicate == Utils.MAIL_DUPLICATE_IN_SAME_FOLDER) {
            // will "never" come here
            // but we need to make sure ...
            return false;
          }
        }
      } catch (Exception e) {
        if (logger.isDebugEnabled())
          logger.debug(e);
      }

      logger.debug("Saving message to JCR ...");
      t1 = System.currentTimeMillis();
      Node node = null;
      try {
        node = msgHomeNode.addNode(msgId, Utils.EXO_MESSAGE);
      } catch (Exception e) {
        msgId = "Message" + IdGenerator.generate(); // generating
        // another msgId
        logger.debug("The MessageId is NOT GOOD, generated another one = " + msgId);
        node = msgHomeNode.addNode(msgId, Utils.EXO_MESSAGE);
      }
      try {
        msgHomeNode.save();
        node.setProperty(Utils.EXO_ID, msgId);
        try {
          String uid = String.valueOf(msgUID[0]);
          /*
           * if (Utils.isEmptyField(uid)) uid = MimeMessageParser.getMD5MsgId(msg);
           */
          node.setProperty(Utils.EXO_UID, uid);
        } catch (Exception e) {
        }
        node.setProperty(Utils.EXO_ACCOUNT, accId);
        from = Utils.decodeText(InternetAddress.toString(msg.getFrom())).replaceAll("\"", "");
        node.setProperty(Utils.EXO_FROM, from);
        node.setProperty(Utils.EXO_TO, getAddresses(msg, javax.mail.Message.RecipientType.TO));
        node.setProperty(Utils.EXO_CC, getAddresses(msg, javax.mail.Message.RecipientType.CC));
        node.setProperty(Utils.EXO_BCC, getAddresses(msg, javax.mail.Message.RecipientType.BCC));
        node.setProperty(Utils.EXO_REPLYTO, Utils.decodeText(InternetAddress.toString(msg.getReplyTo())));

        String subject = msg.getSubject();
        if (!Utils.isEmptyField(subject))
          subject = Utils.decodeText(subject);
        else
          subject = "";
        node.setProperty(Utils.EXO_SUBJECT, subject);

        node.setProperty(Utils.EXO_RECEIVEDDATE, gc);
        node.setProperty(Utils.EXO_LAST_UPDATE_TIME, gc);

        Calendar sc = GregorianCalendar.getInstance();
        if (msg.getSentDate() != null)
          sc.setTime(msg.getSentDate());
        else
          sc = gc;
        node.setProperty(Utils.EXO_SENDDATE, sc);
        if (gc == null)
          node.setProperty(Utils.EXO_LAST_UPDATE_TIME, sc);

        int msgSize = Math.abs(msg.getSize());
        node.setProperty(Utils.EXO_SIZE, msgSize);

        node.setProperty(Utils.EXO_ISUNREAD, !isReadMessage);
        node.setProperty(Utils.EXO_STAR, false);

        if (isReturnReceipt)
          node.setProperty(Utils.IS_RETURN_RECEIPT, true);
        else
          node.setProperty(Utils.IS_RETURN_RECEIPT, false);

        if (spamFilter != null && spamFilter.checkSpam(msg)) {
          folderIds = new String[] { Utils.generateFID(accId, Utils.FD_SPAM, false) };
        }

        node.setProperty(Utils.MSG_FOLDERS, folderIds);

        if (tagList != null && tagList.size() > 0)
          node.setProperty(Utils.EXO_TAGS, tagList.toArray(new String[] {}));

        node.setProperty(Utils.EXO_IN_REPLY_TO_HEADER, inReplyToHeader);

        ArrayList<String> values = new ArrayList<String>();
        Enumeration enu = msg.getAllHeaders();
        while (enu.hasMoreElements()) {
          Header header = (Header) enu.nextElement();
          values.add(header.getName() + "=" + header.getValue());
        }
        node.setProperty(Utils.MSG_HEADERS, values.toArray(new String[] {}));
        long priority = MimeMessageParser.getPriority(msg);
        node.setProperty(Utils.EXO_PRIORITY, priority);
        node.setProperty(Utils.EXO_HASATTACH, false);
        node.save();

        if (infoObj != null && continuation != null)
          setCometdMessage(continuation, infoObj, from, msgId, isReadMessage, subject, Utils.convertSize(msgSize), accId, gc, sc, currentUserName, username);
        if (saveTotal)
          saveTotalMessage(username, accId, msgId, msg, sProvider);

        t4 = System.currentTimeMillis();
        logger.debug("Saved total message to JCR finished : " + (t4 - t1) + " ms");
        logger.debug("Adding message to thread ...");
        t1 = System.currentTimeMillis();
        addMessageToThread(sProvider, username, accId, inReplyToHeader, node);
        t2 = System.currentTimeMillis();
        logger.debug("Added message to thread finished : " + (t2 - t1) + " ms");

        for (int i = 0; i < folderIds.length; i++) {
          increaseFolderItem(sProvider, username, accId, folderIds[i], isReadMessage);
        }

        return true;
      } catch (Exception e) {
        try {
          msgHomeNode.refresh(true);
        } catch (Exception ex) {
          logger.debug(" [WARNING] Can't refresh.");
        }
        logger.debug(" [WARNING] Cancel saving message to JCR.");
        return false;
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public boolean saveTotalMessage(String username, String accId, String msgId, javax.mail.Message msg, SessionProvider sProvider) throws Exception {
    boolean closeProvider = false;
    try {
      if (sProvider == null) {
        sProvider = createSessionProvider();
        closeProvider = true;
      }
      Calendar gc = MimeMessageParser.getReceivedDate(msg);
      Node msgHomeNode = getDateStoreNode(sProvider, username, accId, gc.getTime());
      Node node = null;
      try {
        node = msgHomeNode.getNode(msgId);
      } catch (Exception e) {
      }
      if (node != null) {
        try {
          if (node.getProperty(Utils.IS_LOADED).getBoolean())
            return true;
        } catch (PathNotFoundException e) {
        }
        node.setProperty(Utils.IS_LOADED, true);
        node.save();
      } else {
        return false;
      }
      return true;
    } finally {
      if (closeProvider)
        closeSessionProvider(sProvider);
    }
  }

  public String getAddresses(javax.mail.Message msg, javax.mail.Message.RecipientType type) throws Exception {
    String recipients = "";
    String t = "To";
    if (type.equals(javax.mail.Message.RecipientType.CC))
      t = "Cc";
    else if (type.equals(javax.mail.Message.RecipientType.BCC))
      t = "Bcc";
    try {
      recipients = InternetAddress.toString(msg.getRecipients(type));
    } catch (Exception e) {
      String[] ccs = msg.getHeader(t);
      for (int i = 0; i < ccs.length; i++)
        if (!Utils.isEmptyField(ccs[i]))
          recipients += ccs[i].replaceAll("\"", "") + ",";
    }
    return Utils.decodeText(recipients);
  }

  public void increaseFolderItem(SessionProvider sProvider, String username, String accId, String folderId, boolean isReadMessage) throws Exception {
    try {
      Node node = getFolderNodeById(sProvider, username, accId, folderId);
      if (node != null) {
        if (!isReadMessage) {
          node.setProperty(Utils.EXO_UNREADMESSAGES, node.getProperty(Utils.EXO_UNREADMESSAGES).getLong() + 1);
        }
        node.setProperty(Utils.EXO_TOTALMESSAGE, node.getProperty(Utils.EXO_TOTALMESSAGE).getLong() + 1);
        node.save();
      }
    } catch (PathNotFoundException e) {
    }
  }

  public String getContent(Node node, javax.mail.Message msg) throws Exception {
    StringBuffer strBuffer = new StringBuffer();
    Object obj = msg.getContent();
    if (obj instanceof String) {
      strBuffer.append((String) obj);
    } else if (obj instanceof Multipart) {
      Multipart multipart = (Multipart) obj;
      setMultiPart(multipart, node, strBuffer);
    } else if (obj instanceof InputStream) {
      strBuffer.append("JCDDataStorage(getContent)It is InputStream");
    }

    node.setProperty(Utils.EXO_CONTENT_TYPE, msg.getContentType());
    node.setProperty(Utils.EXO_BODY, strBuffer.toString());
    node.save();
    return strBuffer.toString();
  }

  public StringBuffer setMultiPart(Multipart multipart, Node node, StringBuffer body) {
    try {
      boolean readText = true;
      if (multipart.getContentType().toLowerCase().indexOf("multipart/alternative") > -1) {
        Part bodyPart;
        for (int i = 0; i < multipart.getCount(); i++) {
          bodyPart = multipart.getBodyPart(i);
          if (bodyPart.isMimeType("text/html") || bodyPart.isMimeType("multipart/*") || bodyPart.isMimeType("text/calendar")) {
            body = setPart(bodyPart, node, body);
            readText = false;
          }
        }
      }
      if (readText) {
        int n = multipart.getCount();
        for (int i = 0; i < n; i++) {
          Part part = multipart.getBodyPart(i);
          body = setPart(part, node, body);
        }
      }
    } catch (Exception e) {
    }
    return body;
  }

  public StringBuffer setPart(Part part, Node node, StringBuffer body) {
    try {
      String disposition = part.getDisposition();
      String ct = part.getContentType();
      if (disposition == null) {
        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
          body = appendMessageBody(part, node, body);
        } else if (part.isMimeType("multipart/alternative")) {
          Part bodyPart;
          boolean readText = true;
          MimeMultipart mimeMultiPart = (MimeMultipart) part.getContent();
          for (int i = 0; i < mimeMultiPart.getCount(); i++) {
            bodyPart = mimeMultiPart.getBodyPart(i);
            if (bodyPart.isMimeType("text/html") || bodyPart.isMimeType("multipart/*")) {
              body = setPart(bodyPart, node, body);
              readText = false;
            }
          }
          if (readText) {
            for (int i = 0; i < mimeMultiPart.getCount(); i++) {
              body = setPart(mimeMultiPart.getBodyPart(i), node, body);
            }
          }
        } else if (part.isMimeType("multipart/*")) {
          MimeMultipart mimeMultiPart = (MimeMultipart) part.getContent();
          for (int i = 0; i < mimeMultiPart.getCount(); i++) {
            body = setPart(mimeMultiPart.getBodyPart(i), node, body);
          }
        } else if (part.isMimeType("message/rfc822")) {
          body = getNestedMessageBody(part, node, body);
        }
      } else if (disposition.equalsIgnoreCase(Part.INLINE)) {
        /*
         * this must be presented INLINE, hence inside the body of the message
         */
        if (part.isMimeType("text/plain") || part.isMimeType("text/html")) {
          body = appendMessageBody(part, node, body);
        } else if (part.isMimeType("message/rfc822")) {
          body = getNestedMessageBody(part, node, body);
        }
      }
      if ((disposition != null && disposition.equalsIgnoreCase(Part.ATTACHMENT)) || part.isMimeType("image/*")) {
        Node attHome = null;
        String attId = "";
        try {
          attHome = node.getNode(Utils.KEY_ATTACHMENT);
        } catch (PathNotFoundException e) {
          attHome = node.addNode(Utils.KEY_ATTACHMENT, Utils.NT_UNSTRUCTURED);
        }
        if (part.getHeader("X-Attachment-Id") != null) {
          attId = part.getHeader("X-Attachment-Id")[0].toString();
        } else if (part.getHeader("Content-Id") != null) {
          attId = part.getHeader("Content-Id")[0].toString();
          attId = attId.substring(1, attId.length());
          attId = attId.substring(0, attId.length() - 1);
        } else {
          attId = "Attachment" + IdGenerator.generate();
        }

        if (attHome.hasNode(attId)) {
          return body;
        }
        Node nodeFile = attHome.addNode(attId, Utils.EXO_MAIL_ATTACHMENT);
        Node nodeContent = nodeFile.addNode(Utils.JCR_CONTENT, Utils.NT_RESOURCE);
        if (ct.indexOf(";") > 0) {
          String[] type = ct.split(";");
          nodeContent.setProperty(Utils.JCR_MIMETYPE, type[0]);
        } else {
          nodeContent.setProperty(Utils.JCR_MIMETYPE, ct);
        }
        try {
          if (!Utils.isEmptyField(part.getFileName())) {
            nodeFile.setProperty(Utils.EXO_ATT_NAME, Utils.decodeText(part.getFileName()));
          } else {
            nodeFile.setProperty(Utils.EXO_ATT_NAME, "No name");
          }
        } catch (Exception e) {
          nodeFile.setProperty(Utils.EXO_ATT_NAME, "Corrupted attachment");
        }
        try {
          nodeContent.setProperty(Utils.JCR_DATA, part.getInputStream());
          nodeFile.setProperty(Utils.ATT_IS_LOADED_PROPERLY, true);
          nodeFile.setProperty(Utils.ATT_IS_SHOWN_IN_BODY, false);
        } catch (Exception e) {
          nodeContent.setProperty(Utils.JCR_DATA, new ByteArrayInputStream("".getBytes()));
          nodeFile.setProperty(Utils.ATT_IS_LOADED_PROPERLY, false);
          node.setProperty(Utils.ATT_IS_LOADED_PROPERLY, false);
        }
        nodeContent.setProperty(Utils.JCR_LASTMODIFIED, Calendar.getInstance().getTimeInMillis());
        node.setProperty(Utils.EXO_HASATTACH, true);
      }
    } catch (Exception e) {
      logger.warn(e);
    }
    return body;
  }

  public StringBuffer getNestedMessageBody(Part part, Node node, StringBuffer body) throws Exception {
    try {
      body = setPart((Part) part.getContent(), node, body);
    } catch (ClassCastException e) {
      Object obj = part.getContent();
      if (obj instanceof String) {
        body.append(obj);
      } else if (obj instanceof InputStream) {
        StringBuffer sb = new StringBuffer();
        InputStream is = (InputStream) obj;
        int c;
        while ((c = is.read()) != -1)
          sb.append(c);
        body.append(sb);
      } else if (obj instanceof Multipart) {
        body = setMultiPart((Multipart) obj, node, body);
      } else {
        logger.debug("This is a unknown type.");
      }
    }
    return body;
  }

  public StringBuffer appendMessageBody(Part part, Node node, StringBuffer body) throws Exception {
    StringBuffer messageBody = new StringBuffer();
    InputStream is = part.getInputStream();
    String ct = part.getContentType();
    String charset = "UTF-8";
    if (ct != null) {
      String cs = new ContentType(ct).getParameter("charset");
      boolean convertCharset = true;
      for (int i = 0; i < Utils.NOT_SUPPORTED_CHARSETS.length; i++) {
        if (cs != null && cs.equalsIgnoreCase(Utils.NOT_SUPPORTED_CHARSETS[i])) {
          convertCharset = false;
        }
      }
      if (cs != null && convertCharset) {
        charset = cs;
      }
    }
    BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));
    String inputLine;

    String breakLine = "";
    if (part.isMimeType("text/plain"))
      breakLine = "\n";

    while ((inputLine = reader.readLine()) != null) {
      messageBody.append(inputLine + breakLine);
    }

    if (part.isMimeType("text/plain")) {
      if (body != null && !body.equals("")) {
        body.append("\n").append(Utils.encodeHTML(messageBody.toString()));
      } else {
        body = new StringBuffer(Utils.encodeHTML(messageBody.toString()));
      }
    } else if (part.isMimeType("text/html")) {
      if (body != null && !body.equals("")) {
        body.append("<br>").append(messageBody);
      } else {
        body = messageBody;
      }
    }
    return body;
  }

  public Folder getFolder(String username, String accountId, String folderId) throws Exception {
    SessionProvider sProvider = null;
    Folder folder = null;
    try {
      sProvider = createSessionProvider();
      Node node = getFolderNodeById(sProvider, username, accountId, folderId);
      if (node != null)
        folder = getFolder(node);
    } finally {
      closeSessionProvider(sProvider);
    }
    return folder;
  }

  public String getFolderParentId(String username, String accountId, String folderId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node parentNode = getFolderNodeById(sProvider, username, accountId, folderId).getParent();
      try {
        if (parentNode != null)
          return parentNode.getProperty(Utils.EXO_ID).getString();
        else
          return null;
      } catch (PathNotFoundException e) {
        return null;
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public Node getFolderNodeById(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception {
    Node accountNode = getMailHomeNode(sProvider, username).getNode(accountId);
    Session sess = getSession(sProvider);
    QueryManager qm = sess.getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + accountNode.getPath() + "//element(*,exo:folder)[@exo:id='").append(folderId).append("']");
    QueryImpl query = (QueryImpl) qm.createQuery(queryString.toString(), Query.XPATH);
    query.setOffset(0);
    query.setLimit(1);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    Node node = null;
    if (it.hasNext())
      node = it.nextNode();
    return node;
  }

  public Folder getFolder(Node node) throws Exception {
    Folder folder = new Folder();
    folder.setId(node.getProperty(Utils.EXO_ID).getString());
    folder.setURLName(node.getProperty(Utils.EXO_LABEL).getString());
    folder.setPath(node.getPath());
    folder.setName(node.getProperty(Utils.EXO_NAME).getString());
    folder.setPersonalFolder(node.getProperty(Utils.EXO_PERSONAL).getBoolean());
    folder.setType(node.getProperty(Utils.EXO_FOLDERTYPE).getLong());
    folder.setNumberOfUnreadMessage(node.getProperty(Utils.EXO_UNREADMESSAGES).getLong());
    folder.setTotalMessage(node.getProperty(Utils.EXO_TOTALMESSAGE).getLong());
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTimeInMillis(node.getProperty(Utils.EXO_LAST_CHECKED_TIME).getLong());
      folder.setLastCheckedDate(cal.getTime());
    } catch (Exception e) {
      folder.setLastCheckedDate(null);
    }
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTimeInMillis(node.getProperty(Utils.EXO_LAST_START_CHECKING_TIME).getLong());
      folder.setLastStartCheckingTime(cal.getTime());
    } catch (Exception e) {
      folder.setLastStartCheckingTime(null);
    }
    try {
      GregorianCalendar cal = new GregorianCalendar();
      cal.setTimeInMillis(node.getProperty(Utils.EXO_CHECK_FROM_DATE).getLong());
      folder.setCheckFromDate(cal.getTime());
    } catch (Exception e) {
    }

    return folder;
  }

  public List<Folder> getFolders(String username, String accountId) throws Exception {
    SessionProvider sProvider = null;
    List<Folder> folders = new ArrayList<Folder>();
    try {
      sProvider = createSessionProvider();
      Node folderHomeNode = getFolderHome(sProvider, username, accountId);
      NodeIterator iter = folderHomeNode.getNodes();
      while (iter.hasNext()) {
        Node folder = (Node) iter.next();
        folders.add(getFolder(username, accountId, folder.getName()));
      }
    } finally {
      closeSessionProvider(sProvider);
    }
    return folders;
  }

  public void saveFolder(String username, String accountId, Folder folder) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node home = getFolderHome(sProvider, username, accountId);
      Node myFolder = null;
      Node node = getFolderNodeById(sProvider, username, accountId, folder.getId());
      if (node != null) {
        myFolder = node;
      } else {
        myFolder = home.addNode(folder.getId(), Utils.EXO_FOLDER);
        home.save();
      }
      saveFolderNode(myFolder, folder);
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public boolean isExistFolder(String username, String accountId, String parentId, String folderName) throws Exception {
    SessionProvider sProvider = null;
    boolean isExist = false;
    try {
      sProvider = createSessionProvider();
      Node parentNode;
      if (parentId != null && parentId.trim().length() > 0) {
        parentNode = getFolderNodeById(sProvider, username, accountId, parentId);
      } else {
        parentNode = getFolderHome(sProvider, username, accountId);
      }
      
      if (parentNode == null) {
        return false;
      }
      
      NodeIterator nit = parentNode.getNodes();
      while (nit.hasNext()) {
        Node node = nit.nextNode();
        String fn = node.getProperty(Utils.EXO_NAME).getString();
        if (fn.trim().equalsIgnoreCase(folderName))
          isExist = true;
      }
    } finally {
      closeSessionProvider(sProvider);
    }
    return isExist;
  }

  public void saveFolder(String username, String accountId, String parentId, Folder folder) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node parentNode = getFolderNodeById(sProvider, username, accountId, parentId);
      Node myFolder = null;
      if (parentNode.hasNode(folder.getId())) {
        myFolder = parentNode.getNode(folder.getId());
      } else {
        myFolder = parentNode.addNode(folder.getId(), Utils.EXO_FOLDER);
        parentNode.save();
      }
      saveFolderNode(myFolder, folder);
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void renameFolder(String username, String accountId, String newName, Folder folder) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node myFolder = getFolderNodeById(sProvider, username, accountId, folder.getId());
      if (myFolder != null) {
        myFolder.setProperty(Utils.EXO_NAME, newName);
        myFolder.setProperty(Utils.EXO_LABEL, folder.getURLName());
        myFolder.save();
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void removeFolderInMessages(SessionProvider sProvider, String username, String accountId, List<Node> msgNodes, String folderId) throws Exception {
    for (Node msgNode : msgNodes) {
      try {
        Value[] propFolders = msgNode.getProperty(Utils.MSG_FOLDERS).getValues();
        String[] oldFolderIds = new String[propFolders.length];
        for (int i = 0; i < propFolders.length; i++) {
          oldFolderIds[i] = propFolders[i].getString();
        }
        List<String> folderList = new ArrayList<String>(Arrays.asList(oldFolderIds));
        folderList.remove(folderId);
        msgNode.setProperty(Utils.MSG_FOLDERS, folderList.toArray(new String[folderList.size()]));
        msgNode.save();
      } catch (Exception e) {
      }
    }
  }

  public void removeUserFolder(String username, String accountId, String folderId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      List<Node> msgNodes = getMessageNodeByFolder(sProvider, username, accountId, folderId);
      removeFolderInMessages(sProvider, username, accountId, msgNodes, folderId);

      Node node = getFolderNodeById(sProvider, username, accountId, folderId);
      if (node != null) {
        node.remove();
      }
      node.getSession().save();
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public Node getFilterHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId);
    if (accountHome.hasNode(Utils.KEY_FILTER))
      return accountHome.getNode(Utils.KEY_FILTER);
    else {
      accountHome.addNode(Utils.KEY_FILTER, Utils.NT_UNSTRUCTURED);
      accountHome.save();
    }
    return accountHome.getNode(Utils.KEY_FILTER);
  }

  public List<MessageFilter> getFilters(String username, String accountId) throws Exception {
    SessionProvider sProvider = null;
    List<MessageFilter> filterList = new ArrayList<MessageFilter>();
    try {
      sProvider = createSessionProvider();
      Node filterHomeNode = getFilterHome(sProvider, username, accountId);
      NodeIterator iter = filterHomeNode.getNodes();
      while (iter.hasNext()) {
        Node filterNode = (Node) iter.next();
        MessageFilter filter = getFilter(filterNode, username, accountId);
        filterList.add(filter);
      }
    } finally {
      closeSessionProvider(sProvider);
    }
    return filterList;
  }

  public MessageFilter getFilterById(String username, String accountId, String filterId) throws Exception {
    SessionProvider sProvider = null;
    MessageFilter filter = null;
    try {
      sProvider = createSessionProvider();
      Node filterHomeNode = getFilterHome(sProvider, username, accountId);
      if (filterHomeNode.hasNode(filterId)) {
        Node filterNode = filterHomeNode.getNode(filterId);
        filter = getFilter(filterNode, username, accountId);
      }
    } finally {
      closeSessionProvider(sProvider);
    }
    return filter;
  }

  public void saveFilter(String username, String accountId, MessageFilter filter, boolean applyAll) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node home = getFilterHome(sProvider, username, accountId);
      Node filterNode = null;
      if (home.hasNode(filter.getId())) { // if the filter exists, gets it
        filterNode = home.getNode(filter.getId());
      } else { // if it doesn't exist, creates it
        filterNode = home.addNode(filter.getId(), Utils.EXO_FILTER);
      }
      // sets some properties
      filterNode.setProperty(Utils.EXO_ID, filter.getId());
      filterNode.setProperty(Utils.EXO_NAME, filter.getName());
      filterNode.setProperty(Utils.EXO_FROM, filter.getFrom());
      filterNode.setProperty(Utils.EXO_FROM_CONDITION, (long) filter.getFromCondition());
      filterNode.setProperty(Utils.EXO_TO, filter.getTo());
      filterNode.setProperty(Utils.EXO_TO_CONDITION, (long) filter.getToCondition());
      filterNode.setProperty(Utils.EXO_SUBJECT, filter.getSubject());
      filterNode.setProperty(Utils.EXO_SUBJECT_CONDITION, (long) filter.getSubjectCondition());
      filterNode.setProperty(Utils.EXO_BODY, filter.getBody());
      filterNode.setProperty(Utils.EXO_BODY_CONDITION, (long) filter.getBodyCondition());
      if (!Utils.isEmptyField(filter.getApplyFolder()))
        filterNode.setProperty(Utils.EXO_APPLY_FOLDER, filter.getApplyFolder());
      else
        filterNode.setProperty(Utils.EXO_APPLY_FOLDER, Utils.generateFID(accountId, Utils.FD_INBOX, false));
      filterNode.setProperty(Utils.EXO_APPLY_TAG, filter.getApplyTag());
      filterNode.setProperty(Utils.EXO_KEEP_IN_INBOX, filter.keepInInbox());
      filterNode.setProperty(Utils.EXO_APPLY_FOR_ALL, filter.applyForAll());

      try {
        if (applyAll) {
          runFilter(sProvider, username, accountId, filter);
        }
      } catch (Exception e) {
        return;
      }

      home.getSession().save();
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void runFilter(SessionProvider sProvider, String username, String accountId, MessageFilter filter) throws Exception {
    List<Message> msgList = getMessagePageList(username, filter).getAll(username);
    String applyFolder = filter.getApplyFolder();
    String applyTag = filter.getApplyTag();
    List<Tag> tagList = new ArrayList<Tag>();
    for (Message msg : msgList) {
      Folder folder = getFolder(username, accountId, applyFolder);
      if (folder != null && (msg.getFolders()[0] != applyFolder)) {
        Folder appFolder = getFolder(username, accountId, applyFolder);
        if (appFolder != null)
          moveMessage(username, accountId, msg, msg.getFolders()[0], applyFolder, true);
      }
    }
    if (!Utils.isEmptyField(applyTag)) {
      Tag tag = getTag(username, accountId, applyTag);
      if (tag != null) {
        tagList.add(tag);
        addTag(username, accountId, msgList, tagList);
      }
    }
  }

  public void removeFilter(String username, String accountId, String filterId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node filterHome = getFilterHome(sProvider, username, accountId);
      if (filterHome.hasNode(filterId)) {
        filterHome.getNode(filterId).remove();
      }
      filterHome.getSession().save();
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public Node getMessageHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    sProvider = createSessionProvider();
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId);
    Node msgHome = null;
    try {
      msgHome = accountHome.getNode(Utils.KEY_MESSAGE);
    } catch (PathNotFoundException e) {
      msgHome = accountHome.addNode(Utils.KEY_MESSAGE, Utils.NT_UNSTRUCTURED);
      accountHome.save();
    }
    return msgHome;
  }

  public Node getFolderHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId);
    Node folderHome = null;
    try {
      folderHome = accountHome.getNode(Utils.KEY_FOLDERS);
    } catch (PathNotFoundException e) {
      folderHome = accountHome.addNode(Utils.KEY_FOLDERS, Utils.NT_UNSTRUCTURED);
      accountHome.save();
    }
    return folderHome;
  }

  public Node getTagHome(SessionProvider sProvider, String username, String accountId) throws Exception {
    Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId);
    Node tagHome = null;
    try {
      tagHome = accountHome.getNode(Utils.KEY_TAGS);
    } catch (PathNotFoundException e) {
      tagHome = accountHome.addNode(Utils.KEY_TAGS, Utils.NT_UNSTRUCTURED);
      accountHome.save();
    }
    return tagHome;
  }

  public void addTag(String username, String accountId, Tag tag) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node tagHome = getTagHome(sProvider, username, accountId);
      if (!tagHome.hasNode(tag.getId())) {
        Node tagNode = tagHome.addNode(tag.getId(), Utils.EXO_MAILTAG);
        tagNode.setProperty(Utils.EXO_ID, tag.getId());
        tagNode.setProperty(Utils.EXO_NAME, tag.getName());
        tagNode.setProperty(Utils.EXO_DESCRIPTION, tag.getDescription());
        tagNode.setProperty(Utils.EXO_COLOR, tag.getColor());
        tagHome.save();
      }
    } finally {
      try {
        closeSessionProvider(sProvider);
      } catch (Exception e) {
      }
    }
  }

  public void addTag(String username, String accountId, List<Message> messages, List<Tag> tagList) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Map<String, String> tagMap = new HashMap<String, String>();
      Node tagHome = getTagHome(sProvider, username, accountId);
      for (Tag tag : tagList) {
        if (!tagHome.hasNode(tag.getId())) {
          Node tagNode = tagHome.addNode(tag.getId(), Utils.EXO_MAILTAG);
          tagNode.setProperty(Utils.EXO_ID, tag.getId());
          tagNode.setProperty(Utils.EXO_NAME, tag.getName());
          tagNode.setProperty(Utils.EXO_DESCRIPTION, tag.getDescription());
          tagNode.setProperty(Utils.EXO_COLOR, tag.getColor());
        }
        tagMap.put(tag.getId(), tag.getId());
      }
      tagHome.getSession().save();

      Node mailHome = getMailHomeNode(sProvider, username);
      for (Message message : messages) {
        Map<String, String> messageTagMap = new HashMap<String, String>();
        Node messageNode = (Node) mailHome.getSession().getItem(message.getPath());
        try {
          Value[] values = messageNode.getProperty(Utils.EXO_TAGS).getValues();
          for (Value value : values) {
            messageTagMap.put(value.getString(), value.getString());
          }
        } catch (Exception e) {
        }
        messageTagMap.putAll(tagMap);
        messageNode.setProperty(Utils.EXO_TAGS, messageTagMap.values().toArray(new String[] {}));

        messageNode.save();
      }
    } finally {
      try {
        closeSessionProvider(sProvider);
      } catch (Exception e) {
      }
    }
  }

  public List<Tag> getTags(String username, String accountId) throws Exception {
    SessionProvider sProvider = null;
    List<Tag> tags = new ArrayList<Tag>();
    try {
      sProvider = createSessionProvider();
      Node tagHomeNode = getTagHome(sProvider, username, accountId);
      NodeIterator iter = tagHomeNode.getNodes();
      while (iter.hasNext()) {
        Node tagNode = (Node) iter.next();
        Tag tag = getTag(tagNode);
        tags.add(tag);
      }
    } finally {
      closeSessionProvider(sProvider);
    }
    return tags;
  }

  public Tag getTag(String username, String accountId, String tagId) throws Exception {
    SessionProvider sProvider = null;
    Tag tag = null;
    try {
      sProvider = createSessionProvider();
      Node tagHomeNode = getTagHome(sProvider, username, accountId);
      NodeIterator iter = tagHomeNode.getNodes();
      while (iter.hasNext()) {
        Node tagNode = (Node) iter.next();
        if (tagNode.getProperty(Utils.EXO_ID).getString().equals(tagId)) {
          tag = getTag(tagNode);
        }
      }
    } finally {
      closeSessionProvider(sProvider);
    }
    return tag;
  }

  public void removeTagsInMessages(String username, String accountId, List<Message> msgList, List<String> tagIds) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node mailHome = getMailHomeNode(sProvider, username);
      for (Message msg : msgList) {
        try {
          Node msgNode = (Node) mailHome.getSession().getItem(msg.getPath());
          try {
            Value[] propTags = msgNode.getProperty(Utils.EXO_TAGS).getValues();
            String[] oldTagIds = new String[propTags.length];
            for (int i = 0; i < propTags.length; i++) {
              oldTagIds[i] = propTags[i].getString();
            }
            List<String> tagList = new ArrayList<String>(Arrays.asList(oldTagIds));
            tagList.removeAll(tagIds);
            String[] newTagIds = tagList.toArray(new String[tagList.size()]);
            msgNode.setProperty(Utils.EXO_TAGS, newTagIds);
            msgNode.save();
          } catch (Exception e) {
          }
        } catch (PathNotFoundException e) {
        }
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void removeTag(String username, String accountId, String tagId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      // remove this tag in all messages
      List<Message> listMessage = getMessageByTag(username, accountId, tagId);
      List<String> listTag = new ArrayList<String>();
      listTag.add(tagId);
      removeTagsInMessages(username, accountId, listMessage, listTag);

      // remove tag node
      Node tagHomeNode = getTagHome(sProvider, username, accountId);
      if (tagHomeNode.hasNode(tagId)) {
        tagHomeNode.getNode(tagId).remove();
      }
      tagHomeNode.getSession().save();
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public void updateTag(String username, String accountId, Tag tag) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node tagHome = getTagHome(sProvider, username, accountId);
      if (tagHome.hasNode(tag.getId())) {
        Node tagNode = tagHome.getNode(tag.getId());
        tagNode.setProperty(Utils.EXO_NAME, tag.getName());
        tagNode.setProperty(Utils.EXO_DESCRIPTION, tag.getDescription());
        tagNode.setProperty(Utils.EXO_COLOR, tag.getColor());
      }
      tagHome.save();
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public List<Message> getMessageByTag(String username, String accountId, String tagId) throws Exception {
    SessionProvider sProvider = null;
    List<Message> messages = new ArrayList<Message>();
    try {
      sProvider = createSessionProvider();
      Node accountNode = getMailHomeNode(sProvider, username).getNode(accountId);
      QueryManager qm = getSession(sProvider).getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + accountNode.getPath() + "//element(*,exo:message)[@exo:tags='").append(tagId).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      while (it.hasNext()) {
        Message message = getMessage(it.nextNode());
        messages.add(message);
      }
    } finally {
      closeSessionProvider(sProvider);
    }
    return messages;
  }

  public List<Node> getMessageNodeByFolder(SessionProvider sProvider, String username, String accountId, String folderId) throws Exception {
    List<Node> msgNodes = new ArrayList<Node>();
    Node accountNode = getMailHomeNode(sProvider, username).getNode(accountId);
    QueryManager qm = getSession(sProvider).getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + accountNode.getPath() + "//element(*,exo:message)[@exo:folders='").append(folderId).append("']");
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    NodeIterator it = result.getNodes();
    while (it.hasNext()) {
      msgNodes.add(it.nextNode());
    }
    return msgNodes;
  }

  public Node getSpamFilterHome(String username, String accountId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node accountHome = getMailHomeNode(sProvider, username).getNode(accountId);
      if (accountHome.hasNode(Utils.KEY_SPAM_FILTER))
        return accountHome.getNode(Utils.KEY_SPAM_FILTER);
      else
        return accountHome.addNode(Utils.KEY_SPAM_FILTER, Utils.NT_UNSTRUCTURED);
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public SpamFilter getSpamFilter(String username, String accountId) throws Exception {
    Node accountNode = getSpamFilterHome(username, accountId);
    NodeIterator it = accountNode.getNodes();
    Node spamFilterNode = null;
    while (it.hasNext()) {
      Node node = it.nextNode();
      if (node.isNodeType(Utils.EXO_SPAM_FILTER)) {
        spamFilterNode = node;
        break;
      }
    }
    SpamFilter spamFilter = new SpamFilter();
    if (spamFilterNode != null) {
      try {
        Value[] propFroms = spamFilterNode.getProperty(Utils.EXO_FROMS).getValues();
        String[] froms = new String[propFroms.length];
        for (int i = 0; i < propFroms.length; i++) {
          froms[i] = propFroms[i].getString();
        }
        spamFilter.setSenders(froms);
      } catch (Exception e) {
      }
    }
    return spamFilter;
  }

  public void saveSpamFilter(String username, String accountId, SpamFilter spamFilter) throws Exception {
    Node accountNode = getSpamFilterHome(username, accountId);
    Node spamFilterNode = null;
    if (accountNode.hasNode(Utils.EXO_SPAM_FILTER)) {
      spamFilterNode = accountNode.getNode(Utils.EXO_SPAM_FILTER);
    } else {
      spamFilterNode = accountNode.addNode(Utils.EXO_SPAM_FILTER, Utils.EXO_SPAM_FILTER);
    }

    spamFilterNode.setProperty(Utils.EXO_FROMS, spamFilter.getSenders());
    accountNode.getSession().save();
  }

  public void toggleMessageProperty(String username, String accountId, List<Message> msgList, String property, boolean value) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node mailHome = getMailHomeNode(sProvider, username);
      for (Message msg : msgList) {
        Node msgNode = (Node) mailHome.getSession().getItem(msg.getPath());
        if (property.equals(Utils.EXO_STAR)) {
          msgNode.setProperty(Utils.EXO_STAR, value);
          msgNode.save();
        } else if (property.equals(Utils.EXO_ISUNREAD)) {
          msgNode.setProperty(Utils.EXO_ISUNREAD, value);
          msgNode.save();

          Node currentFolderNode = getFolderNodeById(sProvider, username, accountId, msgNode.getProperty(Utils.MSG_FOLDERS).getValues()[0].getString());
          if (currentFolderNode != null) {
            if (!value) {
              currentFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (currentFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() - 1));
            } else {
              currentFolderNode.setProperty(Utils.EXO_UNREADMESSAGES, (currentFolderNode.getProperty(Utils.EXO_UNREADMESSAGES).getLong() + 1));
            }
            currentFolderNode.save();
          }
        } else {
          msgNode.setProperty(property, !msgNode.getProperty(property).getBoolean());
          msgNode.save();
        }
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public String getFolderHomePath(String username, String accountId) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      return getFolderHome(sProvider, username, accountId).getPath();
    } finally {
      // sProvider.close();
    }
  }

  public List<Folder> getSubFolders(String username, String accountId, String parentPath) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node home = getFolderHome(sProvider, username, accountId);
      Node parentNode = (Node) home.getSession().getItem(parentPath);
      List<Folder> childFolders = new ArrayList<Folder>();
      NodeIterator it = parentNode.getNodes();
      while (it.hasNext()) {
        Node node = it.nextNode();
        if (node.isNodeType(Utils.EXO_FOLDER)) {
          if (node.hasProperty(Utils.EXO_PERSONAL) && node.getProperty(Utils.EXO_PERSONAL).getBoolean())
            childFolders.add(getFolder(node));
        }
      }
      return childFolders;
    } finally {
      // sProvider.close();
    }
  }

  public void execActionFilter(String username, String accountId, Calendar checkTime) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      List<MessageFilter> msgFilters = getFilters(username, accountId);
      Node homeMsg = getMessageHome(sProvider, username, accountId);
      Session sess = getSession(sProvider);
      QueryManager qm = sess.getWorkspace().getQueryManager();
      for (MessageFilter filter : msgFilters) {
        String applyFolder = filter.getApplyFolder();
        String applyTag = filter.getApplyTag();
        filter.setAccountPath(homeMsg.getPath());
        filter.setAccountId(accountId);
        filter.setFromDate(checkTime);
        String queryString = filter.getStatement();
        Query query = qm.createQuery(queryString, Query.XPATH);
        QueryResult result = query.execute();
        NodeIterator it = result.getNodes();
        while (it.hasNext()) {
          Message msg = getMessage(it.nextNode());
          if (!Utils.isEmptyField(applyFolder) && (getFolder(username, accountId, applyFolder) != null)) {
            Folder folder = getFolder(username, accountId, applyFolder);
            if (folder != null)
              moveMessage(username, accountId, msg, msg.getFolders()[0], applyFolder, true);
          }
          if (!Utils.isEmptyField(applyTag)) {
            Tag tag = getTag(username, accountId, applyTag);
            if (tag != null) {
              List<Message> msgList = new ArrayList<Message>();
              msgList.add(msg);
              List<Tag> tagList = new ArrayList<Tag>();
              tagList.add(tag);
              addTag(username, accountId, msgList, tagList);
            }
          }
        }
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public Node getDateStoreNode(SessionProvider sProvider, String username, String accountId, Date date) throws Exception {
    Node msgHome = getMessageHome(sProvider, username, accountId);
    java.util.Calendar calendar = new GregorianCalendar();
    calendar.setTime(date);
    Node yearNode;
    Node monthNode;
    String year = "Y" + String.valueOf(calendar.get(java.util.Calendar.YEAR));
    String month = "M" + String.valueOf(calendar.get(java.util.Calendar.MONTH) + 1);
    String day = "D" + String.valueOf(calendar.get(java.util.Calendar.DATE));
    try {
      yearNode = msgHome.getNode(year);
    } catch (Exception e) {
      yearNode = msgHome.addNode(year, Utils.NT_UNSTRUCTURED);
      msgHome.save();
    }
    try {
      monthNode = yearNode.getNode(month);
    } catch (Exception e) {
      monthNode = yearNode.addNode(month, Utils.NT_UNSTRUCTURED);
      yearNode.save();
    }
    try {
      return monthNode.getNode(day);
    } catch (Exception e) {
      Node dayNode = monthNode.addNode(day, Utils.NT_UNSTRUCTURED);
      monthNode.save();
      return dayNode;
    }
  }

  public List<Node> getMatchingThreadAfter(SessionProvider sProvider, String username, String accountId, Node msg) throws Exception {
    Node accountNode = getMailHomeNode(sProvider, username).getNode(accountId);
    List<Node> converNodes = new ArrayList<Node>();
    try {
      if (msg.getName().equals(msg.getProperty(Utils.EXO_IN_REPLY_TO_HEADER).getString()))
        return null;
      Session sess = getSession(sProvider);
      QueryManager qm = sess.getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + accountNode.getPath() + "//element(*,exo:message)[@exo:inReplyToHeader='").append(msg.getName()).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      while (it.hasNext()) {
        converNodes.add(it.nextNode());
      }
    } catch (Exception e) {
      // Invalid query
    }
    return converNodes;
  }

  public Node getMatchingThreadBefore(SessionProvider sProvider, String username, String accountId, String inReplyToHeader, Node msg) throws Exception {
    Node accountNode = getMailHomeNode(sProvider, username).getNode(accountId);
    Node converNode = null;
    try {
      if (inReplyToHeader.equals(msg.getName()))
        return null;
      Session sess = getSession(sProvider);
      QueryManager qm = sess.getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + accountNode.getPath() + "//element(*,exo:message)[@exo:id='").append(inReplyToHeader).append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      if (it.hasNext())
        converNode = it.nextNode();
    } catch (Exception e) {
      // Invalid query
    }
    return converNode;
  }

  public void addMessageToThread(SessionProvider sProvider, String username, String accountId, String inReplyToHeader, Node msgNode) throws Exception {
    List<Node> converNodeChilds = getMatchingThreadAfter(sProvider, username, accountId, msgNode);
    try {
      msgNode.addMixin("mix:referenceable");
      if (converNodeChilds != null && converNodeChilds.size() > 0) {
        for (Node converChild : converNodeChilds) {
          createReference(converChild, msgNode);
          converChild = setIsRoot(accountId, converChild, msgNode);
          msgNode.setProperty(Utils.EXO_IS_ROOT, true);
          converChild.save();
          msgNode.save();
        }
      } else {
        Node converNodeParent = getMatchingThreadBefore(sProvider, username, accountId, inReplyToHeader, msgNode);
        if (converNodeParent != null && converNodeParent.isNodeType("exo:message")) {
          createReference(msgNode, converNodeParent);
          msgNode = setIsRoot(accountId, msgNode, converNodeParent);
          msgNode.save();
          converNodeParent.save();
          GregorianCalendar cal = new GregorianCalendar();
          cal.setTimeInMillis(msgNode.getProperty(Utils.EXO_LAST_UPDATE_TIME).getLong());
          updateLastTimeToParent(username, accountId, msgNode, converNodeParent, cal);
        } else {
          msgNode.setProperty(Utils.EXO_IS_ROOT, true);
          msgNode.save();
        }
      }
    } catch (Exception e) {
    }
  }

  public void updateLastTimeToParent(String username, String accountId, Node node, Node parentNode, Calendar cal) throws Exception {
    Node grandParent = null;
    if (parentNode != null) {
      parentNode.setProperty(Utils.EXO_LAST_UPDATE_TIME, cal);
      grandParent = getReferentParent(username, accountId, parentNode);
      if (grandParent != null) {
        updateLastTimeToParent(username, accountId, parentNode, grandParent, cal);
      }
      parentNode.save();
    }
  }

  public Node getReferentParent(String username, String accountId, Node node) throws Exception {
    Node parentNode = null;
    try {
      if (node.hasProperty("exo:conversationId")) {
        Value[] currentValues = node.getProperty("exo:conversationId").getValues();
        if (currentValues.length > 0)
          parentNode = node.getSession().getNodeByUUID(currentValues[0].getString());
      }
    } catch (Exception e) {
    }
    return parentNode;
  }

  public Node setIsRoot(String accountId, Node msgNode, Node converNode) throws Exception {
    boolean isRoot = true;
    try {
      Value[] propFoldersMsgNode = msgNode.getProperty(Utils.MSG_FOLDERS).getValues();
      String[] foldersMsgNode = new String[propFoldersMsgNode.length];
      for (int i = 0; i < propFoldersMsgNode.length; i++) {
        foldersMsgNode[i] = propFoldersMsgNode[i].getString();
      }

      Value[] propFoldersConverNode = converNode.getProperty(Utils.MSG_FOLDERS).getValues();
      String[] foldersConverNode = new String[propFoldersConverNode.length];
      for (int i = 0; i < propFoldersConverNode.length; i++) {
        foldersConverNode[i] = propFoldersConverNode[i].getString();
      }

      for (int i = 0; i < foldersMsgNode.length; i++) {
        for (int j = 0; j < foldersConverNode.length; j++) {
          if (foldersConverNode[j].equals(foldersMsgNode[i])) {
            isRoot = false;
          }
        }
      }
    } catch (Exception e) {
    }
    msgNode.setProperty(Utils.EXO_IS_ROOT, isRoot);
    return msgNode;
  }

  public Node setIsRoot(String accountId, Node msgNode) throws Exception {
    Node coverNode;
    PropertyIterator iter = msgNode.getReferences();
    msgNode.setProperty(Utils.EXO_IS_ROOT, true);
    while (iter.hasNext()) {
      coverNode = iter.nextProperty().getParent();
      coverNode = setIsRoot(accountId, coverNode, msgNode);
      coverNode.save();
    }

    Value[] values = {};
    if (msgNode.isNodeType("exo:messageMixin")) {
      values = msgNode.getProperty("exo:conversationId").getValues();
    }
    for (int i = 0; i < values.length; i++) {
      Value value = values[i];
      String uuid = value.getString();
      Node refNode = null;
      try {
        refNode = msgNode.getSession().getNodeByUUID(uuid);
      } catch (ItemNotFoundException e) {
        // do nothing
      }
      if (refNode != null) {
        msgNode = setIsRoot(accountId, msgNode, refNode);
        if (!msgNode.getProperty(Utils.EXO_IS_ROOT).getBoolean()) {
          refNode.save();
          break;
        }
      }
    }
    return msgNode;
  }

  public void createReference(Node msgNode, Node converNode) throws Exception {
    Value[] values = {};
    if (msgNode.isNodeType("exo:messageMixin")) {
      values = msgNode.getProperty("exo:conversationId").getValues();
    } else {
      msgNode.addMixin("exo:messageMixin");
    }
    HashMap<String, Value> valueMap = new HashMap<String, Value>();
    for (Value value : values) {
      valueMap.put(value.getString(), value);
    }
    valueMap.put(converNode.getUUID(), msgNode.getSession().getValueFactory().createValue(converNode));
    List<Value> valueList = new ArrayList<Value>(valueMap.values());
    if (valueList.size() > 0) {
      msgNode.setProperty("exo:conversationId", valueList.toArray(new Value[valueList.size()]));
      msgNode.save();
    }
  }

  /*
   * Move reference : to first parent if it is exist, if not move reference to first child message.
   */
  public Node moveReference(String accountId, Node node) throws Exception {
    List<Value> valueList = new ArrayList<Value>();
    Value[] values = {};
    PropertyIterator iter = node.getReferences();
    Node msgNode;
    Node firstNode = null;
    if (iter != null) {
      while (iter.hasNext()) {
        msgNode = iter.nextProperty().getParent();
        if (msgNode.isNodeType("exo:messageMixin")) {
          values = msgNode.getProperty("exo:conversationId").getValues();

          for (int i = 0; i < values.length; i++)
            valueList.add(values[i]);

          Node parentNode = null;
          try {
            if (node.hasProperty("exo:conversationId")) {
              Value[] currentValues = node.getProperty("exo:conversationId").getValues();
              // TODO: get parent have the same folder with child
              // message
              if (currentValues.length > 0) {
                parentNode = node.getSession().getNodeByUUID(currentValues[0].getString());
              }
            }
          } catch (Exception e) {
          }

          if (parentNode != null) {
            valueList.add(msgNode.getSession().getValueFactory().createValue(parentNode));
          } else if (firstNode != null) {
            valueList.add(msgNode.getSession().getValueFactory().createValue(firstNode));
          }

          if (firstNode == null)
            firstNode = msgNode;

          msgNode.setProperty("exo:conversationId", valueList.toArray(new Value[valueList.size()]));
          msgNode.save();
        }
      }

      node = setIsRoot(accountId, node);
    }
    return node;
  }

  public List<Message> getReferencedMessages(String username, String accountId, String msgPath) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node mailHome = getMailHomeNode(sProvider, username);
      List<Message> msgList = new ArrayList<Message>();
      Node converNode = (Node) mailHome.getSession().getItem(msgPath);
      PropertyIterator iter = converNode.getReferences();
      Node msgNode;
      while (iter.hasNext()) {
        msgNode = iter.nextProperty().getParent();
        msgList.add(getMessage(msgNode));
      }
      return msgList;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  private Map<String, JCRMessageAttachment> getAttachments(Node messageNode) throws Exception {
    Map<String, JCRMessageAttachment> attachments = new HashMap<String, JCRMessageAttachment>();
    if (messageNode.hasNode(Utils.KEY_ATTACHMENT)) {
      NodeIterator msgAttachmentIt = messageNode.getNode(Utils.KEY_ATTACHMENT).getNodes();
      while (msgAttachmentIt.hasNext()) {
        Node node = msgAttachmentIt.nextNode();
        if (node.isNodeType(Utils.EXO_MAIL_ATTACHMENT)) {
          JCRMessageAttachment file = Utils.getJCRMessageAttachment(node);
          attachments.put(file.getId(), file);
        }
      }
    }
    return attachments;
  }

  /**
   * recheck attachments of mail message are in body or attached file.
   * It will update attachments, save changes to jcr and re-set 'attachments' value for message object 'msg'.
   * @param messageNode JCR node of message
   * @param msg - message object (must include body text)
   * @throws Exception
   */
  private Message reCheckAttachment(Node messageNode, Message msg) throws Exception {
    String body = msg.getMessageBody();
    Node attNode = messageNode.getNode(Utils.KEY_ATTACHMENT); // reload attachments node
    Map<String, JCRMessageAttachment> mapOfAtts = getAttachments(messageNode); // get all of attachments. they have not been distinguished between attached file and html resource.
    if (!mapOfAtts.isEmpty()) {
      boolean attachedFile = false;
      Iterator<String> attIdIter = mapOfAtts.keySet().iterator();
      while (attIdIter.hasNext()) {
        String attId = attIdIter.next();
        if (body.indexOf("cid:" + attId.substring(attId.lastIndexOf("/") + 1)) >= 0) {
          Node anAttNode = (Node) attNode.getSession().getItem(attId);
          anAttNode.setProperty(Utils.ATT_IS_SHOWN_IN_BODY, true); // attachment is html resource.
          mapOfAtts.get(attId).setIsShowInBody(true);
        } else {
          attachedFile = true;
        }
      }
      msg.setAttachements(new ArrayList<Attachment>(mapOfAtts.values()));
      if (!attachedFile) {
        messageNode.setProperty(Utils.EXO_HASATTACH, false);
        msg.setHasAttachment(false);
      } else {
        messageNode.setProperty(Utils.EXO_HASATTACH, true);
        msg.setHasAttachment(true);
      }

      messageNode.save();
    }
    return msg;
  }

  public Message loadTotalMessage(String username, String accountId, Message msg, javax.mail.Message message) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      try {
        Node messageNode = getDateStoreNode(sProvider, username, accountId, msg.getReceivedDate()).getNode(msg.getId());
        Node attNode = null;
        try {
          attNode = messageNode.getNode(Utils.KEY_ATTACHMENT);// if the message
          // created Local:
          // Sent Item,
          // Draft, ...
        } catch (Exception ex) {
          logger.trace("[EXO WARNING] Attachment is not existed, loading them from server mail", ex);
        }
        if (attNode == null) {
          String body = this.getContent(messageNode, message);
          msg.setMessageBody(body);
          msg = reCheckAttachment(messageNode, msg);
          return msg;
        }
        Map<String, JCRMessageAttachment> mapOfAtt = getAttachments(messageNode);
        if (!mapOfAtt.isEmpty()) {
          msg.setAttachements(new ArrayList<Attachment>(mapOfAtt.values()));
        }
      } catch (PathNotFoundException e) {
        logger.debug("[EXO WARNING] PathNotFoundException when load attachment");
      }
      return msg;
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  public Message loadTotalMessage(String username, String accountId, Message msg) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node messageNode = getDateStoreNode(sProvider, username, accountId, msg.getReceivedDate()).getNode(msg.getId());

      // setting attachment files
      if (messageNode.hasNode(Utils.KEY_ATTACHMENT)) {
        NodeIterator msgAttachmentIt = messageNode.getNode(Utils.KEY_ATTACHMENT).getNodes();
        List<Attachment> attachments = new ArrayList<Attachment>();
        while (msgAttachmentIt.hasNext()) {
          Node node = msgAttachmentIt.nextNode();
          if (node.isNodeType(Utils.EXO_MAIL_ATTACHMENT)) {
            JCRMessageAttachment file = Utils.getJCRMessageAttachment(node);
            attachments.add(file);
          }
        }
        if (attachments.size() > 0) {
          msg.setAttachements(attachments);
        }
      }
      msg.setMessageBody(messageNode.getProperty(Utils.EXO_BODY).getString());
    } catch (Exception e) {
      logger.trace("Can not load message body on Local", e);
    } finally {
      closeSessionProvider(sProvider);
    }

    return msg;
  }

  /**
   * @param username
   * @param msgHomeNode
   * @param accId
   * @param folderId
   * @param msg
   * @param msgId
   * @return
   */
  public byte checkDuplicateStatus(SessionProvider sProvider, String username, Node msgHomeNode, String accId, Node msgNode, String folderId) {
    byte ret = Utils.NO_MAIL_DUPLICATE;
    try {
      Value[] propFolders = msgNode.getProperty(Utils.MSG_FOLDERS).getValues();
      String[] folders = new String[propFolders.length + 1];
      folders[0] = folderId;

      for (int i = 0; i < propFolders.length; i++) {
        if (propFolders[i].getString().indexOf(folderId) > -1) {
          logger.debug("DUPLICATE MAIL ... removed");
          return Utils.MAIL_DUPLICATE_IN_SAME_FOLDER;
        } else {
          folders[i + 1] = propFolders[i].getString();
        }
      }
      msgNode.setProperty(Utils.EXO_STAR, false);
      msgNode.setProperty(Utils.MSG_FOLDERS, folders);
      msgNode.save();
      increaseFolderItem(sProvider, username, accId, folderId, true);

      logger.warn("DUPLICATE MAIL IN ANOTHER FOLDER ... ");

      ret = Utils.MAIL_DUPLICATE_IN_OTHER_FOLDER;
    } catch (Exception e) {
    }
    return ret;
  }

  public boolean savePOP3Message(String username, String accId, javax.mail.Message msg, String folderIds[], List<String> tagList, SpamFilter spamFilter, Info infoObj, ContinuationService continuation, String currentUserName) throws Exception {
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      long t1, t2, t3, t4;
      String from;
      String msgId = MimeMessageParser.getMessageId(msg);
      logger.debug("MessageId = " + msgId);
      Calendar gc = MimeMessageParser.getReceivedDate(msg);
      Node msgHomeNode = getDateStoreNode(sProvider, username, accId, gc.getTime());
      if (msgHomeNode == null)
        return false;
      try {
        Node msgNode = msgHomeNode.getNode(msgId);
        logger.debug("Check duplicate ......................................");
        // check duplicate
        for (int i = 0; i < folderIds.length; i++) {
          String folderId = folderIds[i];
          byte checkDuplicate = checkDuplicateStatus(sProvider, username, msgHomeNode, accId, msgNode, folderId);
          if (checkDuplicate == Utils.MAIL_DUPLICATE_IN_OTHER_FOLDER) {
            // there is a duplicate but in another folder
            return true;
          }

          if (checkDuplicate == Utils.MAIL_DUPLICATE_IN_SAME_FOLDER) {
            // will "never" come here
            // but we need to make sure ...
            return false;
          }
        }
      } catch (Exception e) {

      }

      logger.debug("Saving message to JCR ...");
      t1 = System.currentTimeMillis();
      Node node = null;
      try {
        node = msgHomeNode.addNode(msgId, Utils.EXO_MESSAGE);
      } catch (Exception e) {
        // generating another msgId
        msgId = "Message" + IdGenerator.generate();
        logger.debug("The MessageId is NOT GOOD, generated another one = " + msgId);
        node = msgHomeNode.addNode(msgId, Utils.EXO_MESSAGE);
      }
      try {
        msgHomeNode.save();
        node.setProperty(Utils.EXO_ID, msgId);
        node.setProperty(Utils.EXO_IN_REPLY_TO_HEADER, MimeMessageParser.getInReplyToHeader(msg));
        node.setProperty(Utils.EXO_ACCOUNT, accId);
        from = Utils.decodeText(InternetAddress.toString(msg.getFrom()));
        node.setProperty(Utils.EXO_FROM, from);

        node.setProperty(Utils.EXO_TO, getAddresses(msg, javax.mail.Message.RecipientType.TO));
        node.setProperty(Utils.EXO_CC, getAddresses(msg, javax.mail.Message.RecipientType.CC));
        node.setProperty(Utils.EXO_BCC, getAddresses(msg, javax.mail.Message.RecipientType.BCC));

        node.setProperty(Utils.EXO_REPLYTO, Utils.decodeText(InternetAddress.toString(msg.getReplyTo())));
        String subject = msg.getSubject();
        if (subject != null)
          subject = Utils.decodeText(msg.getSubject());
        else
          subject = "";
        node.setProperty(Utils.EXO_SUBJECT, subject);
        node.setProperty(Utils.EXO_RECEIVEDDATE, gc);
        node.setProperty(Utils.EXO_LAST_UPDATE_TIME, gc);

        Calendar sc = GregorianCalendar.getInstance();
        if (msg.getSentDate() != null)
          sc.setTime(msg.getSentDate());
        else
          sc = gc;
        node.setProperty(Utils.EXO_SENDDATE, sc);
        if (gc == null)
          node.setProperty(Utils.EXO_LAST_UPDATE_TIME, sc);

        node.setProperty(Utils.EXO_SIZE, Math.abs(msg.getSize()));
        boolean isReadMessage = MimeMessageParser.isSeenMessage(msg);
        node.setProperty(Utils.EXO_ISUNREAD, !isReadMessage);
        node.setProperty(Utils.EXO_STAR, false);

        long priority = MimeMessageParser.getPriority(msg);
        node.setProperty(Utils.EXO_PRIORITY, priority);

        if (MimeMessageParser.requestReturnReceipt(msg))
          node.setProperty(Utils.IS_RETURN_RECEIPT, true);
        else
          node.setProperty(Utils.IS_RETURN_RECEIPT, false);

        if (spamFilter != null && spamFilter.checkSpam(msg)) {
          folderIds = new String[] { Utils.generateFID(accId, Utils.FD_SPAM, false) };
        }
        node.setProperty(Utils.MSG_FOLDERS, folderIds);

        if (tagList != null && tagList.size() > 0)
          node.setProperty(Utils.EXO_TAGS, tagList.toArray(new String[] {}));

        ArrayList<String> values = new ArrayList<String>();
        Enumeration enu = msg.getAllHeaders();
        while (enu.hasMoreElements()) {
          Header header = (Header) enu.nextElement();
          values.add(header.getName() + "=" + header.getValue());
        }
        node.setProperty(Utils.MSG_HEADERS, values.toArray(new String[] {}));

        logger.debug("Saved body and attachment of message .... size : " + Math.abs(msg.getSize()) + " B");
        t2 = System.currentTimeMillis();

        String body = this.getContent(node, msg);
        Message fakeMsg = new Message();
        fakeMsg.setMessageBody(body);
        fakeMsg = reCheckAttachment(node, fakeMsg);

        t3 = System.currentTimeMillis();
        logger.debug("Saved body (and attachments) of message finished : " + (t3 - t2) + " ms");
        node.save();
        if (infoObj != null && continuation != null)
          setCometdMessage(continuation, infoObj, from, msgId, isReadMessage, subject, Utils.convertSize(Math.abs(msg.getSize())), accId, gc, sc, currentUserName, username);
        t4 = System.currentTimeMillis();
        logger.warn("Saved total message to JCR finished : " + (t4 - t1) + " ms");
        logger.debug("Adding message to thread ...");
        t1 = System.currentTimeMillis();
        addMessageToThread(sProvider, username, accId, MimeMessageParser.getInReplyToHeader(msg), node);
        t2 = System.currentTimeMillis();
        logger.debug("Added message to thread finished : " + (t2 - t1) + " ms");

        logger.debug("Updating number message to folder ...");
        t1 = System.currentTimeMillis();

        for (int i = 0; i < folderIds.length; i++) {
          increaseFolderItem(sProvider, username, accId, folderIds[i], isReadMessage);
        }

        t2 = System.currentTimeMillis();
        logger.debug("Updated number message to folder finished : " + (t2 - t1) + " ms");
        return true;

      } catch (Exception e) {
        try {
          msgHomeNode.refresh(true);
        } catch (Exception ex) {
          logger.debug(" [WARNING] Can't refresh.");
        }
        logger.debug(" [WARNING] Cancel saving message to JCR.");
        return false;
      }
    } finally {
      closeSessionProvider(sProvider);
    }
  }

  /**
   * Create a session provider for current context. The method first try to get
   * a normal session provider, then attempts to create a system provider if the
   * first one was not available.
   * 
   * @return a SessionProvider initialized by current SessionProviderService
   * @see SessionProviderService#getSessionProvider(null)
   * @edit on 26/10/2010: using system provider instead.
   */
  public SessionProvider createSessionProvider() {
    SessionProvider provider = SessionProvider.createSystemProvider();
    // ExoContainer container = null;
    // try {
    // container = PortalContainer.getInstance();
    // } catch (IllegalStateException ie) {
    // container = ExoContainerContext.getCurrentContainer();
    // }
    //    
    // SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    // SessionProvider provider = service.getSessionProvider(null);
    // if (provider == null)
    // provider = service.getSystemSessionProvider(null);
    return provider;
  }

  /**
   * Safely closes JCR session provider. Call this method in finally to clean
   * any provider initialized by createSessionProvider()
   * 
   * @param sessionProvider the sessionProvider to close
   * @see SessionProvider#close();
   */
  public void closeSessionProvider(SessionProvider sessionProvider) {
    if (sessionProvider != null) {
      // TODO check this when update to gatein
      // sessionProvider.close();
    }
  }

  public SessionProvider createSystemProvider() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService service = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);
    return service.getSystemSessionProvider(null);
  }

  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    return (Node) getSession(sessionProvider).getItem(nodePath);
  }

  public Session getSession(SessionProvider sprovider) throws Exception {
    ManageableRepository currentRepo = repoService_.getCurrentRepository();
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo);
  }

  public BufferAttachment getAttachmentFromDMS(String userName, String relPath) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node node = nodeHierarchyCreator_.getUserNode(sProvider, userName);

      Node fileNode = node.getNode(relPath);

      if (!fileNode.getPrimaryNodeType().getName().equals("nt:file"))
        return null;
      Node fileContentNode = fileNode.getNode(Utils.JCR_CONTENT);
      BufferAttachment attachFile = new BufferAttachment();
      attachFile.setId("Attachment" + IdGenerator.generate());
      String fileName = fileNode.getName();

      attachFile.setName(fileName);
      attachFile.setInputStream(fileContentNode.getProperty(Utils.JCR_DATA).getStream());
      attachFile.setMimeType(fileContentNode.getProperty(Utils.JCR_MIMETYPE).getString());

      return attachFile;
    } finally {
      sProvider.close();
    }

  }

  public String[] getDMSDataInfo(String userName) throws Exception {
    String[] arr = new String[3];
    RepositoryService service = (RepositoryService) PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);

    arr[0] = service.getCurrentRepository().getConfiguration().getName();

    // service.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node node = nodeHierarchyCreator_.getUserNode(SessionProvider.createSystemProvider(), userName);

      arr[1] = node.getSession().getWorkspace().getName();
      arr[2] = node.getPath();
      return arr;
    } finally {
      sProvider.close();
    }
  }

  public Node getDMSSelectedNode(String userName, String relPath) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node userNode = nodeHierarchyCreator_.getUserNode(sProvider, userName);
    try {
      Node folderNode = userNode.getNode(relPath);
      return folderNode;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * checking a Part whether has IMG tag in body
   * 
   * @return {@link Boolean}
   * @throws IOException
   */
  // TODO fix cs-4403
  private boolean hasIMGTag(Part part) throws Exception {
    try {
      if (part.isMimeType("text/html")) {
        String body = (String) part.getContent();
        if (body != null && body.indexOf("<img") > -1) {
          String imgtag = body.substring(body.indexOf("<img"), body.length());
          if (imgtag.length() > 0)
            imgtag = imgtag.substring(0, imgtag.indexOf(">") + 1);
          if (body != null && body.contains("<img src=") || imgtag.contains("src"))
            return true;
        }
      }
    } catch (MessagingException e) {
      if (logger.isDebugEnabled())
        logger.debug("Cannot analyses a text/html MimeType", e);
    }
    return false;
  }

  /**
   * The attachment in INLINE mode of Content-Disposition header property always
   * has Content-Id value
   **/
  private boolean hasContentId(Part part) throws MessagingException {
    return part.getHeader("Content-Id") == null ? false : true;
  }

  private MessageFilter getFilter(Node filterNode, String username, String accountId) throws Exception {
    MessageFilter filter = new MessageFilter("");
    if (filterNode.hasProperty(Utils.EXO_ID))
      filter.setId((filterNode.getProperty(Utils.EXO_ID).getString()));
    if (filterNode.hasProperty(Utils.EXO_NAME))
      filter.setName(filterNode.getProperty(Utils.EXO_NAME).getString());
    if (filterNode.hasProperty(Utils.EXO_FROM))
      filter.setFrom(filterNode.getProperty(Utils.EXO_FROM).getString());
    if (filterNode.hasProperty(Utils.EXO_FROM_CONDITION))
      filter.setFromCondition((int) (filterNode.getProperty(Utils.EXO_FROM_CONDITION).getLong()));
    if (filterNode.hasProperty(Utils.EXO_TO))
      filter.setTo(filterNode.getProperty(Utils.EXO_TO).getString());
    if (filterNode.hasProperty(Utils.EXO_TO_CONDITION))
      filter.setToCondition((int) (filterNode.getProperty(Utils.EXO_TO_CONDITION).getLong()));
    if (filterNode.hasProperty(Utils.EXO_SUBJECT))
      filter.setSubject(filterNode.getProperty(Utils.EXO_SUBJECT).getString());
    if (filterNode.hasProperty(Utils.EXO_SUBJECT_CONDITION))
      filter.setSubjectCondition((int) (filterNode.getProperty(Utils.EXO_SUBJECT_CONDITION).getLong()));
    if (filterNode.hasProperty(Utils.EXO_BODY))
      filter.setBody(filterNode.getProperty(Utils.EXO_BODY).getString());
    if (filterNode.hasProperty(Utils.EXO_BODY_CONDITION))
      filter.setBodyCondition((int) (filterNode.getProperty(Utils.EXO_BODY_CONDITION).getLong()));
    if (filterNode.hasProperty(Utils.EXO_APPLY_FOLDER)) {
      String folder = filterNode.getProperty(Utils.EXO_APPLY_FOLDER).getString();
      if (!Utils.isEmptyField(folder) && getFolder(username, accountId, folder) != null)
        filter.setApplyFolder(folder);
      else
        filter.setApplyFolder(Utils.generateFID(accountId, Utils.FD_INBOX, false));
    }
    if (filterNode.hasProperty(Utils.EXO_APPLY_TAG))
      filter.setApplyTag(filterNode.getProperty(Utils.EXO_APPLY_TAG).getString());
    if (filterNode.hasProperty(Utils.EXO_KEEP_IN_INBOX))
      filter.setKeepInInbox(filterNode.getProperty(Utils.EXO_KEEP_IN_INBOX).getBoolean());
    if (filterNode.hasProperty(Utils.EXO_APPLY_FOR_ALL))
      filter.setApplyForAll(filterNode.getProperty(Utils.EXO_APPLY_FOR_ALL).getBoolean());

    return filter;
  }

  private void saveFolderNode(Node folderNode, Folder folder) throws Exception {
    folderNode.setProperty(Utils.EXO_ID, folder.getId());
    folderNode.setProperty(Utils.EXO_NAME, folder.getName());
    folderNode.setProperty(Utils.EXO_UNREADMESSAGES, folder.getNumberOfUnreadMessage());
    folderNode.setProperty(Utils.EXO_FOLDERTYPE, folder.getType());
    folderNode.setProperty(Utils.EXO_LABEL, folder.getURLName());
    folderNode.setProperty(Utils.EXO_TOTALMESSAGE, folder.getTotalMessage());
    folderNode.setProperty(Utils.EXO_PERSONAL, folder.isPersonalFolder());
    if (folder.getLastCheckedDate() != null)
      folderNode.setProperty(Utils.EXO_LAST_CHECKED_TIME, folder.getLastCheckedDate().getTime());
    else
      folderNode.setProperty(Utils.EXO_LAST_CHECKED_TIME, (Value) null);

    if (folder.getLastStartCheckingTime() != null)
      folderNode.setProperty(Utils.EXO_LAST_START_CHECKING_TIME, folder.getLastStartCheckingTime().getTime());
    else
      folderNode.setProperty(Utils.EXO_LAST_START_CHECKING_TIME, (Value) null);
    if (folder.getCheckFromDate() != null)
      folderNode.setProperty(Utils.EXO_CHECK_FROM_DATE, folder.getCheckFromDate().getTime());
    else
      folderNode.setProperty(Utils.EXO_CHECK_FROM_DATE, (Value) null);
    folderNode.save();
  }

  private void setCometdMessage(ContinuationService continuation, Info infoObj, String from, String msgId, boolean isReadMessage, String subject, String size, String accId, Calendar gc, Calendar sc, String currentUserName, String username) {
    infoObj.setFrom(from);
    infoObj.setMsgId(Utils.encodeMailId(msgId));
    infoObj.setIsRead(isReadMessage);
    infoObj.setSubject(subject);
    infoObj.setSize(size);
    infoObj.setAccountId(accId);
    if (gc != null)
      infoObj.setDate(gc.getTime().toString());
    else if (sc != null)
      infoObj.setDate(sc.getTime().toString());
    else
      infoObj.setDate(new Date().toString());

    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    JsonValue json = null;
    try {
      json = generatorImpl.createJsonObject(infoObj);
    } catch (JsonException e) {
      if (logger.isDebugEnabled())
        logger.debug("Cannot create json object for cometd", e);
      return;
    }
    if (!Utils.isEmptyField(currentUserName))
      continuation.sendMessage(currentUserName, "/eXo/Application/mail/messages", json);
    else
      continuation.sendMessage(username, "/eXo/Application/mail/messages", json);
  }

  public Tag getTag(Node tagNode) throws Exception {
    Tag tag = new Tag();
    if (tagNode.hasProperty(Utils.EXO_ID))
      tag.setId((tagNode.getProperty(Utils.EXO_ID).getString()));
    if (tagNode.hasProperty(Utils.EXO_NAME))
      tag.setName(tagNode.getProperty(Utils.EXO_NAME).getString());
    if (tagNode.hasProperty(Utils.EXO_DESCRIPTION))
      tag.setDescription(tagNode.getProperty(Utils.EXO_DESCRIPTION).getString());
    if (tagNode.hasProperty(Utils.EXO_COLOR))
      tag.setColor(tagNode.getProperty(Utils.EXO_COLOR).getString());
    return tag;
  }
  
  /**
   * get list of message ids by filter
   * @param username
   * @param filter
   * @return
   * @throws Exception
   */
  public List<String> getListOfMessageIds(String username, MessageFilter filter) throws Exception {
    List<String> strList = new ArrayList<String>();
    SessionProvider sProvider = null;
    try {
      sProvider = createSessionProvider();
      Node homeMsg = getMessageHome(sProvider, username, filter.getAccountId());
      filter.setAccountPath(homeMsg.getPath());
      filter.setReturnedProperties(new String[] {Utils.EXO_ID});
      QueryManager qm = getSession(sProvider).getWorkspace().getQueryManager();
      String queryString = filter.getStatement();
      Query query = qm.createQuery(queryString, Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator iter = result.getNodes();
      while (iter.hasNext()) {
        Node node = iter.nextNode();
        if (node.hasProperty(Utils.EXO_ID)) {
          strList.add(node.getProperty(Utils.EXO_ID).getString());
        }
      }
    } finally {
      closeSessionProvider(sProvider);
    }
    return strList;
  }
  
}
