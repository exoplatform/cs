function Reminder() {
  
} ;

Reminder.prototype.init = function(eXoUser, eXoToken){
  eXo.core.Cometd.exoId = eXoUser;
  eXo.core.Cometd.exoToken = eXoToken;
  eXo.core.Cometd.subscribe('/eXo/Application/Calendar/messages', function(eventObj) {
		eXo.calendar.Reminder.alarm(eventObj) ;
  });
	if (!eXo.core.Cometd.isConnected()) {
     eXo.core.Cometd.init();
  }
} ;

Reminder.prototype.alarm = function(eventObj){
	var a = eXo.core.JSON.parse(eventObj);
} ;

eXo.calendar.Reminder = new Reminder() ;
