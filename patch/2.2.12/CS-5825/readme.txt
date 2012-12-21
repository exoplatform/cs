CS-5825: impossible to display the list of users mail in the reminder of the calender when there are big number of users

Problem description
* What is the problem to fix?
When trying to add reminder with large number of users (more than 1000 users), the button keep loading without displaying anything.

Fix description
* Problem analysis
The problem is the delay when we display the list of users with emails. What we did was to load all users, therefore, if the number of users becomes big, the delay grows. For 2000 users, the delay is 10 minutes or more, which is unacceptable.  

* How is the problem fixed?
What we did to overcome the problem is to load a fix number of users to display (in our case: 10 users) instead of loading all users. Each time the customer clicks on the "next page" button, 10 users will be queried and return to user-interface. 
For this, we need to override the template UIPageIterator and also re-implement the mechanism of querying users. 
New mechanism consisting of keeping track of index of users received from the database. For example: a database with 2000 users will be queried from 0 - 9, then store the 9 as index, clicking on "Next Page" button in UIPageIterator will query from 10 - 19, then we store 19.. and etc.
That way we load users fast and do not performance problem when numbers of users grow 

Tests to perform
* Reproduction test
To reproduce , we should have a server with 1000 or more users ( big number)
- log in
- Calender -> add event
- more details -> reminders
- Click on the icone of "+" add more.
Expected behaviour: the list of user's mail is shown directly
Result: with large number of users, the button keep loading without displaying anything.
