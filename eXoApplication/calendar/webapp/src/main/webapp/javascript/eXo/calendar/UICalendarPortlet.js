
/** 
 * Class to cover common actions of Calendar portlet
 * @author <a href="mailto:dung14000@gmail.com">Hoang Manh Dung</a>
 * @constructor
 */
function UICalendarPortlet(){
	this.clickone = 0 ;
	this.portletId = "calendars";
}

/**
 * Set stylesheet for a DOM element
 * @param {Object} object DOM Element
 * @param {Object} styles Object contains style name and style value
 */
UICalendarPortlet.prototype.setStyle = function(object, styles){
    for (var value in styles) {
        object.style[value] = styles[value];
    }
} ;

UICalendarPortlet.prototype.attachSwapClass = function(compId,className,hoverClass){
    var component = document.getElementById(compId);
    var items = eXo.core.DOMUtil.findDescendantsByClass(component,"div",className);
    var i = items.length;
    while(i--){
    	items[i].onmouseover = function(){
    		eXo.cs.Utils.swapClass(this,hoverClass);
    	}
    	items[i].onmouseout = function(){
    		eXo.cs.Utils.swapClass(this,hoverClass);
    	}
    };
} ;
/**
 * Show Quick add event and task form 
 * @param {obj, type} has action object, type of form : event 1 | task 2
 */
UICalendarPortlet.prototype.addQuickShowHidden = function(obj, type){
	var startTime = new Date().getTime() ;
    this.addQuickShowHiddenWithTime(obj, type, startTime, startTime + 15*60*1000) ;
} ;

/**
 * Show Quick add event and task form with selected time
 * @param {obj, type, from, to} has action object, type of form : event 1 | task 2, from in milliseconds, to in milliseconds
 */
UICalendarPortlet.prototype.addQuickShowHiddenWithTime = function(obj, type, fromMilli, toMilli){
    var CalendarWorkingWorkspace =  document.getElementById("UICalendarWorkingContainer");
    var UIQuckAddEventPopupWindow = eXo.core.DOMUtil.findDescendantById(CalendarWorkingWorkspace,"UIQuckAddEventPopupWindow");
    var UIQuckAddTaskPopupWindow = eXo.core.DOMUtil.findDescendantById(CalendarWorkingWorkspace,"UIQuckAddTaskPopupWindow");
    var formater = eXo.cs.DateTimeFormater ;
    var data = {
    		from:parseInt(fromMilli),
    		fromTime:parseInt(fromMilli),
    		to:parseInt(toMilli),
    		toTime:parseInt(toMilli)
    };
    if(type == 1) {
    	var uiform = eXo.core.DOMUtil.findDescendantById(UIQuckAddEventPopupWindow, "UIQuickAddEvent") ;
    	uiform.reset() ;
    	this.fillData(uiform, data) ;
    	eXo.webui.UIPopupWindow.show(UIQuckAddEventPopupWindow);
    	eXo.webui.UIPopup.hide(UIQuckAddTaskPopupWindow) ;
    } else if(type == 2) {
    	var uiform = eXo.core.DOMUtil.findDescendantById(UIQuckAddTaskPopupWindow, "UIQuickAddTask") ;
    	uiform.reset() ;
    	this.fillData(uiform, data) ;
    	eXo.webui.UIPopupWindow.show(UIQuckAddTaskPopupWindow);
    	eXo.webui.UIPopup.hide(UIQuckAddEventPopupWindow);
    }
} ;
/**
 * fill data to quick event/task form
 * @param {uiform, data} uifrom obj or id, data is array of value for each element of form
 */
UICalendarPortlet.prototype.fillData = function(uiform, data) {
	uiform = (typeof uiform == "string") ? document.getElementById(uiform):uiform;
	var fromField = uiform.elements["from"] ;
	var fromFieldTime = uiform.elements["fromTime"] ;
	var toField = uiform.elements["to"] ;
	var toFieldTime = uiform.elements["toTime"] ;
	var formater = eXo.cs.DateTimeFormater ;
	var timeType = "HH:MM" ;
	var dateType = fromField.getAttribute("format").replace("MM","mm") ;
	if(fromFieldTime.value.trim().length > 5)  timeType = formater.masks.shortTime ;
	fromField.value = formater.format(data.from, dateType);
	fromFieldTime.value = formater.format(data.fromTime, timeType);
	
	toField.value = formater.format(data.to, dateType);
	toFieldTime.value = formater.format(data.toTime, timeType); 
}
/**
 * Convert time from milliseconds to minutes
 * @param {Int} Milliseconds Milliseconds
 */
UICalendarPortlet.prototype.timeToMin = function(milliseconds){
    if (typeof(milliseconds) == "string") milliseconds = parseInt(milliseconds);
    var d = new Date(milliseconds);
    var hour = d.getHours();
    var min = d.getMinutes();
    var min = hour * 60 + min;
    return min;
};

/** 
 * Convert time from minutes to string
 * @param {Int} min  Minutes
 * @param {String} timeFormat  Format string of time
 * @return minutes
 */
UICalendarPortlet.prototype.minToTime = function(min, timeFormat){
    var minutes = min % 60;
    var hour = (min - minutes) / 60;
    if (hour < 10) 
        hour = "0" + hour;
    if (minutes < 10) 
        minutes = "0" + minutes;
    if (eXo.calendar.UICalendarPortlet.timeFormat != "hh:mm a") 
        return hour + ":" + minutes;
    var time = hour + ":" + minutes;
    if (!timeFormat) 
        return time;
    if (hour < 12) 
        time += " " + timeFormat.am;
    else 
        if (hour == 12) 
            time += " " + timeFormat.pm;
        else {
            hour -= 12;
            if (hour < 10) 
                hour = "0" + hour;
            time = hour + ":" + minutes;
            time += " " + timeFormat.pm;
        }
    return time;
};

/**
 * Gets begining day
 * @param {Object} millis Milliseconds
 * @return date object Date object
 */
UICalendarPortlet.prototype.getBeginDay = function(millis){
    var d = new Date(parseInt(millis));
    var date = d.getDate();
    var month = d.getMonth() + 1;
    var year = d.getFullYear();
    var strDate = month + "/" + date + "/" + year + " 00:00:00 AM";
    return Date.parse(strDate);
};

/**
 * Gets difference of two days
 * @param {Object} start Beginning date in milliseconds
 * @param {Object} end Ending date in milliseconds
 * @return Difference of two days
 */
UICalendarPortlet.prototype.dateDiff = function(start, end){
    var start = this.getBeginDay(start);
    var end = this.getBeginDay(end);
    var msDiff = end - start;
    var dateDiff = msDiff / (24 * 60 * 60 * 1000);
    return dateDiff;
};

/**
 * Apply time setting for Calendar portet
 * @param {Object} time Timi in milliseconds
 * @param {Object} settingTimeZone Timezone offset of user setting
 * @param {Object} severTimeZone Timezone offset of server
 */
UICalendarPortlet.prototype.toSettingTime = function(time, settingTimeZone, severTimeZone){
    var GMT = time - (3600000 * serverTimeZone);
    var settingTime = GMT + (3600000 * settingTimeZone);
    return settingTime;
};

/**
 * Gets full year from date object
 * @param {Object} date Date object
 * @return Full year
 */
UICalendarPortlet.prototype.getYear = function(date){
    x = date.getYear();
    var y = x % 100;
    y += (y < 38) ? 2000 : 1900;
    return y;
};

/**
 * Gets day from time in milliseconds
 * @param {Object} milliseconds Time in milliseconds
 * @return Day of week
 */
UICalendarPortlet.prototype.getDay = function(milliseconds){
    var d = new Date(milliseconds);
    var day = d.getDay();
    return day;
};

/**
 * Checks time is beginning of date or not
 * @param {Object} milliseconds Time in milliseconds
 * @return Boolean value
 */
UICalendarPortlet.prototype.isBeginDate = function(milliseconds){
    var d = new Date(milliseconds);
    var hour = d.getHours();
    var min = d.getMinutes();
    if ((hour == 0) && (hour == min)) 
        return true;
    return false;
};

/**
 * Checks time is beginning of week or not
 * @param {Object} milliseconds Time in milliseconds
 * @return Boolean value
 */
UICalendarPortlet.prototype.isBeginWeek = function(milliseconds){
    var d = new Date(milliseconds);
    var day = d.getDay();
    var hour = d.getHours();
    var min = d.getMinutes();
    if ((day == 0) && (hour == 0) && (min == 0)) 
        return true;
    return false;
};

/**
 * Gets number of week in current year
 * @param {Object} now Time in milliseconds
 * @return number of week
 */
UICalendarPortlet.prototype.getWeekNumber = function(now){
    var today = new Date(now);
    var Year = this.getYear(today);
    var Month = today.getMonth();
    var Day = today.getDate();
    var now = Date.UTC(Year, Month, Day + 1, 0, 0, 0);
    var Firstday = new Date();
    Firstday.setYear(Year);
    Firstday.setMonth(0);
    Firstday.setDate(1);
    var then = Date.UTC(Year, 0, 1, 0, 0, 0);
    var Compensation = Firstday.getDay();
    if (Compensation > 3) 
        Compensation -= 4;
    else 
        Compensation += 3;
    var NumberOfWeek = Math.round((((now - then) / 86400000) + Compensation) / 7);
    return NumberOfWeek;
};

UICalendarPortlet.prototype.setTimeValue = function(event, start,end,currentCol){
	event.setAttribute("startTime",start);
	event.setAttribute("endTime",end);
	if(currentCol) event.setAttribute("eventindex",currentCol.getAttribute("eventindex"));
};

/**
 * Gets working days of week from user setting then overrides weekdays property of UICalendarPorlet object
 * @param {Object} weekdays
 */
UICalendarPortlet.prototype.getWorkingdays = function(weekdays){
    this.weekdays = weekdays;
}

/* common method */
/**
 * Apply common setting for portlet
 * @param param1 Time interval in minutes
 * @param param2 User working time in minutes
 * @param param3 User time format
 * @param param4 Portlet id
 */
UICalendarPortlet.prototype.setting = function(){
    // paras 1: time interval, paras 2: working time, paras 3: time format type, paras 4: portletid
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    this.interval = ((arguments.length > 0) && (isNaN(parseInt(arguments[0])) == false)) ? parseInt(arguments[0]) : parseInt(15);
    var workingStart = ((arguments.length > 1) && (isNaN(parseInt(arguments[1])) == false) && (arguments[1] != "null")) ? arguments[1] : "";
    workingStart = Date.parse("1/1/2007 " + workingStart);
    this.workingStart = UICalendarPortlet.timeToMin(workingStart);
    this.timeFormat = (arguments.length > 2) ? (new String(arguments[2])).trim() : null;
    this.portletName = arguments[3];
};

/**
 * Scroll vertical scrollbar to position of active calendar event
 * @param {Object} obj DOM element
 * @param {Object} container DOM element contains all calendar events
 */
UICalendarPortlet.prototype.setFocus = function(){
  if(document.getElementById("UIWeekView")){
    var obj = document.getElementById("UIWeekViewGrid") ;
    var container = eXo.core.DOMUtil.findAncestorByClass(obj,"EventWeekContent") ;
  }
  else if(document.getElementById("UIDayView")){
    var obj = document.getElementById("UIDayView") ;
		obj = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "EventBoardContainer");
    var container = eXo.core.DOMUtil.findAncestorByClass(obj, "EventDayContainer");
  } else return ;
  var events = eXo.core.DOMUtil.findDescendantsByClass(obj,"div", "EventContainerBorder");
	events = this.getBlockElements(events);
  var len = events.length;
	var scrollTop =  this.timeToMin((new Date()).getTime());
	if(this.workingStart){
		if(len == 0) scrollTop = this.workingStart ;
		else {
			scrollTop = (this.hasEventThrough(scrollTop,events))? scrollTop : this.workingStart ;
		}
	}	
    var lastUpdatedId = obj.getAttribute("lastUpdatedId");
    if (lastUpdatedId && (lastUpdatedId != "null")) {
        for (var i = 0; i < len; i++) {
            if (events[i].getAttribute("eventId") == lastUpdatedId) {
                scrollTop = events[i].offsetTop;
                break;
            }
        }
    }
    container.scrollTop = scrollTop;
};
/**
 * 
 * @param {Object} min minutes
 * @param {Object} events array of calendar events
 */
