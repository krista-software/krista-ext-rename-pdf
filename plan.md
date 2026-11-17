# Implementation Plan — Rename PDF

## Summary

| | |
|---|---|
| **Extension title** | Rename PDF |
| **Module / slug** | `rename-pdf` (module dir `renamepdf`) |
| **Version** | 1.0.0 |
| **Java package** | `app.krista.extensions.essentials.documents.renamepdf` |
| **Gradle group** | `app.krista.extensions.Essentials.Documents` |
| **Ecosystem / Domain** | Essentials / Documents |
| **krista-apis** | 1.0.126 (matched by `extension-impl-anno-processors`) |
| **Auth pattern** | **NONE** |

One capability: hand the request a PDF and a name, get the same PDF back under that name. Nothing
leaves the Krista appliance.

Krista stores files as `app.krista.model.base.File` — a media reference (`mediaId` + `fileName`),
not a path on disk — and the platform's `FileRepository` offers **no in-place rename**. The only
supported way to change a file's name is `createNewFileByName(newName)` followed by
`setContent(originalBytes)`, confirmed in
`skills/krista-extension-business-logic/idioms/KristaMediaClient.java:69`. So the request returns a
**new** file reference holding byte-identical content; the original file is left untouched. That is
a framework constraint, not a design preference.

---

## Area: PDF *(existing area in this domain — reused, not newly invented)*

| Request | Inputs | Output | External endpoint | Reused or new |
|---|---|---|---|---|
| **Rename PDF File** (`CHANGE_SYSTEM`) | `Input File` (File, required)<br>`Output File Name` (Text, required) | `Renamed PDF File: File` | none — in-process `FileRepository` | **New** (no match in catalog) |

**Behaviour, per your answers:**

- **`.pdf` suffix** — appended automatically when the supplied name doesn't already end in `.pdf`
  (case-insensitive, so `Report.PDF` is not doubled). A name ending in some *other* extension, e.g.
  `scan.docx`, still gains `.pdf` rather than being left mislabelled.
- **Input validation** — the file's first five bytes are checked for the `%PDF-` signature. Anything
  else is rejected with an actionable error naming the expected type. A filename check alone was not
  chosen, so a `.pdf`-named file that isn't really a PDF is still caught.
- **Output** — the renamed file only. No extra metadata fields.

## Area: Setup

| Request | Inputs | Output | External endpoint | Reused or new |
|---|---|---|---|---|
| **Health Check** (`QUERY_SYSTEM`) | — | `Healthy: Boolean, Message: Text` | none — verifies `FileRepository` is injectable | **New** |

> **One judgment call for you to confirm.** The builder runbook says to always add a Setup area with
> Test Connection + Health Check. But auth here is NONE, so there is no connection to test, and the
> 22 requests already published in Essentials/Documents include **no** setup or health-check entry —
> adding one is arguably catalog clutter in a domain that has done without it. I've included a
> Health Check only (no Test Connection, no credential fields) as the middle ground. **Say the word
> and I'll drop the Setup area entirely.**

---

## Authentication — NONE

You selected `none`, and the material agrees rather than conflicts: this extension contacts no
external system. `FileRepository` is an in-process platform service supplied by the HK2 container.

Concretely: **no secured connection fields, no token/API-key field, no authenticator class, no
Authentication tab.** The extension class declares no `@Field.Text(isSecured=true)` connection
attributes at all.

This supersedes the provisional "pattern A" recorded in `api-model.json` during analysis — that was
only there because the analysis schema forced a choice of A/B/C/D and offered no "none" option. I
flagged it as not-really-pattern-A at the time; `authPattern` is now `NONE`.

## Capabilities

| Capability | Enabled | Why |
|---|---|---|
| Binary files | **Yes** | Both the input and the output are files — `@Field.File` + `KristaMediaClient`/`FileRepository` |
| Entity search | No | No searchable domain objects; the output is binary content |
| Webhooks / events | No | Nothing pushes notifications to wait on |
| MCP (`tool = true`) | No | Not requested. Easy to enable later if you want this agent-callable |
| Solutions tab | No | Not requested |

### On entities — deliberately none

