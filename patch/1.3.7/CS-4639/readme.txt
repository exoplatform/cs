Summary

    * Status: [Mail, Contact]: Missing Portlet's ID
    * CCP Issue: N/A, Product Jira Issue: CS-4639.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Hard-coded portlet id.

Fix description

How is the problem fixed?

    * Replace by dynamical portlet Id. 
    * Pass this information to JavaScript code

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: CS-4639.patch

Tests to perform

Reproduction test
    * Case 1: Mail portlet
         1. Go to Mail portlet
         2. Create a new mail account: at the last step click Finish --> error popup is shown
         3. Error the same when delete added mail account
    * Case 2: Contact portlet
         1. Go to Contact portlet
         2. Add address book or contact => popup error is displayed
    * Case 4.1: Chat window and Mail portlet
        * Two users are chatting from the home page.
        * 1 user changes to Mail portlet. The chat window will disappear.
    * Case 4.2: Chat bar and Mail portlet
        * John and root send messages to each other from the home page.
        * 1 user minimizes the chat window.
          Change current page to Mail page. The chat bar will appear incorrectly: loss of minimized chat window and the user is offline.

Tests performed at DevLevel
* Run stand alone version of cs

* Login by any user (root, marry, john, demo)

* Go to contact application --> have no any js message pop-up : OK 

* Go to mail application 

* Add new account by wizard form 

* Click finish as step 5 --> account create  successfully and no js message pop-up : OK

* ChatBar has no problem with UI : OK

Tests performed at QA/Support Level
* No
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

    * Function or ClassName change : None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* 

Support Comment
* 

QA Feedbacks
*

