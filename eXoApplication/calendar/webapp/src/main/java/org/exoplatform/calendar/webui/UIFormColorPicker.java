/**
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
 **/
package org.exoplatform.calendar.webui;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.calendar.Colors;
import org.exoplatform.calendar.Colors.Color;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInput;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Feb 29, 2008  
 */
public class UIFormColorPicker extends UIFormInputBase<String>  {

  /**
   * The size of the list (number of select options)
   */
  private int items_ = 10 ;
  /**
   * The list of options
   */
  //private List<SelectItemOption<String>> options_ ;

  /**
   * The javascript expression executed when an onChange event fires
   */
  private String onchange_;

  /**
   * The javascript expression executed when an client onChange event fires
   */
  public static final String ON_CHANGE = "onchange".intern();

  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_BLUR = "onblur".intern();

  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_FOCUS = "onfocus".intern();

  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_KEYUP = "onkeyup".intern();

  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_KEYDOWN = "onkeydown".intern();

  /**
   * The javascript expression executed when an client event fires
   */
  public static final String ON_CLICK = "onclick".intern();

  private Map<String, String> jsActions_ = new HashMap<String, String>() ;
  private Color[] colors_ = null ;
  //private Map<String, Color> colors_ = new HashMap<String, Color>() ;

  public UIFormColorPicker(String name, String bindingExpression, String value) {
    super(name, bindingExpression, String.class);
    this.value_ = value ;
    setColors(Colors.COLORS) ;
  }

  public UIFormColorPicker(String name, String bindingExpression, Color[] colors) {
    super(name, bindingExpression, null);
    setColors(colors);
  }

  public void setJsActions(Map<String, String> jsActions) {
    if(jsActions != null) jsActions_ = jsActions;
  }

  public Map<String, String> getJsActions() {
    return jsActions_;
  }
  public void addJsActions(String action, String javaScript) {
    jsActions_.put(action, javaScript) ;
  }
  public UIFormColorPicker(String name, String bindingExpression, Color[] colors, Map<String, String> jsActions) {
    super(name, bindingExpression, null);
    setColors(colors) ;
    setJsActions(jsActions) ;
  }

  public UIFormColorPicker(String name, String value) {
    this(name, null, value);
  }
  /*final public UIFormColorPicker setColors(List<SelectItemOption<String>> options) { 
    options_ = options ; 
    if(options_ == null || options_.size() < 1) return this;
    value_ = options_.get(0).getValue();
    return this ;
  } */
  
  @SuppressWarnings("unused")
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    value_ = (String) input;
    if(value_ != null && value_.trim().length() == 0) value_ = null ;
  }
  public void setOnChange(String onchange){ onchange_ = onchange; } 

  protected String renderOnChangeEvent(UIForm uiForm) throws Exception {
    return uiForm.event(onchange_, (String)null);
  }
  protected UIForm getUIform() {
    return getAncestorOfType(UIForm.class) ; 
  }

  private String renderJsActions() {
    StringBuffer sb = new StringBuffer("") ;
    for(String k : jsActions_.keySet()){
      if(sb != null && sb.length() > 0 ) sb.append(" ") ;
      if(jsActions_.get(k) != null) {
        sb.append(k).append("=\"").append(jsActions_.get(k)).append("\"") ;
      }  
    }
    return sb.toString() ;
  }

  private Color[] getColors(){
    return colors_ ;
  }
  private void setColors(Color[] colors){
    colors_ = colors ;
    value_ = colors_[0].getName() ;
  }
  private int items() {return items_ ;}
  private int size() {return colors_.length ;}
  public void processRender(WebuiRequestContext context) throws Exception {
    Writer w =  context.getWriter() ; 
    w.write("<div class='UIFormColorPicker'>") ;
      w.write("<div class=\"UIColorPickerInput\" onclick=\"eXo.calendar.UIColorPicker.show(this)\">") ;
      w.write("<span class=\" DisplayValue "+encodeValue(value_).toString()+"\"></span>") ;
      w.write("</div>") ;
      w.write("<div class=\"CalendarTableColor\" selectedColor=\""+encodeValue(value_).toString()+" \">") ;
      int i = 0 ;
      int count = 0 ;
      while(i <= size()/items())  {
        w.write("<div class='UIColorLine'>") ; 
        int j = 0 ;
        while(j <= items() && count < size()){
          Color color = getColors()[count] ; 
          String actionLink = "javascript:eXo.calendar.UIColorPicker.setColor('"+color.getName()+"')" ;   
          w.write("<a href=\""+actionLink+"\" class=\""+color.getName()+" ColorCell \" onmousedown=\"event.cancelBubble=true\"><img src=\"/eXoResources/skin/sharedImages/Blank.gif\" /></a>") ;
          count++ ;
          j++;
        }
        w.write("</div>") ;  
        i++ ;
      }
      w.write("</div>") ;
      w.write("<input class='UIColorPickerValue' name='"+getId()+"' type='hidden'" + " id='"+getId()+"' " + renderJsActions());
      if(value_ != null && value_.trim().length() > 0) {      
        w.write(" value='"+value_+"'");
      }
      w.write(" \\>") ;
    w.write("</div>") ;
  }

  @Override
  public UIFormInput setValue(String arg0) {
    if(arg0 == null) arg0 = colors_[0].getName() ;
    return super.setValue(arg0);
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
