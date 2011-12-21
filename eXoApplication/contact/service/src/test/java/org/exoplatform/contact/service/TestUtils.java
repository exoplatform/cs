package org.exoplatform.contact.service;

import junit.framework.TestCase;

public class TestUtils extends TestCase {

  public void testIsEmpty() {
    String s = null;
    assertEquals(true, Utils.isEmpty(s));
    s = "";
    assertEquals(true, Utils.isEmpty(s));
    s = " ";
    assertEquals(true, Utils.isEmpty(s));
    s = "abc";
    assertEquals(false, Utils.isEmpty(s));
  }

  public void testIsArrayEmpty() {
    String[] strs = null;
    assertEquals(true, Utils.isEmpty(strs));
    strs = new String[] {};
    assertEquals(true, Utils.isEmpty(strs));
    strs = new String[] { "" };
    assertEquals(true, Utils.isEmpty(strs));
    strs = new String[] { " " };
    assertEquals(true, Utils.isEmpty(strs));
    strs = new String[] { "abc" };
    assertEquals(false, Utils.isEmpty(strs));
  }
}
