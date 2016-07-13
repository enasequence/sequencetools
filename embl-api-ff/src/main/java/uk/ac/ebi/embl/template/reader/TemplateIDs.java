package uk.ac.ebi.embl.template.reader;

import java.util.Arrays;
import java.util.List;

public class TemplateIDs {
    public static final String FEATURE_TABLE_TEMPLATE_ID = "21";
    public static final String SINGLE_CDS_MRNA_TEMPLATE_ID = "006";
    public static final String ITS_TEMPLATE_ID = "9";
    public static final String PHYLO_MARKER_TEMPLATE_ID = "38";
    public static final String NCRNA_TEMPLATE_ID = "42";
    public static final String MOBILE_ELEMENT_TEMPLATE_ID = "56";
    public static final String MULTI_GENE_MARKER_TEMPLATE_ID = "58";
    public static final String MULTI_EXON_TEMPLATE_ID = "59";
    public static final String SINGLE_CDS_GENOMIC_DNA_TEMPLATE_ID = "29";
    public static final String RIBOSOMAL_RNA_TEMPLATE_ID = "2";
    public static final String SATELLITE_DNA_TEMPLATE_ID = "39";
    public static final String PROMOTER_TEMPLATE_ID = "54";
    public static final String IGS_TEMPLATE_ID = "35";
    public static final String GENE_INTRON_TEMPLATE_ID = "37";
    public static final String ISR_TEMPLATE_ID = "50";
    public static final String TSA_UNANNOTATED_TEMPLATE_ID = "48";
    public static final String TSA_ANNOTATED_TEMPLATE_ID = "49";
    public static final List<String> FREQUENTLY_USED_CHECKLISTS = Arrays.asList(RIBOSOMAL_RNA_TEMPLATE_ID, SINGLE_CDS_GENOMIC_DNA_TEMPLATE_ID, SINGLE_CDS_MRNA_TEMPLATE_ID, MULTI_EXON_TEMPLATE_ID, "30", "36", NCRNA_TEMPLATE_ID, SATELLITE_DNA_TEMPLATE_ID, MOBILE_ELEMENT_TEMPLATE_ID, PROMOTER_TEMPLATE_ID);
    public static final List<String> MARKER_SEQUENCE_CHECKLISTS = Arrays.asList("20", ITS_TEMPLATE_ID, "32", PHYLO_MARKER_TEMPLATE_ID, MULTI_GENE_MARKER_TEMPLATE_ID, "34", IGS_TEMPLATE_ID, GENE_INTRON_TEMPLATE_ID, "53", ISR_TEMPLATE_ID);
    public static final List<String> VIRUS_SPECIFIC_CHECKLISTS = Arrays.asList("28", "51", "52", "60", "57", "47", "31");
    public static final List<String> LARGE_SCALE_DATA_CHECKLISTS = Arrays.asList("3", "55", "24", TSA_UNANNOTATED_TEMPLATE_ID, TSA_ANNOTATED_TEMPLATE_ID);
    public static final List<String> BARCODE_COMPLIANT_TEMPLATE_IDs = Arrays.asList("33");
    public static final List<String> MIMARKS_COMPLIANT_TEMPLATE_IDs = Arrays.asList("11");
    public static final List<String> MIENS_TEMPLATE_IDs = Arrays.asList("11");
    public static final List<String> NEW_TEMPLATE_IDS = Arrays.asList("31", NCRNA_TEMPLATE_ID, MOBILE_ELEMENT_TEMPLATE_ID, "57", MULTI_GENE_MARKER_TEMPLATE_ID, MULTI_EXON_TEMPLATE_ID, "60");
}
