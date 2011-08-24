Summary

    * Status: CLONE - IE7 Calendar - view Week - event card located in wrong time range
    * CCP Issue: N/A, Product Jira Issue: CS-5074.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Steps to reproduce: (use IE)
1. Login /portal/private/intranet
2. Click Calendar icon (at the bottom of the page)
3. Create an event which starts on 9:00 and ends on 11:00.
4. View the event in Day view: the event is located in correct time range.
5. View the event in Week view: the time range is INCORRECT (starts earlier than 9:00 and ends earlier than 11:00).

Additional information: I have reproduced this bug several times using different languages / time zones. If you want me to give a certain setting, let's use Vietnamese and GMT+7 Asia/Ho_Chi_Minh.
Also I have an alternative way to show the bug: Create some events (step 2 & 3) in Firefox and view them in IE.
Fix description

How is the problem fixed?

    * Repair incorrect css of UICalendarView.

Patch files:CS-5074.patch

Tests to perform

Reproduction test
*

Tests performed at DevLevel
* Reproduction test

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

    * No Function or ClassName change

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

