Summary

	* Status: Chat - Problem when username contains capital characters
	* CCP Issue: CCP-975, Product Jira Issue: CS-4601
	* Complexity: N/A

The Proposal
Problem description

	* Error on chat service when username contains capital characters

Fix description

	- Encode username before login to Openfire
	- Decode username when Openfire authenticate that username with Organization service

Patch file: CS-4601.patch

Tests to perform

Reproduction test

	Register new user with username containing capital characters
	Case 1:
		- Login as new registered user -> Cannot use chat service.
	Case 2:
		- Login as john
		- Add this new registered user-> error

Tests performed at DevLevel
*

Tests performed at QA/Support Level
	Test cases
		- Add a user with capital name multiple time
		- Chat with a user with capital name
		- Display name of a user with upper case name
		- Chat history with a user with upper case name
		- Send file
		- Invite a user with capital name to a room only one time
		- Display in a chat room
		- Chatroom History

Documentation changes

Documentation changes:
	* No

Configuration changes

Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impact
Can this bug fix have any side effects on current client projects?
	* Function or ClassName change : No

Is there a performance risk/cost?
	* Low


Validation (PM/Support/QA)

PM Comment
	* Patch approved

Support Comment
	* Patch validated
QA Feedbacks
	*
