Summary

    * Status: Chat Room: Messages lost when changing navigation
    * CCP Issue: CCP-500, Product Jira Issue: CS-4411.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * To reproduce the problem you should follow these steps:
      1. Using the same chatRoom, John starts to send a message to Root
      2. Root changes to classic home page. In the same time John continues to send messages
      3. Messages sent by John when Root switches to another session are lost.
      4. These messages are found whenever Root checks the historical messages.
      The joined Video shows the problem (CS-4411)

Fix description

How is the problem fixed?
    * Save message to history. When the user switches page (application in page mode) the history is reloaded to show the messages.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:

Tests to perform

Reproduction test
    * Apply the patch, run web server, start chat-server
    * Online and invite other user to chat
    * Send some message and switch page (application), the message should still show after page loaded.

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No
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

    * PM review: patch validated

Support Comment

    * Proposed patch validated by Support Team

QA Feedbacks
*

