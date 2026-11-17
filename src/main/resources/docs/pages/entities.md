# Entities

**This extension defines no entities.**

Entities describe structured records a request returns — a job, a ticket, a contact — so that
workflows can read individual fields from them. Rename PDF returns a **file**, not a record. A file
is a first-class Krista type, not a set of fields, so there is nothing to model as an entity and
nothing for a transformer to map.

This matches the rest of the Essentials / Documents domain, where every published request returns a
file, a list, or plain text, and no entities are defined.

## What the requests return instead

| Request | Returns | Type |
|---|---|---|
| [Rename PDF File](pages/RenamePdfFile.md) | Renamed PDF File | File |
| [Health Check](pages/HealthCheck.md) | Healthy / Message | Boolean / Text |

## Working with the file output

The **Renamed PDF File** output is a Krista file reference. Pass it straight into any later workflow
step that accepts a file — attaching it to an email, uploading it to another system, or feeding it
into another request in the PDF area. You do not need to unpack it, and its content is identical to
the file you supplied.
