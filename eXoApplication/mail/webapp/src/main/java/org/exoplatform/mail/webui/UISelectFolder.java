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
package org.exoplatform.mail.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Jan 5, 2008  
 */
public class UISelectFolder extends UIFormInputSet {
  final public static String SELECT_FOLDER = "folder" ;
  public String level = "" ;
  
  public UISelectFolder(String id) throws Exception {  
    setId(id);
    addUIFormInput(new UIFormSelectBox(SELECT_FOLDER, SELECT_FOLDER, getOptions()));
  }
  
  public void setSelectedValue(String s) {
     getUIFormSelectBox(SELECT_FOLDER).setValue(s) ;
  }
  
  public String getSelectedValue() {
    return getUIFormSelectBox(SELECT_FOLDER).getValue() ;
  }
  
  public List<SelectItemOption<String>> getOptions() throws Exception {
    List<SelectItemOption<String>> optionList = new ArrayList<SelectItemOption<String>>();  
    for (Folder cf : getDefaultFolders()) {
      optionList.add(new SelectItemOption<String>(cf.getName(), cf.getId()));
    }
    for (Folder cf : getCustomizeFolders()) {
      optionList.add(new SelectItemOption<String>(cf.getName(), cf.getId()));
      if (getSubFolders(cf.getPath()).size() > 0) { 
        optionList = addChildOption(cf.getPath(), optionList);
      }
    }
    return optionList;
  }
  
  public List<SelectItemOption<String>> addChildOption(String folderPath,  List<SelectItemOption<String>> optionList) throws Exception {
    level += "----" ;
    for (Folder cf : getSubFolders(folderPath)) {
      if (cf != null) {
        optionList.add(new SelectItemOption<String>(level + " " + cf.getName(), cf.getId()));
        if (getSubFolders(cf.getPath()).size() > 0) { 
          optionList = addChildOption(cf.getPath(), optionList);
        }
      }
    }
    level = level.substring(0, level.length() - 4);
    return optionList ;
  }
  
  public List<Folder> getDefaultFolders() throws Exception{
    return getFolders(false);
  } 
  
  public List<Folder> getCustomizeFolders() throws Exception{
    return getFolders(true);
  }
  
  public List<Folder> getSubFolders(String parentPath) throws Exception {
    MailService mailSvr = MailUtils.getMailService();
    String username = MailUtils.getCurrentUser() ;
    String accountId = getAncestorOfType(UIMailPortlet.class).findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    List<Folder> subFolders = new ArrayList<Folder>();
    for (Folder f : mailSvr.getSubFolders(SessionsUtils.getSessionProvider(), username, accountId, parentPath)) {
      subFolders.add(f);
    }
    return subFolders ;
  }

  public List<Folder> getFolders(boolean isPersonal) throws Exception{
    List<Folder> folders = new ArrayList<Folder>() ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = MailUtils.getCurrentUser() ;
    String accountId = getAncestorOfType(UIMailPortlet.class).findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    try {
      folders.addAll(mailSvr.getFolders(SessionsUtils.getSessionProvider(), username, accountId, isPersonal)) ;
    } catch (Exception e){
      //e.printStackTrace() ;
    }
    return folders ;
  }
}