UICalendarPortlet.prototype.hasEventThrough = function(min,events){
	var start = 0 ;
	var end = 0 ;
	var i = events.length
	while(i--){
		start = parseInt(events[i].getAttribute("startTime")) ;
		end = parseInt(events[i].getAttribute("endTime")) ;
		if((start <= min) && (end >= min)){
			return true ;
		}
	}
	return false;
};

/**
 * Hide a DOM elemnt automatically after interval time
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.autoHide = function(evt){
    var _e = window.event || evt;
    var eventType = _e.type;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    if (eventType == 'mouseout') {
        UICalendarPortlet.timeout = setTimeout("eXo.calendar.UICalendarPortlet.menuElement.style.display='none'", 5000);
    }
    else {
        if (UICalendarPortlet.timeout) 
            clearTimeout(UICalendarPortlet.timeout);
    }
};

/**
 * Show/hide a DOM element
 * @param {Object} obj DOM element
 */
UICalendarPortlet.prototype.showHide = function(obj){
    if (obj.style.display != "block") {
        eXo.core.DOMUtil.cleanUpHiddenElements();
        obj.style.display = "block";
        obj.onmouseover = eXo.calendar.UICalendarPortlet.autoHide;
        obj.onmouseout = eXo.calendar.UICalendarPortlet.autoHide;
        eXo.core.DOMUtil.listHideElements(obj);
    }
    else {
        obj.style.display = "none";
    }
};

/**
 * Show/hide Calendar menu
 * @param {Object} obj DOM Element to click
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.showMainMenu = function(obj, evt){
    var _e = window.event || evt;
    _e.cancelBubble = true;
    var d = new Date();
    var currentTime = d.getTime();
    var timezoneOffset = d.getTimezoneOffset();
    var oldmenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu");
    var actions = eXo.core.DOMUtil.findDescendantsByTagName(oldmenu, "div");
    actions[1].onclick = String(actions[1].onclick).replace(/&.*/, "&ct=" + currentTime + "&tz=" + timezoneOffset + "')");
    eXo.calendar.UICalendarPortlet.swapMenu(oldmenu, obj);
};

