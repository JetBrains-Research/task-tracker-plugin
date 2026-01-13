[![JetBrains Research](https://jb.gg/badges/research.svg)](https://confluence.jetbrains.com/display/ALL/JetBrains+on+GitHub)

Repository archived by abarilo as no longer maintained

# TaskTracker

**TaskTracker** is an IntelliJ-based IDE plugin for tracking code changes while solving programming problems.
The plugin is built by the [ML4SE lab @ JetBrains Research](https://research.jetbrains.org/groups/ml_methods) 
to facilitate collection of data for research projects.

### Code changes tracking

TaskTracker tracks changes made in code during problem solving.
We use this data to analyze programming patterns of students and collect diverse sets of solutions for programming problems. 
For example, we are building an automated hint generation system based on this data.

### Problem solving

On startup, TaskTracker asks the user to specify their age and programming experience.
The UI of TaskTracker enables the user to choose a problem to solve and see correct examples of desired behavior. 
All data about problems is received from the [TaskTracker server](https://github.com/JetBrains-Research/task-tracker-server) 
when the plugin starts.  

Currently TaskTracker is available in English and Russian.

<img src="https://github.com/JetBrains-Research/task-tracker-plugin/blob/master/readme-img/codetracker.gif" width="250">

## Privacy

Disclaimer for participants in our research only. By installing the TaskTracker plugin using our distribution file, you agree **to send us changes in code** that you make during problem solving.

The TaskTracker plugin tracks changes **only** in the documents it **creates and opens automatically** to solve the tasks. 
The data is sent to our **privately hosted instance of the [TaskTracker server](https://github.com/JetBrains-Research/task-tracker-server)**. 
TaskTracker only sends the data when you **click** the **Submit** button. All data is sent **anonymously**, and only includes filenames, source code, and changes. For more information, see our [Privacy policy](https://github.com/JetBrains-Research/task-tracker-plugin/wiki/Privacy-policy).

## Installation

Just clone the repo by `git clone https://github.com/JetBrains-Research/task-tracker-plugin` and run `./gradlew build shadowJar` to build a .zip distribution of the plugin. 
The .zip is located in `build/distributions/tasktracker-1.0-SNAPSHOT.zip`. Then __install the plugin from disk__ into an IntelliJ-based IDE of your choice.
For instructions, see [how to install plugins in IntelliJ IDEs](https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk) and [how to install and uninstall the TaskTracker plugin](https://github.com/JetBrains-Research/task-tracker-plugin/wiki) (the guide is available in English and Russian).

## Releases

To learn more about TaskTracker releases, see the [list](https://github.com/JetBrains-Research/task-tracker-plugin/releases).

