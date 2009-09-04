/**
 * @author Uoc Nguyen
 *
 */

function SimpleTaskManager() {
}
 
SimpleTaskManager.prototype.init = function() {
  this.taskStack = new Array();
};

SimpleTaskManager.prototype.destroy = function() {
  
};

function TaskObject(name) {
  this.taskName = name;
}

eXo.communication.chatbar.core.SimpleTaskManager = new SimpleTaskManager();
