Summary

    * Status: Event category listbox must be filled by configuration
    * CCP Issue: CCP-886, Product Jira Issue: CS-4914.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Problem: Cannot override the default values of event Category listbox.
      This list box is filled with CalenderServiceImpl class.
      After configuring default values in the calendar-service-configuration.xml file, these new configures is not updated
    * Expected: It must be configurable under calendar-service-configuration.xml file

Fix description

How is the problem fixed?

    * Read the configuration from the calendar-service-configuration.xml file. Create default calendar category, default calendar, default event categories by this configuration.
    * Add more comment in the calendar-service-configuration.xml file to explain how to config the default calendar category, default calendar, default event categories.

Patch files:CS-4914.patch

Tests to perform

Reproduction test

    * Edit default values of event categories by configuration these parameters in the file cs-extension.war/WEB-INF/cs-extension/cs/calendar/calendar-service-configuration.xml
      By default,
      ?
      <value-param>
                <name>defaultEventCategories</name>
                <value>Meeting,Calls,Clients,Holiday,Anniversary</value>
      </value-param>

      Configure new values for this configuration (ex: Add new value, delete exited value ...)
      example:
      ?
      <value-param>
                <name>defaultEventCategories</name>
                <value>Meeting,Calls,Clients</value>
      </value-param>

Reproduce:

Case 1:

    * Go to Calendar
    * Click to the Categories drop-down list --> Cannot view the new configuration of event category

Case 2:

    * Go to Calendar
    * Click to Add Event button
    * Add/Edit event form displays
    * Click the the Event category drop-down list --> Cannot view the new configuration of event category

Case 3:

    * Go to Calendar
    * Click to the Add task button
    * Add/Edit tasks form displays
    * Click to the Task Category drop down list --> Cannot view the new configuration of event category

Case 4:

    * Go to Calendar
    * Click to the Advanced Search button
    * Advanced Search form displays
    * Click to the Category drop down list --> Cannot view the new configuration of event category

Case 5:

    * Go to Mail
    * Click to New Event button
    * Add new event form displays
    * Click to Event Category drop down list --> Cannot view the new configuration of event category

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No
Configuration changes

Configuration changes:
* Yes. Add more comment to explain how to config.

Will previous configuration continue to work?
* yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