UICalendarPortlet.prototype.calendarMenuCallback = function(evt){
  var DOMUtil = eXo.core.DOMUtil ;
  var obj = eXo.core.EventManager.getEventTargetByClass(evt,"CalendarItem") || eXo.core.EventManager.getEventTargetByClass(evt,"GroupItem");
  var calType = obj.getAttribute("calType");
  var calName = obj.getAttribute("calName");
  var calColor = obj.getAttribute("calColor");
  var canEdit = String(obj.getAttribute("canedit")).toLowerCase();
  var menu = eXo.webui.UIContextMenu.menuElement ;
  var selectedCategory = (eXo.calendar.UICalendarPortlet.filterSelect) ? eXo.calendar.UICalendarPortlet.filterSelect : null;
	if(selectedCategory) selectedCategory = selectedCategory.options[selectedCategory.selectedIndex].value;
	if(!menu || !obj.id) {
    eXo.webui.UIContextMenu.menuElement = null ;
    return ;
  } 
  var value = "" ;
  value = "objectId=" + obj.id;
  if (calType) {
      value += "&calType=" + calType;
  }
  if (calName) {
      value += "&calName=" + calName;
  }
  if (calColor) {
      value += "&calColor=" + calColor;
  }
  var items = DOMUtil.findDescendantsByTagName(menu, "a");  
  for (var i = 0; i < items.length; i++) {
      if (DOMUtil.hasClass(items[i].firstChild, "SelectedColorCell")) {
          items[i].firstChild.className = items[i].firstChild.className.toString().replace(/SelectedColorCell/, "");
      }
      if (DOMUtil.hasClass(items[i], calColor)) {
          var selectedCell = items[i].firstChild;
          DOMUtil.addClass(selectedCell, "SelectedColorCell");
      }
      if (items[i].href.indexOf("ChangeColor") != -1) {
          value = value.replace(/calColor\s*=\s*\w*/, "calColor=" + items[i].className.split(" ")[0]);
      }
      items[i].href = String(items[i].href).replace(/objectId\s*=.*(?='|")/, value);
  }
	
  if (DOMUtil.hasClass(obj, "CalendarItem")) {
      items[0].href = String(items[0].href).replace("')", "&categoryId=" + selectedCategory + "')");
      items[1].href = String(items[1].href).replace("')", "&categoryId=" + selectedCategory + "')");      
  }
  if (calType && (calType != "0")) {
  
      var actions = DOMUtil.findDescendantsByTagName(menu, "a");
      for (var j = 0; j < actions.length; j++) {
          if ((actions[j].href.indexOf("EditCalendar") >= 0) ||
          (actions[j].href.indexOf("RemoveCalendar") >= 0) ||
          (actions[j].href.indexOf("ShareCalendar") >= 0) ||
          (actions[j].href.indexOf("ChangeColorCalendar") >= 0)) {
              actions[j].style.display = "none";
          }
      }
  }
  if (canEdit && (canEdit == "true")) {
      for (var j = 0; j < actions.length; j++) {
          if (actions[j].href.indexOf("EditCalendar") >= 0 || actions[j].href.indexOf("RemoveCalendar") >= 0) {
              actions[j].style.display = "block";
          }
      }
  }  
} ;

UICalendarPortlet.prototype.switchLayoutCallback = function(layout,status){
	var layoutMan = eXo.calendar.LayoutManager ;
	var panelWorking = eXo.core.DOMUtil.findNextElementByTagName(layoutMan.layouts[0], "div");
	var layoutcookie = eXo.core.Browser.getCookie(layoutMan.layoutId);
	
	if((layout == 2) || (layout == 3)){
		if(layoutcookie.indexOf('1') >= 0) return ;
	}
	if(!status) {
		layoutMan.layouts[layout-1].style.display = "none" ;
		if(layout == 1){			
			layoutMan.layouts[layout].style.display = "none" ;
			layoutMan.layouts[layout+1].style.display = "none" ;
			if (eXo.core.I18n.isRT()) {
				panelWorking.style.marginRight = "0px" ;
			}else{
				panelWorking.style.marginLeft = "0px" ;
			}
			if(layoutcookie.indexOf('2') < 0) layoutcookie = layoutcookie.concat(2) ;
			if(layoutcookie.indexOf('3') < 0) layoutcookie = layoutcookie.concat(3) ;
			eXo.core.Browser.setCookie(layoutMan.layoutId,layoutcookie,1);
		}
		
	} else {		
		layoutMan.layouts[layout-1].style.display = "block" ;
		if(layout == 1){			
			layoutMan.layouts[layout].style.display = "block" ;
			layoutMan.layouts[layout+1].style.display = "block" ;
			if (eXo.core.I18n.isRT()) {
				panelWorking.style.marginRight = "243px" ;
			}else{
				panelWorking.style.marginLeft = "243px" ;
			}
			if(layoutcookie.indexOf('2') >= 0) layoutcookie = layoutcookie.replace('2','') ;
			if(layoutcookie.indexOf('3') >= 0) layoutcookie = layoutcookie.replace('3','') ;
			eXo.core.Browser.setCookie(layoutMan.layoutId,layoutcookie,1);
		}
	}
	if(eXo.core.Browser.isFF() && document.getElementById("UIWeekView") && (layout == 1)) eXo.calendar.UIWeekView.onResize();
	if(eXo.core.Browser.isFF() && document.getElementById("UIMonthView") && (layout == 1)) eXo.calendar.UICalendarMan.initMonth();
};

UICalendarPortlet.prototype.checkLayoutCallback = function(layoutcookie){
	if (layoutcookie.indexOf("1") >=0) {
		var workingarea = eXo.core.DOMUtil.findNextElementByTagName(eXo.calendar.LayoutManager.layouts[0], "div");
		if (eXo.core.I18n.isRT()) {
			workingarea.style.marginRight = "0px";
		}else{
			workingarea.style.marginLeft = "0px";
		}
	}
};

UICalendarPortlet.prototype.resetLayoutCallback = function(){
	var workingarea = eXo.core.DOMUtil.findNextElementByTagName(eXo.calendar.LayoutManager.layouts[0], "div");
	if (eXo.core.I18n.isRT()) {
			workingarea.style.marginRight = "243px";
		}else{
			workingarea.style.marginLeft = "243px";
		}
	if(eXo.core.Browser.isFF() && document.getElementById("UIWeekView")) eXo.calendar.UIWeekView.onResize();
	if(eXo.core.Browser.isFF() && document.getElementById("UIMonthView")) eXo.calendar.UICalendarMan.initMonth();
};

/**
 * Check layout configuration when page load to render a right layout
 */
UICalendarPortlet.prototype.checkLayout = function(){
	eXo.calendar.LayoutManager = new LayoutManager("calendarlayout");
	var	layout1 = document.getElementById("UICalendarContainer") ;
	var	layout2 = document.getElementById("UIMiniCalendar") ;
	var	layout3 = document.getElementById("UICalendars") ;
	eXo.calendar.LayoutManager.layouts = [] ;
	eXo.calendar.LayoutManager.layouts.push(layout1);
	eXo.calendar.LayoutManager.layouts.push(layout2);
	eXo.calendar.LayoutManager.layouts.push(layout3);
	eXo.calendar.LayoutManager.switchCallback = eXo.calendar.UICalendarPortlet.switchLayoutCallback;
	eXo.calendar.LayoutManager.callback = eXo.calendar.UICalendarPortlet.checkLayoutCallback;
	eXo.calendar.LayoutManager.resetCallback = eXo.calendar.UICalendarPortlet.resetLayoutCallback;
	eXo.calendar.LayoutManager.check();
};

/** 
 * Switch among types of layout
 * @param {Int} layout Layout value in order number
 */
UICalendarPortlet.prototype.switchLayout = function(layout){
	var layoutMan = eXo.calendar.LayoutManager ;
	if(layout == 0){
		layoutMan.reset(); 
		return ;
	}
	layoutMan.switchLayout(layout);
};
/* for event */
/**
 * Initialize some properties in Day view
 */
UICalendarPortlet.prototype.init = function(){
    try {
        var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
        var uiDayViewGrid = document.getElementById("UIDayViewGrid");
        if (!uiDayViewGrid) 
            return false;
        UICalendarPortlet.viewer = eXo.core.DOMUtil.findFirstDescendantByClass(uiDayViewGrid, "div", "EventBoardContainer");
        UICalendarPortlet.step = 60;
    } 
    catch (e) {
        window.status = " !!! Error : " + e.message;
        return false;
    }
    return true;
};

/**
 * Get all event element
 * @param {Object} viewer DOM element contains all calendar events
 * @return All event from container
 */
UICalendarPortlet.prototype.getElements = function(viewer){
    var className = (arguments.length > 1) ? arguments[1] : "EventContainerBorder";
    var elements = eXo.core.DOMUtil.findDescendantsByClass(viewer, "div", className);
    var len = elements.length;
    var elems = new Array();
    for (var i = 0; i < len; i++) {
        if (elements[i].style.display != "none") {
            elements[i].style.left = "0%";
            elements[i].style.zIndex = 1;
            elems.push(elements[i]);
        }
    }
    return elems;
};

/**
 * Checks a DOM element is visible or hidden
 * @param {Object} obj DOM element
 * @return Boolean value
 */
UICalendarPortlet.prototype.isShow = function(obj){
    if (obj.style.display != "none") 
        return true;
    return false;
};

/**
 * Gets all visible event element
 * @param {Object} elements All calendar event
 * @return An array of DOM element
 */
UICalendarPortlet.prototype.getBlockElements = function(elements){
    var el = new Array();
    var len = elements.length;
    for (var i = 0; i < len; i++) {
        if (this.isShow(elements[i])) 
            el.push(elements[i]);
    }
    return el;
};

/**
 * Sets size for a DOM element that includes height and top properties
 * @param {Object} obj Calendar event element
 */
UICalendarPortlet.prototype.setSize = function(obj){
	var domUtil = eXo.core.DOMUtil;
    var start = parseInt(obj.getAttribute("startTime"));
    var end = parseInt(obj.getAttribute("endTime"));
    var eventContainer = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "EventContainer");
    if (end == 0) 
        end = 1440;
    end = (end != 0) ? end : 1440;
    height = Math.abs(start - end);
    if (height < 30) 
        height = 30;
    var styles = {
        "top": start + "px",
        "height": (height - 2) + "px"
    };
    eXo.calendar.UICalendarPortlet.setStyle(obj, styles);
	var busyIcon = domUtil.getChildrenByTagName(obj,"div")[0] ;
	if(!busyIcon ||  (busyIcon.offsetHeight <= 5)) busyIcon = domUtil.findFirstDescendantByClass(obj,"div","EventContainerBar") ;
	var extraHeight = busyIcon.offsetHeight + domUtil.findFirstDescendantByClass(obj,"div","ResizeEventContainer").offsetHeight;
    height -= (extraHeight + 5);
	eventContainer.style.height = height + "px";
};

/**
 * Sets width for a DOM element in percent unit
 * @param {Object} element A DOM element
 * @param {Object} width Width of element
 */
UICalendarPortlet.prototype.setWidth = function(element, width){
    element.style.width = width + "%";
};

/**
 * Gets starttime and endtime attribute of a calendar event element
 * @param {Object} el A calendar event element
 * @return A array includes two elements that are start and end time
 */
UICalendarPortlet.prototype.getSize = function(el){
    var start = parseInt(el.getAttribute("startTime"));
    var end = parseInt(el.getAttribute("endTime"));
		var delta = end - start ;
		if(delta < 30) end = start + 30 ;
    return [start, end];
};

/**
 * Gets interval time from a array of event elements
 * @param {Object} el Array of calendar events
 * @return An array of intervals
 */
UICalendarPortlet.prototype.getInterval = function(el){
    var bottom = new Array();
    var interval = new Array();
    var size = null;
    if (!el || (el.length <= 0)) 
        return;
    for (var i = 0; i < el.length; i++) {
        size = this.getSize(el[i]);
        bottom.push(size[1]);
        if (bottom[i - 1] && (size[0] > bottom[i - 1])) {
            interval.push(i);
        }
    }
    
    interval.unshift(0);
    interval.push(el.length);
    return interval;
};

/**
 * Sets dimension for event elements
 * @param {Object} el An array of calendar events
 * @param {Object} totalWidth Width of calendar event container
 */
UICalendarPortlet.prototype.adjustWidth = function(el, totalWidth){
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var inter = UICalendarPortlet.getInterval(el);
    if (el.length <= 0) 
        return;
    var width = "";
    for (var i = 0; i < inter.length; i++) {
        var totalWidth = (arguments.length > 1) ? arguments[1] : parseFloat(100);
        totalWidth -= 2 ;
        var offsetLeft = parseFloat(0);
        var left = parseFloat(0);
        if (arguments.length > 2) {
            offsetLeft = parseFloat(arguments[2]);
            left = arguments[2];
        }
        var len = (inter[i + 1] - inter[i]);
        if (isNaN(len)) 
            continue;
        var mark = null;
        if (i > 0) {
            for (var l = 0; l < inter[i]; l++) {
                if ((el[inter[i]].offsetTop > el[l].offsetTop) && (el[inter[i]].offsetTop < (el[l].offsetTop + el[l].offsetHeight))) {
                    mark = l;
                }
            }
            if (mark != null) {
                offsetLeft = parseFloat(el[mark].style.left) + parseFloat(el[mark].style.width);
            }
        }
        var n = 0;
        for (var j = inter[i]; j < inter[i + 1]; j++) {
            if (mark != null) {
                width = parseFloat((totalWidth + left - parseFloat(el[mark].style.left) - parseFloat(el[mark].style.width)) / len);
            }
            else {
                width = parseFloat(totalWidth / len);
            }
            UICalendarPortlet.setWidth(el[j], width);
            if (el[j - 1] && (len > 1)) 
//                el[j].style.left = offsetLeft + parseFloat(el[j - 1].style.width) * n + "%";
//                el[j].style.right = offsetLeft + parseFloat(el[j - 1].style.width) * n + "%";
                  setLeft(el[j],offsetLeft + parseFloat(el[j - 1].style.width) * n);
            else {
//                el[j].style.left = offsetLeft + "%";
//                el[j].style.right = offsetLeft + "%";
                  setLeft(el[j],offsetLeft);
            }
            n++;
        }
    }
    function setLeft(obj,left){
		obj.style.left = left + "%";
		if(eXo.core.I18n.isRT()){
			obj.style.right = left + "%";	
		}
	}
};
/**
 * Sort event elemnents in time table
 */
UICalendarPortlet.prototype.showEvent = function(){
    this.init();
    var EventDayContainer = eXo.core.DOMUtil.findAncestorByClass(this.viewer, "EventDayContainer");
//    this.setFocus(this.viewer, EventDayContainer);
    this.editAlldayEvent(EventDayContainer);
    if (!this.init()) 
        return;
    this.viewType = "UIDayView";
    var el = this.getElements(this.viewer);
    el = this.sortByAttribute(el, "startTime");
    if (el.length <= 0) 
        return;
    var marker = null;
    for (var i = 0; i < el.length; i++) {
        this.setSize(el[i]);
        el[i].onmousedown = eXo.calendar.UICalendarPortlet.initDND;
        el[i].ondblclick = eXo.calendar.UICalendarPortlet.ondblclickCallback;
        marker = eXo.core.DOMUtil.findFirstChildByClass(el[i], "div", "ResizeEventContainer");
        marker.onmousedown = eXo.calendar.UIResizeEvent.init;
    }
    this.items = el;
    this.adjustWidth(this.items);
    this.items = null;
    this.viewer = null;
};

UICalendarPortlet.prototype.editAlldayEvent = function(cont){
	cont = eXo.core.DOMUtil.findPreviousElementByTagName(cont,"div");
	var events = eXo.core.DOMUtil.findDescendantsByClass(cont,"div","EventContainerBorder");
	var i = events.length ;
	if(!events || (i <= 0)) return ;
	while(i--){
		events[i].ondblclick = this.ondblclickCallback;
	}
}


/**
 * Deal with incorrect event sorting when portlet loads in the first times
 */
UICalendarPortlet.prototype.onLoad = function(){	
    window.setTimeout("eXo.calendar.UICalendarPortlet.checkFilter() ;", 2000);
};

/**
 * Callback method when browser resizes
 */
UICalendarPortlet.prototype.browserResizeCallback = function(){
    if (!eXo.calendar.UICalendarPortlet.items) 
        return;
    eXo.calendar.UICalendarPortlet.adjustWidth(eXo.calendar.UICalendarPortlet.items);
}

/**
 * Callback method when double click on a calendar event
 */
UICalendarPortlet.prototype.ondblclickCallback = function(){
    var eventId = this.getAttribute("eventId");
    var calendarId = this.getAttribute("calid");
    var calendarType = this.getAttribute("caltype");
    eXo.webui.UIForm.submitEvent(eXo.calendar.UICalendarPortlet.portletId + '#' + eXo.calendar.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + eXo.calendar.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType);
}

/**
 * Sorts calendar event by their attribute
 * @param {Object} obj An array of calendar events
 * @param {Object} attribute A attribute to sort
 * @return An sorted array of calendar event
 */
UICalendarPortlet.prototype.sortByAttribute = function(obj, attribute){
    var len = obj.length;
    var tmp = null;
    var attribute1 = null;
    var attribute2 = null;
    for (var i = 0; i < len; i++) {
        for (var j = i + 1; j < len; j++) {
            attribute1 = parseInt(obj[i].getAttribute(attribute));
            attribute2 = parseInt(obj[j].getAttribute(attribute));
            if (attribute2 < attribute1) {
                tmp = obj[i];
                obj[i] = obj[j];
                obj[j] = tmp;
            }
            if (attribute2 == attribute1) {
                var end1 = parseInt(obj[i].getAttribute("endTime"));
                var end2 = parseInt(obj[j].getAttribute("endTime"));
                if (end2 > end1) {
                    tmp = obj[i];
                    obj[i] = obj[j];
                    obj[j] = tmp;
                }
            }
        }
    }
    return obj;
};
/* for resizing event box */
/**
 * Class to control calendar event resizing
 * @constructor
 */
function UIResizeEvent(){

}

/**
 * Initilize some propertis of UIResizeEvent
 * @param {Object} evt Mouse event
 */
UIResizeEvent.prototype.init = function(evt){
    var _e = window.event || evt;
    _e.cancelBubble = true;
    var UIResizeEvent = eXo.calendar.UIResizeEvent;
    var outerElement = eXo.core.DOMUtil.findAncestorByClass(this, 'EventBoxes');
    var innerElement = eXo.core.DOMUtil.findPreviousElementByTagName(this, "div");
    var container = eXo.core.DOMUtil.findAncestorByClass(outerElement, 'EventDayContainer');
    var minHeight = 15;
    var interval = eXo.calendar.UICalendarPortlet.interval;
    UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval);
    UIResizeEvent.callback = UIResizeEvent.resizeCallback;
};

UIResizeEvent.prototype.getOriginalHeight = function(obj){
	var domUtil = eXo.core.DOMUtil;
	var paddingTop = domUtil.getStyle(obj,"paddingTop",true);
	var paddingBottom = domUtil.getStyle(obj,"paddingBottom",true);
	var originalHeight = obj.offsetHeight - (paddingTop + paddingBottom);
	return originalHeight;
}

/**
 * Sets up calendar event resizing when mouse down on it
 * @param {Object} evt Mouse event
 * @param {Object} innerElement DOM element before maker element
 * @param {Object} outerElement DOM element after maker element
 * @param {Object} container DOM element contains all events
 * @param {Object} minHeight Minimum height to resize
 * @param {Object} interval Resizing step( default is 30 minutes)
 */
UIResizeEvent.prototype.start = function(evt, innerElement, outerElement, container, minHeight, interval){
    var _e = window.event || evt;
    var UIResizeEvent = eXo.calendar.UIResizeEvent;
    this.innerElement = innerElement;
    this.outerElement = outerElement;
    this.container = container;
    eXo.calendar.UICalendarPortlet.resetZIndex(this.outerElement);
    this.minHeight = (minHeight) ? parseInt(minHeight) : 15;
    this.interval = (interval != "undefined") ? parseInt(interval) : 15;
    document.onmousemove = UIResizeEvent.execute;
    document.onmouseup = UIResizeEvent.end;
    this.beforeHeight = this.getOriginalHeight(this.outerElement);
    this.innerElementHeight = this.getOriginalHeight(this.innerElement);
    this.posY = _e.clientY;
    this.uppermost = outerElement.offsetTop + minHeight - container.scrollTop;
    if (document.getElementById("UIPageDesktop")) {
        var uiWindow = eXo.core.DOMUtil.findAncestorByClass(container, "UIResizableBlock");
        this.uppermost -= uiWindow.scrollTop;
    }
};

/**
 * Executes calendar event resizing
 * @param {Object} evt Mouse event
 */
