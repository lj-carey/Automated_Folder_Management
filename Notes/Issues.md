Issue 01: 
When a path is moved, the modified date is changed due to the move. 
This means the next time the system is ran the path has a more recent date than it should and is moved 
when it should not behaviourally.
Condition for solution: No storage used.
Solution Thoughts: Need to preserve the lastmodified timestamp from before when creating the new path. 
Research this
Implemented Solution: Setting the lastModified and lastAccessed attributes of the moved path to the prior data.
This preserves the last modified time and if modified after the system runs will be updated to ensure behaviour.

