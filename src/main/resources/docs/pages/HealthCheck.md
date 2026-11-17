# Health Check

## Overview

Reports whether this extension can reach Krista's file store — the one dependency it has — and how
much JVM heap it is currently using. Use it to confirm the extension is working before relying on it
in a workflow, or when a rename has started failing.

## Request Details

- **Area**: Setup
- **Type**: QUERY_SYSTEM
- **External API**: none — nothing is contacted off-appliance.

## Input Parameters

This request takes no inputs.

| Parameter | Type | Required | Description |
|---|---|---|---|
| _(none)_ | — | — | — |

## Output Parameters

| Parameter | Type | Description |
|---|---|---|
| Healthy | Boolean | `true` if the Krista file store is available to the extension. |
| Message | Text | Human-readable detail: heap in use when healthy, or what to check when not. |

## Example

**Input**: _(none)_
**Output**: Healthy = `true`, Message = `OK — Krista file store available. Heap in use: 212 MB.`

When the file store is unavailable: Healthy = `false`, Message =
`The Krista file store (FileRepository) is not available, so files cannot be read or written. Check
the appliance logs for extension startup errors.`

## Error Handling

This request is written not to throw: an unavailable file store is reported as `Healthy = false`
with an explanatory `Message`, rather than as a failed request. If the request itself fails, the
failure is classified and the reason is in the response.

A `Healthy = false` result is not something you can fix from the Setup tab — there is no
configuration to correct. It points at the appliance: check the extension's startup logs.

## Why there is no Test Connection request

Other extensions pair Health Check with a Test Connection catalog request that validates credentials
against a vendor API. This extension has neither credentials nor a vendor API — see
[Authentication](pages/Authentication.md) — so that request would have nothing to do. The Setup
tab's own **Test Connection** button performs the same file-store check this request does.

## Related Requests

- [Rename PDF File](pages/RenamePdfFile.md) — the extension's actual capability.
- [Configuration](pages/ExtensionConfiguration.md) — why the Setup tab is empty.
