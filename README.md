# Automated Folder Management

## Overview

A Java application that uses configurable rules to manage files and folders by automatically moving content into 
predefined locations, creating a more consistent and maintainable directory structure. 
This application organises files and folders without deleting any items.

## Project Status

This project is currently under development. The current development stage and planned features are documented in `devPlan`.

## Why This Project Matters

File management can involve repetitive tasks that become difficult to maintain as the number of files and directories grows. 
Automating these tasks through a rule-based system provides a way to make file organisation more consistent and reduce manual effort. 
The rule-based approach allows varying levels of control over how content is organised, while providing a foundation for future customisation and extension.

This project focuses on backend development with no plans of a frontend.

This project explores rule-based automation, file system operations, and the design of a system that can apply configurable logic to file management.

### Limitation Note
This application relies on filesystem-provided timestamps to determine file and folder activity. 
These timestamps are not necessarily a reliable indicator of actual user activity.
The use of both lastAccessTime and lastModifiedTime was used to reduce this limitation by considering two indicators of activity,
but does not eliminate this limitation completely.

Empty folders are intentionally retained to maintain the application's non-destructive approach to file management.


## Features

* Rule-based file and folder management.
* Automated file organisation using defined conditions.
* A Java implementation of file management logic.
* Logging to ensure any movement, skipped folders or exceptions due to paths already existing are noted.
* Does not automatically delete items, reducing the risk of accidental data loss.

## Tech Stack

- Java
  - Java NIO (File and Directory operations)
- Git
- GitHub

## Project Structure

```text
Automated_Folder_Management/
├── Notes/
│   └── DevPlan.md
│   └── Issues.md
├── src/
│   └── main/
│       └── java/
│           ├── Classes/
│           │   └── FileInfo.java
│           │   └── Rule.java
│           └── Main.java
├── .gitignore
└── README.md
```

## Key Locations

* **Java source code:** `src/main/java/` — contains the application's Java implementation.
* **Notes:** `Notes/` — contains project notes and supporting documentation.
* **Development plan:** `DevPlan` — documents the planned development work for each version.
* **Project documentation:** `README.md` — provides an overview of the application and instructions for using it.

### Current Rule Implementation

The current implementation evaluates folder activity using:

- Last modified date
- Last accessed date

The most recent activity timestamp is compared against configurable thresholds, which are created using the Rule class.
Actions are then performed depending on whether the folder falls within or beyond these thresholds.


## Requirements

* A compatible Java Development Kit (JDK).
* Git, if cloning the repository.
* A Java-compatible IDE or editor, such as IntelliJ IDEA, Eclipse, or Visual Studio Code.

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/lj-carey/Automated_Folder_Management.git
cd Automated_Folder_Management
```

### Open the Project

Open the repository in a Java-compatible IDE and inspect the source files under `src/main/java/`.

Identify the application's entry point and follow the project's existing build and execution configuration.

*Exact build and run commands will be added once the project's build configuration and entry point have been confirmed.*


