Summary

    * Status: Chat Portlet doesn't work
    * CCP Issue: CCP-1013, Product Jira Issue: CS-5154.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Start PLF-3.0.4 server.
    * Start the chat server.
    * Import portlets.
    * Add new page which contains Chat portlet.

=>Cannot add room,add contact...
Fix description

How is the problem fixed?

    * Because name space of js class is wrong, so it's fixed as: modify
      ?
      var uiMainChatWindow = eXo.communication.chatbar.webui.UIMainChatWindow;

      to
      ?
      var uiMainChatWindow = eXo.communication.chat.webui.UIMainChatWindow;

Patch files: CS-5154.patch 

Tests to perform

Reproduction test

    * cf.above

Tests performed at DevLevel

    * cf.above

Tests performed at QA/Support Level

    *  

Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