UIResizeEvent.prototype.execute = function(evt){
    var _e = window.event || evt;
    var UIResizeEvent = eXo.calendar.UIResizeEvent;
    var mouseY = eXo.core.Browser.findMouseRelativeY(UIResizeEvent.container, _e);
    var mDelta = _e.clientY - UIResizeEvent.posY;
    if (mouseY <= UIResizeEvent.uppermost) {
        return;
    }
    else {
        if (mDelta % UIResizeEvent.interval == 0) {
            UIResizeEvent.outerElement.style.height = UIResizeEvent.beforeHeight - 2 + mDelta + "px";
            UIResizeEvent.innerElement.style.height = UIResizeEvent.innerElementHeight + mDelta + "px";
            
        }
    }
		var min = (eXo.core.Browser.isIE6())?(UIResizeEvent.outerElement.offsetTop - 1) : UIResizeEvent.outerElement.offsetTop;
		eXo.calendar.UICalendarPortlet.updateTitle(UIResizeEvent.outerElement, UIResizeEvent.outerElement.offsetTop, 1);
};

/**
 * End calendar event resizing, this method clean up some unused properties and execute callback function
 * @param {Object} evt Mouse event
 */
UIResizeEvent.prototype.end = function(evt){
    var _e = window.event || evt;
    var UIResizeEvent = eXo.calendar.UIResizeEvent;
    if (typeof(UIResizeEvent.callback) == "function") 
        UIResizeEvent.callback();
    UIResizeEvent.innerElement = null;
    UIResizeEvent.outerElement = null;
    UIResizeEvent.posY = null;
    UIResizeEvent.minHeight = null;
    UIResizeEvent.interval = null;
    UIResizeEvent.innerElementHeight = null;
    UIResizeEvent.outerElementHeight = null;
    document.onmousemove = null;
    document.onmouseup = null;
    UIResizeEvent.container = null;
    UIResizeEvent.innerElementHeight = null;
    UIResizeEvent.beforeHeight = null;
    UIResizeEvent.posY = null;
    UIResizeEvent.uppermost = null;
};

/**
 * Resizing callback method
 * @param {Object} evt Mouse object
 */
UIResizeEvent.prototype.resizeCallback = function(evt){
    var UIResizeEvent = eXo.calendar.UIResizeEvent;
    var eventBox = UIResizeEvent.outerElement;
    var start = parseInt(eventBox.getAttribute("startTime"));
    var calType = eventBox.getAttribute("calType");
    var end = start + eventBox.offsetHeight;
    if (eventBox.offsetHeight != UIResizeEvent.beforeHeight) {
//        var actionLink = eXo.calendar.UICalendarPortlet.adjustTime(start, end, eventBox);
//        if (calType) 
//            actionLink = actionLink.replace(/'\s*\)/, "&calType=" + calType + "')");
//        eval(actionLink);
		var actionLink = eventBox.getAttribute("actionLink");
		var params = [
			{name:"calendarId",value:eventBox.getAttribute("calid")},
			{name:"startTime",value:start},
			{name:"finishTime",value:end}
		];
		eXo.calendar.UICalendarPortlet.setTimeValue(eventBox,start,end);
		eXo.calendar.UICalendarPortlet.showEvent();
		ajaxAsyncGetRequest(eXo.cs.Utils.createUrl(actionLink,params), false) ;
    }
};

/* for drag and drop */
/**
 * Resets z-Index of DOM element when drag and drop calendar event
 * @param {Object} obj DOM element
 */
UICalendarPortlet.prototype.resetZIndex = function(obj){
    try {
        var maxZIndex = parseInt(obj.style.zIndex);
        var items = eXo.core.DOMUtil.getChildrenByTagName(obj.parentNode, "div");
        var len = items.length;
        for (var i = 0; i < len; i++) {
            if (!items[i].style.zIndex) 
                items[i].style.zIndex = 1;
            if (parseInt(items[i].style.zIndex) > maxZIndex) {
                maxZIndex = parseInt(items[i].style.zIndex);
            }
        }
        obj.style.zIndex = maxZIndex + 1;
    } 
    catch (e) {
        //alert(e.message) ;
    }
};
/**
 * Initializes drag and drop actions
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.initDND = function(evt){
    var _e = window.event || evt;
    _e.cancelBubble = true;
		if(eXo.core.EventManager.getMouseButton(evt) == 2) return ;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    UICalendarPortlet.dragObject = this;
    UICalendarPortlet.resetZIndex(UICalendarPortlet.dragObject);
    UICalendarPortlet.dragContainer = eXo.core.DOMUtil.findAncestorByClass(UICalendarPortlet.dragObject, "EventDayContainer");
    UICalendarPortlet.resetZIndex(UICalendarPortlet.dragObject);
    UICalendarPortlet.eventY = _e.clientY;
    UICalendarPortlet.eventTop = UICalendarPortlet.dragObject.offsetTop;
    UICalendarPortlet.dragContainer.onmousemove = UICalendarPortlet.dragStart;
    UICalendarPortlet.dragContainer.onmouseup = UICalendarPortlet.dragEnd;
    UICalendarPortlet.title = eXo.core.DOMUtil.findDescendantsByTagName(UICalendarPortlet.dragObject, "p")[0].innerHTML;
};
/**
 * Processes when dragging object
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.dragStart = function(evt){
    var _e = window.event || evt;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var delta = null;
    var mouseY = eXo.core.Browser.findMouseRelativeY(UICalendarPortlet.dragContainer, _e) + UICalendarPortlet.dragContainer.scrollTop;
    var posY = UICalendarPortlet.dragObject.offsetTop;
    var height = UICalendarPortlet.dragObject.offsetHeight;
    if (mouseY <= posY) {
        UICalendarPortlet.dragObject.style.top = parseInt(UICalendarPortlet.dragObject.style.top) - UICalendarPortlet.interval + "px";
    }
    else {
        if (mouseY >= (posY + height)) {
            UICalendarPortlet.dragObject.style.top = parseInt(UICalendarPortlet.dragObject.style.top) + UICalendarPortlet.interval + "px";
        }
        else {
            delta = _e.clientY - UICalendarPortlet.eventY;
            if (delta % UICalendarPortlet.interval == 0) {
                var top = UICalendarPortlet.eventTop + delta;
                UICalendarPortlet.dragObject.style.top = top + "px";
            }
        }
		}
    UICalendarPortlet.updateTitle(UICalendarPortlet.dragObject, posY);
};
/**
 * Updates title of event when dragging calendar event
 * @param {Object} events DOM elemnt contains a calendar event
 * @param {Object} min Time in minutes
 */
UICalendarPortlet.prototype.updateTitle = function(events, min, type){
    var timeFormat = events.getAttribute("timeFormat");
    var title = eXo.core.DOMUtil.findDescendantsByTagName(events, "p")[0];
		var delta = parseInt(events.getAttribute("endTime")) - parseInt(events.getAttribute("startTime")) ;
    timeFormat = (timeFormat) ? eval(timeFormat) : {
        am: "AM",
        pm: "PM"
    };
		if (type == 1) {
			title.innerHTML = this.minToTime(min, timeFormat) + " - " + this.minToTime(min + events.offsetHeight, timeFormat);
			return ;
		}	
    title.innerHTML = this.minToTime(min, timeFormat) + " - " + this.minToTime(min + delta, timeFormat);
}

/**
 * End calendar event dragging, this method clean up some unused properties and execute callback function
 */
UICalendarPortlet.prototype.dragEnd = function(){
    this.onmousemove = null;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var dragObject = UICalendarPortlet.dragObject;
    var calType = dragObject.getAttribute("calType");
    var start = parseInt(dragObject.getAttribute("startTime"));
    var end = parseInt(dragObject.getAttribute("endTime"));
    var title = eXo.core.DOMUtil.findDescendantsByTagName(dragObject, "p")[0];
    var titleName = UICalendarPortlet.title;
    if (end == 0) 
        end = 1440;
    var delta = end - start;
    var currentStart = dragObject.offsetTop;
    var currentEnd = currentStart + delta;
    var eventDayContainer = eXo.core.DOMUtil.findAncestorByClass(dragObject, "EventDayContainer");
    var eventTop = UICalendarPortlet.eventTop;
    eventDayContainer.onmousemove = null;
    eventDayContainer.onmouseup = null;
    UICalendarPortlet.dragObject = null;
    UICalendarPortlet.eventTop = null;
    UICalendarPortlet.eventY = null;
    UICalendarPortlet.dragContainer = null;
    UICalendarPortlet.title = null;
    if (dragObject.offsetTop != eventTop) {
//        var actionLink = UICalendarPortlet.adjustTime(currentStart, currentEnd, dragObject);
//        if (calType) 
//            actionLink = actionLink.replace(/'\s*\)/, "&calType=" + calType + "')");
//        eval(actionLink);
		var actionLink = dragObject.getAttribute("actionLink");
		var params = [
			{name:"calendarId",value:dragObject.getAttribute("calid")},
			{name:"startTime",value:currentStart},
			{name:"finishTime",value:currentEnd}
		];
		eXo.calendar.UICalendarPortlet.setTimeValue(dragObject,currentStart,currentEnd);
		eXo.calendar.UICalendarPortlet.showEvent();
		ajaxAsyncGetRequest(eXo.cs.Utils.createUrl(actionLink,params), false) ;
    }
    //title.innerHTML = titleName;
};

/* for adjusting time */
/**
 * Change action link excuted when drop event
 * @param {Object} currentStart Current start time of calendar event 
 * @param {Object} currentEnd Current end time of calendar event 
 * @param {Object} obj Calendar event
 * @return Corrected action link
 */
UICalendarPortlet.prototype.adjustTime = function(currentStart, currentEnd, obj){
    var actionLink = obj.getAttribute("actionLink");
    var pattern = /startTime.*endTime/g;
    var params = "startTime=" + currentStart + "&finishTime=" + currentEnd;
    actionLink = actionLink.replace(pattern, params).replace("javascript:", "");
    return actionLink;
};

/* for showing context menu */
/**
 * Sets up context menu for Calendar portlet
 * @param {Object} compid Portlet id
 */
UICalendarPortlet.prototype.showContextMenu = function(compid){
    var UIContextMenu = eXo.webui.UIContextMenu;
    this.portletName = compid;
    UIContextMenu.portletName = this.portletName;
    var config = {
        'preventDefault': false,
        'preventForms': false
    };
    UIContextMenu.init(config);
    UIContextMenu.attach("CalendarContentNomal", "UIMonthViewRightMenu");
    UIContextMenu.attach("EventOnDayContent", "UIMonthViewEventRightMenu");
    UIContextMenu.attach("TimeRule", "UIDayViewRightMenu");
    UIContextMenu.attach("EventBoxes", "UIDayViewEventRightMenu");
    UIContextMenu.attach(["Weekday","Weekend","Today", "EventAlldayContainer"], "UIWeekViewRightMenu");
    UIContextMenu.attach("UIListViewRow", "UIListViewEventRightMenu");
    UIContextMenu.attach("CalendarItemPrivate", "CalendarPopupMenu");
    UIContextMenu.attach("CalendarItemPublic", "CalendarPopupMenu");
    UIContextMenu.attach("CalendarItemShared", "CalendarPopupMenu2");
    UIContextMenu.attach("GroupItem", "CalendarGroupPopupMenu");
    if(document.getElementById("UIPageDesktop")) this.firstRun = false ;
    this.fixIE();
};

/**
 * Fixs relative positioning problems in IE
 */
UICalendarPortlet.prototype.fixIE = function(){
    var isDesktop = document.getElementById("UIPageDesktop");
    if ((eXo.core.Browser.browserType == "ie") && isDesktop) {
        var portlet = document.getElementById(this.portletName);
        var uiResizeBlock = eXo.core.DOMUtil.findAncestorByClass(portlet, "UIResizableBlock");
        var relative = eXo.core.DOMUtil.findFirstDescendantByClass(uiResizeBlock, "div", "FixIE");
        if (!relative) 
            return;
        relative.className = "UIResizableBlock";
        var style = {
            position: "relative",
            height: uiResizeBlock.offsetHeight + 'px',
            width: "100%",
            overflow: "auto"
        };
        this.setStyle(relative, style);
    }
};

