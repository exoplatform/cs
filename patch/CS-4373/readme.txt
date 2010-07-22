Summary

    * Status: Problem of refresh and unread message displaying
    * CCP Issue: CCP-420, Product Jira Issue : CS-4373
    * Complexity: HIGH
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?
*One problem occurs after applying CS-3606 patch. After refreshing the page, the number of unread messages is shown even if there is no new one.
To reproduce:
1- connect with root, add a chat room
2- from another side, connect with john, enter the newly added chat room
3-root send message to john
4- john views the message and reduces the chat room window (there is no unread message)
5- Refresh the page, the number of unreaded message is set to 1.
 
Fix description

How is the problem fixed ?

    * We add more information such as status manager system to manage all status of conversation windows, queuing unread message
    * The status will be stored, every action such as refresh browser, go and visit a page, application, the status of conversation windows will be kept.

Patch information:
Patches files:
CS-4373.patch


Tests to perform

Which test should have detected the issue ?

    * Log in, add contact, start conversation
    * Switch application or refresh browser, the conversation should be shown

Is a test missing in the TestCase file ?
* Yes, have to add

Added UnitTest ?
* No

Recommended Performance test?
* No


Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration ?
* No

Describe configuration changes:
* None

Will previous configuration continue to work?
* Yes


Risks and impacts

Is there a risk applying this bug fix ?
* No

Can this bug fix have an impact on current client projects ?
* Yes, this is customer feedback

Is there a performance risk/cost?
* No


Validation By PM & Support

PM Comment
*

Support Comment
*


QA Feedbacks

Performed Tests
*

