/**
 * @author Uoc Nguyen (uoc.nguyen@exoplatform.com)
 * @description This file do nothing, it made to keep user interactive with portal to keep portal session from timeout.
 *  When user login to chat it will download this file automatically by period time.
 *
 * TODO: Remove this file because it has no affect with portal session as old way do.
 */
function PortalSessionKeeper() {
}

eXo.communication.chat.core.PortalSessionKeeper = new PortalSessionKeeper();
