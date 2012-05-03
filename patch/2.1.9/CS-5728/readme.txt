Summary
Problem of end date when importing an ics file in eXo Calendar 
CCP Issue:  CCP-1260 
Product Jira Issue: CS-5728.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?
Steps to reproduce:
- Open eXo Calendar
- Open Import Calendar and choose the attached ObmCalendar.ics
- Click on Save

A message is shown : "The file type is not correct"
The same file was imported in Google Calendar successfully.
In this ics file there is an event without an end date which is not an error as said in RFC2445 :

The "DTSTART" property for a "VEVENT" specifies the inclusive start of the event. For recurring events, it also specifies the very first instance in the recurrence set. The "DTEND" property for a "VEVENT" calendar component specifies the non-inclusive end of the event. For cases where a "VEVENT" calendar component specifies a "DTSTART" property with a DATE data type but no "DTEND" property, the events non-inclusive end is the end of the calendar date specified by the "DTSTART" property. For cases where a "VEVENT" calendar component specifies a "DTSTART" property with a DATE-TIME data type but no "DTEND" property, the event ends on the same calendar date and time of day specified by the "DTSTART" property.

This event was isolated in the attached ics file ObmCalendar_isolated.ics

Fix description
Problem analysis

In case "DTEND" property doesn't existed in imported data, a NullPointerException occurs and breaks the importing process:
dateTime.setTime(event.getToDateTime()) ;
toDate = dateTime.get(java.util.Calendar.DAY_OF_YEAR) ;
=> dateTime is null
How is the problem fixed?

For cases where a "VEVENT" calendar component specifies a "DTSTART" property with a DATE-TIME data type but no "DTEND" property, the event ends on the same calendar date and time of day specified by the "DTSTART" property.
Check whether there is no "DTEND" property in imported data, set "toDate" field  the value of "fromDate" of this event:
dateTime.setTime(event.getFromDateTime());
...
if (event.getToDateTime() != null) {
      dateTime.setTime(event.getToDateTime());
    }
...
publicEvent.setProperty(Utils.EXO_TO_DATE_TIME, dateTime);
Tests to perform
Reproduction test

cf. above
Tests performed at DevLevel

cf. above
Tests performed at Support Level

cf. above
Tests performed at QA
*

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

None
Changes in Selenium scripts 

None
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

None
Configuration changes
Configuration changes:

None
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: None
Data (template, node type) migration/upgrade: None
Is there a performance risk/cost?

No
Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

N/A
