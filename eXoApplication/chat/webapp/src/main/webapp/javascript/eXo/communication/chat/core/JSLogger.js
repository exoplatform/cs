/**
 * @author Uoc Nguyen
 *         email: uoc.nguyen@exoplatform.com
 *
 * @description jsconsole is created for write log to help developer to debug js.
 * To use this object you must set jsconsole.debugLevel = 0->6 which number is level of log will be print out.
 * debugLevel number will be used for enable the functions assigned to it.
 * Default debugLevel is 0 (meant disabled).
 */
function jsconsole() {
  this.debugLevel = 0;
  this.console = window.console;
  if (eXo.core.Browser.isIE6() ||
      (!this.console ||
      !this.console.log ||
      !this.console.info ||
      !this.console.warn ||
      !this.console.error ||
      !this.console.debug ||
      !this.console.dir)) {
    this.console = {
      log : function(msg) {
      },

      info : function(msg) {
      },

      warn : function(msg) {
      },

      error : function(msg) {
      },

      dir : function(obj) {
      },

      debug : function(msg, obj) {
      }
    };
  }
}

jsconsole.prototype = {
  log : function(msg) {
    if (this.debugLevel >= 1) this.console.log(msg);
  },

  info : function(msg) {
    if (this.debugLevel >= 2) this.console.info(msg);
  },

  warn : function(msg) {
    if (this.debugLevel >= 3) this.console.warn(msg);
  },

  error : function(msg) {
    if (this.debugLevel >= 4) this.console.error(msg);
  },

  dir : function(obj) {
    if (this.debugLevel >= 5) this.console.dir(obj);
  },

  debug : function(msg, obj) {
    if (this.debugLevel >= 6) this.console.debug(msg, obj);
  }
};

jsconsole.prototype.init = function() {
};

eXo.communication.chat.core.jsconsole = new jsconsole();
window.jsconsole = eXo.communication.chat.core.jsconsole;
