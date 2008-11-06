function Highlighter() {

}

Highlighter.prototype.getPos = function(cell) {
	return {
		"x" : cell.cellIndex,
		"y" : cell.parentNode.rowIndex
	}
} ;

Highlighter.prototype.isInCell = function(cell, _e) {
	var Highlighter = eXo.calendar.Highlighter ;
	var cellX = eXo.core.Browser.findPosX(cell) - Highlighter.container.scrollLeft ;
	var cellY = eXo.core.Browser.findPosY(cell) - Highlighter.container.scrollTop ;
	var mouseX = eXo.core.Browser.findMouseXInPage(_e) ;
	var mouseY = eXo.core.Browser.findMouseYInPage(_e) ;
	if(document.getElementById("UIPageDesktop")) {
		mouseX = eXo.core.Browser.findMouseXInPage(_e) ;
		mouseY = eXo.core.Browser.findMouseYInPage(_e) ;
		cellX = eXo.core.Browser.findPosX(cell) - eXo.cs.Utils.getScrollLeft(cell) ;
		cellY = eXo.core.Browser.findPosY(cell) - eXo.cs.Utils.getScrollTop(cell) ;
	}
	var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
	if(document.all && uiControlWorkspace && (!document.getElementById("UIPageDesktop") ||  eXo.core.Browser.isIE7())) cellX -= uiControlWorkspace.offsetWidth ;
	if (
		 (mouseX > cellX) && (mouseX < (cellX + cell.offsetWidth))
	&& (mouseY > cellY) && (mouseY < (cellY + cell.offsetHeight))
	) { return true ;}
	return false ;
} ;

Highlighter.prototype.getMousePos = function(evt) {
	var Highlighter = eXo.calendar.Highlighter ;
	var _e = window.event || evt ;
	var cell = Highlighter.cell ;
	var len = cell.length ;
	for(var i = 0 ; i < len ; i ++) {
		if (Highlighter.isInCell(cell[i], _e)) {
			Highlighter.currentCell = cell[i] ;
			return Highlighter.getPos(Highlighter.currentCell) ;
		}
	}
} ;

Highlighter.prototype.hideAll = function() {
	var obj  = (arguments.length >0) ? arguments[0]: null ;
	var blocks = eXo.calendar.Highlighter.block ;
	var len = blocks.length ;
	for(var i = 0 ; i < len ; i ++ ) {
		if ((obj != null) && (obj == blocks[i])) blocks[i].style.display  = "block" ;		
		else blocks[i].style.display  = "none" ;
	}
} ;

Highlighter.prototype.hideBlock = function(start,end) {
	var blocks = eXo.calendar.Highlighter.block ;
	var len = blocks.length ;
	for(var i = 0 ; i < len ; i ++ ) {
		if ((i < start) || (i > end)) blocks[i].style.display  = "none" ;
	}
} ;

Highlighter.prototype.createBlock = function(cell) {
	var DOMUtil = eXo.core.DOMUtil ;
	var table = DOMUtil.findAncestorByTagName(cell, "table") ;
	var tr = DOMUtil.findDescendantsByTagName(table, "tr") ;
	var len = tr.length ;
	var div = null ;
	var block = new Array() ;
	for(var i = 0 ; i < len ; i ++) {
		div = document.createElement("div") ;
		div.onmousedown = eXo.calendar.Highlighter.hideAll ;
		if(document.getElementById("UserSelectionBlock"+i)) DOMUtil.removeElement(document.getElementById("UserSelectionBlock"+i)) ; 
		div.setAttribute("id", "UserSelectionBlock"+i) ;
		div.className = "UserSelectionBlock" ;
		table.parentNode.appendChild(div) ;
		block.push(div) ;
	}
	eXo.calendar.Highlighter.block = block ;
} ;

