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
  this.cutDownNodes = new Array();
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
  this.linkDay = false;
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
      return true;
    }
  }
  return false;
};

/**
 * 
 * @param {EventObject} eventObj
 */
DayMan.prototype.isInvisibleEventExist = function(eventObj) {
  for (var i=0; i<this.invisibleGroup.length; i++) {
    if (this.invisibleGroup[i] == eventObj) {
      return true;
    }
  }
  return false;
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
    if (this.linkDay && this.linkDay.isInvisibleEventExist(this.events[i])) {
      this.invisibleGroup.push(this.events[i]);
//      this.invisibleGroup[i] = this.events[i];
    } else if(this.visibleGroup.length < this.totalEventVisible) {
      this.visibleGroup.push(this.events[i]);
//      this.visibleGroup[i] = this.events[i];
    } else {
      this.invisibleGroup.push(this.events[i]);
//      this.invisibleGroup[i] = this.events[i];
    }

//    if (i<(this.totalEventVisible)) {
//      this.visibleGroup.push(this.events[i]);
////      this.visibleGroup[this.visibleGroup.length] = this.events[i];
//    } else {
//      this.invisibleGroup.push(this.events[i]);
////      this.invisibleGroup[this.invisibleGroup.length] = this.events[i];
//    }
  }
};

/**
 * 
 * This method will do: get free place event's index of currrent day for
 * next day paint event process.
 * Free place event's index are anything else visible group place index. 
 * 
 * @param {Integer} index
 */
DayMan.prototype.getFreeEventIndex = function(index) {
  var cnt = 0;
  for (var i=0; i<this.visibleGroup.length; i++) {
    if (!this.visibleGroup[i]) {
      if (cnt == index) {
        return i;
      }
      cnt ++;
    }
  }
  if (this.visibleGroup.length == index) {
    return this.visibleGroup.length;
  }
  return ((index - this.visibleGroup.length) + this.visibleGroup.length);
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
      this.days[i].linkDay = this.days[i-1];
    }
  }

  // Put events to days
  for (var i=0; i<this.events.length; i++) {
    var eventObj = this.events[i];
    var startWeekTime = eventObj.weekStartTimeIndex[this.weekIndex];
    var endWeekTime = eventObj.endTime > this.endWeek ? this.endWeek : eventObj.endTime;
    var startDay = (new Date(parseInt(startWeekTime))).getDay();
    var endDay = (new Date(parseInt(endWeekTime))).getDay();
    for (var j=startDay; j<=endDay; j++) {
      this.days[j].events.push(eventObj);
    }
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

function EventMan(){
  this.events = false;
  this.weeks = new Array();
}

/**
 *
 * @param {Object} rootNode
 */
EventMan.prototype.init = function(rootNode){
  rootNode = typeof(rootNode) == 'string' ? document.getElementById(rootNode) : rootNode;
  this.rootNode = rootNode;
  var DOMUtil = eXo.core.DOMUtil;
  // Parse all event node to event object
  var allEvents = DOMUtil.findDescendantsByClass(rootNode, 'div', 'DayContentContainer');
  this.events = new Array();
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
  }
};

function GUIMan(){
  this.EVENT_BAR_HEIGH = 18;
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
  this.paint();
};

GUIMan.prototype.paint = function(){
  var weeks = eXo.calendar.UICalendarMan.EventMan.weeks;
  for (var i=0; i<weeks.length; i++) {
    var curentWeek = weeks[i];
    curentWeek.putEvents2Days();
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
  dayObj.synchronizeGroups();
  // Pre-calculate event position
  var dayNode = (this.tableData[weekObj.weekIndex])[dayIndex];
  var dayInfo = {
    width : dayNode.offsetWidth,
    left : dayNode.offsetLeft - 1,
    top : dayNode.offsetTop
  }
  // Draw visible events
  for (var i=0; i<dayObj.visibleGroup.length; i++) {
    if (!dayObj.visibleGroup[i] || 
        (dayObj.linkDay && dayObj.linkDay.isVisibleEventExist(dayObj.visibleGroup[i]))) {
      continue;
    }
    var eventObj = dayObj.visibleGroup[i];
    var startTime = eventObj.weekStartTimeIndex[weekObj.weekIndex];
    var endTime = eventObj.endTime > weekObj.endWeek ? weekObj.endWeek : eventObj.endTime;
    var eventIndex = i;
    if (dayObj.linkDay) {
      eventIndex = dayObj.linkDay.getFreeEventIndex(i);
    }
    dayInfo.eventTop = dayInfo.top + ((this.EVENT_BAR_HEIGH) * eventIndex);
    this.drawEvent(eventObj, startTime, endTime, weekObj.weekIndex, eventIndex, dayInfo);
  }
  
  // Draw invisible events (put all into more)
  if (dayObj.invisibleGroup.length > 0) {
    var moreNode = document.createElement('div');
    this.rowContainerDay.appendChild(moreNode);
    moreNode.innerHTML = 'more...';
    with (moreNode.style) {
      position = 'absolute';
      width = dayInfo.width + 'px';
      left = dayInfo.left + 'px' ;
      top = dayInfo.top + (dayObj.visibleGroup.length * this.EVENT_BAR_HEIGH) + 5 + 'px';
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
    for (var i=0; i<dayObj.invisibleGroup.length; i++) {
      var eventObj = dayObj.invisibleGroup[i];
      if (!eventObj) {
        continue;
      }
      var eventNode = eventObj.rootNode;
//      debugger;
      eventNode.style.display = 'none';
      eventNode = eventNode.cloneNode(true);
      eventNode.setAttribute('eventclone', 'true');
      // Remove checkbox on clone event
      try {
        var checkBoxTmp = eventNode.getElementsByTagName('input')[0].parentNode;
        checkBoxTmp.parentNode.removeChild(checkBoxTmp);
      } catch(e) {}
      moreContainerNode.appendChild(eventNode);
      eventObj.cutDownNodes.push(eventNode);
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
    }
    moreNode.appendChild(moreContainerNode);
  }
};

GUIMan.prototype.toggleMore = function() {
  var moreNode = this;
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
  if (eventNode.getAttribute('used')) {
    eventNode = eventNode.cloneNode(true);
    eventNode.setAttribute('eventclone', 'true');
    // Remove checkbox on clone event
    try {
      var checkBoxTmp = eventNode.getElementsByTagName('input')[0].parentNode;
      checkBoxTmp.parentNode.removeChild(checkBoxTmp);
    } catch(e) {}
    this.rowContainerDay.appendChild(eventNode);
    eventObj.cutDownNodes.push(eventNode);
  }
  startTime = new Date(parseInt(startTime));
  var startDay = startTime.getDay();
  var topPos = dayInfo.eventTop ;
  var leftPos = dayInfo.left;
  endTime = new Date(parseInt(endTime));
  var endDay = endTime.getDay();
  endDay ++ ;
  var eventLen = ((endDay - startDay) * (dayInfo.width)) - 1;
  with (eventNode.style) {
    display = 'block';
    top = topPos + 'px';
    left = leftPos + 'px';
    width = eventLen + 'px';
  }
  eventNode.setAttribute('used', 'true');
}

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