package org.exoplatform.contact.service;

public class ContactData {
    private String id ;
    private String fullName ;
    private String email ;

    public ContactData(String id,String fullName,String email){
      this.id = id ;
      this.fullName = fullName;
      this.email = email ;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public String getFullName() {
      return fullName;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getEmail() {
      return email;
    }

    @Override
    public boolean equals(Object o) {
      if  (!(o instanceof ContactData)) return false;
      return this.id.equals(((ContactData)o).id) ;
    }

    @Override
    public int hashCode(){
      return id.hashCode();
    }
  }