Highlighter.prototype.start = function(evt) {
	try{		
	var Highlighter = eXo.calendar.Highlighter ;
	var _e = window.event || evt ;
	if(_e.button == 2) return ;
	_e.cancelBubble = true ;
	Highlighter.startCell = this ;
	var table = eXo.core.DOMUtil.findAncestorByTagName(Highlighter.startCell, "table") ;
	var callback = table.getAttribute("eXoCallback") ;
	if (callback) Highlighter.callback = callback ;
	Highlighter.cell = eXo.core.DOMUtil.findDescendantsByClass(table, Highlighter.startCell.tagName.toLowerCase(), "UICellBlock") ;
	Highlighter.cellLength = eXo.core.DOMUtil.findDescendantsByTagName(Highlighter.startCell.parentNode,Highlighter.startCell.tagName.toLowerCase()).length ;
	Highlighter.dimension = {"x":(Highlighter.startCell.offsetWidth), "y":(Highlighter.startCell.offsetHeight)} ;
	var pos = Highlighter.getPos(Highlighter.startCell) ;
	Highlighter.createBlock(Highlighter.startCell) ;
	Highlighter.hideAll() ;
	Highlighter.startBlock = Highlighter.block[pos.y] ;
	Highlighter.startBlock.style.display = "block" ;
	Highlighter.container = Highlighter.startBlock.offsetParent ;
	var fixleftIE = (document.all && document.getElementById("UIWeekView"))? 6 : 0 ; //TODO : No hard code 
	var x = eXo.core.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) -  fixleftIE ;
	var y = eXo.core.Browser.findPosYInContainer(Highlighter.startCell, Highlighter.container) - document.getElementById(eXo.calendar.UICalendarPortlet.portletName).parentNode.scrollTop ;
	Highlighter.startBlock.style.left = x + "px" ;
	Highlighter.startBlock.style.top = y + "px" ;
	Highlighter.startBlock.style.width = Highlighter.dimension.x + "px" ;
	Highlighter.startBlock.style.height = Highlighter.dimension.y + "px" ;
	document.onmousemove = Highlighter.execute ;
	document.onmouseup = Highlighter.end ;
	Highlighter.firstCell = Highlighter.startCell ;
	Highlighter.lastCell = Highlighter.startCell ;
	} catch(e) {alert(e.message) ;}
} ;

