# gong-notifier

gong-notifier is a GoCD plugin that aims to give pipeline owners better and more direct control over who gets notified.

Instead of each user specifying what they want to get notified about; or having central notification rules
managed by an admin, this plugin allows setting notification rules in the pipeline configuration via environment variables.

## Plugin Installation

Just drop the plugin jar into ```plugins/external``` and restart the server as per
[official guide](https://docs.gocd.org/current/extension_points/plugin_user_guide.html).

## Plugin configuration

There are a number of global configuration settings for the plugin that can be set in the GoCD server plugins view.

None of these settings are *required* but it's likely that the defaults do not match your setup.

* **SMTP Host**
  * SMTP server host via which to send mails
  * **Default:** ```localhost```
* **SMTP Port** 
  * Port on which to connect to SMTP server 
  * **Default:** ```25```
* **From E-mail address:** 
  * E-Mail address to use as sender
  * **Default:** ```noreply@localhost.com```
* **Server Display URL:** 
  * The base url to use when linking to the GoCd GUI in mails 
  * **Default:** ```https://localhost:8154/go```
* **E-Mail subject template:**
  * The template to use for the E-Mail subject. Allows using certain templating variables. See below.
  * **Default:**: ```Stage [{pipeline}/{pipelineCounter}/{stage}/{stageCounter}] {event}``` 
* **E-Mail body template:**
  * The template to use for the E-Mail body. Allows using certain templating variables. See below.
  * **Default:**:
    ``` 
    See details: <a href="{serverUrl}/pipelines/{pipeline}/{pipelineCounter}/{stage}/{stageCounter}">{serverUrl}/pipelines/{pipeline}/{pipelineCounter}/{stage}/{stageCounter}</a>
    <br/>
    <br/>-- CHECK-INS --
    <br/>
    <br/>{modificationList}
    ``` 
* **REST URL:** 
  * The base url to use when making REST calls to the GoCD server 
  * **Default:** ```http://localhost:8153/go```
* **REST user name:** 
  * The user to use when authorizing against an admin REST interface of the GoCD server 
  * **Default:** [none]
* **REST user password:** 
  * The password to use when authorizing against an admin REST interface of the GoCD server 
  * **Default:** [none]
  
The REST user credentials are necessary because the plugin needs to use an admin interface to retrieve the pipelines' environment
variables. They do not need to be set if your server does not have any authorization enabled.

### E-Mail templating options

Templating is limited to replacing simple variables with their values. The following variables are available:
* {pipeline} - Pipeline name
* {stage} - Stage name
* {pipelineCounter} - Counter for the pipeline
* {stageCounter} - Counter for the stage
- {event} - The stage transition event in a user friendly verb form. E.g. *is fixed*, *passed*, *is broken*, *failed*
- {serverUrl} - The base server url as specified in the Server Display Url setting
- {modificationList} - The list of modifications that caused the build

## Pipeline notification configuration

Each pipeline can specify whom to e-mail when a stage change event occurs.
The notification settings are done via pipeline environment variables.

```GONG_EMAIL_ADDRESS``` allows you to set a *single* e-mail address to notify. Example:

```
GONG_EMAIL_ADDRESS = bob@example.com
```

If you specify nothing else, this e-mail address gets notified for any of the default stage change events:

- **building**: Stage is building
- **cancelled**: Stage was cancelled
- **passed**: Stage passed
- **failed**: Stage failed
- **broken**: Stage previously passed, but now failed
- **fixed**: Stage previously failed, but now passed

*Note: **broken** and **failed**, as well as **fixed** and **passed**, are mutually exclusive. If a pipeline transitions from **failed**
to **passed**, only a **fixed** event is triggered, not a **passed** event. This is the same behavior as in the default
 GoCd notification.*

### Filtering by state change event

If only specific state changes should trigger a notification, you can use ```GONG_EMAIL_ADDRESS_EVENTS``` and list the events
you're interested in as a comma-separated list. Example:

```
GONG_EMAIL_ADDRESS_EVENTS = broken,fixed
```

### Multiple targets

You can specify multiple different notification targets with different event filters by using an arbitrary suffix after
```GONG_EMAIL_ADDRESS```. Example:

```
GONG_EMAIL_ADDRESS_1 = bob@example.com
GONG_EMAIL_ADDRESS_1_EVENTS = broken,fixed
 
GONG_EMAIL_ADDRESS_2 = sarah@example.com
GONG_EMAIL_ADDRESS_2_EVENTS = passed, failed
 
GONG_EMAIL_ADDRESS_bananas = frank@example.com
GONG_EMAIL_ADDRESS_bananas_EVENTS = building
```

## Development

### Building

The project uses Gradle as its build tool.
```
./gradlew assemble
```
This builds the final jar file (which includes all dependencies) in ```build/libs```. 

You can run all tests with:
```
./gradlew test
```

All tests are self-contained and do not require a running GoCD server.