/**
 * Callback method when right click in list view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.listViewCallack = function(evt){
    var _e = window.event || evt;
    var src = _e.srcElement || _e.target;
    if (!eXo.core.DOMUtil.hasClass(src, "UIListViewRow")) 
        src = eXo.core.DOMUtil.findAncestorByClass(src, "UIListViewRow");
    var eventId = src.getAttribute("eventid");
    var calendarId = src.getAttribute("calid");
    var calType = src.getAttribute("calType");
    map = {
        "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
        "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
        "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType
    };
    eXo.webui.UIContextMenu.changeAction(eXo.webui.UIContextMenu.menuElement, map);
};

/**
 * Callback method when right click in day view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.dayViewCallback = function(evt){
    var _e = window.event || evt;
    var src = _e.srcElement || _e.target;
   
     
    var map = null;
    if (src.nodeName == "TD") {
        src = eXo.core.DOMUtil.findAncestorByTagName(src, "tr");
        var startTime = parseInt(Date.parse(src.getAttribute('startFull')));
    	/*var endTime = parseInt(Date.parse(DOMUtil.findAncestorByTagName(src, "td").getAttribute('startFull')))  + 24*60*60*1000 - 1;
    	
        var startTime = parseInt(src.getAttribute("startTime"));*/
        var endTime = startTime + 15*60*1000 ;
        var items = eXo.core.DOMUtil.findDescendantsByTagName(eXo.webui.UIContextMenu.menuElement, "a");
        for(var i = 0; i < items.length; i++){
        	var aTag = items[i];
        	if(eXo.core.DOMUtil.hasClass(aTag, "QuickAddEvent")) {
        		aTag.href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,1,"+startTime+","+endTime+");" 
        	} else if(eXo.core.DOMUtil.hasClass(aTag, "QuickAddTask")) {
        		aTag.href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,2,"+startTime+","+endTime+");"
        	}
        }
        /*map = {
            "startTime\s*=\s*.*(?=&|'|\")": "startTime=" + startTime
        };*/
    }
    else {
        src = (eXo.core.DOMUtil.hasClass(src, "EventBoxes")) ? src : eXo.core.DOMUtil.findAncestorByClass(src, "EventBoxes");
        var eventId = src.getAttribute("eventid");
        var calendarId = src.getAttribute("calid");
        var calType = src.getAttribute("calType");
        map = {
            "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
            "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
            "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType
        };
    }
    eXo.webui.UIContextMenu.changeAction(eXo.webui.UIContextMenu.menuElement, map);
};

/**
 * Callback method when right click in week view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.weekViewCallback = function(evt){
    var src = eXo.core.EventManager.getEventTarget(evt);
    var DOMUtil = eXo.core.DOMUtil;
    var UIContextMenu = eXo.webui.UIContextMenu;
    var map = null;
    var obj = eXo.core.EventManager.getEventTargetByClass(evt,"WeekViewEventBoxes");
    var items = DOMUtil.findDescendantsByTagName(UIContextMenu.menuElement, "a");
    if (obj) {
				var eventId = obj.getAttribute("eventid");
        var calendarId = obj.getAttribute("calid");
        var calType = obj.getAttribute("calType");
        map = {
            "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
            "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId
        };
        if (calType) {
            map = {
                "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
                "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
                "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType
            };
        }
		if(!DOMUtil.hasClass(obj,"EventAlldayContainer")){
			var container = DOMUtil.findAncestorByClass(src,"EventWeekContent");
			var mouseY = (eXo.core.Browser.findMouseRelativeY(container,evt) + container.scrollTop)*60000;
			obj =parseInt(DOMUtil.findAncestorByTagName(src, "td").getAttribute("startTime")) + mouseY;
		} else obj = null;
        for (var i = 0; i < items.length; i++) {
            if (DOMUtil.hasClass(items[i].parentNode,"EventActionMenu")) {
                items[i].parentNode.style.display = "block";
                items[i].href = UIContextMenu.replaceall(String(items[i].href), map);
            }
            else {
                //TODO Menu on allday events
            	//items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/, "startTime=" + obj);
                /*var fTime = parseInt(obj);
                var tTime = fTime + 15*60*1000 ;*/
        		if(DOMUtil.hasClass(items[i],"QuickAddEvent")){
        			items[i].style.display="none" ;
        			/*items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 1,"+fTime+","+tTime+");"
        			 if(isNaN(fTime)) {
        				 items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 1);" ;
        		     } */
            		 
            	} else if (DOMUtil.hasClass(items[i],"QuickAddTask")) {
            		items[i].style.display="none" ;
            		/*items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 2, "+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 2);" ;
        		     } */
            	}
            }
        }
    } else {
		var container = DOMUtil.findAncestorByClass(src,"EventWeekContent");
		var mouseY = (eXo.core.Browser.findMouseRelativeY(container,evt) + container.scrollTop)*60000;
        obj = eXo.core.EventManager.getEventTargetByTagName(evt,"td"); //(DOMUtil.findAncestorByTagName(src, "td")) ? DOMUtil.findAncestorByTagName(src, "td") : src;
        map = eXo.calendar.UICalendarPortlet.getBeginDay(obj.getAttribute("startTime")) + mouseY;
        for (var i = 0; i < items.length; i++) {
            if (items[i].style.display == "block") {
                items[i].style.display = "none";
            }
            else {
            	items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/, "startTime=" + map);
                var fTime = parseInt(map);
                var tTime = fTime + 15*60*1000 ;
                
            	if(DOMUtil.hasClass(items[i],"QuickAddEvent")){
            		items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 1,"+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 1);" ;
        		     } 
            	} else if (DOMUtil.hasClass(items[i],"QuickAddTask")) {
            		items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this, 2, "+fTime+","+tTime+");"
            		 if(isNaN(fTime)) {
        				 items[i].href = "javascript:eXo.calendar.UICalendarPortlet.addQuickShowHidden(this, 2);" ;
        		     } 
            	}
            }
            
        }
    }
};

/**
 * Callback method when right click in month view
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.monthViewCallback = function(evt){
    var _e = window.event || evt;
    var src = _e.srcElement || _e.target;
    var UIContextMenu = eXo.webui.UIContextMenu;
    var DOMUtil = eXo.core.DOMUtil;
    var objectValue = "";
    var links = eXo.core.DOMUtil.findDescendantsByTagName(UIContextMenu.menuElement, "a");
    if (!DOMUtil.findAncestorByClass(src, "EventBoxes")) {
        if (objectValue = DOMUtil.findAncestorByTagName(src, "td").getAttribute("startTime")) {
        	//TODO CS-2800
        	var startTime = parseInt(Date.parse(DOMUtil.findAncestorByTagName(src, "td").getAttribute('startTimeFull')));
        	var endTime = parseInt(Date.parse(DOMUtil.findAncestorByTagName(src, "td").getAttribute('startTimeFull')))  + 24*60*60*1000 - 1;
        	for(var i = 0; i < links.length; i++){
            	if(DOMUtil.hasClass(links[i], "QuickAddEvent")) {
            		links[i].href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,1,"+startTime+","+endTime+");" 
            	} else if(DOMUtil.hasClass(links[i], "QuickAddTask")) {
            		links[i].href="javascript:eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(this,2,"+startTime+","+endTime+");"
            	}
            }
        	/*var map = {
                "startTime\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "startTime=" + objectValue
            };
            UIContextMenu.changeAction(UIContextMenu.menuElement, map);*/
        }
    }
    else 
        if (objvalue = DOMUtil.findAncestorByClass(src, "DayContentContainer")) {
            var eventId = objvalue.getAttribute("eventId");
            var calendarId = objvalue.getAttribute("calId");
            var calType = objvalue.getAttribute("calType");
            var map = {
                "objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "objectId=" + eventId,
                "calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calendarId=" + calendarId,
                "calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")": "calType=" + calType
            };
            UIContextMenu.changeAction(UIContextMenu.menuElement, map);
        }
        else {
            return;
        }
};
/* BOF filter */

/**
 * Gets all calendar events of a calendar by its id
 * @param {Object} events All calendar events
 * @param {Object} calid Calendar id
 * @return All events of certain calendar
 */
UICalendarPortlet.prototype.getEventsByCalendar = function(events, calid){
    var calendarid = null;
    var len = events.length;
    var event = new Array();
    for (var i = 0; i < len; i++) {
        calendarid = events[i].getAttribute("calid");
        if (calendarid == calid) 
            event.push(events[i]);
    }
    return event;
};

/**
 * Gets all of events for filtering
 * @param {Object} events All calendar events
 * @return An array of events for filtering
 */
UICalendarPortlet.prototype.getEventsForFilter = function(events){
    var form = this.filterForm;
    var checkbox = eXo.core.DOMUtil.findDescendantsByClass(form, "input", "checkbox");
    var el = new Array();
    var len = checkbox.length;
    var calid = null;
    for (var i = 0; i < len; i++) {
        if (checkbox[i].checked) {
            calid = checkbox[i].name;
            el.pushAll(this.getEventsByCalendar(events, calid));
        }
    }
    return el;
};

/**
 * Filters calendar event by calendar group
 */
UICalendarPortlet.prototype.filterByGroup = function(){
    var DOMUtil = eXo.core.DOMUtil;
    var uiVtab = DOMUtil.findAncestorByClass(this, "UIVTab");
    var checkboxes = DOMUtil.findDescendantsByClass(uiVtab, "input", "checkbox");
    var checked = this.checked;
    var len = checkboxes.length;
    for (var i = 0; i < len; i++) {
        eXo.calendar.UICalendarPortlet.runFilterByCalendar(checkboxes[i].name, checked);
        if (checkboxes[i].checked == checked) 
            continue;
        checkboxes[i].checked = checked;
    }
};

/**
 * Filters calendar event by calendar
 * @param {Object} calid Calendar id
 * @param {Boolean} checked Status of calendar(activated or disactivated)
 */
UICalendarPortlet.prototype.runFilterByCalendar = function(calid, checked){
    var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer");
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    if (!uiCalendarViewContainer) 
        return;
    var className = "EventBoxes";
    if (document.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var events = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className);
    if (!events) 
        return;
    var len = events.length;
    for (var i = 0; i < len; i++) {
        if (events[i].getAttribute("calId") == calid) {
            if (checked) {
                events[i].style.display = "block";
            }
            else {
                events[i].style.display = "none";
            }
        }
    } 
};

/**
 * Filters calendar event by calendar
 */
UICalendarPortlet.prototype.filterByCalendar = function(){
    var calid = this.name;
    var checked = this.checked;
    var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer");
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    if (!uiCalendarViewContainer) 
        return;
    var className = "EventBoxes";
    if (document.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var events = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className);
    if (!events) 
        return;
    var len = events.length;
    for (var i = 0; i < len; i++) {
        if (events[i].getAttribute("calId") == calid) {
            if (checked) {
                events[i].style.display = "block";
            }
            else {
                events[i].style.display = "none";
            }
        }
    }
    UICalendarPortlet.runFilterByCategory();
    try { //TODO: review order javascript file 
        if (document.getElementById("UIMonthView")) 
            eXo.calendar.UICalendarMan.initMonth();
        if (document.getElementById("UIDayViewGrid")) 
            eXo.calendar.UICalendarPortlet.showEvent();
        if (document.getElementById("UIWeekViewGrid")) {
            eXo.calendar.UICalendarMan.initWeek();
            eXo.calendar.UIWeekView.init();
        }
    } 
    catch (e) {
    };
    
};

/**
 * Filters events by event category
 */
