/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS       All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 22, 2007  
 */

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.validator.Validator;

public class ForumNameValidator implements Validator {
    
  public void validate(UIFormInput uiInput) throws Exception {
    String s = (String)uiInput.getValue();
    if(s == null || s.length() == 0) {
      Object[] args = { uiInput.getName(), uiInput.getBindingField() };
      throw new MessageException(new ApplicationMessage("NameValidator.msg.empty-input", args)) ;
    }
    for(int i = 0; i < s.length(); i ++){
      char c = s.charAt(i);
      if (Character.isLetter(c) || Character.isDigit(c) || c=='_' || c=='-' || c=='.' || c=='*' || c==' ' ){
        continue;
      }
      Object[] args = { uiInput.getName(), uiInput.getBindingField() };
      throw new MessageException(new ApplicationMessage("ForumNameValidator.msg.Invalid-char", args)) ;
    }
  }
  
}
