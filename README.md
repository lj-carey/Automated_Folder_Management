# Automated Folder Management

## Overview

A Java application that uses configurable rules to manage files and folders by automatically moving content into predefined locations, creating a more consistent and maintainable directory structure.

## Project Status

This project is currently under development. The current development stage and planned features are documented in `devPlan`.

## Why This Project Matters

File management can involve repetitive tasks that become difficult to maintain as the number of files and directories grows. Automating these tasks through a rule-based system provides a way to make file organisation more consistent and reduce manual effort. The rule-based approach allows varying levels of control over how content is organised, while providing a foundation for future customisation and extension.

This project focuses on backend development with no plans of a frontend.

This project explores rule-based automation, file system operations, and the design of a system that can apply configurable logic to file management.

## Features

* Rule-based file and folder management.
* Automated file organisation using defined conditions.
* A Java implementation of file management logic.
* A foundation for extending the system with additional rules and functionality.

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
├── src/
│   └── main/
│       └── java/
│           ├── Classes/
│           │   └── FileInfo.java
│           └── Main.java
├── .gitignore
└── README.md
```

## Key Locations

* **Java source code:** `src/main/java/` — contains the application's Java implementation.
* **Notes:** `Notes/` — contains project notes and supporting documentation.
* **Development plan:** `devPlan` — documents the planned development work, where applicable.
* **Project documentation:** `README.md` — provides an overview of the application and instructions for using it.

### Current Rule Implementation

The current implementation evaluates folder activity using:

- Last modified date
- Last accessed date

The most recent activity timestamp is compared against configurable thresholds:

- 15 days
- 31 days

Actions are then performed depending on whether the folder falls within or beyond these thresholds.

Future versions will replace these hard-coded rules with configurable rule definitions.

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

## Future Development

Planned improvements and development priorities are maintained in the project's development plan. This section can be updated as features are implemented and the project progresses.

