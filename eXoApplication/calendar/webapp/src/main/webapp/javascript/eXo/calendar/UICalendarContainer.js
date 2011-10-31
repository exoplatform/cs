function UICalendarContainer() {
  this.calendarsListHeight = 305;
  this.miniCalendarContainerHeight = 148;
}

UICalendarContainer.prototype.assignDOMElements = function() {
  //TODO need to be changed to be relevant to WebOS
  this.UICalendarContainer = document.getElementById("UICalendarContainer");
  var DOMUtil = eXo.core.DOMUtil;
  this.UIMiniCalendar = DOMUtil.findFirstDescendantByClass(this.UICalendarContainer, "div", "UIMiniCalendar");
  this.UICalendarsList = DOMUtil.findFirstDescendantByClass(this.UICalendarContainer, "div", "UICalendars");
  this.UIMiniCalendarContainer = DOMUtil.findFirstDescendantByClass(this.UIMiniCalendar, "div", "MiniCalendarContainer");
  this.UICalendarsListContentContainer = DOMUtil.findFirstDescendantByClass(this.UICalendarsList, "div", "ContentContainer");
  this.UIMiniCalendarToggleButton = DOMUtil.findFirstDescendantByClass(this.UIMiniCalendar, "div", "UIMiniCalendarToggleButton");
  this.UICalendarsToggleButton = DOMUtil.findFirstDescendantByClass(this.UICalendarsList, "div", "UICalendarsToggleButton");
  var layoutMan = eXo.calendar.LayoutManager;
  this.layoutcookie = eXo.core.Browser.getCookie(layoutMan.layoutId);
};

UICalendarContainer.prototype.updateCalendarContainerLayout = function() {
  this.assignDOMElements();
  if (this.layoutcookie.indexOf("1") >= 0) {
    this.collapseCalendarContainer();
  } else {
    this.expandCalendarContainer();
  }
};

UICalendarContainer.prototype.updateMiniCalendarLayout = function() {
  this.assignDOMElements();
  if (this.layoutcookie.indexOf("2") >= 0) {
    this.expandMiniCalendar();
  } else {
    this.collapseMiniCalendar();
  }
};

UICalendarContainer.prototype.updateUICalendarsLayout = function() {
  this.assignDOMElements();
  if (this.layoutcookie.indexOf("3") >= 0) {
    this.collapseUICalendars();
  } else {
    this.expandUICalendars();
  }
};

UICalendarContainer.prototype.collapseCalendarContainer = function() {
  this.UICalendarContainer.style.display = "none";
  var UICalendarViewContainer = eXo.core.DOMUtil.findNextElementByTagName(this.UICalendarContainer, "div");
  if (eXo.core.I18n.isRT()) {
      UICalendarViewContainer.style.marginRight = "0px";
    }else{
      UICalendarViewContainer.style.marginLeft = "0px";
    }
};

UICalendarContainer.prototype.expandCalendarContainer = function() {
  this.UICalendarContainer.style.display = "block";
  var UICalendarViewContainer = eXo.core.DOMUtil.findNextElementByTagName(this.UICalendarContainer, "div");
  if (eXo.core.I18n.isRT()) {
    UICalendarViewContainer.style.marginRight = "236px" ;
  }else{
    UICalendarViewContainer.style.marginLeft = "236px" ;
  }
};

UICalendarContainer.prototype.toggleMiniCalendar = function() {
  this.init();
  if (this.UIMiniCalendarContainer.style.display == "none" || this.UIMiniCalendarContainer.style.display == undefined)
    this.expandMiniCalendar();
  else this.collapseMiniCalendar();
};

UICalendarContainer.prototype.collapseMiniCalendar = function() {
  this.UIMiniCalendarContainer.style.display = "none";
  var downCssClass = this.UIMiniCalendarToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UIMiniCalendarToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UIMiniCalendarToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(upCssClass, downCssClass);
  this.UIMiniCalendarToggleButton.className = buttonCssClassStr;
  this.UICalendarsListContentContainer.style.height = (this.miniCalendarContainerHeight + this.calendarsListHeight) + "px";
};

UICalendarContainer.prototype.expandMiniCalendar = function() {
  this.UIMiniCalendarContainer.style.display = "block";
  var downCssClass = this.UIMiniCalendarToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UIMiniCalendarToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UIMiniCalendarToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(downCssClass, upCssClass);
  this.UIMiniCalendarToggleButton.className = buttonCssClassStr;  
  this.UICalendarsListContentContainer.style.height = this.calendarsListHeight + "px";
};

UICalendarContainer.prototype.collapseUICalendars = function() {
  this.UICalendarsListContentContainer.style.display = "none";
  var downCssClass = this.UICalendarsToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UICalendarsToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UICalendarsToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(upCssClass, downCssClass);
  this.UICalendarsToggleButton.className = buttonCssClassStr;
};

UICalendarContainer.prototype.expandUICalendars = function() {
  this.UICalendarsListContentContainer.style.display = "block";
  var downCssClass = this.UICalendarsToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UICalendarsToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UICalendarsToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(downCssClass, upCssClass);
  this.UICalendarsToggleButton.className = buttonCssClassStr;
};


if (!eXo.calendar.UICalendarContainer) eXo.calendar.UICalendarContainer = new UICalendarContainer();