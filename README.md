# Automated Folder Management

## Overview
A Java-based application that automatically organises files and folders using configurable activity-based rules.

The application scans directory structures, evaluates filesystem timestamps, and moves items into predefined locations according to their activity. It also provides operational logging, error reporting and a heartbeat monitoring mechanism. 
It operates directly on the filesystem using Java NIO and maintains the original directory structure when moving items.

It is primarily a backend development project, with no graphical frontend included.

The application is non-destructive: it moves items but does not automatically delete them.

## Project Status
**Complete**
This is the first and final stable release of Automated Folder Management. The project is considered complete, and no further development or updates are currently planned.


## Project Objectives
The project aims to:

- Automate repetitive file and folder organisation.
- Maintain a consistent directory structure.
- Use activity-based rules to determine item destinations.
- Preserve filesystem timestamps when moving items.
- Avoid automatically deleting user data.
- Provide logging to track application activity and identify errors.
- Support recursive scanning of directory structures.
- Provide visibility into application execution through heartbeat monitoring.

## Why This Project Matters
File management can involve repetitive tasks that become difficult to maintain as the number of files and directories grows. 
Automating these tasks through a rule-based system provides a way to make file organisation more consistent and reduce manual effort. 
The rule-based approach allows varying levels of control over how content is organised, while providing a foundation for future customisation and extension.
This project explores rule-based automation, file system operations, and the design of a system that can apply configurable logic to file management.

This project was originally created as an opportunity to develop a Java application without a graphical frontend, while automating folder management through file movement.
It also served as a learning project, providing experience with Java, Java NIO, heartbeat processes and application deployment.

### Limitation Note
This application relies on filesystem-provided timestamps to determine file and folder activity. 
These timestamps are not necessarily a reliable indicator of actual user activity.
The use of both lastAccessTime and lastModifiedTime was used to reduce this limitation by considering two indicators of activity,
but does not eliminate this limitation completely.

Empty folders are intentionally retained to maintain the application's non-destructive approach to file management.


## Features
* Rule-based organisation - Assigns destinations according to inactivity thresholds.
* Recursive scanning - Searches directory structures up to a configurable depth.
* File and folder support - Processes both regular files and directories.
* Activity detection - Uses last-accessed and last-modified timestamps.
* Timestamp preservation - Restores timestamps following a move.
* Directory preservation - Retains relative directory structures when moving items.
* Destination checking - Prevents overwriting existing destinations.
* Error handling - Records inaccessible paths and skipped items.
* Heartbeat monitoring - Periodically records that the application is running.
* Overview logging - Records scan totals and file/directory counts.
* Non-destructive operation - Does not automatically delete files or folders.

## How It Works

The application follows a sequential execution process.

1. Identifies the user's Documents directory.
2. Initialises heartbeat monitoring.
3. Identifies directories to scan.
4. Recursively scans their contents.
5. Collects filesystem information for discovered items.
6. Evaluates each item against the configured rules.
7. Constructs the appropriate destination path.
8. Checks whether the destination already exists.
9. Creates missing parent directories and moves eligible items.
10. Restores the original filesystem timestamps.
11. Records movements and operational information as different .log files.
12. Generates an overview log to show what root documents were scanned and how many paths.
13. Stops heartbeat monitoring.

The main execution logic is currently managed by `Main.java`.

## Tech Stack
- Java
  - Java NIO (File and Directory operations)
- Git
- GitHub

## Project Structure

```text
Automated_Folder_Management/
├── Notes/
│   ├── DevPlan.md
│   └── Issues.md
├── src/
│   └── main/
│       └── java/
│           ├── Classes/
│           │   ├── FileInfo.java
│           │   ├── Heartbeat.java
│           │   ├── Logs.java
│           │   ├── PathManager.java
│           │   └── Rule.java
│           └── Main.java
│   └── test/
│       └── java/
│           └── Classes/
│           │   ├── FileInfoTest.java
│           │   ├── LogsTest.java
│           │   ├── PathManagerTest.java
│           │   └── RuleTest.java
│           └── MainTest.java
├── pom.xml
├── .gitignore
└── README.md
```

## Key Locations

- **Java source code:** `src/main/java/` — contains the application's Java implementation.
- **Classes:**
  - FileInfo to read the attributes of the paths and set lastActivity.
  - Heartbeat to periodically record application activity, providing visibility into runtime execution.
  - Rule to create the rules for the application and dictates movement of paths.
  - PathManager to manage the scanning of paths and the movement of files and folders.
  - Logs to manage the logging of the application.
- **Testing:** `test/` — contains JUnit5 testing for the following files/classes:
  - FileInfoTest
  - LogsTest
  - PathManagerTest
  - RuleTest
  - MainTest
  These tests use temporary directories and do not operate on the user's actual Documents directory.
