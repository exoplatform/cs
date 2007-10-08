package org.exoplatform.contact.webui.popup;

import java.util.List;

import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UISendEmail.SendActionListener.class),      
      @EventConfig(listeners = UISendEmail.CancelActionListener.class)
    }
)

public class UISendEmail extends UIForm implements UIPopupComponent {
  public static final String FIELD_TO_INPUT = "to";
  public static final String FIELD_SUBJECT_INPUT = "subject";
  public static final String FIELD_CONTENT_INPUT = "content";
  
  public UISendEmail() {
    addUIFormInput(new UIFormStringInput(FIELD_TO_INPUT, FIELD_TO_INPUT, null));    
    addUIFormInput(new UIFormStringInput(FIELD_SUBJECT_INPUT, FIELD_SUBJECT_INPUT, null)); 
    addUIFormInput(new UIFormTextAreaInput(FIELD_CONTENT_INPUT, FIELD_CONTENT_INPUT, null)) ;    
  }
  public String[] getActions() { return new String[] {"Send", "Cancel"} ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
 
  public void setEmails(List<String> emails) { 
    StringBuffer emailBuffer = new StringBuffer("") ;
    if (emails.size() > 0) emailBuffer.append(emails.get(0)) ;
    for (int i = 1; i < emails.size(); i ++) emailBuffer.append(", " + emails.get(i)) ;
    getUIStringInput(FIELD_TO_INPUT).setValue(emailBuffer.toString()) ;
  }
  
  static  public class SendActionListener extends EventListener<UISendEmail> {
    public void execute(Event<UISendEmail> event) throws Exception {
      
    }
  }
    
  static  public class CancelActionListener extends EventListener<UISendEmail> {
    public void execute(Event<UISendEmail> event) throws Exception {
      UISendEmail uiSendEmail = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiSendEmail.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.cancelAction() ;
    }
  }
}