UICalendarPortlet.prototype.filterByCategory = function(){
    var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer");
    if (!uiCalendarViewContainer) 
        return;
    var category = this.options[this.selectedIndex].value;
    eXo.calendar.UICalendarPortlet.selectedCategory = category;
    var className = "EventBoxes";
    if (document.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var allEvents = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className);
    var events = eXo.calendar.UICalendarPortlet.getEventsForFilter(allEvents);
    if (!events) 
        return;
    var len = events.length;
    for (var i = 0; i < len; i++) {
        if (category == events[i].getAttribute("eventCat")) {
            events[i].style.display = "block";
        }
        else 
            if (category == "" || category == "all") {
                events[i].style.display = "block";
            }
            else 
                events[i].style.display = "none";
    }
    if (document.getElementById("UIMonthView")) 
        eXo.calendar.UICalendarMan.initMonth();
    if (document.getElementById("UIDayViewGrid")) 
        eXo.calendar.UICalendarPortlet.showEvent();
    if (document.getElementById("UIWeekViewGrid")) {
        eXo.calendar.UICalendarMan.initWeek();
        eXo.calendar.UIWeekView.init();
    }
};

/**
 * Filters event by event category
 * @param {Object} selectobj Select element
 */
UICalendarPortlet.prototype.runFilterByCategory = function(){
    var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer");
		selectobj = eXo.core.DOMUtil.findFirstDescendantByClass(uiCalendarViewContainer,"select","selectbox");
    if (!selectobj) return;
    var category = selectobj.options[selectobj.selectedIndex].value;
    var className = "EventBoxes";
    if (document.getElementById("UIWeekViewGrid")) 
        className = "WeekViewEventBoxes"; // TODO : review event box gettting
    var allEvents = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className);
    var events = eXo.calendar.UICalendarPortlet.getEventsForFilter(allEvents);
    if (!events) 
        return;
    var len = events.length;
    for (var i = 0; i < len; i++) {
        if (category == events[i].getAttribute("eventCat")) {
            events[i].style.display = "block";
        }
        else 
            if (category == "" || category == "all") {
                events[i].style.display = "block";
            }
            else 
                events[i].style.display = "none";
    }
};

UICalendarPortlet.prototype.runAction = function(obj){
	var actionLink = obj.getAttribute("actionLink");
	var categoryId = this.filterSelect.options[this.filterSelect.selectedIndex].value;
	actionLink = actionLink.replace("')","&categoryId="+categoryId+"')");
	eval(actionLink);
};

/**
 * Gets filtering form and sets up filtering actions for checkboxes containing calendar group id and calendar id
 * @param {Object} form Form id containing calendar group id and calendar id
 */
UICalendarPortlet.prototype.getFilterForm = function(form){
    if (typeof(form) == "string") 
        form = document.getElementById(form);
    this.filterForm = form;
    var CalendarGroup = eXo.core.DOMUtil.findDescendantsByClass(form, "input", "CalendarGroup");
    var CalendarItem = eXo.core.DOMUtil.findDescendantsByClass(form, "input", "checkbox");
    var len = CalendarGroup.length;
    var clen = CalendarItem.length;
    for (var i = 0; i < len; i++) {
        CalendarGroup[i].onclick = eXo.calendar.UICalendarPortlet.filterByGroup;
    }
    for (var j = 0; j < clen; j++) {
        CalendarItem[j].onclick = eXo.calendar.UICalendarPortlet.filterByCalendar;
    }
};

/**
 * Gets select element that contains event category and sets up filtering action by event category
 * @param {Object} form Form id contains event category select element
 */
UICalendarPortlet.prototype.getFilterSelect = function(form){
    if (typeof(form) == "string") 
        form = document.getElementById(form);
    var eventCategory = eXo.core.DOMUtil.findFirstDescendantByClass(form, "div", "EventCategory");
		if (!eventCategory) return ;
    var select = eXo.core.DOMUtil.findDescendantsByTagName(eventCategory, "select")[0];
    var onchange = select.getAttribute("onchange");
    if (!onchange) 
        select.onchange = eXo.calendar.UICalendarPortlet.filterByCategory;
    this.filterSelect = select;
};

/**
 * Sets selected event category
 * @param {Object} form Form id contains event category select element
 */
UICalendarPortlet.prototype.setSelected = function(form){
    try {
      this.getFilterSelect(form);
      this.selectedCategory = this.filterSelect.options[this.filterSelect.selectedIndex].value;
    	this.listViewDblClick(form);
		} 
    catch (e) {
			this.listViewDblClick(form);
		}
};

UICalendarPortlet.prototype.listViewDblClick = function(form){
	form = (typeof(form) == "string")? document.getElementById(form):form ;
	if(!form) return ;
	var tr = eXo.core.DOMUtil.findDescendantsByClass(form,"tr","UIListViewRow");
	var i = tr.length ;
	eXo.calendar.UICalendarPortlet.viewType = "UIListView";
	var chk = null ;
	while(i--){
		eXo.core.EventManager.addEvent(tr[i],"dblclick",this.listViewDblClickCallback);
	}
};

UICalendarPortlet.prototype.doClick = function(){
	if(eXo.calendar.UICalendarPortlet.dblDone){
		delete eXo.calendar.UICalendarPortlet.dblDone;
		window.clearTimeout(eXo.calendar.UICalendarPortlet.clickone);
		return ;
	}
	eval(eXo.calendar.UICalendarPortlet.listViewAction);
};

UICalendarPortlet.prototype.listViewClickCallback = function(obj){
	this.listViewAction = obj.getAttribute("actionLink");
	this.clickone = setTimeout(this.doClick,200);
	return false ;
};

UICalendarPortlet.prototype.ondblclickCallbackInListView = function(obj){
	var eventId = obj.getAttribute("eventid");
	var calendarId = obj.getAttribute("calid");
	var calendarType = obj.getAttribute("caltype");
	eXo.webui.UIForm.submitEvent(eXo.calendar.UICalendarPortlet.portletId+'#' + eXo.calendar.UICalendarPortlet.viewType, 'Edit', '&subComponentId=' + eXo.calendar.UICalendarPortlet.viewType + '&objectId=' + eventId + '&calendarId=' + calendarId + '&calType=' + calendarType);
};

UICalendarPortlet.prototype.listViewDblClickCallback = function(){
	eXo.calendar.UICalendarPortlet.dblDone = true;
	eXo.calendar.UICalendarPortlet.ondblclickCallbackInListView(this);
};
/**
 * Filter event when page load
 */
UICalendarPortlet.prototype.checkFilter = function(){	
		var w = document.getElementById("UICalendarViewContainer");
		if(w.offsetParent && ((w.offsetParent.offsetWidth - w.offsetWidth) < 10)) {
			eXo.calendar.UICalendarPortlet.firstLoadTimeout = window.setTimeout(eXo.calendar.UICalendarPortlet.checkFilter,0);
			return ;
		}
		
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    for (var i = 0; i < UICalendarPortlet.filterSelect.options.length; i++) {
        if (UICalendarPortlet.filterSelect.options[i].value == UICalendarPortlet.selectedCategory) {
            UICalendarPortlet.filterSelect.options[i].selected = true;
        }
    }
    UICalendarPortlet.checkCalendarFilter();
		if (document.getElementById("UIMonthView")) 
        eXo.calendar.UICalendarMan.initMonth();
    if (document.getElementById("UIDayViewGrid")) 
        eXo.calendar.UICalendarPortlet.showEvent();
    if (document.getElementById("UIWeekViewGrid")) {
        eXo.calendar.UICalendarMan.initWeek();
        eXo.calendar.UIWeekView.init();
    }
		UICalendarPortlet.setFocus();
		if(eXo.calendar.UICalendarPortlet.firstLoadTimeout) delete eXo.calendar.UICalendarPortlet.firstLoadTimeout;
};

/**
 * Filter event by calendar when page load
 */
UICalendarPortlet.prototype.checkCalendarFilter = function(){
    if (!this.filterForm) 
        return;
    var checkbox = eXo.core.DOMUtil.findDescendantsByClass(this.filterForm, "input", "checkbox");
    var len = checkbox.length;
    for (var i = 0; i < len; i++) {
        this.runFilterByCalendar(checkbox[i].name, checkbox[i].checked);
    }
    this.runFilterByCategory();
};

/**
 * Filter event by event category when page load
 */
UICalendarPortlet.prototype.checkCategoryFilter = function(){
    if (this.filterSelect) 
        eXo.calendar.UICalendarPortlet.runFilterByCategory();
};

/* EOF filter */
/**
 * Change among task and event view in list view
 * @param {Object} obj DOM element
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.switchListView = function(obj, evt){
    var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIPopupCategory");
    if (eXo.core.Browser.isIE6()) {
        var size = {
            top: obj.offsetHeight,
            left: "-" + obj.offsetWidth
        };
        this.setStyle(menu, size);
    }
    else {
        var size = {
            marginLeft: "-18px"
        };
        this.setStyle(menu, size);
    }
    eXo.webui.UIPopupSelectCategory.show(obj, evt);
};

/**
 * Shows view menu
 * @param {Object} obj DOM element
 * @param {Object} evt Mouse event
 */
UICalendarPortlet.prototype.showView = function(obj, evt){
//    var _e = window.event || evt;
//    _e.cancelBubble = true;
		eXo.core.EventManager.cancelBubble(evt);
    var oldmenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu");
    var actions = eXo.core.DOMUtil.findDescendantsByClass(oldmenu, "a", "ItemLabel");
    if (!this.selectedCategory) 
        this.selectedCategory = null;
    for (var i = 0; i < actions.length; i++) {
        if (actions[i].href.indexOf("categoryId") < 0) 
            continue;
        actions[i].href = String(actions[i].href).replace(/categoryId.*&/, "categoryId=" + this.selectedCategory + "&");
    }
    eXo.calendar.UICalendarPortlet.swapMenu(oldmenu, obj);
};

/**
 * Swap menu in IE
 * @param {Object} menu Menu DOM element
 * @param {Object} clickobj Click DOM element
 */
UICalendarPortlet.prototype.swapIeMenu = function(menu, clickobj){
    var DOMUtil = eXo.core.DOMUtil;
    var Browser = eXo.core.Browser;
    var x = Browser.findPosXInContainer(clickobj, menu.offsetParent) - eXo.cs.Utils.getScrollLeft(clickobj);
    var y = Browser.findPosYInContainer(clickobj, menu.offsetParent) - eXo.cs.Utils.getScrollTop(clickobj) + clickobj.offsetHeight;
    var browserHeight = document.documentElement.clientHeight;
    var uiRightClickPopupMenu = (!DOMUtil.hasClass(menu, "UIRightClickPopupMenu")) ? DOMUtil.findFirstDescendantByClass(menu, "div", "UIRightClickPopupMenu") : menu;
    this.showHide(menu);
    if ((y + uiRightClickPopupMenu.offsetHeight) > browserHeight) {
        y = browserHeight - uiRightClickPopupMenu.offsetHeight;
    }
    //TODO: fix on IE7 when there is the ControlWorkspace. This bug occurs only developing parameter assigned false
    if (Browser.isIE7()) {
        var uiControWorkspace = document.getElementById("UIControlWorkspace");
        if (uiControWorkspace) 
            x -= uiControWorkspace.offsetWidth;
    }
    DOMUtil.addClass(menu, "UICalendarPortlet UIEmpty");
    menu.style.zIndex = 2000;
    menu.style.left = x + "px";
    menu.style.top = y + "px";
};

/**
 * Swap menu
 * @param {Object} oldmenu Menu DOM element
 * @param {Object} clickobj clickobj Click DOM element
 */
