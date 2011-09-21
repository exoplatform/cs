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

import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.model.SelectItem;
import org.exoplatform.webui.core.model.SelectOption;
import org.exoplatform.webui.core.model.SelectOptionGroup;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Jan 5, 2008  
 */
public class UISelectFolder extends UIFormInputSet {
  private static final Log log = ExoLogger.getExoLogger(UISelectFolder.class);
  
  final public static String SELECT_FOLDER = "folder" ;
  public String level = "" ;
  public String accountId_ = "";
  
  public UISelectFolder() throws Exception { }
  
  public void init(String accountId) throws Exception {
    setId("UISelectFolder");
    accountId_ = accountId ; 
    addUIFormInput(new UIFormSelectBoxWithGroups(SELECT_FOLDER, SELECT_FOLDER, getOptions()));
  }
  
  public void setSelectedValue(String s) {
    ((UIFormSelectBoxWithGroups)getChildById(SELECT_FOLDER)).setValue(s) ;
  }
  
  public String getSelectedValue() {
    return ((UIFormSelectBoxWithGroups)getChildById(SELECT_FOLDER)).getValue() ;
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
    List<Folder> subFolders = new ArrayList<Folder>();
    for (Folder f : mailSvr.getSubFolders(username, accountId_, parentPath)) {
      subFolders.add(f);
    }
    return subFolders ;
  }

  public List<Folder> getFolders(boolean isPersonal) throws Exception {
    DataCache dataCache = (DataCache) WebuiRequestContext.getCurrentInstance().getAttribute(DataCache.class);
    List<Folder> folders = new ArrayList<Folder>();
    String username = MailUtils.getCurrentUser();
    try {
      folders.addAll(dataCache.getFolders(username, accountId_, isPersonal));
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method getFolders", e);
      }
    }
    return folders;
  }
  
  public SelectOptionGroup addChildOption(String folderPath,  SelectOptionGroup optionList) throws Exception {
    level += "----" ;
    for (Folder cf : getSubFolders(folderPath)) {
      if (cf != null) {
        optionList.addOption(new SelectOption(level + " " + cf.getName(), cf.getId()));
        if (getSubFolders(cf.getPath()).size() > 0) { 
          optionList = addChildOption(cf.getPath(), optionList);
        }
      }
    }
    level = level.substring(0, level.length() - 4);
    return optionList ;
  }
  
  protected UIForm getUIForm() {
    return getAncestorOfType(UIForm.class) ;
  }
  
  public List<SelectItem> getOptions() throws Exception {
    List<SelectItem> options = new ArrayList<SelectItem>() ;
    SelectOptionGroup defaultFolders = new SelectOptionGroup("default-folder");
    for(Folder df : getDefaultFolders()) {
      defaultFolders.addOption(new SelectOption(getUIForm().getLabel(df.getName()), df.getId())) ;
    }
    options.add(defaultFolders);
    SelectOptionGroup customizeFolders = new SelectOptionGroup("my-folder");
    for(Folder cf : getCustomizeFolders()) {
      customizeFolders.addOption(new SelectOption(cf.getName(), cf.getId())) ;
        if (getSubFolders(cf.getPath()).size() > 0) { 
          customizeFolders = addChildOption(cf.getPath(), customizeFolders);
        }
    }
    options.add(customizeFolders);
      
    return options ;
  }
}