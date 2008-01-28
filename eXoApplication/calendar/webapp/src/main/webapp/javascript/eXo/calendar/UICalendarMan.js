/**
 * @author Uoc Nguyen
 */

function QuickSortObject(){
  this.processArray = false;
  this.desc = false;
  this.compareFunction = false;
  this.compareArgs = false;
}

/**
 *
 * @param {Array} array
 * @param {Boolean} desc
 * @param {Function} compareFunction
 * @param {Object | Array} compareArgs
 */
QuickSortObject.prototype.doSort = function(array, desc, compareFunction, compareArgs){
  this.processArray = array;
  this.desc = desc;
  this.compareFunction = compareFunction;
  this.compareArgs = compareArgs;
  this.qSortRecursive(0, this.processArray.length);
};

/**
 *
 * @param {Integer} x
 * @param {Integer} y
 */
QuickSortObject.prototype.swap = function(x, y){
  if (this.processArray) {
    var tmp = this.processArray[x];
    this.processArray[x] = this.processArray[y];
    this.processArray[y] = tmp;
  }
};

/**
 *
 * @param {Integer} begin
 * @param {Integer} end
 * @param {Integer} pivotIndex
 */
QuickSortObject.prototype.qSortRecursive = function(begin, end){
  if (!this.processArray || begin >= end - 1) 
    return;
  var pivotIndex = begin + Math.floor(Math.random() * (end - begin - 1));
  var partionIndex = this.partitionProcess(begin, end, pivotIndex);
  this.qSortRecursive(begin, partionIndex);
  this.qSortRecursive(partionIndex + 1, end);
};

/**
 *
 * @param {Integer} begin
 * @param {Integer} end
 * @param {Integer} pivotIndex
 */
QuickSortObject.prototype.partitionProcess = function(begin, end, pivotIndex){
  var pivotValue = this.processArray[pivotIndex];
  this.swap(pivotIndex, end - 1);
  var scanIndex = begin;
  for (var i = begin; i < end - 1; i++) {
    if (typeof(this.compareFunction) == 'function') {
      if (!this.desc && this.compareFunction(this.processArray[i], pivotValue, this.compareArgs) <= 0) {
        this.swap(i, scanIndex);
        scanIndex++;
        continue;
      }
      else 
        if (this.desc && this.compareFunction(this.processArray[i], pivotValue, this.compareArgs) > 0) {
          this.swap(i, scanIndex);
          scanIndex++;
          continue;
        }
    }
    else {
      if (!this.desc && this.processArray[i] <= pivotValue) {
        this.swap(i, scanIndex);
        scanIndex++;
        continue;
      }
      else 
        if (this.desc && this.processArray[i] > pivotValue) {
          this.swap(i, scanIndex);
          scanIndex++;
          continue;
        }
    }
  }
  this.swap(end - 1, scanIndex);
  return scanIndex;
};

eXo.core.QuickSortObject = new QuickSortObject();

function EventObject(){
  this.LABEL_MAX_LEN = 10;
  this.calId = false;
  this.calType = false;
  this.endTime = false;
  this.eventCat = false;
  this.eventId = false;
  this.eventIndex = false;
  this.startIndex = false;
  this.startTime = false;
  this.weekStartTimeIndex = new Array();
  this.cloneNodes = new Array();
  this.rootNode = false;
  this.name = false;
  if (arguments.length > 0) {
    this.init(arguments[0]);
  }
}

EventObject.prototype.init = function(rootNode){
  if (!rootNode) {
    return;
  }
  rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  this.rootNode = rootNode;
  this.rootNode.style['cursor'] = 'pointer';
  this.startIndex = this.rootNode.getAttribute('startindex');
  this.calType = this.rootNode.getAttribute('caltype');
  this.eventId = this.rootNode.getAttribute('eventid');
  this.eventIndex = this.rootNode.getAttribute('eventindex');
  this.calId = this.rootNode.getAttribute('calid');
  this.eventCat = this.rootNode.getAttribute('eventcat');
//  this.startTime = parseInt(this.rootNode.getAttribute('starttime'));
  this.startTime = Date.parse(this.rootNode.getAttribute('starttimefull'));
//  this.endTime = parseInt(this.rootNode.getAttribute('endtime'));
  this.endTime = Date.parse(this.rootNode.getAttribute('endtimefull'));
  if (this.rootNode.innerText) {
    this.name = (this.rootNode.innerText + '').trim();
  } else {
    this.name = (this.rootNode.textContent + '').trim();
  }
};

EventObject.prototype.updateIndicator = function(nodeObj, hasBefore, hasAfter) {
  var labelStr = this.name;
  if (hasBefore) {
    labelStr = '>> ' + labelStr;
  }
  if (hasAfter) {
    labelStr += ' >>';
  }
  var labelNode = eXo.core.DOMUtil.findFirstDescendantByClass(nodeObj, 'div', 'EventLabel');
  if (labelNode) {
    labelNode.innerHTML = labelStr;
  }
};

