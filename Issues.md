# Issue 01:
When a path is moved, the modified date is changed due to the move. 
This means the next time the system has been run the path has a more recent date than it should and is moved 
    when it should not behaviourally.
Condition for solution: No storage used.
## Solution Thoughts: 
Need to preserve the lastModified timestamp from before when creating the new path. 
Research this
## Implemented Solution: 
Setting the lastModified and lastAccessed attributes of the moved path to the prior data.
This preserves the last modified time and if modified after the system runs will be updated to 
    ensure behaviour.

# Issue 02 (when implementing depth):
Access Denied when scanning folders.
## Solution Thoughts: 
Error, needs to use catch exception. Log the path without access and state it was skipped.
## Implemented Solution: 
Catch AccessDeniedException, make a log file ErrorWithPaths.txt, clears before each scan.

# Issue 03 (when implementing depth):
Path already exists when moving items.
## Solution Thoughts: 
Check if path exists then log and do not move that path. Required a Continue; command.
## Implemented Solution: 
Check if destination exists, if it does make a new dated log file and log the path,
    then continue the program so other paths are moved.

# Issue 04 (when implementing depth):
Paths did not keep whole path when moving, meant folders did not persist as they should have, 
    side effect of above issue.
## Solution Thoughts: 
Need to research path methods to see if any simplify getting the userPath, the dir to move to
    and the whole path after the main dir change.
## Implemented Solution: 
Created a method which uses relativize() to get the difference between the user's path
    and items path, then removes the old "main" directory, construct the destination path from the two parts.

# Issue 05:
When changed to scanning all folders it added desktop.ini which is not a directory.
This caused issues and I had to update to ensure "locked" and "logs" were not included, so that
    the logs were not included in the organisation.
## Solution Thoughts:
Exclude any files, only add directories in getPaths and in scanPaths include a check to ensure that 
    paths containing "locked", "logs" or ends with ".ini" are not added.
## Implemented Solution:
if statement including Files.isDirectory and if !name.contains locked or logs in getPaths().
if statement including f.toString().toLowerCase().contains locked or logs and endsWith .ini
    with a continues; command to skip paths with those conditions.

# Issue 06:
ScanPaths test failed did not exclude .ini files.
## Solution Thoughts:
Check the if statement in scanPaths to ensure it is checking for .ini files and not just .ini, 
    check that the operators are correct as well.
## Implemented Solution:
ScanPaths updated to use .getFileName and .tolowerCase() to ensure .ini files are excluded.
Or operators used.
## Separate note:
createDestinationPath updated to include a safeguard check for destination paths with just one part,
for the relative path as using substring(1, pathLength) could cause issues if it is (1,1).