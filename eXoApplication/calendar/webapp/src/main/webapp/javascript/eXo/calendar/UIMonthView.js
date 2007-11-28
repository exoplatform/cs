eXo.require('eXo.calendar.UICalendarPortlet', '/javascript/calendar/') ;
function UIMonthView() {
	
}

UIMonthView.prototype.init = function() {
	var UIMonthView = document.getElementById("UIMonthView") ;
	var UIMonthViewGrid = document.getElementById("UIMonthViewGrid") ;
	this.eventContainer = eXo.core.DOMUtil.findFirstDescendantByClass(UIMonthView, "div","RowContainerDay") ;
	this.items = eXo.core.DOMUtil.findDescendantsByClass(UIMonthView, "div", "DayContentContainer") ;	
	var len = this.items.length ;
	if (len <=0 ) return ;
	this.cells = eXo.core.DOMUtil.findDescendantsByTagName(UIMonthViewGrid, "td") ;
	this.startMonth = parseInt(this.cells[0].getAttribute("startTime")) ;
	this.endMonth = parseInt(this.cells[this.cells.length-1].getAttribute("startTime")) ;
	this.unitX = this.cells[0].offsetWidth - 1;
	this.unitY = this.cells[0].offsetHeight - 1 ;
	for(var i = 0 ; i < len ; i++) {
		this.createBars(this.items[i]) ;
	}
	this.items = eXo.core.DOMUtil.findDescendantsByClass(UIMonthView, "div", "DayContentContainer") ;
	this.resetSize(this.items) ;
	var row = this.cells.length/7 ;
	var eventInRows = null ;
	for(var i = 0 ; i < row ; i++) {
		eventInRows = this.getEventsInRow(i, this.items) ;
		this.arrangeEventInRows(eventInRows) ;
	}
} ;

UIMonthView.prototype.createBars = function(event) {
	var DOMUtil = eXo.core.DOMUtil ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var start = parseInt(event.getAttribute("startTime")) ;
	var end = parseInt(event.getAttribute("endTime")) ;
	if(start < this.startMonth) start = this.startMonth ;
	if(end > this.endMonth) end = this.endMonth ;
	var top = parseInt(event.getAttribute("startIndex")) ;
	var startWeek = UICalendarPortlet.getWeekNumber(start) ;
	var endWeek = UICalendarPortlet.getWeekNumber(end) ;
	var delta = endWeek - startWeek ;
	var checkbox = null ;
	if (delta == 0) {
		event.style.top = (top - 1) * this.unitY + 16 + "px" ;
		event.style.left = (new Date(start)).getDay() * this.unitX + "px" ;
		event.style.width = ((new Date(end)).getDay() - (new Date(start)).getDay()) * this.unitX + this.unitX + "px" ;
	}	else if (delta == 1) {
		event.style.top = (top - 1) * this.unitY + 16 + "px" ;
		event.style.left = (new Date(start)).getDay() * this.unitX + "px" ;
		event.style.width = (7 - (new Date(start)).getDay()) * this.unitX + "px" ;
//		event.style.width = (7 - (new Date(start)).getDay()) * this.unitX - (7 - (new Date(start)).getDay()) + "px" ;
		var event1 = event.cloneNode(true) ;
		event1.style.top = parseInt(event.style.top) + this.unitY + "px" ;
		event1.style.left = "0px" ;
		event1.style.width = (new Date(end)).getDay() * this.unitX + "px" ;
		checkbox = DOMUtil.findFirstDescendantByClass(event1, "input", "checkbox") ;
		DOMUtil.removeElement(checkbox) ;
		this.eventContainer.appendChild(event1) ;
	}else {
		var fullDayEvent = new Array() ;
		fullDayEvent.push(event) ;
		for(var i = 0 ; i < delta ; i ++) {
			fullDayEvent.push(event.cloneNode(true)) ;			
		}
		var len = fullDayEvent.length ;
		fullDayEvent[0].style.top =  (top - 1) * this.unitY + 16 + "px" ;
		fullDayEvent[0].style.left = (new Date(start)).getDay() * this.unitX  + "px" ;
		fullDayEvent[0].style.width = (7 - (new Date(start)).getDay()) * this.unitX + "px" ;
//		fullDayEvent[0].style.width = (7 - (new Date(start)).getDay()) * this.unitX - (7 - (new Date(start)).getDay()) + "px" ;
		this.eventContainer.appendChild(fullDayEvent[0]) ;
		for(var i = 1 ; i < len - 1 ; i ++) {
			fullDayEvent[i].style.top = parseInt(fullDayEvent[i-1].style.top) + this.unitY + "px" ;
			fullDayEvent[i].style.left = "0px" ;
			fullDayEvent[i].style.width = 7*this.unitX + "px" ;
			this.eventContainer.appendChild(fullDayEvent[i]) ;
			checkbox = DOMUtil.findFirstDescendantByClass(fullDayEvent[i], "input", "checkbox") ;
			DOMUtil.removeElement(checkbox) ;
		}
		if (len >= 3) {
			fullDayEvent[len - 1].style.top = parseInt(fullDayEvent[len - 2].style.top) + this.unitY + "px" ;
			fullDayEvent[len - 1].style.left = "0px" ;
			fullDayEvent[len - 1].style.width = (new Date(end)).getDay() * this.unitX + this.unitX + "px" ;
			checkbox = DOMUtil.findFirstDescendantByClass(fullDayEvent[len-1], "input", "checkbox") ;
			DOMUtil.removeElement(checkbox) ;
			this.eventContainer.appendChild(fullDayEvent[len-1]) ;
		}
	}
}

