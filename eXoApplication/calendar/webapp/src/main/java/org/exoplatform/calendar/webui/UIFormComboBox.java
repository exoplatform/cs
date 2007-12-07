/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.io.Writer;
import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Dec 3, 2007  
 */
public class UIFormComboBox extends UIFormInputBase<String>  {
  final  static public short TEXT_TYPE = 0 ;
  /**
   * type : password
   */
  final  static public short PASSWORD_TYPE = 1 ;
  /**
   * type of the text field
   */
  private short type_ = TEXT_TYPE ;
  /**
   * The size of the list (number of select options)
   */
  private int size_ = 1 ;

  /**
   * The list of options
   */
  private List<SelectItemOption<String>> options_ ;

  /**
   * The javascript expression executed when an onChange event fires
   */
  private String onchange_;
  public UIFormComboBox(String name, String bindingExpression, String value) {
    super(name, bindingExpression, String.class);
    this.value_ = value ;
  }

  public UIFormComboBox(String name, String bindingExpression, List<SelectItemOption<String>> options) {
    super(name, bindingExpression, null);
    setOptions(options);
  }

  public UIFormComboBox(String name, String value) {
    this(name, null, value);
  }

  public UIFormComboBox setType(short type) {
    type_ = type;
    return this ;
  } 
  final public UIFormComboBox setOptions(List<SelectItemOption<String>> options) { 
    options_ = options ; 
    if(options_ == null || options_.size() < 1) return this;
    value_ = options_.get(0).getValue();
    return this ;
  } 
  @SuppressWarnings("unused")
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    value_ = (String) input;
    if(value_ != null && value_.length() == 0) value_ = null ;
  }
  public void setOnChange(String onchange){ onchange_ = onchange; } 
  protected String renderOnChangeEvent(UIForm uiForm) throws Exception {
    return uiForm.event(onchange_, (String)null);
  }
  private UIForm getUIform() {
    return getAncestorOfType(UIForm.class) ; 
  }
  public void processRender(WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().addJavascript("eXo.calendar.UICombobox.init('" + getUIform().getId()+ "');") ;  
    Writer w =  context.getWriter() ;
    w.write("<div class='UIComboboxContainer'>") ;
      w.write("<input type='text' value=''><br>") ;
      w.write("<div class='UIComboboxList'>") ;
        for(SelectItemOption item : options_) {
          w.write("<a href='#' value='" + item.getValue()+ "' class='UIComboboxItem'>") ;
            w.write("<div class='UIComboboxIcon'>") ;
              w.write("<div class='UIComboboxLabel'>" + item.getLabel() + "</div>") ;
            w.write("</div>");
          w.write("</a>") ;
        }
      w.write("</div>") ;
    w.write("</div>") ;
  }

  private StringBuilder encodeValue(String value){
    char [] chars = {'\'', '"'};
    String [] refs = {"&#39;", "&#34;"};
    StringBuilder builder = new StringBuilder(value);
    int idx ;
    for(int i = 0; i < chars.length; i++){
      idx = indexOf(builder, chars[i], 0);
      while(idx > -1){
        builder = builder.replace(idx, idx+1, refs[i]);
        idx = indexOf(builder, chars[i], idx);
      }
    }    
    return builder;
  }

  private int indexOf(StringBuilder builder, char c, int from){
    int i = from;
    while(i < builder.length()){
      if(builder.charAt(i) == c) return i;
      i++;
    }
    return -1;
  }

}