EventObject.prototype.getLabel = function() {
  if (this.name.length > this.LABEL_MAX_LEN) {
    return this.name.substring(0, this.LABEL_MAX_LEN) + '...';
  } else {
    return this.name;
  }
};

/**
 *
 * @param {EventObject} event1
 * @param {EventObject} event2
 *
 * @return {Integer} 0 if equals
 *                   > 0 if event1 > event2
 *                   < 0 if event1 < event2
 */
EventObject.prototype.compare = function(event1, event2){
  if ((event1.startTime == event2.startTime && event1.endTime < event2.endTime) ||
      event1.startTime > event2.startTime) {
    return 1;
  } else if (event1.startTime == event2.startTime && event1.endTime == event2.endTime) {
      return 0;
  } else {
      return -1;
  }
};

EventObject.prototype.toString = function(){
  var objIdentify = 'name: ' + this.name +
                    ' - start: ' + (new Date(parseInt(this.startTime))).toString() +
                    ' - end: ' + (new Date(parseInt(this.endTime))).toString();
  for (var i = 0; i < this.weekStartTimeIndex.length; i++) {
    var weekStartTime = new Date(parseInt(this.weekStartTimeIndex[i]));
    objIdentify += ' - week:' + i + ' day: ' + weekStartTime.getDay();
  }
  return objIdentify;
};

function DayMan(){
  this.previousDay = false;
  this.nextDay = false;
  this.MAX_EVENT_VISIBLE = 4;
  this.totalEventVisible = 0;
  this.visibleGroup = new Array();
  this.invisibleGroup = new Array();
  this.linkGroup = new Array();
  this.events = new Array();
}

/**
 * 
 * @param {EventObject} eventObj
 */
DayMan.prototype.isVisibleEventExist = function(eventObj) {
  for (var i=0; i<this.visibleGroup.length; i++) {
    if (this.visibleGroup[i] == eventObj) {
      return i;
    }
  }
  return -1;
};

/**
 * 
 * @param {EventObject} eventObj
 */
DayMan.prototype.isInvisibleEventExist = function(eventObj) {
  for (var i=0; i<this.invisibleGroup.length; i++) {
    if (this.invisibleGroup[i] == eventObj) {
      return i;
    }
  }
  return -1;
};

DayMan.prototype.synchronizeGroups = function(){
  if (this.events.length <= 0) {
    return;
  }
  if (this.MAX_EVENT_VISIBLE < 0 ||
      this.events.length <= this.MAX_EVENT_VISIBLE) {
    this.totalEventVisible = this.MAX_EVENT_VISIBLE;
  } else {
    this.totalEventVisible = this.MAX_EVENT_VISIBLE - 1;
  }
  for (var i=0; i<this.events.length; i++) {
    if (this.MAX_EVENT_VISIBLE < 0) {
      this.visibleGroup.push(this.events[i]);
    } else if (this.previousDay && 
        this.previousDay.isInvisibleEventExist(this.events[i]) >= 0) {
      this.invisibleGroup.push(this.events[i]);
    } else if(this.visibleGroup.length < this.totalEventVisible) {
      this.visibleGroup.push(this.events[i]);
    } else {
      this.invisibleGroup.push(this.events[i]);
    }
  }
  this.reIndex();
};

DayMan.prototype.reIndex = function() {
  var tmp = new Array();
  var cnt = 0;
  master : for (var i=0; i<this.visibleGroup.length; i++) {
    var eventTmp = this.visibleGroup[i];
    var eventIndex = i;
    // check cross event conflic
    if (this.previousDay && 
        this.invisibleGroup.length > 0 &&
        this.previousDay.visibleGroup[(this.MAX_EVENT_VISIBLE - 1)] == eventTmp) {
      this.invisibleGroup.push(eventTmp);
      this.invisibleGroup = this.invisibleGroup.reverse();
      this.visibleGroup.push(this.invisibleGroup.pop());
      this.invisibleGroup = this.invisibleGroup.reverse();
      continue;
    } 
    
    // check cross event
    if (this.previousDay) {
      eventIndex = this.previousDay.isVisibleEventExist(eventTmp);
      if (eventIndex >= 0) {
        tmp[eventIndex] = eventTmp;
        continue;
      }
    }
    for (var j=0; j<tmp.length; j++) {
      if (!tmp[j]) {
        tmp[j] = eventTmp;
        continue master;
      }
    }
    tmp[i] = eventTmp;
  }
  this.visibleGroup = tmp;
};

function WeekMan(){
  this.startWeek = false;
  this.endWeek = false;
  this.weekIndex = false;
  this.events = new Array();
  this.days = new Array();
  this.isEventsSorted = false;
  this.MAX_EVENT_VISIBLE = 4;
}

WeekMan.prototype.resetEventWeekIndex = function() {
  for (var i=0; i<this.events.length; i++) {
    var eventObj = this.events[i];
    if (eventObj.startTime > parseInt(this.startWeek)) {
      eventObj.weekStartTimeIndex[this.weekIndex] = eventObj.startTime;
    } else {
      eventObj.weekStartTimeIndex[this.weekIndex] = this.startWeek;
    }
  }
};

