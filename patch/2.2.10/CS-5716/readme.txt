CS-5716: [Calendar] UI Error: Wrong postion of "Close search" button

Problem description
What is the problem to fix?
	"Close" search button is displayed on the bottom of page.

Fix description
Problem analysis
	Current css for search button is in wrong place (at the bottom of the page).

How is the problem fixed?
	- Add new css for close search button.
	- Use javascript to assign above css to search button.

Tests to perform
Reproduction test
	Login
	Go to Calendar application
	Create event named: "test"
	Fill "test" on search text box and press Enter
	Show all matching result => OK
	Problem: "Close" search button is displayed on the bottom of page.

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
* No

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects? 
	No

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
	No
Is there a performance risk/cost?
	No

Validation (PM/Support/QA)
PM Comment
	PM validated
Support Comment
	Support validated
QA Feedbacks
...