UICalendarPortlet.prototype.swapMenu = function(oldmenu, clickobj){
    var DOMUtil = eXo.core.DOMUtil;
    var Browser = eXo.core.Browser;
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var uiDesktop = document.getElementById("UIPageDesktop");
    var uiWorkSpaceWidth = (document.getElementById("UIControlWorkspace")) ? document.getElementById("UIControlWorkspace").offsetWidth : 0;
    uiWorkSpaceWidth = (Browser.isIE6() || Browser.isIE7()) ? 2 * uiWorkSpaceWidth : uiWorkSpaceWidth;
    if (document.getElementById("tmpMenuElement")) 
        DOMUtil.removeElement(document.getElementById("tmpMenuElement"));
    var tmpMenuElement = oldmenu.cloneNode(true);
    tmpMenuElement.setAttribute("id", "tmpMenuElement");
    tmpMenuElement.style.zIndex = 1 ;
    this.menuElement = tmpMenuElement;
 		if(Browser.isIE6()) this.menuElement.style.width = "140px";
    if (uiDesktop) {
        document.body.appendChild(this.menuElement);
        this.swapIeMenu(this.menuElement, clickobj);
        return;
    }
    else {
        document.getElementById(this.portletName).appendChild(tmpMenuElement);
    }
		
    var menuX = Browser.findPosX(clickobj) - uiWorkSpaceWidth;
    var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight;
    if (arguments.length > 2) {
        menuY -= arguments[2].scrollTop;
    }
		if (eXo.core.I18n.isRT()) {
      menuX -= (eXo.cs.Utils.getElementWidth(this.menuElement) - clickobj.offsetWidth) - uiWorkSpaceWidth;      
    }
    this.menuElement.style.top = menuY + "px";
    this.menuElement.style.left =  menuX + "px";
 		if (eXo.core.I18n.isRT() && Browser.isIE6()) {
      menuX = Browser.findPosXInContainer(clickobj,this.menuElement.offsetParent,true);
      menuX += uiWorkSpaceWidth/2 ;
      this.menuElement.style.right = menuX + "px";
      this.menuElement.style.left =  "";
    }
    this.showHide(this.menuElement);
    var uiRightClick = (DOMUtil.findFirstDescendantByClass(UICalendarPortlet.menuElement, "div", "UIRightClickPopupMenu")) ? DOMUtil.findFirstDescendantByClass(UICalendarPortlet.menuElement, "div", "UIRightClickPopupMenu") : UICalendarPortlet.menuElement;
    var mnuBottom = UICalendarPortlet.menuElement.offsetTop + uiRightClick.offsetHeight - window.document.documentElement.scrollTop;
    if (window.document.documentElement.clientHeight < mnuBottom) {
        menuY += (window.document.documentElement.clientHeight - mnuBottom);
        UICalendarPortlet.menuElement.style.top = menuY + "px";
    }
    
};

UICalendarPortlet.prototype.isAllday = function(form){
    try {
        if (typeof(form) == "string") 
            form = document.getElementById(form);
        if (form.tagName.toLowerCase() != "form") {
            form = eXo.core.DOMUtil.findDescendantsByTagName(form, "form");
        }
        for (var i = 0; i < form.elements.length; i++) {
            if (form.elements[i].getAttribute("name") == "allDay") {
                eXo.calendar.UICalendarPortlet.allDayStatus = form.elements[i];
                eXo.calendar.UICalendarPortlet.showHideTime(form.elements[i]);
                break;
            }
        }
    } 
    catch (e) {
    
    }
};

/**
 * Show/hide time field in Add event form
 * @param {Object} chk Checkbox element
 */
UICalendarPortlet.prototype.showHideTime = function(chk){
    var DOMUtil = eXo.core.DOMUtil;
    if (chk.tagName.toLowerCase() != "input") {
        chk = DOMUtil.findFirstDescendantByClass(chk, "input", "checkbox");
    }
    var selectboxes = DOMUtil.findDescendantsByTagName(chk.form, "input");
    var fields = new Array();
    var len = selectboxes.length;
    for (var i = 0; i < len; i++) {
        if ((selectboxes[i].getAttribute("name") == "toTime") || (selectboxes[i].getAttribute("name") == "fromTime")) {
            fields.push(selectboxes[i]);
        }
    }
    eXo.calendar.UICalendarPortlet.showHideField(chk, fields);
};

/**
 * Show/hide field in form
 * @param {Object} chk Checkbox element
 * @param {Object} fields Input field in form
 */
UICalendarPortlet.prototype.showHideField = function(chk, fields){
    var display = "";
    if (typeof(chk) == "string") 
        chk = document.getElementById(chk);
    display = (chk.checked) ? "hidden" : "visible";
    var len = fields.length;
    for (var i = 0; i < len; i++) {
        fields[i].style.visibility = display;
        i
    }
};

/**
 * Sets up dragging selection for calendar view
 */
UICalendarPortlet.prototype.initSelection = function(){
    var UISelection = eXo.calendar.UISelection;
    var container = eXo.core.DOMUtil.findFirstDescendantByClass(document.getElementById("UIDayViewGrid"), "div", "EventBoard");
    UISelection.step = 30;
    UISelection.container = container;
    UISelection.block = document.createElement("div");
    UISelection.block.className = "UserSelectionBlock";
    UISelection.container.appendChild(UISelection.block);
    UISelection.container.onmousedown = UISelection.start;
    UISelection.relativeObject = eXo.core.DOMUtil.findAncestorByClass(UISelection.container, "EventDayContainer");
    UISelection.viewType = "UIDayView";
};

/* for selection creation */
/**
 * Class control dragging selection
 * @author <a href="mailto:dung14000@gmail.com">Hoang Manh Dung</a>
 * @constructor
 */
function UISelection(){

};

/**
 * Sets up dragging selection when mouse down on calendar event
 * @param {Object} evt Mouse event
 */
UISelection.prototype.start = function(evt){
    try {
        var UISelection = eXo.calendar.UISelection;
        var src = eXo.core.EventManager.getEventTarget(evt);
        if ((src == UISelection.block) || (eXo.core.EventManager.getMouseButton(evt) == 2) || (eXo.core.DOMUtil.hasClass(src,"TdTime"))) {
						return;
        }
        
        UISelection.startTime = parseInt(Date.parse(src.getAttribute("startFull")));//src.getAttribute("startTime");
        UISelection.startX = eXo.core.Browser.findPosXInContainer(src, UISelection.container) - document.getElementById(eXo.calendar.UICalendarPortlet.portletName).parentNode.scrollTop;
        UISelection.block.style.display = "block";
        UISelection.startY = eXo.core.Browser.findPosYInContainer(src, UISelection.container);
        UISelection.block.style.width = src.offsetWidth + "px";
        UISelection.block.style.left = UISelection.startX + "px";
        UISelection.block.style.top = UISelection.startY + "px";
        UISelection.block.style.height = UISelection.step + "px";
        UISelection.block.style.zIndex = 1; 
        eXo.calendar.UICalendarPortlet.resetZIndex(UISelection.block);
        document.onmousemove = UISelection.execute;
        document.onmouseup = UISelection.clear;
    } 
    catch (e) {
        window.status = e.message ;
    }
};

/**
 * Executes dragging selection
 * @param {Object} evt Mouse event
 */
UISelection.prototype.execute = function(evt){
    var UISelection = eXo.calendar.UISelection;
    var _e = window.event || evt;
    var delta = null;
		var containerHeight = UISelection.container.offsetHeight;
    var scrollTop = eXo.cs.Utils.getScrollTop(UISelection.block);
    var mouseY = eXo.core.Browser.findMouseRelativeY(UISelection.container, _e) + UISelection.relativeObject.scrollTop;
    if (document.getElementById("UIPageDesktop")) 
        mouseY = eXo.core.Browser.findMouseRelativeY(UISelection.container, _e) + scrollTop;
    var posY = UISelection.block.offsetTop;
    var height = UISelection.block.offsetHeight;
    delta = posY + height - mouseY;
    if (UISelection.startY < mouseY) {
        UISelection.block.style.top = UISelection.startY + "px";
        if (delta >= UISelection.step) {
            UISelection.block.style.height = height - UISelection.step + "px";
        }
        if ((mouseY >= (posY + height)) && ((posY + height)< containerHeight) ) {
            UISelection.block.style.height = height + UISelection.step + "px";
        }
    }
    else {
        delta = mouseY - posY;
        UISelection.block.style.bottom = UISelection.startY - UISelection.step + "px";
        if ((mouseY <= posY) && (posY > 0)) {
            UISelection.block.style.top = posY - UISelection.step + "px";
            UISelection.block.style.height = height + UISelection.step + "px";
        }
        if (delta >= UISelection.step) {
            UISelection.block.style.top = posY + UISelection.step + "px";
            UISelection.block.style.height = height - UISelection.step + "px";
        }
    }
    
};

/**
 * Ends dragging selection, this method clean up some unused properties and execute callback function
 */
UISelection.prototype.clear = function(){
    var UISelection = eXo.calendar.UISelection;
    var endTime = UISelection.block.offsetHeight * 60 * 1000 + parseInt(UISelection.startTime);
    var startTime = UISelection.startTime;
		var bottom = UISelection.block.offsetHeight + UISelection.block.offsetTop;
    if (UISelection.block.offsetTop < UISelection.startY) {
        startTime = parseInt(UISelection.startTime) - UISelection.block.offsetHeight * 60 * 1000 + UISelection.step * 60 * 1000;
        endTime = parseInt(UISelection.startTime) + UISelection.step * 60 * 1000;
    }
		if(bottom >= UISelection.container.offsetHeight) endTime -= 1;
	var container = document.getElementById("UICalendarViewContainer");	
	eXo.calendar.UICalendarPortlet.addQuickShowHiddenWithTime(container, 1, startTime, endTime) ;
    //eXo.webui.UIForm.submitEvent(UISelection.viewType, 'QuickAdd', '&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime);
    eXo.core.DOMUtil.listHideElements(UISelection.block);
		UISelection.startTime = null;
		UISelection.startY = null;
		UISelection.startX = null;
    document.onmousemove = null;
    document.onmouseup = null;
};

// check free/busy time
/**
 * Checks free/busy in day of an user
 * @param {Object} chk Checkbox element
 */
UICalendarPortlet.prototype.checkAllInBusy = function(chk){
    var UICalendarPortlet = eXo.calendar.UICalendarPortlet;
    var isChecked = chk.checked;
    var timeField = eXo.core.DOMUtil.findFirstDescendantByClass(chk.form, "div", "TimeField");
    if (isChecked) {
        timeField.style.display = "none";
    }
    else {
        timeField.style.display = "block";
    }
    if (UICalendarPortlet.allDayStatus) {
        UICalendarPortlet.allDayStatus.checked = isChecked;
        UICalendarPortlet.showHideTime(UICalendarPortlet.allDayStatus);
    }
};

/**
 * Sets up checking free/busy
 * @param {Object} container DOM element contains event data
 */
UICalendarPortlet.prototype.initCheck = function(container){
    var DOMUtil = eXo.core.DOMUtil;
    if (typeof(container) == "string") 
        container = document.getElementById(container);
    var dateAll = DOMUtil.findDescendantsByClass(container, "input", "checkbox")[1];
    var serverTimezone = parseInt(container.getAttribute("serverTimezone"));
    var table = DOMUtil.findFirstDescendantByClass(container, "table", "UIGrid");
    var tr = DOMUtil.findDescendantsByTagName(table, "tr");
    var firstTr = tr[1];
    this.busyCell = DOMUtil.findDescendantsByTagName(firstTr, "td").slice(1);
    var len = tr.length;
    for (var i = 2; i < len; i++) {
        this.showBusyTime(tr[i], serverTimezone);
    }
    if (eXo.calendar.UICalendarPortlet.allDayStatus) 
        dateAll.checked = eXo.calendar.UICalendarPortlet.allDayStatus.checked;
    eXo.calendar.UICalendarPortlet.checkAllInBusy(dateAll);
    dateAll.onclick = function(){
        eXo.calendar.UICalendarPortlet.checkAllInBusy(this);
    }
    eXo.calendar.UICalendarPortlet.initSelectionX(firstTr);
};