WeekMan.prototype.createDays = function() {
  // Create 7 days
  for (var i=0; i<7; i++) {
    this.days[i] = new DayMan();
    // link days
    if (i > 0) {
      this.days[i].previousDay = this.days[i-1];
    }
  }
  
  for (var i=0; i<this.days.length-1; i++) {
    if (this.MAX_EVENT_VISIBLE) {
      this.days[i].MAX_EVENT_VISIBLE = this.MAX_EVENT_VISIBLE;
    }
    this.days[i].nextDay = this.days[i+1];    
  }
};

WeekMan.prototype.putEvents2Days = function(){
  if (this.events.length <= 0) {
    return;
  }
  if (!this.isEventsSorted) {
    this.sortEvents();
  }
  
  this.createDays();
  // Put events to days
  for (var i=0; i<this.events.length; i++) {
    var eventObj = this.events[i];
    var startWeekTime = eventObj.weekStartTimeIndex[this.weekIndex];
    var endWeekTime = eventObj.endTime > this.endWeek ? this.endWeek : eventObj.endTime;
    var startDay = (new Date(parseInt(startWeekTime))).getDay();
    var endDay = (new Date(parseInt(endWeekTime))).getDay();
    // fix date
    var delta = (new Date(eventObj.endTime)) - (new Date(eventObj.startTime));
    delta /= (1000 * 60 * 60 * 24);
    if (delta == 1) {
      endDay = startDay;
    }
    for (var j=startDay; j<=endDay; j++) {
      this.days[j].events.push(eventObj);
    }
  }
  for (var i=0; i<this.days.length; i++) {
    this.days[i].synchronizeGroups();
  }
};

WeekMan.prototype.sortEvents = function(checkDepend){
  if (this.events.length > 1) {
    if (checkDepend) {
      eXo.core.QuickSortObject.doSort(this.events, false, this.compareEventByWeek, this.weekIndex);
    } else {
      eXo.core.QuickSortObject.doSort(this.events, false, this.events[0].compare);
    }
    this.isEventsSorted = true;
  }
};

/**
 *
 * @param {EventObject} event1
 * @param {EventObject} event2
 *
 * @return {Integer} 0 if equals
 *                   > 0 if event1 > event2
 *                   < 0 if event1 < event2
 */
WeekMan.prototype.compareEventByWeek = function(event1, event2, weekIndex){
  var weekObj = eXo.calendar.UICalendarMan.EventMan.weeks[weekIndex];
  var e1StartWeekTime = event1.weekStartTimeIndex[weekIndex];
  var e2StartWeekTime = event2.weekStartTimeIndex[weekIndex];
  var e1EndWeekTime = event1.endTime > weekObj.endWeek ? weekObj.endWeek : event1.endTime;
  var e2EndWeekTime = event2.endTime > weekObj.endWeek ? weekObj.endWeek : event2.endTime;
  if ((e1StartWeekTime == e2StartWeekTime && e1EndWeekTime < e2EndWeekTime) ||
      e1StartWeekTime > e2StartWeekTime) {
    return 1;
  } else if (e1StartWeekTime == e2StartWeekTime && e1EndWeekTime == e2EndWeekTime) {
      return 0;
  } else {
      return -1;
  }
};

function EventMan(){}

/**
 *
 * @param {Object} rootNode
 */
EventMan.prototype.initMonth = function(rootNode){
  rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  this.rootNode = rootNode;
  this.cleanUp();
  this.events = new Array();
  this.weeks = new Array();
  var DOMUtil = eXo.core.DOMUtil;
  // Parse all event node to event object
  var allEvents = DOMUtil.findDescendantsByClass(rootNode, 'div', 'DayContentContainer');
  // Create and init all event
  for (var i = 0; i < allEvents.length; i++) {
    if (allEvents[i].style.display == 'none') {
      continue;
    }
    var eventObj = new EventObject();
    eventObj.init(allEvents[i]);
    this.events.push(eventObj);
  }
  this.UIMonthViewGrid = document.getElementById('UIMonthViewGrid');
  this.groupByWeek();
  this.sortByWeek();
};

