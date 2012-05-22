CS-5769: Calendar performance improvements with TQA

What is the problem to fix?
	- we have this result of TQA test 
	https://docs.google.com/a/exoplatform.com/spreadsheet/ccc?key=0AjQGex7ypPwtdFk4c2syVDR5cmN1ZU14ZU5COUdLcVE#gid=2
	and the requirement is
	- The target for Calendar is to be lower than 1 second for the same test case.

Problem analysis:
	- Loading node properties is slow now, need to improve.
	- Optimize code in both UI and service side .

How is the problem fixed?
	* Loading node properties improvement
 		We replaced hasProperty usage by getProperties + namePattern when we get data from jcr. It helped to reduced the performance to 37%.
	* Optimize code in both UI and service side
		There are many methods are called twice in the UI as getColor, Calendar.intance() ... We created a local variable for that function and re-use it.

Documentation (User/Admin/Dev/Ref) changes:
	No

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?
	Function or ClassName change: No
	Data (template, node type) migration/upgrade: No 
Is there a performance risk/cost? N/A

Validation (PM/Support/QA)
PM Comment
	PM validated.
Support Comment
	Support validated.
QA Feedbacks
...