The builder's completeness contract normally requires an `@Entity` per domain object, so the empty
`entities` list is a conscious decision, not an oversight. There is no JSON domain object here: the
output is a binary file, and `@Field.File` is a first-class Krista output type — the file-output
idiom in `references/file-handling.md` returns `app.krista.model.base.File` directly. This also
matches the domain, which has **zero** entities across all 22 published requests.

---

## Domain reuse

`domain-catalog.json` reported `found: true` — Essentials/Documents is a real, populated domain with
**22 published requests** across 5 areas and **0 entities**.

**Nothing was reused, because nothing matched.** I checked all 22 requests for a rename capability.
The nearest neighbours only accept a name while doing something else entirely:

- `Generate Merged Document` — takes `Document Name: Text`, but *merges* two documents.
- `Fill The Docx Template for Date Field` — takes `Output File Name: Text`, but *fills a template*.

Neither is the business concept "return this file under a new name", so there was no genuine
semantic match to put to you for a reuse/modify decision, and no item was excluded from scope.

**What I did carry over from the domain, to avoid naming drift:**

- The request goes in the **existing `PDF` area** — no new area invented.
- Input named `Input File`, matching `Split PDF Based On Condition` and `Split PDF Into Two Equal Part`.
- Name input called `Output File Name`, matching `Fill The Docx Template for Date Field` — rather
  than a fresh coinage like "New File Name" for the same concept. *Flag this if you'd prefer the
  clearer "New File Name"; it reads better in isolation but drifts from the domain.*
- Output named `Renamed PDF File: File`, matching the `<X> File: File` shape used by `Convert Excel
  to PDF`, `Convert Docx File to PDF File`, and others.
- Vendor-agnostic naming throughout, so a future Documents extension can reuse this request.

---

## ⚠️ Placeholder IDs — MUST be replaced before deploy

`domainReuse.placeholderIds = true`. These compile fine but **will not bind** to the real catalog:

| Placeholder | Needs |
|---|---|
| `{{DOMAIN_ID}}` (`catEntryDomain_…`) | The **real registered** Documents domain ID |
| `{{ECOSYSTEM_ID}}` (`catEntryEcosystem_…`) | The **real registered** Essentials ecosystem ID |
| `{{ECOSYSTEM_VERSION}}` | A stable UUID — the one loose value, safe as generated |

**Important and actionable:** this domain is *not* new — it already has 22 published requests, so
real registered IDs for Essentials/Documents definitely exist. They simply aren't included in
`domain-catalog.json`. Don't register a new domain. Copy the values verbatim from any sibling
extension already published in Essentials/Documents (`git grep '@Domain'` on its release branch).
A random UUID compiles and then silently fails to bind.

`localDomainRequest_<uuid>` request IDs are the opposite case — local to the extension, so
random-with-prefix is correct. They'll be generated as `uuid5` of `"renamepdf.<area>.<method>"` so
they stay stable across releases.

---

## Build shape

Logic lives in pure, framework-free classes so it's actually testable — per the testing policy, the
`@CatalogRequest` method stays a thin delegator and is deliberately *not* unit-tested.

```
renamepdf/
├─ RenamePdfExtension.java        @Extension — no connection fields (auth NONE)
├─ area/PdfArea.java              @CatalogRequest "Rename PDF File"  (thin delegator)
├─ area/SetupArea.java            @CatalogRequest "Health Check"     (thin delegator)
├─ integration/KristaMediaClient.java   FileRepository bridge (per the idiom)
├─ service/PdfRenameService.java  orchestration: validate → rename → upload
├─ util/PdfSignatureValidator.java      PURE — %PDF- magic-byte check
└─ util/PdfFileNameResolver.java        PURE — .pdf suffixing + sanitising
```

**Unit tests** (JUnit 5 + Mockito) target the two pure classes — 8 cases covering suffix appending,
case-insensitive `.pdf` detection, unsafe-character sanitising, blank-name rejection, the `%PDF-`
accept/reject paths, and truncated input that must not throw `IndexOutOfBounds`.

**Delivered by:** `gradle clean jar` → `build/libs/rename-pdf-1.0.0.jar`, with
`META-INF/krista/extension.json` generated by the annotation processor. Stops at the jar — binding
the domain and publishing to `krista-global-catalog` need Krista's registered infrastructure.