EventMan.prototype.cleanUp = function() {
  if (!this.events) {
    return;
  }
  var DOMUtil = eXo.core.DOMUtil;
  var rowContainerDay = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'RowContainerDay');
  
  for (var i=0; i<this.events.length; i++) {
    var eventObj = this.events[i];
    if (!eventObj) {
      continue;
    }
    for (var j=0; j<eventObj.cloneNodes.length; j++) {
      DOMUtil.removeElement(eventObj.cloneNodes[j]);
    }
    eventObj.rootNode.setAttribute('used', 'false');
    if (eventObj.rootNode.getAttribute('moremaster') == 'true') {
      eventObj.rootNode.setAttribute('moremaster', 'false');
      var eventNode = eventObj.rootNode.cloneNode(true);
      // Restore checkbox
      var checkBoxTmp = eventNode.getElementsByTagName('input')[0];
      if (checkBoxTmp) {
        checkBoxTmp.style.display = null;
      }
      var bodyNode = eXo.core.DOMUtil.findAncestorByTagName(eventObj.rootNode, 'body');
      if (bodyNode) {
      	rowContainerDay.appendChild(eventNode);
      }
    }
    this.events[i] = null;
  }
  var moreNodes = DOMUtil.findDescendantsByClass(this.rootNode, 'div', 'MoreEvent');
  var rowContainerDay = DOMUtil.findDescendantsByClass(this.rootNode, 'div', 'RowContainerDay');

  for (var i=0; i<moreNodes.length; i++) {
    var eventNodes = DOMUtil.findDescendantsByClass(moreNodes[i], 'div', 'DayContentContainer');
    DOMUtil.removeElement(moreNodes[i]);
  }
};

/**
 * 
 * @param {Element} rootNode
 */
EventMan.prototype.initWeek = function(rootNode) {
  this.events = new Array();
  this.weeks = new Array();
  rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  this.rootNode = rootNode;
  var DOMUtil = eXo.core.DOMUtil;
  // Parse all event node to event object
  var allEvents = DOMUtil.findDescendantsByClass(rootNode, 'div', 'EventContainer');
  // Create and init all event
  for (var i=0; i < allEvents.length; i++) {
    if (allEvents[i].style.display == 'none') {
      continue;
    }
    var eventObj = new EventObject();
    eventObj.init(allEvents[i]);
    this.events.push(eventObj);
  }
  this.dayNodes = DOMUtil.findDescendantsByClass(this.rootNode, 'th', 'UICellBlock');
  this.week = new WeekMan();
  this.week.weekIndex = 0;
//  this.week.startWeek = parseInt(this.dayNodes[0].getAttribute('starttime'));
  this.week.startWeek = Date.parse(this.dayNodes[0].getAttribute('starttimefull'));
  this.week.endWeek = this.week.startWeek + (1000 * 60 * 60 * 24 * 7) -1;
  this.week.events = this.events;
  this.week.resetEventWeekIndex();
  // Set unlimited event visible for all days
  this.week.MAX_EVENT_VISIBLE = -1;
  this.week.putEvents2Days();
};

EventMan.prototype.groupByWeek = function(){
  var DOMUtil = eXo.core.DOMUtil;
  var weekNodes = DOMUtil.findDescendantsByTagName(this.UIMonthViewGrid, "tr");
  var startWeek = 0;
  var endWeek = 0;
  var startCell = null;
  for (var i = 0; i < weekNodes.length; i++) {
    var currentWeek = new WeekMan();
    currentWeek.weekIndex = i;
    for (var j = 0; j < this.events.length; j++) {
      var eventObj = this.events[j];
      startCell = DOMUtil.findFirstDescendantByClass(weekNodes[i], "td", "UICellBlock");
//      startWeek = parseInt(startCell.getAttribute("startTime"));
      startWeek = Date.parse(startCell.getAttribute('starttimefull'));
      endWeek = (startWeek + 7 * 24 * 60 * 60 * 1000) - 1;
      currentWeek.startWeek = startWeek;
      currentWeek.endWeek = endWeek;
      if ((eventObj.startTime >= startWeek && eventObj.startTime < endWeek) ||
      (eventObj.endTime >= startWeek && eventObj.endTime < endWeek) ||
      (eventObj.startTime <= startWeek && eventObj.endTime >= endWeek)) {
        if (eventObj.startTime > startWeek) {
          eventObj.weekStartTimeIndex[currentWeek.weekIndex] = eventObj.startTime;
        } else {
          eventObj.weekStartTimeIndex[currentWeek.weekIndex] = startWeek;
        }
        currentWeek.events.push(eventObj);
      }
    }
    this.weeks.push(currentWeek);
  }
};

EventMan.prototype.sortByWeek = function(){
  for (var i = 0; i < this.weeks.length; i++) {
    var currentWeek = this.weeks[i];
    currentWeek.sortEvents();
    currentWeek.putEvents2Days();
  }
};

function GUIMan(){
  this.EVENT_BAR_HEIGH = 0;
}

/**
 *
 * @param {EventMan} eventMan
 */
GUIMan.prototype.initMonth = function(){
  var events = eXo.calendar.UICalendarMan.EventMan.events;
  if (events.length > 0) {
    if (events[0]) {
      this.EVENT_BAR_HEIGH = events[0].rootNode.offsetHeight - 1;
    }
  }
  var DOMUtil = eXo.core.DOMUtil;
  for (var i=0; i<events.length; i++) {
    var eventObj = events[i];
    var eventLabelNode = eXo.core.DOMUtil.findFirstDescendantByClass(eventObj.rootNode, 'div', 'EventLabel');
    eventLabelNode.innerHTML = eventObj.getLabel();
    eventObj.rootNode.setAttribute('title', eventObj.name);
  }
  this.rowContainerDay = DOMUtil.findFirstDescendantByClass(eXo.calendar.UICalendarMan.EventMan.rootNode, 'div', 'RowContainerDay');
  var rows = eXo.calendar.UICalendarMan.EventMan.UIMonthViewGrid.getElementsByTagName('tr');
  this.tableData = new Array();
  for (var i = 0; i < rows.length; i++) {
    var rowData = DOMUtil.findDescendantsByClass(rows[i], 'td', 'UICellBlock');
    this.tableData[i] = rowData;
  }
  this.paintMonth();
  this.setDynamicSize4Month();
  this.scrollTo();
  this.initDND();
};

