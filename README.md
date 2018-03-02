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
- **broken**: Stage is broken
- **fixed**: Stage is fixed

*Note: **broken** and **failed**, as well as **fixed** and **passed**, are mutually exclusive. If a pipeline transitions from **failed**
to **passed**, only a **fixed** event is triggered, not a **passed** event.* This is the same behavior as in the default
 GoCd notification.

### Filtering by state change event

If only specific state changes should trigger a notification, you use ```GONG_EMAIL_ADDRESS_EVENTS``` and list the events
you're interested in as a comma-separated list. Example:

```
GONG_EMAIL_ADDRESS_EVENTS = broken,fixed
```

### Multiple targets

You can specify multiple different notification targets with different event filters by using an arbitrary suffix after
```GONG_EMAIL_ADDRESS```. Example

```
GONG_EMAIL_ADDRESS_1 = bob@example.com
GONG_EMAIL_ADDRESS_1_EVENTS = broken,fixed
GONG_EMAIL_ADDRESS_2 = sarah@example.com
GONG_EMAIL_ADDRESS_2_EVENTS = passed, failed
```
