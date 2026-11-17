# Supported Requests

Every request this extension exposes, grouped by area.

## PDF

| Request | Type | Inputs | Output | Description |
|---|---|---|---|---|
| [Rename PDF File](pages/RenamePdfFile.md) | CHANGE_SYSTEM | Input File (File), Output File Name (Text) | Renamed PDF File (File) | Returns the supplied PDF under a new file name, content unchanged. |

## Setup

| Request | Type | Inputs | Output | Description |
|---|---|---|---|---|
| [Health Check](pages/HealthCheck.md) | QUERY_SYSTEM | _(none)_ | Healthy (Boolean), Message (Text) | Reports whether Krista's file store is available, plus JVM heap in use. |

## Agent (MCP) callability

Neither request is exposed as an AI-agent tool in this release. Both are called from workflows in
the usual way.

## Where this extension sits

Rename PDF joins the existing **PDF** area of the Essentials / Documents domain, alongside the
already-published `Convert Excel to PDF`, `Convert Docx File to PDF File`, `Convert image to pdf`,
`Split PDF into Pages`, `Parse PDF` and related requests. Those conversion requests pair naturally
with this one: convert a document to PDF, then rename the result before sending it on.
