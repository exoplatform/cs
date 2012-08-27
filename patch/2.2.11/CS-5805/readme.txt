CS-5805: String hardcoded in English calendar invitations

Problem description
* What is the problem to fix?
Some words are hard coded in English calendar initations:
- Would you like to attend?
- Would you like to see more details?
- Not sure
- Import to your eXo Calendar
- Jump to eXo Calendar
- or
- Yes
- No

Fix description
* Problem analysis
Some words are hard coded in function getBodyMail in eXoApplication/calendar/webapp/src/main/java/org/exoplatform/calendar/webui/popup/UIEventForm.java

* How is the problem fixed?
Add key and translations in CalendarPortlet in eXoApplication/calendar/webapp/src/main/webapp/WEB-INF/classes/locale/portlet/calendar
In function getBodyMail, use function getLabel(String keyword) to get the key in properties files. 

Pull request: https://github.com/exoplatform/cs/pull/18 

Tests to perform
* Reproduction test
Prequesite: you must config CS mail server first, so eXo Calendar can send an email to you. (Read more)
Steps to reproduce on CS 2.2.10 standalone:
- Login as John 
- Click on John Anthony and set email from john@localhost to your email (eg: abc@exoplatform.com). 
- Go to Agenda
- Change language to French
- Add new event
- Fill in "test" in Objet field
- Click on button "Plus de détails"
- Click Sauver
- Click Sauver et Envoyer
- Check your email and see that email content is translated to french.

Tests performed at DevLevel
...
Tests performed at Support Level
...
Tests performed at QA
...

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
	No
Changes in Selenium scripts 
	No

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
	No
Configuration changes
Configuration changes:
	No
Will previous configuration continue to work?
	Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: None
Data (template, node type) migration/upgrade: No
Is there a performance risk/cost?
	No

Validation (PM/Support/QA)
PM Comment
	PM validated
Support Comment
	Support validated
QA Feedbacks
...
