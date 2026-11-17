# Release Notes

- **Developer Name** : Krista Extension Builder
- **Krista Service APIs Java** : 1.0.126
- **Global Catalog Version** : Not Available

## Version 1.0.0 (November 2026)

First release — renaming a PDF inside a Krista workflow.

### New Features

* **Rename PDF File** — supply a PDF and a name, and get the same document back under that name.
  The content is copied exactly and the original file is left untouched. See
  [Rename PDF File](pages/RenamePdfFile.md).
* `.pdf` is appended automatically when the supplied name omits it, and the check ignores case, so
  `Report.PDF` is not doubled into `Report.PDF.pdf`.
* Files that are not genuinely PDFs are rejected by inspecting the file's `%PDF-` signature rather
  than trusting its name, so a mislabelled file is caught before it is renamed.
* **Health Check** — reports whether Krista's file store is reachable and how much JVM heap is in
  use. See [Health Check](pages/HealthCheck.md).

### Resolved Bugs

* Not Available — first release.

### Dependency Updates

* Krista Service APIs Java (`krista-apis` / `extension-impl-anno-processors`) 1.0.126.
* No HTTP, JSON, or vendor SDK dependency: the extension contacts no external system.

### Known Issues

* Not Available.

### Limitations and Caveats

* Renaming produces a **new** file reference rather than renaming in place, because Krista's file
  store has no in-place rename. The original file remains and must be removed separately if it is no
  longer wanted.
* Only PDF documents are accepted. Convert other formats first using the Documents domain's
  `Convert Excel to PDF`, `Convert Docx File to PDF File` or `Convert image to pdf` requests.
* Characters outside `A–Z`, `a–z`, `0–9`, `.`, `_` and `-` are replaced with `_` in the stored file
  name.
* No authentication is required or supported — the extension reaches no external system. See
  [Authentication](pages/Authentication.md).
* Neither request is exposed as an AI-agent (MCP) tool in this release.

<!-- Newest block on top; keep prior blocks untouched. This file is the @ChangeLog target. -->
