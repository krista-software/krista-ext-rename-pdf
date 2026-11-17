# Authentication

**This extension has no authentication. There is nothing to set up here.**

Rename PDF does not contact any external system. It renames a file inside Krista, using the
platform's own file store, so there is no third-party account to connect to and no credential for
anyone to supply.

Concretely, this means:

- There is **no Authentication tab** on this extension — and that is correct, not a missing feature.
- There are **no connection fields** on the Setup tab: no API key, no token, no client ID or secret,
  no username or password, no base URL.
- There is **no OAuth consent**, no redirect URI to register, and no scopes or permissions to grant
  in another product's admin portal.
- Nothing needs to be rotated, renewed, or kept in a secret store.

## What about the Test Connection button?

The Setup tab's **Test Connection** button still works, but it is not checking credentials — there
are none. It verifies the one dependency this extension genuinely has: that Krista's file store is
available to the extension, so PDFs can be read and written.

If Test Connection fails, the message points at the appliance logs rather than at any configuration
you could correct on the Setup tab. The [Health Check](pages/HealthCheck.md) request reports the
same condition from inside a workflow.

## Permissions

Running [Rename PDF File](pages/RenamePdfFile.md) needs no special permission beyond the ability to
execute catalog requests in the workspace. The file it reads is the one handed to it by the calling
workflow.
