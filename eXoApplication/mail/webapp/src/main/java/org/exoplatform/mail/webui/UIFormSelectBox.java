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
package org.exoplatform.mail.webui ;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
/**
 * Represents a select element
 * Can hold option and optgroup elements
 */
public class UIFormSelectBox extends UIFormStringInput {
  
  /**
   * It make SelectBox's ability to select multiple values
   */
  private boolean isMultiple_ = false ;
  
  /**
   * The size of the list (number of select options)
   */
  private int size_ = 1 ;

  /**
   * The list of options
   * Modified by Philippe : philippe.aristote@gmail.com
   * An option can either be a SelectItemOption or a SelectItemOptionGroup, hence the list
   * accepts SelectItem elements which are more generic.
   */
 // private List<SelectItemOption<String>> options_ ;
  private List<SelectItem> options_ ;
  
  /**
   * The javascript expression executed when an onChange event fires
   */
  private String onchange_;
  
  public UIFormSelectBox(String name, String bindingExpression, List<SelectItem> options) {
    super(name, bindingExpression, null);
    setOptions(options);
  }
  
  final public UIFormSelectBox setMultiple(boolean bl) {
    isMultiple_ = bl ; return this ;
  }
  
  final public UIFormSelectBox setSize(int i) { 
    size_ = i ; return this ;
  }
  
  @SuppressWarnings("unchecked")
public UIFormSelectBox setValue(String value) {
    value_ = value ;
    //return setSelectedValues(new String[]{value});
    for(SelectItem option : options_) {
    	if (option instanceof SelectItemOption) {
	      if(((SelectItemOption<String>)option).getValue().equals(value_)) ((SelectItemOption)option).setSelected(true) ;
	      else ((SelectItemOption<String>)option).setSelected(false) ;
    	} else if (option instanceof SelectItemOptionGroup) {
    		((SelectItemOptionGroup)option).setValue(value);
    	}
    }
    return this ;
  }
 

  @SuppressWarnings("unchecked")
public String[] getSelectedValues() {
    if(isMultiple_) {
      List<String> selectedValues = new ArrayList<String>() ;
      for(int i = 0; i < options_.size(); i ++) {
    	if (options_.get(i) instanceof SelectItemOption) {
          SelectItemOption<String> item = (SelectItemOption<String>)options_.get(i) ; 
          if(item.isSelected()) selectedValues.add(item.getValue()); 
    	} else if (options_.get(i) instanceof SelectItemOptionGroup) {
    		selectedValues.addAll(((SelectItemOptionGroup)options_.get(i)).getSelectedValues());
    	}
      }
      return selectedValues.toArray(new String[0]) ;
    }
    return new String[]{value_} ;
  }
  
  public UIFormSelectBox setSelectedValues(String[] values) {
    for(SelectItem option : options_) {
      if (option instanceof SelectItemOption) {
    	  ((SelectItemOption)option).setSelected(false) ;
	      for(String value : values) {
	    	  //option.setSelectedValue(value);
	        if(value.equals(((SelectItemOption)option).getValue())) {
	        	((SelectItemOption)option).setSelected(true) ;
	          break ;
	        }
	      }
      } else if (option instanceof SelectItemOptionGroup) {
    	  ((SelectItemOptionGroup)option).setSelectedValue(values);
      }
    }
    return this ;
  }
    
  final public List<SelectItem> getOptions() { return options_ ; }
  
  @SuppressWarnings("unchecked")
final public UIFormSelectBox setOptions(List<SelectItem> options) { 
    options_ = options ; 
    if(options_ == null || options_.size() < 1) return this;
    if (options_.get(0) instanceof SelectItemOption) value_ = ((SelectItemOption<String>)options_.get(0)).getValue();
    return this ;
  } 
  
  @SuppressWarnings("unchecked")
@Override
  public void reset() {
    // TODO Auto-generated method stub - dang.tung
    if(options_ == null || options_.size() < 1) return;
    if (options_.get(0) instanceof SelectItemOption) value_ = ((SelectItemOption<String>)options_.get(0)).getValue();
    for(SelectItem option : options_) {
    	if (option instanceof SelectItemOption)
    		((SelectItemOption<String>)option).setSelected(false) ;
    	else if (option instanceof SelectItemOptionGroup)
    		((SelectItemOptionGroup)option).reset(value_);
    }
    ((SelectItemOption<String>)options_.get(0)).setSelected(true) ;
  }
  
