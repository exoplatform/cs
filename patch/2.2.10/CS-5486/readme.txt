CS-5486: [Mail] Can't close print form, null pointer exception is shown on terminal

What is the problem to fix?
Can't close print form of an email, null pointer exception is shown on terminal.

Problem analysis
After call the method UIMailPortlet.cancelAction(), the popup will be remove from it's parent, so don't need to call method addUIComponentToUpdateByAjax

How is the problem fixed?
Remove the code line that call method addUIComponentToUpdateByAjax

Reproduction test
- Open Mail application
- Select a mail from Inbox and click print
- A print form is displayed
- Click on "Close" button

Documentation (User/Admin/Dev/Ref) changes:
No

Configuration changes:
NO
Will previous configuration continue to work?
Yes

Can this bug fix have any side effects on current client projects?
- Function or ClassName change: No
- Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?
No