UIMonthView.prototype.resetSize = function(bars) {
	var len = bars.length ;
	var totalWidth = this.unitX*7 ;
	if (len) {
		for(var i = 0 ; i < len ; i ++) {
			bars[i].style.width = parseFloat(parseInt(bars[i].style.width)/totalWidth)*100 + "%";
			bars[i].style.left = parseFloat(parseInt(bars[i].style.left)/totalWidth)*100 + "%";
		}		
	} else {
		bars[i].style.width = parseFloat(parseInt(bars[i].style.width)/totalWidth)*100 + "%";
		bars[i].style.left = parseFloat(parseInt(bars[i].style.left)/totalWidth)*100 + "%";
	}
} ;

UIMonthView.prototype.getEventsInRow = function(row, events) {
	var minY = row*this.unitY ;
	var maxY = minY + this.unitY ;
	var len = events.length ;
	var eventInRows = new Array() ;
	var top = null ;
	for(var i = 0 ; i < len ; i ++) {
		top = events[i].offsetTop ;
		if ((top > minY) && (top < maxY)) {
			eventInRows.push(events[i])
		}
	}
	eventInRows = this.sortEventsInRow(eventInRows) ;
	return eventInRows ;
} ;

UIMonthView.prototype.arrangeEventInRows = function(eventInRows) {
	var len = eventInRows.length ;
	var checkbox = null ;
	for(var i = 0 ; i < len ; i ++) {
		checkbox = eXo.core.DOMUtil.findFirstDescendantByClass(eventInRows[i], "input", "checkbox") ;
		if (checkbox) {
			checkbox.onmousedown = function (evt) {
				var _e = window.event || evt ;
				_e.cancelBubble = true ;
			}		
		}
		eventInRows[i].onmousedown = eXo.calendar.UICalendarDragDrop.init ;
		if (i > 0) eventInRows[i].style.top = parseInt(eventInRows[i-1].style.top) + eventInRows[i-1].offsetHeight + "px" ;
	}
} ;

UIMonthView.prototype.sortEventsInRow = function(obj, type) {
	var len = obj.length ;
	var tmp = null ;
	var attribute1 = null ;
	var attribute2 = null ;
	var attribute3 = null ;
	var attribute4 = null ;
	for(var i = 0 ; i < len ; i ++){
		for(var j = i + 1 ; j < len ; j ++) {		
				attribute1 = obj[i].offsetLeft ;
				attribute2 = obj[j].offsetLeft ;
				attribute3 = obj[i].offsetWidth ;
				attribute4 = obj[j].offsetWidth ;
			if((attribute2 < attribute1) && (attribute4 > attribute3)) {
				tmp = obj[i] ;
				obj[i] = obj[j] ;
				obj[j] = tmp ;
			}
		}
	}
	return obj ;
};

eXo.calendar.UIMonthView = new UIMonthView() ;
