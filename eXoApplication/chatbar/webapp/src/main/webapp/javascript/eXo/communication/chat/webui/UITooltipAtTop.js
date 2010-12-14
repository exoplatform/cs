

function UITooltip() {
}


UITooltip.prototype.showToolTip = function(e,text){
	if(document.all)e = event;
	var DOMUtil = eXo.core.DOMUtil;
	var tooltip = DOMUtil.findFirstDescendantByClass(e, 'div', 'UITooltipAtTop');
	if (!tooltip) return;
	var tooltipContent = DOMUtil.findFirstDescendantByClass(tooltip, 'span', 'UITooltipAtTop_Content');
	tooltipContent.innerHTML = text;
	tooltip.style.display = 'block';
	var st = Math.max(document.body.scrollTop,document.documentElement.scrollTop);
	if(navigator.userAgent.toLowerCase().indexOf('safari')>=0)st=0; 
	var leftPos = e.clientX - 100;
	if(leftPos<0)leftPos = 0;
	obj.style.left = leftPos + 'px';
	obj.style.top = e.clientY - tooltip.offsetHeight -1 + st + 'px';
}	

UITooltip.prototype.showToolTipOfElement = function(e,text){
	var DOMUtil = eXo.core.DOMUtil;
	var tooltip = DOMUtil.findFirstDescendantByClass(e, 'div', 'UITooltipAtTop');
	if (!tooltip) return;
	var tooltipContent = DOMUtil.findFirstDescendantByClass(tooltip, 'span', 'UITooltipAtTop_Content');
	tooltipContent.innerHTML = text;
	tooltip.style.display = 'block';
	tooltip.style.left = -100 + 'px';
	tooltip.style.top = - tooltip.offsetHeight + 'px';
}	

UITooltip.prototype.hideMe = function (e) {
	e.style.display= 'none';
}

UITooltip.prototype.hideToolTip = function(e)
{
	var DOMUtil = eXo.core.DOMUtil;
	var tooltip = DOMUtil.findFirstDescendantByClass(e, 'div', 'UITooltipAtTop');
	tooltip.style.display = 'none';
	
}
if (!eXo.cs) eXo.cs = {};
eXo.cs.Tooltip = new UITooltip();