GUIMan.prototype.initWeek = function() {
  var EventMan = eXo.calendar.UICalendarMan.EventMan;
  var events = EventMan.events;
  if (events.length > 0) {
    if (events[0]) {
      this.EVENT_BAR_HEIGH = events[0].rootNode.offsetHeight + 1;
    }
  }
  var DOMUtil = eXo.core.DOMUtil;
  for (var i=0; i<events.length; i++) {
    var eventObj = events[i];
    var eventLabelNode = eXo.core.DOMUtil.findFirstDescendantByClass(eventObj.rootNode, 'div', 'EventAlldayContent');
    eventLabelNode.innerHTML = eventObj.getLabel();
    eventObj.rootNode.setAttribute('used', 'false');
  }
  this.eventAlldayNode = DOMUtil.findFirstDescendantByClass(EventMan.rootNode, 'td', 'EventAllday');
  this.dayNodes = EventMan.dayNodes;
  this.paintWeek();
  this.setDynamicSize4Week();
  this.initSelectionDayEvent();
  this.initSelectionDaysEvent();
};

GUIMan.prototype.paintWeek = function() {
  var DOMUtil = eXo.core.DOMUtil;
  var weekObj = eXo.calendar.UICalendarMan.EventMan.week;
  var maxEventRow = 0;
  for (var i=0; i<weekObj.days.length; i++) {
    var dayObj = weekObj.days[i];
    var dayNode = this.dayNodes[i];
    var dayInfo = {
      width : dayNode.offsetWidth - 1,
      top : 0,
      startTime : parseInt(dayNode.getAttribute('starttime'))
    }
    dayInfo.pixelPerUnit = dayInfo.width / 100;
    dayInfo.left = dayInfo.width * i;

    for (var j=0; j<dayObj.visibleGroup.length; j++) {
      var eventObj = dayObj.visibleGroup[j];
      if (!eventObj ||
          (dayObj.previousDay &&
          dayObj.previousDay.isVisibleEventExist(eventObj) >= 0)) {
        continue;
      }
      var startTime = eventObj.weekStartTimeIndex[weekObj.weekIndex];
      var endTime = eventObj.endTime;
      if (endTime >= weekObj.endWeek) {
        endTime = weekObj.endWeek;
      }
      dayInfo.eventTop = dayInfo.top + ((this.EVENT_BAR_HEIGH) * j);
      dayInfo.eventShiftRightPercent = (((new Date(startTime) - (new Date(dayInfo.startTime)))) / (1000 * 60 * 60 * 24)) * 100;
      this.drawEventByMiliseconds(eventObj, startTime, endTime, dayInfo);
    }
    // update max event rows
    if (maxEventRow < dayObj.visibleGroup.length) {
      maxEventRow = dayObj.visibleGroup.length;
    }
  }
  this.eventAlldayNode.style.height = (maxEventRow * this.EVENT_BAR_HEIGH) + 'px';
};

/**
 * 
 * @param {EventObject} eventObj
 * @param {Integer} startTime
 * @param {Integer} endTime
 * @param {Object} dayInfo
 */
GUIMan.prototype.drawEventByMiliseconds = function(eventObj, startTime, endTime, dayInfo) {
  var eventNode = eventObj.rootNode;
  var topPos = dayInfo.eventTop ;
//  var leftPos = dayInfo.left;
  delta = (new Date(endTime)) - (new Date(startTime));
  delta /= (1000 * 60 * 60 * 24);
  var eventLen = parseFloat(delta * (dayInfo.width)) - 2;
  var leftPos = dayInfo.left + ((dayInfo.eventShiftRightPercent * dayInfo.width) / 100) + 1;
  with (eventNode.style) {
    position = 'absolute';
    top = topPos + 'px';
    left = leftPos + 'px';
    width = eventLen + 'px';
  }
};

GUIMan.prototype.initSelectionDayEvent = function() { 
  var UISelection = eXo.calendar.UISelection ;
  var container = document.getElementById("UIWeekViewGrid") ;
  UISelection.step = 30 ; 
  UISelection.block = document.createElement("div")
  UISelection.block.className = "UserSelectionBlock" ;
  UISelection.container = container ;
  eXo.core.DOMUtil.findPreviousElementByTagName(container, "div").appendChild(UISelection.block) ;
  UISelection.container.onmousedown = UISelection.start ;
  UISelection.relativeObject = eXo.core.DOMUtil.findAncestorByClass(UISelection.container, "EventWeekContent") ;
  UISelection.viewType = "UIWeekView" ;
} ;

