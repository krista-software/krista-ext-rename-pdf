# Configuration

**This extension has no configuration fields.** Install it and it is ready to use.

## Setup-tab fields

| Field | Required | Default | Description |
|---|---|---|---|
| _(none)_ | — | — | The extension declares no connection attributes. It contacts no external system, so there is nothing to point it at and no credential to store. |

That empty table is deliberate. Most extensions ask for a base URL and a token here; Rename PDF
works entirely inside Krista, so it asks for nothing. See [Authentication](pages/Authentication.md)
for the longer explanation.

## Verifying the installation

After adding the extension to a workspace:

1. Open the **Setup** tab and press **Test Connection**. With no credentials to check, this confirms
   that Krista's file store is reachable from the extension.
2. Or run the [Health Check](pages/HealthCheck.md) request, which reports the same thing plus JVM
   heap in use, and can be called from a workflow.

Both succeed on a healthy appliance with no prior configuration.

## Runtime behaviour worth knowing

| Behaviour | Detail |
|---|---|
| Temporary files | Content is staged under `/tmp` while being copied, then deleted — including when the rename fails. |
| Logging | File names and media ids are logged at DEBUG. File **contents** are never logged. |
| Telemetry | Each call records a request/success/failure counter and a duration under the `renamepdf.*` prefix. |
| Java version | 21. |

## Related pages

- [Overview](pages/overview.md)
- [Supported Requests](pages/supported-requests.md)
- [Rename PDF File](pages/RenamePdfFile.md)