- **Notes:** `Notes/` — contains project notes and supporting documentation.
- **Development plan:** `DevPlan` — documents the planned development work for each version.
- **Project documentation:** `README.md` — provides an overview of the application and instructions for using it.

## Rule Engine

The application uses the `Rule` class to define organisational rules.

Each rule contains:

- A rule name.
- A destination parent directory.
- A minimum inactivity threshold.
- A maximum inactivity threshold.
  
The current implementation evaluates folder activity using:
- Last modified date
- Last accessed date

The most recent activity timestamp is compared against configurable thresholds, which are created using the `Rule` class.

### Current Rule Implementation
The current rules defined in `Main.java` are:
| Rule | Minimum Days | Maximum Days | Destination |
|---|---:|---:|---|
| Current | 0 | 13 | `01_Main` |
| Archived | 14 | 30 | `02_Archived` |
| Older Items | 31 | 999 | `03_DeleteCheck` |

#### Folder	Purpose
| Folder | Purpose |
|---|---|
| `01_Main` | Contains recently active files and folders. |
| `02_Archived` | Contains items that have been inactive for longer. |
| `03_DeleteCheck` | Contains older items that may be suitable for manual review. |

### Rule Validation
Rules are registered through the `addRule()` method found in `Main.java`.

Before a rule is added, the application checks:
- Whether its inactivity range overlaps an existing rule.
- Whether its destination directory is already assigned to another rule.

Overlapping rules and duplicate destinations result in an `IllegalArgumentException`.
This provides a basic validation mechanism to prevent conflicting organisational rules.

### Changing the Organisation Rules

The organisation rules can be modified in `Main.java`.

If you wish to change the inactivity periods or the destination folders, locate the relevant rules in `Main.java` and adjust them as required.

The application must be rebuilt after making changes for them to take effect in the executable JAR.


## Heartbeat Monitoring

Heartbeat monitoring is implemented in `Heartbeat.java`.

The heartbeat uses a `ScheduledExecutorService` to execute a background logging task at a fixed interval.

The current configuration uses a five-second interval.

### Heartbeat Messages

The heartbeat log records:

- Application startup.
- Periodic confirmation that the application is running.
- Application completion.

The heartbeat is started when the main application begins and stopped in the `finally` block when execution finishes.

This provides basic visibility into whether the application is still executing, particularly during larger directory scans.

Heartbeat monitoring is only active while the application is running.

## Logging

The application uses filesystem-based logging to record its operations.

All logs are stored within:

```text
Documents/
└── 00_Logs/
```

### Log Files

| Log | Purpose |
|---|---|
| `Heartbeat.log` | Records application startup, periodic heartbeat messages and completion. |
| `ErrorWithPath.log` | Records inaccessible paths and scanning errors. |
| `YYYY MM DD FolderManager.log` | Records successful item movements. |
| `YYYY MM DD PathExists.log` | Records destination conflicts that prevent movement. |
| `YYYY MM DD Overview.log` | Records directory scanning information and item counts. |

### Overview Logging

The overview log records:
- Timestamp of the overview.
- Number of root directories scanned.
- Names of the root directories.
- Total number of discovered items.
- Number of directories.
- Number of regular files.

The overview is generated after the movement process. It also includes information about directories that were encountered but skipped during scanning, such as excluded directories or directories where permission was denied. 

#### Example Overview
Overview @ 2026-11-20 22:28:16

Root paths: 6

path1

path2

path3

...

(Root paths includes skipped paths)

Total items: 41

Folders: 22  |  Files: 19

The overview provides a basic summary of the directories
encountered and the number of files and folders identified
during the scan.

## Requirements

The project uses Java and Java NIO.

Requirements:

- A compatible Java Development Kit (JDK)
- Java SDK: `ms-21`.
- A Java-compatible IDE or editor.
- Git, if cloning the repository.
- Access to the Documents directory being processed.

## Running the Application

### Running from .jar
Download the executable JAR attached to this release.

Java 21 or newer must be installed on your computer.

1. Download `automated-folder-management-1.0.0.jar` from the GitHub release.
2. Save the JAR somewhere convenient on your computer.
3. Open Command Prompt or a terminal.
4. Navigate to the folder containing the downloaded JAR, or open a terminal in that folder.
5. Run the following command:

```bash
java -jar automated-folder-management-1.0.0.jar
```

The application will run automatically and organise files and folders within the user's Documents directory according to the defined rules.

No IDE or Maven installation is required to run the downloaded JAR.

### Building from Source

If you wish to modify the source code or organisation rules, you can build a new executable JAR using Maven.

Requirements:
- Java Development Kit (JDK) 21 or newer.
- Maven installed and configured.

From the root directory of the project, run:

```bash
mvn clean verify
```

Maven will compile the application, run the tests and package the executable JAR.

The resulting JAR can be found in the `target` directory.

Run the newly built application using:

```bash
java -jar target/automated-folder-management-1.0.0.jar
```