GUIMan.prototype.initSelectionDaysEvent = function() {
  var Highlighter = eXo.calendar.Highlighter ;
  for(var i=0; i<this.dayNodes.length; i++) {
    var link = eXo.core.DOMUtil.getChildrenByTagName(this.dayNodes[i],"a")[0] ;    
    if (link) link.onmousedown = this.cancelEvent ;
    this.dayNodes[i].onmousedown = Highlighter.start ;
  }
} ;
 
GUIMan.prototype.scrollTo = function() {
  var lastUpdatedId = this.rowContainerDay.getAttribute("lastUpdatedId") ;
  var events = eXo.calendar.UICalendarMan.EventMan.events; 
  for(var i=0 ; i<events.length ; i++) {
    if(events[i].eventId == lastUpdatedId) {
      this.rowContainerDay.scrollTop = events[i].rootNode.offsetTop - 17;
      return ;
    }
  }
} ;

GUIMan.prototype.initDND = function() {
  var events = eXo.calendar.UICalendarMan.EventMan.events;
  for(var i=0 ; i<events.length ; i++) {
    var eventNode = events[i].rootNode;
    var checkbox = eXo.core.DOMUtil.findFirstDescendantByClass(eventNode, "input", "checkbox") ;
    if (checkbox) {
      checkbox.onmousedown = this.cancelEvent;
    }
  }
  eXo.calendar.UICalendarDragDrop.init(this.tableData, eXo.calendar.UICalendarMan.EventMan.events);
};

/**
 * 
 * @param {Event} event
 */
GUIMan.prototype.cancelEvent = function(event) {
  event = window.event || event ;
  event.cancelBubble = true ;
  if (event.preventDefault) {
    event.preventDefault();
  }
};

GUIMan.prototype.paintMonth = function(){
  var weeks = eXo.calendar.UICalendarMan.EventMan.weeks;
  // Remove old more node if exist
  for (var i=0; i<weeks.length; i++) {
    var curentWeek = weeks[i];
    if (curentWeek.events.length > 0) {
      for (var j=0; j<curentWeek.days.length; j++) {
        if (curentWeek.days[j].events.length > 0) {
          this.drawDay(curentWeek, j);
        }
      }
    }
  }
};

/**
 * 
 * @param {WeekMan} weekObj
 * @param {Integer} dayIndex
 */
GUIMan.prototype.drawDay = function(weekObj, dayIndex) {
  var dayObj = weekObj.days[dayIndex];
  // Pre-calculate event position
  var dayNode = (this.tableData[weekObj.weekIndex])[dayIndex];
  var dayInfo = {
    width : dayNode.offsetWidth - 1,
    left : dayNode.offsetLeft,
    top : dayNode.offsetTop + 17
  }
  // Draw visible events
  for (var i=0; i<dayObj.visibleGroup.length; i++) {
    var eventObj = dayObj.visibleGroup[i];
    if (!eventObj || 
        (dayObj.previousDay && 
        dayObj.previousDay.isVisibleEventExist(eventObj) >= 0)) {
      continue;
    }
    var startTime = eventObj.weekStartTimeIndex[weekObj.weekIndex];
    var endTime = eventObj.endTime > weekObj.endWeek ? weekObj.endWeek : eventObj.endTime;
    var delta = (new Date(endTime)) - (new Date(startTime));
    delta /= (1000 * 60 * 60 *24);
    if (delta > 1 && 
        dayObj.nextDay && 
        i == (dayObj.MAX_EVENT_VISIBLE - 1)) {
      var tmp = dayObj.nextDay;
      var cnt = 1;
      while (tmp.nextDay && cnt<=delta) {
        if (tmp.isInvisibleEventExist(eventObj) >= 0) {
          break;
        }
        cnt++;
        tmp = tmp.nextDay;
      }
      endTime = startTime + ((1000 * 60 * 60 * 24) * cnt) - 1;
    }
    dayInfo.eventTop = dayInfo.top + ((this.EVENT_BAR_HEIGH) * i);
    this.drawEventByDay(eventObj, startTime, endTime, dayInfo);
  }
  
  // Draw invisible events (put all into more)
  if (dayObj.invisibleGroup.length > 0) {
    var moreNode = document.createElement('div');
    moreNode.className = 'MoreEvent';
    this.rowContainerDay.appendChild(moreNode);
    with (moreNode.style) {
      position = 'absolute';
      width = dayInfo.width + 'px';
      left = dayInfo.left + 'px' ;
      top = dayInfo.top + ((dayObj.MAX_EVENT_VISIBLE - 1) * this.EVENT_BAR_HEIGH) + 5 + 'px';
      cursor = 'pointer';
      zIndex = '1';
    }
    moreNode.onclick = this.showMore;
    var moreContainerNode = document.createElement('div');
    with (moreContainerNode.style) {
      backgroundColor = '#fff';
      position = 'absolute';
      display = 'none';
      top = '0px';
      left = '0px';
    }
    moreContainerNode.className = 'MoreContainer';
    moreContainerNode.onmousemove = this.showMore;
    moreContainerNode.onmouseout = this.hideMore;
    // Create invisible event
    var cnt = 0
    for (var i=0; i<dayObj.invisibleGroup.length; i++) {
      var eventObj = dayObj.invisibleGroup[i];
      if (!eventObj) {
        continue;
      }
      cnt ++;
      var eventNode = eventObj.rootNode;
      var checkboxState = 'none';
      if (eventNode.getAttribute('used') == 'true') {
        eventNode = eventNode.cloneNode(true);
        eventNode.setAttribute('eventclone', 'true');
        eventObj.cloneNodes.push(eventNode);
        var hasBefore = true;
        var hasAfter = true;
        if (i >= (dayObj.invisibleGroup.length - 1)) {
          hasAfter = false;
        }
        if (cnt == 0) {
          hasBefore = false;
        }
        eventObj.updateIndicator(eventObj.cloneNodes[eventObj.cloneNodes.length - 1], hasBefore, hasAfter);
      } else {
        eventNode = eventNode.cloneNode(true);
        eXo.core.DOMUtil.removeElement(eventObj.rootNode);
        eventNode.setAttribute('moremaster', 'true');
        eventObj.rootNode = eventNode;
        checkboxState = null;
      }
      // Remove checkbox on clone event
      try {
        var checkBoxTmp = eventNode.getElementsByTagName('input')[0];
        checkBoxTmp.style.display = checkboxState;
      } catch(e) {}
      moreContainerNode.appendChild(eventNode);
      var topPos = this.EVENT_BAR_HEIGH * i;
      with (eventNode.style) {
        display = 'block';
        position = 'absolute';
        top = topPos + 'px';
        left = '0px';
        width = dayInfo.width + 'px';
      }
      eventNode.setAttribute('used', 'true');
    }
    var moreLabel = document.createElement('div');
    with (moreLabel.style) {
      position = 'absolute';
      top = '0px';
      left = '0px';
      padding = '2px';
    }
    moreLabel.innerHTML = 'more ' + cnt + '+';
    moreLabel.style.color = '#00f';
    moreNode.appendChild(moreLabel);
    moreNode.appendChild(moreContainerNode);
    dayObj.moreNode = moreNode;
  }
};

