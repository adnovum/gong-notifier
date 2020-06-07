## Gong Notifier GitHub PR Status

This plugin will update the commit status of pull-requests on GitHub. This assumes GoCD is building 
pull-requests with the  [github-pr-poller plugin](https://github.com/ashwanthkumar/gocd-build-github-pull-requests).

It will set the commit status to **pending**, **success**, **failed**, or **error** depending on the status of
the associated GoCD pull-request pipeline.

Instead of requiring a **central** authentication token for GitHub, the plugin allows each pipeline owner
to define their own authentication token in a secure environment variable in the pipeline configuration.

## Plugin installation

Just drop the plugin jar into ```plugins/external``` and restart the server as per
[official guide](https://docs.gocd.org/current/extension_points/plugin_user_guide.html).

### Configuration

There are a number of global configuration settings for the plugin that can be set in the GoCD server plugins view.

The property keys are listed in brackets and can be used to configure the plugin via the GoCD REST API.

* **Server Display URL (serverDisplayUrl)** 
  * The base url to use when linking to the GoCD GUI in mails 
  * **Default:** ```https://localhost:8154/go```
* **REST URL (serverUrl)** 
  * The base url to use when making REST calls to the GoCD server 
  * **Default:** ```http://localhost:8153/go```
* **REST user name (restName)** 
  * The user to use when authorizing against an admin REST interface of the GoCD server 
  * **Default:** [none]
* **REST user password (restPassword)** 
  * The password to use when authorizing against an admin REST interface of the GoCD server 
  * **Default:** [none]
* **Cipher Key File (cipherKeyFile)** 
  * The `cipher.aes` file which the GoCD server uses to encrypt and decrypt secure variables.
  This is needed to read the access token configured in each pipeline. Normally, the file is located
  at `/etc/go/cipher.aes` for Linux package installations, and `/godata/config/cipher.aes` for Docker instances.
  * **Default:** [none]

The REST user credentials are necessary because the plugin needs to use an admin interface to retrieve the pipelines'
variables. They do not need to be set if your server does not have any authorization enabled.

## User guide for pipeline owners

Each pipeline that wants to enable status updates on GitHub pull-requests must configure
a secret variable `GONG_STATUS_AUTH_TOKEN`. The variable must be defined on the pipeline, not on a stage or job.  
The variable value is a GitHub personal access token with at least the scope `repo:status`.
See the [personal access tokens](https://help.github.com/en/github/authenticating-to-github/creating-a-personal-access-token-for-the-command-line) documentation on GitHub.

![Sample configuration](pipeline_config.png)