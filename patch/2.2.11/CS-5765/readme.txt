CS-5765: [WebOS] Bad display of portlets after "Restore Down/Maximize" in Desktop

Problem description
* What is the problem to fix?
When dragging a non-maximized portlet:
- The portlet is only unreleased from the mouse when its upper border reaches the tool bar
- The portlet disappears when its lower border reaches the lower border of screen. We have to logout/login to see again the portlet.
- We also have error "uiPageIdNode is null" in Firebug console (PLF-2961) when clicking on an application on the dockbar

Fix description
* Problem analysis
The bug is caused by javascript conflict between files UIWindow, UIDesktop of CS and WebOS.
* How is the problem fixed?
Delete all code lines related to UIDesktop and UIWindow in CS

Patch file: https://github.com/exoplatform/cs/pull/28

Tests to perform
* Reproduction test
- Connect to desktop
- Add an application to the desktop (e.g "New User account")
- Maximize the portlet
- Restore Down the portlet
Actual Result: Bad display with the portlet
When dragging a non-maximized portlet:
	+ The portlet is only unreleased from the mouse when its upper border reaches the tool bar
	+ The portlet disappears when its lower border reaches the lower border of screen. We have to logout/login to see again the portlet.
	+ We also have error "uiPageIdNode is null" in Firebug console (PLF-2961) when clicking on an application on the dockbar

Tests performed at DevLevel
...
Tests performed at Support Level
...
Tests performed at QA
...

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
...
Changes in Selenium scripts 
...

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:


Configuration changes
Configuration changes:
*

Will previous configuration continue to work?
*

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
...

Validation (PM/Support/QA)
PM Comment
	PM validated
Support Comment
...
QA Feedbacks
...