GUIMan.prototype.showMore = function() {
  if (this.className.indexOf('MoreContainer') != -1) {
    if (this.style.display == 'none') {
      this.style.display = 'block';
    }
    return;
  }
  var moreNode = this;
  var GUIMan = eXo.calendar.UICalendarMan.GUIMan;
  if (GUIMan.hideMoreTimeoutId) {
    window.clearTimeout(GUIMan.hideMoreTimeoutId);
    GUIMan.hideMoreTimeoutId = false;
  }
  /*
  if (GUIMan.preMoreNode && GUIMan.preMoreNode != moreNode) {
    var moreContainerNode = eXo.core.DOMUtil.findFirstDescendantByClass(GUIMan.preMoreNode, 'div', 'MoreContainer');
    moreContainerNode.style.display = 'none';
  }
  GUIMan.preMoreNode = moreNode;
  */
  var moreContainerNode = eXo.core.DOMUtil.findFirstDescendantByClass(moreNode, 'div', 'MoreContainer');
  if (!moreContainerNode.style.display || 
      moreContainerNode.style.display == 'none') {
    moreContainerNode.style.display = 'block';
  }
};


GUIMan.prototype.hideMore = function() {
  var GUIMan = eXo.calendar.UICalendarMan.GUIMan;
  /*
  if (!GUIMan.backupNode) {
    GUIMan.backupNode = this;
    if (GUIMan.hideMoreTimeoutId) {
      window.clearTimeout(GUIMan.hideMoreTimeoutId);
      GUIMan.hideMoreTimeoutId = false;
    }
    GUIMan.hideMoreTimeoutId = window.setTimeout(GUIMan.hideMore, 2500);
    return;
  }
  if (GUIMan.backupNode &&
      GUIMan.backupNode.style.display != 'none') {
    GUIMan.backupNode.style.display = 'none';
  }
  GUIMan.backupNode = false;
  */
  if (this.style.display != 'none') {
    this.style.display = 'none';
  }
};

/**
 *
 * @param {EventObject} eventObj
 * @param {Integer} startTime
 * @param {Integer} endTime
 * @param {Integer} weekIndex
 * @param {Object} dayInfo
 */
