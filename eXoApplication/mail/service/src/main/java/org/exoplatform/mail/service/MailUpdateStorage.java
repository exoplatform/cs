/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.mail.service;

import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Dec 1, 2008  
 */
public class MailUpdateStorage extends MailUpdateStorageEventListener {
  private MailService service_ ;
  private RepositoryService repositorySerivce_;
  private boolean isUpdate_ = false;
  private String accAction_;
  private String newAccProperties_;
  private String msgAction_;
  private String newMsgProperties;
  
  public MailUpdateStorage(MailService service, InitParams params, RepositoryService repositorySerivce) throws Exception {
    service_ = service;
    ExoProperties isUpdateProps = params.getPropertiesParam("mail.update.param").getProperties() ;
    isUpdate_ = Boolean.valueOf(isUpdateProps.getProperty("isUpdate")); 
    
    ExoProperties accProps =  params.getPropertiesParam("mail.account.param").getProperties() ;
    accAction_ = accProps.getProperty("action") ;
    newAccProperties_ = accProps.getProperty("newProperty") ;
    repositorySerivce_ = repositorySerivce;
    ExoProperties msgProps =  params.getPropertiesParam("mail.message.param").getProperties() ;
    msgAction_ = msgProps.getProperty("action") ;
    newMsgProperties = msgProps.getProperty("newProperty") ;
  }
  
  public void preUpdate(){
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    if (isUpdate_) {
      updateAccountData(sessionProvider);
      updateMessageData(sessionProvider);
    }
  }
  
  public void postUpdate() {
    super.postUpdate();
  }
  
  private void updateAccountData(SessionProvider sessionProvider){
    try {
      String[] properties = newAccProperties_.split(";");
      String jcrPathApps = service_.getMailHierarchyNode(sessionProvider) ;
      String wsName = repositorySerivce_.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
      Session session = sessionProvider.getSession(wsName, repositorySerivce_.getCurrentRepository());
      StringBuffer sqlQuery = new StringBuffer("/jcr:root" + jcrPathApps + "//element(*,exo:account)"); 
      Node node = (Node) session.getItem(jcrPathApps);
      QueryManager qm = node.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(sqlQuery.toString(), Query.XPATH) ;
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes();
      if (accAction_.equals("add")) {
        while (it.hasNext()) {
          Node acc = it.nextNode();
          for (int i = 0; i < properties.length; i++) {
            String[] propers = properties[i].split(",");
            if (!acc.hasProperty(propers[0].trim())) {
              if (propers[0].trim().equals("exo:checkFromDate")) {
                acc.setProperty(propers[0].trim(), (new Date()).getTime());
              } else {
                acc.setProperty(propers[0].trim(), propers[1].trim());
              }
            }
          }
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  private void updateMessageData(SessionProvider sessionProvider) {
    try {
      String[] properties = newMsgProperties.split(";");
      String jcrPathApps = service_.getMailHierarchyNode(sessionProvider) ;
      String wsName = repositorySerivce_.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
      Session session = sessionProvider.getSession(wsName, repositorySerivce_.getCurrentRepository());
      StringBuffer sqlQuery = new StringBuffer("/jcr:root" + jcrPathApps + "//element(*,exo:message)"); 
      Node node = (Node) session.getItem(jcrPathApps);
      QueryManager qm = node.getSession().getWorkspace().getQueryManager();
      Query query = qm.createQuery(sqlQuery.toString(), Query.XPATH) ;
      QueryResult result = query.execute();
      NodeIterator it = result.getNodes(); 
      if (msgAction_.equals("add")) {
        while (it.hasNext()) {
          Node msg = it.nextNode();
          for (int i = 0; i < properties.length; i++) {
            String[] propers = properties[i].split(",");
            if (!msg.hasProperty(propers[0].trim()))
              msg.setProperty(propers[0].trim(), propers[1].trim());
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