Highlighter.prototype.execute = function(evt) {
	var Highlighter = eXo.calendar.Highlighter ;
	var _e = window.event || evt ;	
	var sPos = Highlighter.getPos(Highlighter.startCell) ;
	var fixleftIE = (document.all && document.getElementById("UIWeekView"))? 6 : 0 ; //TODO : No hard code 
	try{
		var cPos = Highlighter.getMousePos(_e) ;		
		var len = cPos.y - sPos.y ;	
		var startBlock = null ;
		var endBlock = null ;
		var startIndex = null ;
		var lastIndex = null ;
		var startX = null ;
		var startY = null ;
		var endX = null ;
		var startWidth = null ;
		if(len == 0) {
			var diff = cPos.x - sPos.x ;
			startBlock = Highlighter.startBlock ;
			Highlighter.hideAll(startBlock) ;
			if (diff > 0) {
				startBlock.style.left = eXo.core.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) - fixleftIE + "px" ;
				startBlock.style.width = (diff + 1)*Highlighter.dimension.x + "px" ;
				Highlighter.firstCell = Highlighter.startCell ;
				Highlighter.lastCell  = Highlighter.currentCell ;
			} else {
				startBlock.style.left = eXo.core.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) + diff*Highlighter.dimension.x - fixleftIE + "px" ;
			 	startBlock.style.width = (1 - diff)*Highlighter.dimension.x + "px" ;
			 	Highlighter.lastCell = Highlighter.startCell ;
				Highlighter.firstCell  = Highlighter.currentCell ;
			}
			
		} else {		
			if (len >= 0) {
				startIndex = sPos.y ;
				lastIndex = startIndex + len ;
				startBlock = Highlighter.startBlock
				endBlock = Highlighter.block[lastIndex] ;
				startX = eXo.core.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) ;
				startY = eXo.core.Browser.findPosYInContainer(Highlighter.startCell, Highlighter.container) ;
				endX = (cPos.x + 1)*Highlighter.dimension.x ;
				startWidth = (Highlighter.cellLength - sPos.x)*Highlighter.dimension.x ;
				Highlighter.firstCell = Highlighter.startCell ;
				Highlighter.lastCell  = Highlighter.currentCell ;
			} else {
				startIndex = sPos.y  + len ;
				lastIndex = sPos.y ;
				startBlock = Highlighter.block[startIndex] ;
				endBlock = Highlighter.block[lastIndex] ;
				startX = eXo.core.Browser.findPosXInContainer(Highlighter.currentCell, Highlighter.container) ;
				startY = eXo.core.Browser.findPosYInContainer(Highlighter.currentCell, Highlighter.container) ;
				endX = (sPos.x + 1)*Highlighter.dimension.x ;
				startWidth = (Highlighter.cellLength - cPos.x)*Highlighter.dimension.x ;
				Highlighter.lastCell = Highlighter.startCell ;
				Highlighter.firstCell  = Highlighter.currentCell ;
			}
			startBlock.style.display = "block" ;
			startBlock.style.top = startY + "px" ;
			startBlock.style.left = startX - fixleftIE + "px" ;
			startBlock.style.width = startWidth + "px" ;
			startBlock.style.height = Highlighter.dimension.y + "px" ;
			if(Math.abs(len) >= 1) {
				for(var i = startIndex + 1 ; i < (startIndex + Math.abs(len)); i ++) {
					Highlighter.block[i].style.display  = "block" ;
					Highlighter.block[i].style.top  = parseInt(Highlighter.block[i - 1].style.top) + Highlighter.dimension.y + "px" ;
					Highlighter.block[i].style.left  = eXo.core.Browser.findPosXInContainer(Highlighter.cell[0], Highlighter.container) + "px" ;
					Highlighter.block[i].style.width = Highlighter.cellLength*Highlighter.dimension.x + "px" ;
					Highlighter.block[i].style.height = Highlighter.dimension.y + "px" ;
				}
			}
			endBlock.style.display  = "block" ;
			endBlock.style.top  = parseInt(Highlighter.block[lastIndex - 1].style.top) + Highlighter.dimension.y + "px" ;
			endBlock.style.left  = eXo.core.Browser.findPosXInContainer(Highlighter.cell[0], Highlighter.container) - fixleftIE + "px" ;
			endBlock.style.width = endX + "px" ;
			endBlock.style.height = Highlighter.dimension.y + "px" ;
			Highlighter.hideBlock(startIndex, lastIndex) ;
		}
	} catch(e){
			window.status = e.message ;
	}			
} ;

Highlighter.prototype.end = function(evt) {
	var Highlighter = eXo.calendar.Highlighter;
	if (Highlighter.callback) eval(Highlighter.callback) ;	
	document.onmousemove = null ;
	document.onmouseup = null ;
} ;

Highlighter.prototype.setCallback = function(str) {
	this.container.setAttribute("eXoCallback",str) ;
} ;

eXo.calendar.Highlighter = new Highlighter() ;

function UIHSelection() {
} ;

UIHSelection.prototype.isInCell = function(cell, _e) {
	var UIHSelection = eXo.calendar.UIHSelection ;
	var cellX = eXo.core.Browser.findPosX(cell) - UIHSelection.container.scrollLeft ;
	var cellY = eXo.core.Browser.findPosY(cell) - UIHSelection.container.scrollTop ;
	var mouseX = eXo.core.Browser.findMouseXInPage(_e) ;
	var mouseY = eXo.core.Browser.findMouseYInPage(_e) ;
	if(document.getElementById("UIPageDesktop")) {
		mouseX = eXo.core.Browser.findMouseXInPage(_e) ;
		mouseY = eXo.core.Browser.findMouseYInPage(_e) ;
		cellX = eXo.core.Browser.findPosX(cell) - eXo.cs.Utils.getScrollLeft(cell) ;
		cellY = eXo.core.Browser.findPosY(cell) - eXo.cs.Utils.getScrollTop(cell) ;
	}
	var uiControlWorkspace = document.getElementById("UIControlWorkspace") ;
	if(document.all && uiControlWorkspace && (!document.getElementById("UIPageDesktop") || eXo.core.Browser.isIE7())) cellX -= uiControlWorkspace.offsetWidth ;
	if (
		 (mouseX > cellX) && (mouseX < (cellX + cell.offsetWidth))
	&& (mouseY > cellY) && (mouseY < (cellY + cell.offsetHeight))
	) { return true ;}
	return false ;
} ;

