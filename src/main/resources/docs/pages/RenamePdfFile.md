# Rename PDF File

## Overview

Returns the PDF you supply under a new file name. The document's content is untouched — only the
name changes. The file that comes back is a **new** file; the one you passed in is left as it was.

## Request Details

- **Area**: PDF
- **Type**: CHANGE_SYSTEM
- **External API**: none — the rename happens inside Krista, against the platform file store. No
  network call is made.

## Input Parameters

| Parameter | Type | Required | Description | Example |
|---|---|---|---|---|
| Input File | File | Yes | The PDF document to rename. Must be a real PDF — see Error Handling. | `scan0001.pdf` |
| Output File Name | Text | Yes | The name you want the returned file to carry. `.pdf` is added for you if you leave it off. | `Invoice 2026-Q1` |

## Output Parameters

| Parameter | Type | Description |
|---|---|---|
| Renamed PDF File | File | A new file holding the original PDF content under the new name. |

## Example

**Input**: Input File=`scan0001.pdf`, Output File Name=`Invoice 2026-Q1`
**Output**: Renamed PDF File = `Invoice_2026-Q1.pdf` — same document, new name, new file reference.

## How the name is resolved

The name you type is adjusted before the file is stored, so that the result is always a usable PDF
file name:

| You enter | You get | Why |
|---|---|---|
| `Invoice 2026-Q1` | `Invoice_2026-Q1.pdf` | `.pdf` is appended; the space becomes `_`. |
| `statement.pdf` | `statement.pdf` | Already correct — left alone. |
| `Report.PDF` | `Report.PDF` | The extension check ignores case, so the suffix is never doubled into `Report.PDF.pdf`. |
| `scan.docx` | `scan.docx.pdf` | The content is a PDF, so the name is made to say so rather than keeping a misleading `.docx`. |
| `a/b:c` | `a_b_c.pdf` | Characters that are not a letter, digit, `.`, `_` or `-` become `_`. |

Leading and trailing spaces are trimmed.

## Error Handling

- **"Output File Name is required…"** — the name was empty or only whitespace. Supply a name such as
  `Invoice 2026-Q1`.
- **"… leaves no file name before the .pdf extension."** — the name was just an extension, e.g.
  `.pdf`. Supply a name with something in front of the extension.
- **"… is not a PDF document…"** — the supplied file does not begin with the `%PDF-` signature every
  PDF starts with. This is checked on the file's actual content, so a file merely *named* `.pdf` is
  still rejected. Convert it first — the Documents domain provides `Convert Excel to PDF`,
  `Convert Docx File to PDF File` and `Convert image to pdf` — then rename the result.
- **"Failed to read Krista file" / "Failed to store Krista file"** — the platform file store could
  not be read or written. Run [Health Check](pages/HealthCheck.md) and check the appliance logs.

Validation runs before any content is copied, so a bad name or a non-PDF fails quickly and changes
nothing.

## Business Rules

1. The output is always a **new** file reference. Krista's file store has no in-place rename, so the
   content is written again under the new name.
2. The **original file is never modified or deleted**. If you no longer want it, remove it in a
   later step.
3. Renaming a file to the name it already has is allowed and simply produces a second file with that
   name.
4. Content is copied exactly — page count, metadata, and signatures inside the PDF are unaffected.

## Related Requests

- [Health Check](pages/HealthCheck.md) — confirm the file store is available.
- [Supported Requests](pages/supported-requests.md) — everything this extension exposes.
