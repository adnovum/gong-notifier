# Gong Notifier E-Mail

This plugin can send E-Mail notifications based on the pipeline's configuration.

It can send notifications based on the following stage change events:

* **building**: Stage is building
* **cancelled**: Stage was cancelled
* **passed**: Stage passed
* **failed**: Stage failed
* **broken**: Stage previously passed, but now failed
* **fixed**: Stage previously failed, but now passed

*Note: **broken** and **failed**, as well as **fixed** and **passed**, are mutually exclusive. If a pipeline transitions from **failed**
to **passed**, only a **fixed** event is triggered, not a **passed** event. This is the same behavior as in the default
 GoCd notification.*

By default, the plugin will only notify for the following events: **broken, fixed, failed**.  
See chapter **Plugin configuration** on how to change the defaults.

## Plugin installation

Just drop the plugin jar into ```plugins/external``` and restart the server as per
[official guide](https://docs.gocd.org/current/extension_points/plugin_user_guide.html).

### Plugin configuration

There are a number of global configuration settings for the plugin that can be set in the GoCD server plugins view.

None of these settings are *required* but it's likely that the defaults do not match your setup.

The property keys are listed in brackets and can be used to configure the plugin via the GoCD REST API.

* **SMTP Host (smtpHost)**
  * SMTP server host via which to send mails
  * **Default:** ```localhost```
* **SMTP Port (smtpPort)**
  * Port on which to connect to SMTP server
  * **Default:** ```25```
* **Default events to notify (defaultEvents)**
  * List of events for which to notify if no specific events are defined for the pipeline.
  * **Default:** ```broken, fixed, failed```  
* **From E-mail address (senderEmail)**
  * E-Mail address to use as sender
  * **Default:** ```noreply@localhost.com```
* **Server Display URL (serverDisplayUrl)**
  * The base url to use when linking to the GoCD GUI in mails
  * **Default:** ```https://localhost:8154/go```
* **E-Mail subject template (subjectTemplate)**
  * The template to use for the E-Mail subject. Allows using certain templating variables. See below.
  * **Default:**: ```Stage [{pipeline}/{pipelineCounter}/{stage}/{stageCounter}] {event}```
* **E-Mail body template (bodyTemplate)**
  * The template to use for the E-Mail body. Allows using certain templating variables. See below.
  * **Default:**

    ```html
    See details: <a href="{serverUrl}/pipelines/{pipeline}/{pipelineCounter}/{stage}/{stageCounter}">{serverUrl}/pipelines/{pipeline}/{pipelineCounter}/{stage}/{stageCounter}</a>
    <br/>
    <br/>-- CHECK-INS --
    <br/>
    <br/>{modificationList}
    ```

* **REST URL (serverUrl)**
  * The base url to use when making REST calls to the GoCD server
  * **Default:** ```http://localhost:8153/go```
* **REST user name (restName)**
  * The user to use when authorizing against an admin REST interface of the GoCD server
  * **Default:** [none]
* **REST user password (restPassword)**
  * The password to use when authorizing against an admin REST interface of the GoCD server
  * **Default:** [none]
  
The REST user credentials are necessary because the plugin needs to use an admin interface to retrieve the pipelines' environment
variables. They do not need to be set if your server does not have any authorization enabled.

### Templating options

Templating is limited to replacing simple variables with their values. The following variables are available:

* {pipeline} - Pipeline name
* {stage} - Stage name
* {pipelineCounter} - Counter for the pipeline
* {stageCounter} - Counter for the stage
* {event} - The stage transition event in a user friendly verb form. E.g. *is fixed*, *passed*, *is broken*, *failed*
* {serverUrl} - The base server url as specified in the Server Display Url setting
* {modificationList} - The list of modifications that caused the build

### User guide for pipeline owners

Each pipeline can specify whom to e-mail when an event occurs.
The notification settings are done via pipeline environment variables.

```GONG_EMAIL_ADDRESS``` allows you to set one or more e-mail addresses to notify. Example:

```text
GONG_EMAIL_ADDRESS = bob@example.com
```

Example with multiple addresses:

```text
GONG_EMAIL_ADDRESS = bob@example.com, alice@example.com
```

If you specify nothing else, these e-mail addresses are notified for any of the default stage change events.

### Filtering by event

If only specific events should trigger a notification, you can use ```GONG_EMAIL_EVENTS``` and list the events
you're interested in as a comma-separated list. Example:

```text
GONG_EMAIL_EVENTS = broken,fixed
```

### Filtering by event and stage

You can also use different triggers for different pipeline stages. For example, if you're only interested in being notified
about failures of the `deploy` stage, you can configure this like so:

```text
GONG_EMAIL_EVENTS = deploy.failed
```

If you're interested in getting all events but only for a specific stage, use `all` instead of a specific event:

```text
GONG_EMAIL_EVENTS = deploy.all
```

### Multiple targets

You can specify multiple different notification targets with different event filters by using an arbitrary suffix after
```GONG_EMAIL```. Example:

```text
GONG_EMAIL_1_ADDRESS = bob@example.com
GONG_EMAIL_1_EVENTS = broken,fixed

GONG_EMAIL_2_ADDRESS = sarah@example.com
GONG_EMAIL_2_EVENTS = passed, failed

GONG_EMAIL_bananas_ADDRESS = frank@example.com
GONG_EMAIL_bananas_EVENTS = deploy.building
```
