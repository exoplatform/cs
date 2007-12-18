function Highlighter() {
}

Highlighter.prototype.getPos = function(cell) {
	return {
		"x" : cell.cellIndex,
		"y" : cell.parentNode.rowIndex
	}
} ;

Highlighter.prototype.getMousePos = function(evt) {
	var Highlighter = eXo.calendar.Highlighter ;
	var _e = window.event || evt ;
	var cell = Highlighter.cell ;
	var len = cell.length ;
	for(var i = 0 ; i < len ; i ++) {
		if (Highlighter.isInCell(cell[i], _e)) {
			Highlighter.currentCell = cell[i] ;
			return Highlighter.getPos(cell[i]) ;
		}
	}
} ;

Highlighter.prototype.isInCell = function(cell, _e) {
	var Highlighter = eXo.calendar.Highlighter ;
	var cellX = eXo.core.Browser.findPosXInContainer(cell, Highlighter.container) ;
	var cellY = eXo.core.Browser.findPosYInContainer(cell, Highlighter.container) - Highlighter.container.scrollTop ;
	var mouseX = eXo.core.Browser.findMouseRelativeX(Highlighter.container, _e) ;
	var mouseY = eXo.core.Browser.findMouseRelativeY(Highlighter.container, _e) ;
	if (
		 (mouseX > cellX) && (mouseX < (cellX + cell.offsetWidth))
	&& (mouseY > cellY) && (mouseY < (cellY + cell.offsetHeight))
	) { return true ;}
	return false ;
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

Highlighter.prototype.start = function(evt) {
	var Highlighter = eXo.calendar.Highlighter ;
	var _e = window.event || evt ;
	if(_e.button == 2) return ;
	_e.cancelBubble = true ;
	Highlighter.startCell = this ;
	Highlighter.cellLength = eXo.core.DOMUtil.findDescendantsByTagName(Highlighter.startCell.parentNode,Highlighter.startCell.tagName.toLowerCase()).length ;
	Highlighter.dimension = {"x":(Highlighter.startCell.offsetWidth), "y":(Highlighter.startCell.offsetHeight)} ;
	var pos = Highlighter.getPos(Highlighter.startCell) ;
	Highlighter.hideAll() ;
	Highlighter.startBlock = Highlighter.block[pos.y] ;
	Highlighter.startBlock.style.display = "block" ;
	var x = eXo.core.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) ;
	var y = eXo.core.Browser.findPosYInContainer(Highlighter.startCell, Highlighter.container) ;
	Highlighter.startBlock.style.top = y + "px" ;
	Highlighter.startBlock.style.left = x + "px" ;
	Highlighter.startBlock.style.width = Highlighter.dimension.x + "px" ;
	Highlighter.startBlock.style.height = Highlighter.dimension.y + "px" ;
	document.onmousemove = Highlighter.execute ;
	document.onmouseup = Highlighter.end ;
} ;

Highlighter.prototype.execute = function(evt) {
	var Highlighter = eXo.calendar.Highlighter ;
	var _e = window.event || evt ;	
	var sPos = Highlighter.getPos(Highlighter.startCell) ;
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
				startBlock.style.left = eXo.core.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) + "px" ;
				startBlock.style.width = (diff + 1)*Highlighter.dimension.x + "px" ;
				Highlighter.firstCell = Highlighter.startCell ;
				Highlighter.lastCell  = Highlighter.currentCell ;
			} else {
				startBlock.style.left = eXo.core.Browser.findPosXInContainer(Highlighter.startCell, Highlighter.container) + diff*Highlighter.dimension.x + "px" ;
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
			startBlock.style.left = startX + "px" ;
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
			endBlock.style.left  = eXo.core.Browser.findPosXInContainer(Highlighter.cell[0], Highlighter.container) + "px" ;
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

eXo.calendar.Highlighter = new Highlighter() ;