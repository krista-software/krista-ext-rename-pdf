package app.krista.extensions.essentials.documents.renamepdf.catalog;

import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.documents.renamepdf.catalog.support.AbstractArea;
import app.krista.extensions.essentials.documents.renamepdf.catalog.support.TelemetryHelper;
import app.krista.extensions.essentials.documents.renamepdf.service.PdfRenameService;

import javax.inject.Inject;

/**
 * PDF area — joins the Documents domain's existing "PDF" area alongside Convert Excel to PDF,
 * Split PDF into Pages, Parse PDF and the other published PDF operations.
 */
@Domain(id = "catEntryDomain_49c18799-c16b-4d33-98ac-f048f446a152",
        name = "Documents",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "12ae5e46-e6c4-4c34-bcf0-ef39bdbddac5")
public class PdfArea extends AbstractArea {

    private final PdfRenameService renameService;

    @Inject
    public PdfArea(PdfRenameService renameService, TelemetryHelper telemetry, Invoker invoker) {
        super(telemetry, invoker);
        this.renameService = renameService;
    }

    @CatalogRequest(
            id = "localDomainRequest_7d3a1c84-5f62-5b19-9e47-2a0c63d8f1ba",
            name = "Rename PDF File",
            description = "Return the supplied PDF document under a new file name. The content is "
                    + "unchanged — only the name differs. The result is a new file; the original is "
                    + "left untouched. '.pdf' is appended automatically if the supplied name omits it, "
                    + "and files that are not genuinely PDFs are rejected.",
            area = "PDF",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.File(name = "Renamed PDF File", multipleFileUpload = false, required = false,
            attributes = {}, options = {})
    public app.krista.model.base.File renamePdfFile(
            @Field.File(name = "Input File", required = true, attributes = {}, options = {})
            app.krista.model.base.File inputFile,
            @Field.Text(name = "Output File Name", required = true,
                    attributes = {@Attribute(name = "visualWidth", value = "M")}, options = {})
            String outputFileName) {

        return auditedValue("RenamePdfFile", () -> renameService.rename(inputFile, outputFileName));
    }
}
