/**
 * 
 */
package org.exoplatform.webservice.cs.bean;

/**
 * @author uocnguyen
 *
 */
public class UIStateDataBean {
  private String data;
  
  /**
   * 
   */
  public UIStateDataBean() {
    // TODO Auto-generated constructor stub
  }
  
  /**
   * 
   */
  public UIStateDataBean(String data) {
    super();
    this.data = data;
  }

  /**
   * @param data the data to set
   */
  public void setData(String data) {
    this.data = data;
  }

  /**
   * @return the data
   */
  public String getData() {
    return data;
  }
}