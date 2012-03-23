CS-5672: Create event problem with monthly repeat
What is the problem to fix?
- This can reproduce only when server timezone difference with client timezone.
- Create all day event with monthly repeat.
- The display will be go to a next one day.

Problem analysis
- There are some countries which time zone is in daylight saving time (DST). In DST, the day is added one hour, that means the date is started and finished one hour later than normal day.

How is the problem fixed?
- To check "From Date" and "To Date" in all calendar events. If they are in DST, they will be reduced on hour.