GUIMan.prototype.drawEventByDay = function(eventObj, startTime, endTime, dayInfo){
  var eventNode = eventObj.rootNode;
  if (eventNode.getAttribute('used') == 'true') {
    eventNode = eventNode.cloneNode(true);
    eventNode.setAttribute('eventclone', 'true');
    // Remove checkbox on clone event
    try {
      var checkBoxTmp = eventNode.getElementsByTagName('input')[0];
      checkBoxTmp.style.display = 'none';
    } catch(e) {}
    this.rowContainerDay.appendChild(eventNode);
    eventObj.cloneNodes.push(eventNode);
  }
  var topPos = dayInfo.eventTop ;
  var leftPos = dayInfo.left;
  endTime = new Date(endTime);
  startTime = new Date(startTime);
  var delta = endTime.getDay() - startTime.getDay();
  if (startTime.getDay() != endTime.getDay()) {
    delta ++ ;
  }
  delta = (delta < 1) ? 1 : delta;
  var eventLen = (Math.round(delta) * (dayInfo.width));
  with (eventNode.style) {
    top = topPos + 'px';
    left = leftPos + 'px';
    width = eventLen + 'px';
  }
  eventNode.setAttribute('used', 'true');
};

GUIMan.prototype.setDynamicSize4Month = function() {
  var DOMUtil = eXo.core.DOMUtil;
  var events = eXo.calendar.UICalendarMan.EventMan.events;
  var cellWidth = (this.tableData[0])[0].offsetWidth - 1;
  var totalWidth = cellWidth * 7;
  for (var i=0; i<events.length; i++) {
    var eventNode = events[i].rootNode;
    eventNode.style.width = parseFloat(parseInt(eventNode.style.width)/totalWidth)*100 + '%';
    eventNode.style.left = parseFloat(parseInt(eventNode.style.left)/totalWidth)*100 + '%';
    for (var j=0; j<events[i].cloneNodes.length; j++) {
      var tmpNode = events[i].cloneNodes[j];
      tmpNode.style.width = parseFloat(parseInt(tmpNode.style.width)/totalWidth)*100 + '%';
    }
  }
  var weeks = eXo.calendar.UICalendarMan.EventMan.weeks;
  // Remove old more node if exist
  for (var i=0; i<weeks.length; i++) {
    var curentWeek = weeks[i];
    if (curentWeek.events.length > 0) {
      for (var j=0; j<curentWeek.days.length; j++) {
        var moreNode = curentWeek.days[j].moreNode;
        if (moreNode) {
          moreNode.style.width = parseFloat(parseInt(moreNode.style.width)/totalWidth)*100 + '%';
          moreNode.style.left = parseFloat(parseInt(moreNode.style.left)/totalWidth)*100 + '%';
          var moreContainer = DOMUtil.findFirstDescendantByClass(moreNode, 'div', 'MoreContainer');
          moreContainer.style.width = 100 + '%';
          var eventNodes = DOMUtil.findDescendantsByClass(moreContainer, 'div', 'DayContentContainer');
          for (var k=0; k<eventNodes.length; k++) {
            eventNodes[k].style.width = '100%';
          }
        }
      }
    }
  }
};

GUIMan.prototype.setDynamicSize4Week = function() {
  var events = eXo.calendar.UICalendarMan.EventMan.week.events;
  var totalWidth = this.eventAlldayNode.offsetWidth;
  for (var i=0; i<events.length; i++) {
    var eventNode = events[i].rootNode;
    eventNode.style.width = parseFloat(parseInt(eventNode.style.width)/totalWidth) * 100 + '%';
    eventNode.style.left = parseFloat(parseInt(eventNode.style.left)/totalWidth) * 100 + '%';
  }
};


// Initialize  highlighter
GUIMan.prototype.initHighlighter = function(form) {
  for(var i=0 ; i<this.tableData.length; i++) {
    var row = this.tableData[i];
    for (var j=0; j<row.length; j++) {
      row[j].onmousedown = eXo.calendar.Highlighter.start ;
    }
  }
} ;

GUIMan.prototype.callbackHighlighter = function() {
  var Highlighter = eXo.calendar.Highlighter ;
  var startTime = parseInt(Highlighter.firstCell.getAttribute('startTime'));
  var endTime = parseInt(Highlighter.lastCell.getAttribute('startTime')) + (1000 * 60 * 60 * 24) - 1;
  eXo.webui.UIForm.submitEvent('UIMonthView' ,'QuickAdd','&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime); 
} ;

GUIMan.prototype.callbackHighlighterDays = function() {
  var Highlighter = eXo.calendar.Highlighter ;
  Highlighter.block[0].style.display = "none" ;
  var startTime = parseInt(Highlighter.firstCell.getAttribute("startTime")) ;
  var endTime = parseInt(Highlighter.lastCell.getAttribute("startTime")) + 24*60*60*1000 - 1 ;
  eXo.webui.UIForm.submitEvent("UIWeekView" ,'QuickAdd','&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime) ; 
} ;

eXo.calendar.UICalendarMan = {
  initMonth : function(rootNode) {
    rootNode = document.getElementById('UIMonthView');
    rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
    this.EventMan.initMonth(rootNode);
    this.GUIMan.initMonth();
  },
  initWeek : function(rootNode) {
    rootNode = document.getElementById('UIWeekViewGridAllDay');
    rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
    this.EventMan.initWeek(rootNode);
    this.GUIMan.initWeek();
  },
  EventMan: new EventMan(),
  GUIMan: new GUIMan()
}