UIHSelection.prototype.getCurrentIndex = function(evt){
	var cells = eXo.calendar.UIHSelection.cells ;
	var len = cells.length ;
	for(var i = 0 ; i < len ; i++) {
		var isCell = eXo.calendar.UIHSelection.isInCell(cells[i],evt) ;
		if(isCell) return cells[i].cellIndex ;
	}
} ;

UIHSelection.prototype.setAttr = function(sIndex, eIndex, cells){
	for(var i = sIndex; i <= eIndex ; i++) {
		eXo.core.DOMUtil.addClass(cells[i],"UserSelection") ;
	}
} ;

UIHSelection.prototype.removeAttr = function(sIndex, eIndex, cells){
	var len = cells.length ;
	var DOMUtil = eXo.core.DOMUtil ;
	for(var i = 0; i < len ; i++) {
		if((i>=sIndex) && (i<=eIndex)) continue ;
		if(DOMUtil.hasClass(cells[i],"UserSelection")) DOMUtil.replaceClass(cells[i],"UserSelection","") ;
	}
} ;

UIHSelection.prototype.removeAllAttr = function(){
	var cells = this.cells ;
	var len = cells.length ;
	var DOMUtil = eXo.core.DOMUtil ;
	for(var i = 0; i < len ; i++) {
		if(DOMUtil.hasClass(cells[i],"UserSelection")) DOMUtil.replaceClass(cells[i],"UserSelection","") ;
	}
} ;

UIHSelection.prototype.start = function(){
	var UIHSelection = eXo.calendar.UIHSelection ;
	var table = eXo.core.DOMUtil.findAncestorByTagName(this, "table") ;
	var callback = table.getAttribute("eXoCallback") ;
	if (callback) UIHSelection.callback = callback ;
	UIHSelection.startIndex = this.cellIndex ;
  var cell = (eXo.core.DOMUtil.findAncestorById(this,"eventAttender"))? "td" : "th" ;
	UIHSelection.cells = eXo.core.DOMUtil.getChildrenByTagName(this.parentNode, cell) ;
	UIHSelection.container = this.parentNode ;
  UIHSelection.removeAllAttr() ;
	eXo.core.DOMUtil.addClass(this,"UserSelection") ;
	document.onmousemove = UIHSelection.execute ;
	document.onmouseup =  UIHSelection.end ;
	UIHSelection.firstCell = UIHSelection.cells[UIHSelection.startIndex] ;
	UIHSelection.lastCell = UIHSelection.cells[UIHSelection.startIndex] ;
} ;

UIHSelection.prototype.execute = function(evt){
	var _e = window.event || evt ;
	var UIHSelection = eXo.calendar.UIHSelection ;
	var sIndex = UIHSelection.startIndex ;
	var eIndex = UIHSelection.getCurrentIndex(_e) ;
	var cells = UIHSelection.cells ;
	if(eIndex) {
		if (eIndex < sIndex) {
			UIHSelection.setAttr(eIndex, sIndex, cells) ;
			UIHSelection.removeAttr(eIndex, sIndex, cells) ;
			UIHSelection.firstCell = cells[eIndex] ;
			UIHSelection.lastCell = cells[sIndex] ;
		}else {
			UIHSelection.setAttr(sIndex, eIndex, cells) ;
			UIHSelection.removeAttr(sIndex, eIndex, cells) ;
			UIHSelection.firstCell = cells[sIndex] ;
			UIHSelection.lastCell = cells[eIndex] ;
		}
	}
} ;

UIHSelection.prototype.end = function(){
	var UIHSelection = eXo.calendar.UIHSelection ;
	UIHSelection.removeAllAttr() ;
	UIHSelection.startIndex = null ;
	UIHSelection.endIndex = null ;
	UIHSelection.cells = null ;
	UIHSelection.container = null ;
	document.onmousemove = null ;
	document.onmouseup = null ;
	if (UIHSelection.callback) eval(UIHSelection.callback) ;
} ;

eXo.calendar.UIHSelection = new UIHSelection() ;