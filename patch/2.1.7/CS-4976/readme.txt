Summary

    * Status: CS Displaying time of event in Calendar is not suitable with event created in Mail
    * CCP Issue: N/A, Product Jira Issue: CS-4976.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Go to Mail application
    * Click on 'New Event' --> show form to create new event
    * Setting time for this event like: 16:30 to 17:00
    * Go to Calendar application --> see that the event is displayed from 10:30 to 11:00

Fix description

How is the problem fixed?

    * Using calendar setting when formatting or parsing date value.


Patch files:CS-4976.patch
Tests to perform

Reproduction test
*

Tests performed at DevLevel
* Reproduction test

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * org.exoplatform.mail.webui.popup.UIEventForm

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

