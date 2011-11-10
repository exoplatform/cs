Summary

    * Status: Cannot open ics file in the invitation's email with MS Office Outlook 2003
    * CCP Issue: CCP-1080, Product Jira Issue: CS-5265.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Steps to reproduce:
1. Prepare data in PLF

    * Login as root
    * Go to Mail Portlet
    * Add new MS-Exchange Account (Detail information to add here), use john account in this case.
    * Go to Calendar Portlet
    * Create new event
    * Click to More Detail button
    * In the table Reminder, delete the current email of root(root@localhost.com), and add new john's email (john@testlab.exoplatform.vn) to reminder
    * In the table Participants, delete the current email of root(root@localhost.com), and add new john's email (john@testlab.exoplatform.vn)as participants
    * Click to Save button.
    * The invitation's emails will be sent to john@testlab.exoplatform.vn with the icalendar.ics file attached.
      @Note: password for MS-Exchange accounts: exoadmin

2. Connect MS-Exchange account to Microsoft Office Outlook

    * Open Microsoft Office Outlook 2003
    * Create new account with: Microsoft Exchange server: msexchange.testlab.exoplatform.vn, email: john@testlab.exoplatform.vn and password: exoadmin
    * Received the invitation's email with john's account in Microsoft Office Outlook.
    * Open the invitation's email with the attached icalendar.ics file
    * Download the icalendar.ics file to local
    * Open the icalendar.ics file in local machine --> NOK: Cannot open the file

Fix description

How is the problem fixed?

    * For importing, MSOutlook2k3 apparently requires VEVENTS to have UID, DTSTAMP, and METHOD properties. (citation/platform/date tested info needed). If any of the three is not present, returns this message:
      ?
      This error can appear if you have attempted to save a recurring Lunar appointment in iCalendar format.
      To avoid this error, set the appointment option to Gregorian instead of Lunar.

      Because of the missing METHOD property, so that it needs to add it into calendar ics as:
      ?
      calendar.getProperties().add(Method.REQUEST);

Patch files:CS-5365.patch

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
* PL review: Patch approved

Support Comment
* Support review: Patch validated

QA Feedbacks
*