  public void setOnChange(String onchange){ onchange_ = onchange; }    
  
  @SuppressWarnings("deprecation")
  public UIFormSelectBox setDisabled(boolean disabled) {
    setEnable(!disabled);
    return this;
  }
  
  @SuppressWarnings({ "unused", "unchecked" })
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    String[] values = context.getRequestParameterValues(getId()) ;
    if(values == null) {
      value_ = null ;
      for(SelectItem option : options_) {
    	  if (option instanceof SelectItemOption)
      		((SelectItemOption<String>)option).setSelected(false) ;
      }
      return ;
    }
    
    int i = 0 ;
    value_ = values[0] ;
    for(SelectItem item: options_) {
    	if (item instanceof SelectItemOption) {
	      if (i > -1 && ((SelectItemOption)item).getValue().equals(values[i])) {
	    	  ((SelectItemOption)item).setSelected(true) ;
	        if(values.length == ++i) i = -1 ;
	      } else ((SelectItemOption)item).setSelected(false) ;
    	}
    }
  }
    
//  protected String renderOnChangeAction(UIForm uiform) throws Exception {
//    StringBuilder builder = new StringBuilder();
//    builder.append(" onchange=\"javascript:eXo.webui.UIForm.submitForm('").
//            append("").append("','").append(onchange_).append("');\" ");
//    return builder.toString();
//  }
  
  protected String renderOnChangeEvent(UIForm uiForm) throws Exception {
    return uiForm.event(onchange_, (String)null);
  }
  
  @SuppressWarnings("unchecked")
public void processRender(WebuiRequestContext context) throws Exception {
    UIForm uiForm = getAncestorOfType(UIForm.class) ;
    Writer w =  context.getWriter() ;
    w.write("<select class=\"selectbox\" name=\""); w.write(name); w.write("\"") ;
    if(onchange_ != null) {
      w.append(" onchange=\"").append(renderOnChangeEvent(uiForm)).append("\"");
    }
    
    if(isMultiple_)  w.write(" multiple=\"true\""); 
    if(size_ > 1)  w.write(" size=\"" + size_ + "\"");
    
    if (!enable_)  w.write(" disabled ");
    
    w.write(">\n") ;
    
    /*
     * Delegates rendering of SelectItemOption and SelectItemOptionGroup to specific private methods
     */
    for(SelectItem item : options_) {
    	if (item instanceof SelectItemOptionGroup) w.write(processRenderOptionGroup((SelectItemOptionGroup)item));
    	else if (item instanceof SelectItemOption) w.write(processRenderOption((SelectItemOption<String>)item));
    }
    
    w.write("</select>\n") ;
    if (this.isMandatory()) w.write(" *");
  }
  protected UIForm getFrom() {
    return getAncestorOfType(UIForm.class) ;
  }
  
  private String processRenderOptionGroup(SelectItemOptionGroup group) {
	  StringBuffer result = new StringBuffer();
    String label = group.getLabel() ;
    try {
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      ResourceBundle res = context.getApplicationResourceBundle() ;     
      label = res.getString(getFrom().getId() + ".optionGroup.label." + label);      
    } catch (Exception e) {
      System.out.println("Could not find: " + getFrom().getId() + ".optionGroup.label." + label);
    }
	  result.append("<optgroup label=\""); result.append(label); result.append("\">") ;
		for (SelectItemOption<String> option : group.getOptions())
			result.append(processRenderOption(option));
	  result.append("</optgroup>") ;
	  return result.toString();
  }
  
  private String processRenderOption(SelectItemOption<String> option) {
	  StringBuffer result = new StringBuffer();
	  result.append("<option");
  	  if(option.isSelected()) result.append(" selected=\"selected\"");
  	  if (option.isDisabled()) result.append(" disabled=\"true\"");
  	    result.append(" value=\""); result.append(option.getValue()); result.append("\">"); 
  	    result.append(option.getLabel()); 
  	  result.append("</option>\n");
	  return result.toString();
  }

}