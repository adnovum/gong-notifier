# gong-notifier

[![CircleCI](https://circleci.com/gh/adnovum/gong-notifier.svg?style=svg)](https://circleci.com/gh/adnovum/gong-notifier)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=adnovum_gong-notifier&metric=alert_status)](https://sonarcloud.io/dashboard?id=adnovum_gong-notifier)

gong-notifier is a GoCD plugin collection that aims to give pipeline owners better and more direct control over who gets notified.

Instead of each user specifying what they want to get notified about; or having central notification rules
managed by an admin, this plugin allows setting notification rules in the pipeline configuration via environment variables.

The following plugins exist:

- [gong-notifier-email](gong-notifier-email): Sends E-Mail notifications
- [gong-notifier-github-pr-status](gong-notifier-github-pr-status): Updates pull-request status on GitHub

For detailed user guides, please see the respective plugin readmes.

## Development

### Building

The project uses Gradle as its build tool.

```shell
./gradlew assemble
```

This builds the final jar file (which includes all dependencies) in ```build/libs```.

You can run all tests with:

```shell
./gradlew check
```

All tests are self-contained and do not require a running GoCD server.
