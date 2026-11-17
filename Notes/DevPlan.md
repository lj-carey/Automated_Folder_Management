# V1
Moves folders and files, logs any movements.

Uses rules: <14 Main, 14 to 29 Archived, >31 DeleteCheck.

Uses Main, Archived, DeleteCheck, (in FileInfo), checks the children of these directories,
then move those depending on rule.

Does not automatically delete.

# V2
Rule class to allow for rules to be added and changed easily. <14 Main, 14 to 30 Archived, 31 to 999 DeleteCheck. 
ScanItems includes depth to check through and move paths. (Completed)
Dry run version - dropped due to the lack of a frontend and user-input.
    if made for others or if a frontend was planned (is not) then would have been implemented.

# V3
Heartbeat log - application or/and progress.

# V4
Deploy (new - make a file explaining this experience).
