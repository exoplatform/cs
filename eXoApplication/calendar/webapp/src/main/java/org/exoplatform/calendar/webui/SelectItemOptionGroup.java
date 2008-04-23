package org.exoplatform.calendar.webui ;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/**
 * Represents an optgroup in a select element
 * Holds a list of SelectItemOption that represent the options inside this optgroup
 * @author philippe
 *
 */
public class SelectItemOptionGroup extends SelectItem {
	
	/**
	 * The lis of SelectItemOption
	 */
	private List<SelectItemOption<String>> options_ ;
	
	public SelectItemOptionGroup(String label) {
		this(label, new ArrayList<SelectItemOption<String>>(3));
	}
	
	public SelectItemOptionGroup(String label, List<SelectItemOption<String>> list) {
		super(label);
		if (list == null) list = new ArrayList<SelectItemOption<String>>(3);
		options_ = list;
	}

	public List<SelectItemOption<String>> getOptions() {
		return options_;
	}

	public void setOptions(List<SelectItemOption<String>> options) {
		this.options_ = options;
	}
	/**
	 * Adds a SelectItemOption to the list
	 * @param option
	 */
	public void addOption(SelectItemOption<String> option) {
		if (options_ == null) options_ = new ArrayList<SelectItemOption<String>>(3);
		options_.add(option);
	}
	
	public void setSelectedValue(String[] values) {
		for (SelectItemOption<String> option : options_) {
    	  option.setSelected(false) ;
	      for(String value : values) {
	        if(value.equals(option.getValue())) {
	        	option.setSelected(true) ;
	          break ;
	        }
		  }
		}
	}
	
	 public void setValue(String value) {
		    for(SelectItemOption<String> option : options_) {
			      if(option.getValue().equals(value)) option.setSelected(true) ;
			      else option.setSelected(false) ;
		    }
		  }
	
	public Collection<String> getSelectedValues() {
	      List<String> selectedValues = new ArrayList<String>() ;
	      for(int i = 0; i < options_.size(); i ++) {
	          SelectItemOption<String> item = options_.get(i) ; 
	          if(item.isSelected()) selectedValues.add(item.getValue());
	      }
	      return selectedValues ;
	}
	
	  public void reset(String value_) {
		    if(options_ == null || options_.size() < 1) return;
		    value_ = options_.get(0).getValue();
		    for(SelectItemOption<String> option : options_) {
		    		option.setSelected(false) ;
		    }
		    options_.get(0).setSelected(true) ;
		  }
}