/**
 * Localizes time
 * @param {Object} millis Time in minutes
 * @param {Object} timezoneOffset Timezone offset of current user
 * @return Time in minutes
 */
UICalendarPortlet.prototype.localTimeToMin = function(millis, timezoneOffset){
    if (typeof(millis) == "string") 
        millis = parseInt(millis);
    millis += timezoneOffset * 60 * 1000;
    var d = new Date(millis);
    var hour = d.getHours();
    var min = d.getMinutes();
    var min = hour * 60 + min;
    return min;
};

/**
 * Parses time from string
 * @param {Object} string String
 * @param {Object} timezoneOffset Timezone offset of user
 * @return Object contains two properties that are from and to
 */
UICalendarPortlet.prototype.parseTime = function(string, timezoneOffset){
    var stringTime = string.split(",");
    var len = stringTime.length;
    var time = new Array();
    var tmp = null;
    for (var i = 0; i < len; i += 2) {
        tmp = {
            "from": this.localTimeToMin(stringTime[i], timezoneOffset),
            "to": this.localTimeToMin(stringTime[i + 1], timezoneOffset)
        };
        time.push(tmp);
    }
    return time;
};

/**
 * Shows free/busy on UI
 * @param {Object} tr Tr tag contains event data
 * @param {Object} serverTimezone Server timezone
 */
UICalendarPortlet.prototype.showBusyTime = function(tr, serverTimezone){
    var stringTime = tr.getAttribute("busytime");
    var localize = (tr.getAttribute("usertimezone")) ? parseInt(tr.getAttribute("usertimezone")) : 0;
    var extraTime = localize - serverTimezone;
    if (!stringTime) 
        return;
    var time = this.parseTime(stringTime, extraTime);
    var len = time.length;
    var from = null;
    var to = null;
    for (var i = 0; i < len; i++) {
        from = parseInt(time[i].from);
        to = parseInt(time[i].to);
        this.setBusyTime(from, to, tr)
    }
};

/**
 * Show free/busy time in a tr tag
 * @param {Object} from Time in minutes
 * @param {Object} to Time in minutes
 * @param {Object} tr Tr tag contains event data
 */
UICalendarPortlet.prototype.setBusyTime = function(from, to, tr){
    var cell = eXo.core.DOMUtil.findDescendantsByTagName(tr, "td").slice(1);
    var start = this.ceil(from, 15) / 15;
    var end = this.ceil(to, 15) / 15;
    for (var i = start; i < end; i++) {
        cell[i].className = "BusyDotTime";
        this.busyCell[i].className = "BusyTime";
    }
};

/**
 * Ceiling round number
 * @param {Object} number Original number
 * @param {Object} dividend Divided end
 * @return rounded number
 */
UICalendarPortlet.prototype.ceil = function(number, dividend){
    var mod = number % dividend;
    if (mod != 0) 
        number += dividend - mod;
    return number;
};

/**
 * Sets up dragging selection for free/busy time table
 * @param {Object} tr Tr tag contains event data
 */
UICalendarPortlet.prototype.initSelectionX = function(tr){
    cell = eXo.core.DOMUtil.findDescendantsByTagName(tr, "td", "UICellBlock").slice(1);
    var len = cell.length;
    for (var i = 0; i < len; i++) {
        cell[i].onmousedown = eXo.calendar.UIHSelection.start;//eXo.calendar.Highlighter.start ;
    }
};

/**
 * Gets AM/PM from input value
 * @param {Object} input Input contains time
 * @return Object contains two properties that are AM and PM
 */
UICalendarPortlet.prototype.getTimeFormat = function(input){
    //var list = eXo.core.DOMUtil.findPreviousElementByTagName(input, "div");
    //var a = eXo.core.DOMUtil.findDescendantsByTagName(list, "a");
    var am = input.getAttribute("value").match(/[A-Z]+/);
    if (!am) 
        return null;
    var pm = a[a.length - 1].getAttribute("value").match(/[A-Z]+/);
    return {
        "am": am,
        "pm": pm
    };
};

/**
 * Callback method when dragging selection end
 */
UICalendarPortlet.prototype.callbackSelectionX = function(){
    var Highlighter = eXo.calendar.UIHSelection;
    var DOMUtil = eXo.core.DOMUtil;
    var len = Math.abs(Highlighter.firstCell.cellIndex - Highlighter.lastCell.cellIndex - 1);
    var start = (Highlighter.firstCell.cellIndex - 1) * 15;
    var end = start + len * 15;
    var timeTable = DOMUtil.findAncestorByTagName(Highlighter.firstCell, "table");
    var dateValue = timeTable.getAttribute("datevalue");
    var uiTabContentContainer = DOMUtil.findAncestorByClass(Highlighter.firstCell, "UITabContentContainer");
    var UIComboboxInputs = DOMUtil.findDescendantsByClass(uiTabContentContainer, "input", "UIComboboxInput");
    len = UIComboboxInputs.length;
    var name = null;
    var timeFormat = this.getTimeFormat(UIComboboxInputs[0]);
    start = this.minToTime(start, timeFormat);
    end = this.minToTime(end, timeFormat);
    if (dateValue) {
        var DateContainer = DOMUtil.findAncestorByTagName(uiTabContentContainer, "form");
        DateContainer.from.value = dateValue;
        DateContainer.to.value = dateValue;
    }
    for (var i = 0; i < len; i++) {
        name = UIComboboxInputs[i].name.toLowerCase();
        if (name.indexOf("from") >= 0) 
            UIComboboxInputs[i].value = start;
        else 
            UIComboboxInputs[i].value = end;
    }
    var cells = eXo.core.DOMUtil.getChildrenByTagName(Highlighter.firstCell.parentNode, "td");
    Highlighter.setAttr(Highlighter.firstCell.cellIndex, Highlighter.lastCell.cellIndex, cells);
};

/**
 * Sets some properties of UICalendarPortlet object again when user changes setting
 * @param {Object} cpid Component id
 */
UICalendarPortlet.prototype.initSettingTab = function(cpid){
    var cp = document.getElementById(cpid);
    var ck = eXo.core.DOMUtil.findFirstDescendantByClass(cp, "input", "checkbox");
    var div = eXo.core.DOMUtil.findAncestorByTagName(ck, "div");
    eXo.calendar.UICalendarPortlet.workingSetting = eXo.core.DOMUtil.findNextElementByTagName(div, "div");
    ck.onclick = eXo.calendar.UICalendarPortlet.showHideWorkingSetting;
    eXo.calendar.UICalendarPortlet.checkWorkingSetting(ck);
}

/**
 * Check status of working time checkbox
 * @param {Object} ck Working time checkbox
 */
UICalendarPortlet.prototype.checkWorkingSetting = function(ck){
    var isCheck = ck.checked;
    if (isCheck) {
        eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "visible";
    }
    else {
        eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "hidden";
    }
}

/**
 * Show/hide working time setting
 */
UICalendarPortlet.prototype.showHideWorkingSetting = function(){
    var isCheck = this.checked;
    if (isCheck) {
        eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "visible";
    }
    else {
        eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "hidden";
    }
};

UICalendarPortlet.prototype.showImagePreview = function(obj){
	var DOMUtil = eXo.core.DOMUtil ;
	var img = DOMUtil.findPreviousElementByTagName(obj.parentNode,"img");	
	var viewLabel = obj.getAttribute("viewLabel");
	var closeLabel = obj.getAttribute("closeLabel");
	if(img.style.display == "none"){
		img.style.display = "block";
		obj.innerHTML = closeLabel ;
		if(DOMUtil.hasClass(obj,"ViewAttachmentIcon")) DOMUtil.replaceClass(obj,"ViewAttachmentIcon"," CloseAttachmentIcon") ;
	}else {
		img.style.display = "none";
		obj.innerHTML = viewLabel ;
		if(DOMUtil.hasClass(obj,"CloseAttachmentIcon")) DOMUtil.replaceClass(obj,"CloseAttachmentIcon"," ViewAttachmentIcon") ;
	}
};

UICalendarPortlet.prototype.showHideSetting = function(obj){
	var checkbox = eXo.core.DOMUtil.findFirstDescendantByClass(obj,"input","checkbox");
	var uiFormGrid = eXo.core.DOMUtil.findFirstDescendantByClass(obj.parentNode.parentNode,"table","UIFormGrid");
	if(checkbox.checked) {
		checkbox.checked = true;
		uiFormGrid.style.display = "";
	}
	else{
		checkbox.checked = false;
		uiFormGrid.style.display = "none";
	}	
};

eXo.calendar.UICalendarPortlet = eXo.calendar.UICalendarPortlet || new UICalendarPortlet();
eXo.calendar.UIResizeEvent = new UIResizeEvent();
eXo.calendar.UISelection = new UISelection();

UICalendarPortlet.prototype.fixFirstLoad = function(){
    if (this.firstRun){
			if (this.delay) {
	        window.clearTimeout(this.delay);
					delete this.delay ;
	    }
        return;
		}
    if (document.getElementById("UIPageDesktop")) {
        if (document.getElementById("UIWeekView")) {
            eXo.calendar.UICalendarMan.initWeek();
            eXo.calendar.UIWeekView.setSize();
            eXo.calendar.UICalendarPortlet.setFocus();
            this.firstRun = true;
        }
    }
    
};

eXo.portal.UIControlWorkspace._showWorkspace = eXo.portal.UIControlWorkspace.showWorkspace ;

eXo.portal.UIControlWorkspace.showWorkspace = function(){
    
		eXo.portal.UIControlWorkspace._showWorkspace();
		
    if ((eXo.core.Browser.browserType != "ie") && !eXo.core.Browser.isDesktop()) {
        if (document.getElementById("UIWeekView")) {
            eXo.calendar.UICalendarMan.initWeek();
            eXo.calendar.UIWeekView.setSize();
        }
        if (document.getElementById("UIMonthView")) {
            eXo.calendar.UICalendarMan.initMonth();
        }
    }
};

UICalendarPortlet.prototype.fixForMaximize = function(){
	var obj = document.getElementById(eXo.calendar.UICalendarPortlet.portletName) ;
	var uiWindow = eXo.core.DOMUtil.findAncestorByClass(obj, "UIWindow");
	if(uiWindow.style.display == "none") return ;
  if ((eXo.core.Browser.browserType != "ie")) {
      if (document.getElementById("UIWeekView")) {
          eXo.calendar.UICalendarMan.initWeek();
          eXo.calendar.UIWeekView.setSize();
      }
      if (document.getElementById("UIMonthView")) {
          eXo.calendar.UICalendarMan.initMonth();
      }
  }
};

if(eXo.desktop.UIDesktop){
UIDesktop.prototype._ShowHideWindow = eXo.desktop.UIDesktop.showHideWindow;
UIWindow.prototype._endResizeWindowEvt = eXo.desktop.UIWindow.endResizeWindowEvt;
UIWindow.prototype._maximizeWindowEvt = eXo.desktop.UIWindow.maximizeWindowEvt;

UIDesktop.prototype.showHideWindow = function(uiWindow, clickedElement, mode){
		eXo.desktop.UIDesktop._ShowHideWindow(uiWindow, clickedElement, mode);
    if (eXo.desktop.UIDesktop.object.style.display != "block") {
        if(uiWindow.indexOf("calendar") >=0) eXo.calendar.UICalendarPortlet.delay = window.setTimeout("eXo.calendar.UICalendarPortlet.fixFirstLoad() ;", 2000);
    }
};




UIWindow.prototype.endResizeWindowEvt = function(evt){
    // Re initializes the scroll tabs managers on the page
		eXo.desktop.UIWindow._endResizeWindowEvt(evt);
    eXo.calendar.UICalendarPortlet.fixForMaximize();
};

UIWindow.prototype.maximizeWindowEvt = function(evt){
    eXo.desktop.UIWindow._maximizeWindowEvt(evt);
    eXo.calendar.UICalendarPortlet.fixForMaximize();
};
}