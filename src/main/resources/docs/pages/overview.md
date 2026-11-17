# Overview

Rename PDF renames a PDF document inside a Krista workflow. You give it a PDF and the name you want,
and it hands back the same PDF — byte for byte — carrying the new name.

Unlike most extensions, this one connects to nothing. There is no vendor API, no server, and no
account: the work happens inside Krista, against Krista's own file store. That is why there is
nothing to authenticate and nothing to configure.

## Use cases

- **Name a generated document before it leaves Krista** — a PDF produced by `Get Table PDF` or
  `Convert Docx File to PDF File` carries a generic name; rename it to `Invoice 2026-Q1.pdf` before
  emailing it to a customer.
- **Apply a naming convention in bulk** — inside a loop, rename each PDF to a name built from the
  record it belongs to, so the files land in a downstream system already correctly labelled.
- **Give a scanned or uploaded file a meaningful name** — replace `scan0001.pdf` with something a
  person can find later.

## What you can do

The extension exposes 2 requests — see [Supported Requests](pages/supported-requests.md) for the
full list, or the sidebar for each request's detailed page.

## How the rename works

Krista stores files as media references, not as files on a disk, and the platform has no in-place
rename. Renaming therefore means reading the original content and storing it again under the new
name. Two consequences are worth knowing before you build a workflow around it:

1. **The result is a new file.** The request returns a new file reference; use that output in the
   following steps.
2. **The original is left untouched.** Renaming never destroys or replaces the file you passed in.

## Before you start

There is no setup. No credentials, no URL, no connection fields — see
[Authentication](pages/Authentication.md) and [Configuration](pages/ExtensionConfiguration.md),
both of which explain why they are empty.

1. Add the extension to your workspace.
2. Optionally run **Health Check** (Setup area) to confirm the extension can reach Krista's file
   store — see [Health Check](pages/HealthCheck.md).
3. Call [Rename PDF File](pages/RenamePdfFile.md) from your workflow.
