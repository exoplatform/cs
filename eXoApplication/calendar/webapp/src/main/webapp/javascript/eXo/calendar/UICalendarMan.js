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
  this.calId = false;
  this.calType = false;
  this.endTime = false;
  this.eventCat = false;
  this.eventId = false;
  this.startIndex = false;
  this.startTime = false;
  this.weekStartTimeIndex = new Array();
  this.cloneNodes = new Array();
  this.rootNode = false;
  this.name = false;
}

EventObject.prototype.init = function(rootNode){
  if (!rootNode) {
    return;
  }
  rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  this.rootNode = rootNode;
  this.startIndex = this.rootNode.getAttribute('startindex');
  this.calType = this.rootNode.getAttribute('caltype');
  this.eventId = this.rootNode.getAttribute('eventid');
  this.calId = this.rootNode.getAttribute('calid');
  this.eventCat = this.rootNode.getAttribute('eventcat');
  this.startTime = this.rootNode.getAttribute('starttime');
  this.endTime = this.rootNode.getAttribute('endtime');
  this.name = this.rootNode.textContent.trim();
};

EventObject.prototype.toString = function(){
  var objIdentify = 'name: ' + this.name;
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
  if (this.events.length > this.MAX_EVENT_VISIBLE) {
    this.totalEventVisible = this.MAX_EVENT_VISIBLE - 1;
  } else {
    this.totalEventVisible = this.MAX_EVENT_VISIBLE;
  }
  for (var i=0; i<this.events.length; i++) {
    if (this.previousDay && 
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
        this.previousDay.visibleGroup[(this.MAX_EVENT_VISIBLE - 1)] == eventTmp &&
        this.invisibleGroup.length > 0) {
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
    var cntTmp = 0;
    for (var j=0; j<tmp.length; j++) {
      if (!tmp[j] && cntTmp == cnt) {
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
}

WeekMan.prototype.init = function(){
};

WeekMan.prototype.putEvents2Days = function(){
  if (this.events.length <= 0) {
    return;
  }
  if (!this.isEventsSorted) {
    this.sortEvents();
  }
  // Create 7 days
  for (var i=0; i<7; i++) {
    this.days[i] = new DayMan();
    // link days
    if (i > 0) {
      this.days[i].previousDay = this.days[i-1];
    }
  }
  
  for (var i=0; i<this.days.length-1; i++) {
    this.days[i].nextDay = this.days[i+1];    
  }

  // Put events to days
  for (var i=0; i<this.events.length; i++) {
    var eventObj = this.events[i];
    var startWeekTime = eventObj.weekStartTimeIndex[this.weekIndex];
    var endWeekTime = eventObj.endTime > this.endWeek ? this.endWeek : eventObj.endTime;
    var startDay = (new Date(parseInt(startWeekTime))).getDay();
    var endDay = (new Date(parseInt(endWeekTime))).getDay();
    // fix date
    var delta = (new Date(parseInt(eventObj.endTime))) - (new Date(parseInt(eventObj.startTime)));
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

WeekMan.prototype.sortEvents = function(){
  eXo.core.QuickSortObject.doSort(this.events, false, this.compareEventByWeek, this.weekIndex);
  this.isEventsSorted = true;
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
EventMan.prototype.init = function(rootNode){
  this.events = new Array();
  this.weeks = new Array();
  rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  this.rootNode = rootNode;
  var DOMUtil = eXo.core.DOMUtil;
  // Parse all event node to event object
  var allEvents = DOMUtil.findDescendantsByClass(rootNode, 'div', 'DayContentContainer');
  // Create and init all event
  for (var i = 0; i < allEvents.length; i++) {
    var eventObj = new EventObject();
    eventObj.init(allEvents[i]);
    this.events[i] = eventObj;
  }
  this.UIMonthViewGrid = document.getElementById('UIMonthViewGrid');
  this.groupByWeek();
  this.sortByWeek();
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
      startWeek = parseInt(startCell.getAttribute("startTime"));
      endWeek = startWeek + 6 * 24 * 60 * 60 * 1000;
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
    if (currentWeek.events.length > 1) {
      currentWeek.sortEvents();
    }
    currentWeek.putEvents2Days();
  }
};

function GUIMan(){
  this.EVENT_BAR_HEIGH = 18;
  this.moreNodes = new Array();
  this.CELL_WIDTH = false;
}

/**
 *
 * @param {EventMan} eventMan
 */
GUIMan.prototype.init = function(){
  if (eXo.calendar.UICalendarMan.EventMan.events.length > 0) {
    this.EVENT_BAR_HEIGH = eXo.calendar.UICalendarMan.EventMan.events[0].rootNode.offsetHeight - 1;
  }
  var DOMUtil = eXo.core.DOMUtil;
  this.rowContainerDay = DOMUtil.findFirstDescendantByClass(eXo.calendar.UICalendarMan.EventMan.rootNode, 'div', 'RowContainerDay');
  var rows = eXo.calendar.UICalendarMan.EventMan.UIMonthViewGrid.getElementsByTagName('tr');
  this.tableData = new Array();
  for (var i = 0; i < rows.length; i++) {
    var rowData = DOMUtil.findDescendantsByClass(rows[i], 'td', 'UICellBlock');
    this.tableData[i] = rowData;
  }
  this.CELL_WIDTH = (this.tableData[0])[0].offsetWidth;
  this.paint();
  this.setDynamicSize();
  this.scrollTo();
  this.initDND();
};
 
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

GUIMan.prototype.paint = function(){
  var weeks = eXo.calendar.UICalendarMan.EventMan.weeks;
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
    width : dayNode.offsetWidth,
    left : dayNode.offsetLeft - 1,
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
    var delta = (new Date(parseInt(endTime))) - (new Date(parseInt(startTime)));
    delta /= (1000 * 60 * 60 *24);
    if (delta > 1 && 
        dayObj.nextDay && 
        i == (dayObj.MAX_EVENT_VISIBLE - 1)) {
      if (eventObj.toString().indexOf('event1') != -1) {
//        debugger;
      }
      var tmp = dayObj.nextDay;
      var cnt = 0;
      while (tmp.nextDay && cnt<delta) {
        if (tmp.isInvisibleEventExist(eventObj) >= 0) {
          break;
        }
        cnt++;
        tmp = tmp.nextDay;
      }
      if (eventObj.toString().indexOf('event1') != -1) {
//        debugger;
      }
      endTime = parseInt(startTime) + ((1000 * 60 * 60 * 24) * cnt);
    }
    dayInfo.eventTop = dayInfo.top + ((this.EVENT_BAR_HEIGH) * i);
    this.drawEvent(eventObj, startTime, endTime, weekObj.weekIndex, i, dayInfo);
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
    moreNode.onclick = this.toggleMore;
    var moreContainerNode = document.createElement('div');
    with (moreContainerNode.style) {
      position = 'absolute';
      display = 'none';
      top = '0px';
      left = '0px';
    }
    moreContainerNode.className = 'MoreContainer';
    // Create invisible event
    var cnt = 0
    for (var i=0; i<dayObj.invisibleGroup.length; i++) {
      var eventObj = dayObj.invisibleGroup[i];
      if (!eventObj) {
        continue;
      }
      cnt ++;
      var eventNode = eventObj.rootNode;
      if (eventNode.getAttribute('used')) {
        eventNode = eventNode.cloneNode(true);
        eventNode.setAttribute('eventclone', 'true');
        this.rowContainerDay.appendChild(eventNode);
        eventObj.cloneNodes.push(eventNode);
        eventNode.setAttribute('eventclone', 'true');
      }
      // Remove checkbox on clone event
      try {
        var checkBoxTmp = eventNode.getElementsByTagName('input')[0];
        eXo.core.DOMUtil.removeElement(checkBoxTmp.parentNode);
      } catch(e) {}
      moreContainerNode.appendChild(eventNode);
      eventObj.cloneNodes.push(eventNode);
      startTime = new Date(parseInt(startTime));
      var startDay = startTime.getDay();
      var topPos = this.EVENT_BAR_HEIGH * i;
      endTime = new Date(parseInt(endTime));
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
    this.moreNodes.push(moreNode);
  }
};

GUIMan.prototype.toggleMore = function() {
  var moreNode = this;
  var GUIMan = eXo.calendar.UICalendarMan.GUIMan;
  if (GUIMan.preMoreNode && GUIMan.preMoreNode != moreNode) {
    var moreContainerNode = eXo.core.DOMUtil.findFirstDescendantByClass(GUIMan.preMoreNode, 'div', 'MoreContainer');
    moreContainerNode.style.display = 'none';
  }
  GUIMan.preMoreNode = moreNode;
  var moreContainerNode = eXo.core.DOMUtil.findFirstDescendantByClass(moreNode, 'div', 'MoreContainer');
  if (!moreContainerNode.style.display || 
      moreContainerNode.style.display == 'none') {
        
    moreContainerNode.style.display = 'block';
  } else {
    moreContainerNode.style.display = 'none';
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
GUIMan.prototype.drawEvent = function(eventObj, startTime, endTime, weekIndex, eventIndex, dayInfo){
  var eventNode = eventObj.rootNode;
  if (eventObj.toString().indexOf('event1') != -1) {
//    debugger;
  }
  if (eventNode.getAttribute('used')) {
    eventNode = eventNode.cloneNode(true);
    eventNode.setAttribute('eventclone', 'true');
    // Remove checkbox on clone event
    try {
      var checkBoxTmp = eventNode.getElementsByTagName('input')[0];
      eXo.core.DOMUtil.removeElement(checkBoxTmp.parentNode);
    } catch(e) {}
    this.rowContainerDay.appendChild(eventNode);
    eventObj.cloneNodes.push(eventNode);
  }
  startTime = new Date(parseInt(startTime));
  var startDay = startTime.getDay();
  var topPos = dayInfo.eventTop ;
  var leftPos = dayInfo.left;
  endTime = new Date(parseInt(endTime));
  var endDay = endTime.getDay();
  // fix date
  var delta = ((new Date(parseInt(eventObj.endTime))) - (new Date(parseInt(eventObj.startTime))));
//  delta = endTime - startTime;
  delta /= (1000 * 60 * 60 * 24);
  if (delta > 1) {
    endDay ++;
    delta = endDay - startDay;
  } else {
    delta = 1;
  }
  var eventLen = (delta * (dayInfo.width)) - delta;
  with (eventNode.style) {
    top = topPos + 'px';
    left = leftPos + 'px';
    width = eventLen + 'px';
  }
  eventNode.setAttribute('used', 'true');
};

GUIMan.prototype.setDynamicSize = function() {
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
      var containerNode = DOMUtil.findAncestorByClass(tmpNode, 'MoreContainer');
      if (containerNode) {
        tmpNode.style.width = '100%';
      } else {
        tmpNode.style.width = parseFloat(parseInt(tmpNode.style.width)/totalWidth)*100 + '%';
      }
    }
  }
  for (var i=0; i<this.moreNodes.length; i++) {
    this.moreNodes[i].style.width = parseFloat(parseInt(this.moreNodes[i].style.width)/totalWidth)*100 + '%';
    this.moreNodes[i].style.left = parseFloat(parseInt(this.moreNodes[i].style.left)/totalWidth)*100 + '%';
    var moreContainer = DOMUtil.findFirstDescendantByClass(this.moreNodes[i], 'div', 'MoreContainer');
    moreContainer.style.width = 100 + '%';
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
  var endTime = parseInt(Highlighter.lastCell.getAttribute('startTime')) + (1000 * 60 * 60 * 24);
  eXo.webui.UIForm.submitEvent('UIMonthView' ,'QuickAdd','&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime); 
} ;

eXo.calendar.UICalendarMan = {
  init : function(rootNode) {
    rootNode = document.getElementById('UIMonthView');
    rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
    this.EventMan.init(rootNode);
    this.GUIMan.init();
  },
  EventMan: new EventMan(),
  GUIMan: new GUIMan()
}