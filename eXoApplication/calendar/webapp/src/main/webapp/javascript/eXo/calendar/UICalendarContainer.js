function UICalendarContainer() {
}

UICalendarContainer.prototype.init = function() {
  //TODO need to be changed to be relevant to WebOS
  this.UICalendarContainer = document.getElementById("UICalendarContainer");
  var DOMUtil = eXo.core.DOMUtil;
  this.UIMiniCalendar = DOMUtil.findFirstDescendantByClass(this.UICalendarContainer, "div", "UIMiniCalendar");
  this.UICalendarsList = DOMUtil.findFirstDescendantByClass(this.UICalendarContainer, "div", "UICalendars");
  this.UIMiniCalendarContainer = DOMUtil.findFirstDescendantByClass(this.UIMiniCalendar, "div", "MiniCalendarContainer");
  this.UICalendarsListContentContainer = DOMUtil.findFirstDescendantByClass(this.UICalendarsList, "div", "ContentContainer");
  this.UIMiniCalendarToggleButton = DOMUtil.findFirstDescendantByClass(this.UIMiniCalendar, "div", "UIMiniCalendarToggleButton");
};

UICalendarContainer.prototype.toggleMiniCalendar = function() {
  this.init();
  if (this.UIMiniCalendarContainer.style.display == "none" || this.UIMiniCalendarContainer.style.display == undefined)
    this.expandMiniCalendar();
  else this.collapseMiniCalendar();
};

UICalendarContainer.prototype.collapseMiniCalendar = function() {
  var formerHeight = this.UIMiniCalendar.offsetHeight;
  this.UIMiniCalendarContainer.style.display = "none";
  var collapseHeight = formerHeight - this.UIMiniCalendar.offsetHeight;
  var newCalListHeight = this.UICalendarsListContentContainer.offsetHeight + collapseHeight; 
  var downCssClass = this.UIMiniCalendarToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UIMiniCalendarToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UIMiniCalendarToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(upCssClass, downCssClass);
  this.UIMiniCalendarToggleButton.className = buttonCssClassStr;
  this.UICalendarsListContentContainer.style.height = newCalListHeight + "px";
};

UICalendarContainer.prototype.expandMiniCalendar = function() {
  var formerHeight = this.UIMiniCalendar.offsetHeight;
  this.UIMiniCalendarContainer.style.display = "block";
  var expandHeight = this.UIMiniCalendar.offsetHeight - formerHeight;
  var newCalListHeight = this.UICalendarsListContentContainer.offsetHeight - expandHeight;
  var downCssClass = this.UIMiniCalendarToggleButton.getAttribute("downCssClass");
  var upCssClass = this.UIMiniCalendarToggleButton.getAttribute("upCssClass");
  var buttonCssClassStr = this.UIMiniCalendarToggleButton.className;
  buttonCssClassStr = buttonCssClassStr.replace(downCssClass, upCssClass);
  this.UIMiniCalendarToggleButton.className = buttonCssClassStr;  
  this.UICalendarsListContentContainer.style.height = newCalListHeight + "px";
};

if (!eXo.calendar.UICalendarContainer) eXo.calendar.UICalendarContainer = new UICalendarContainer();