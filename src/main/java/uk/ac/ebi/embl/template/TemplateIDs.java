/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.template;

import java.util.Arrays;
import java.util.List;

public class TemplateIDs {
  public static final String FEATURE_TABLE_TEMPLATE_ID = "ERT000021";
  public static final String SINGLE_CDS_MRNA_TEMPLATE_ID = "ERT000006";
  public static final String ITS_TEMPLATE_ID = "ERT000009";
  public static final String PHYLO_MARKER_TEMPLATE_ID = "ERT000038";
  public static final String NCRNA_TEMPLATE_ID = "ERT000042";
  public static final String MOBILE_ELEMENT_TEMPLATE_ID = "ERT000056";
  public static final String MULTI_GENE_MARKER_TEMPLATE_ID = "ERT000058";
  public static final String MULTI_EXON_TEMPLATE_ID = "ERT000059";
  public static final String SINGLE_CDS_GENOMIC_DNA_TEMPLATE_ID = "ERT000029";
  public static final String RIBOSOMAL_RNA_TEMPLATE_ID = "ERT000002";
  public static final String SATELLITE_DNA_TEMPLATE_ID = "ERT000039";
  public static final String PROMOTER_TEMPLATE_ID = "ERT000054";
  public static final String IGS_TEMPLATE_ID = "ERT000035";
  public static final String GENE_INTRON_TEMPLATE_ID = "ERT000037";
  public static final String ISR_TEMPLATE_ID = "ERT000050";
  public static final String TSA_UNANNOTATED_TEMPLATE_ID = "ERT000048";
  public static final String TSA_ANNOTATED_TEMPLATE_ID = "ERT000049";
  public static final List<String> FREQUENTLY_USED_CHECKLISTS =
      Arrays.asList(
          RIBOSOMAL_RNA_TEMPLATE_ID,
          SINGLE_CDS_GENOMIC_DNA_TEMPLATE_ID,
          SINGLE_CDS_MRNA_TEMPLATE_ID,
          MULTI_EXON_TEMPLATE_ID,
          "30",
          "36",
          NCRNA_TEMPLATE_ID,
          SATELLITE_DNA_TEMPLATE_ID,
          MOBILE_ELEMENT_TEMPLATE_ID,
          PROMOTER_TEMPLATE_ID);
  public static final List<String> MARKER_SEQUENCE_CHECKLISTS =
      Arrays.asList(
          "20",
          ITS_TEMPLATE_ID,
          "32",
          PHYLO_MARKER_TEMPLATE_ID,
          MULTI_GENE_MARKER_TEMPLATE_ID,
          "34",
          IGS_TEMPLATE_ID,
          GENE_INTRON_TEMPLATE_ID,
          "53",
          ISR_TEMPLATE_ID);
  public static final List<String> VIRUS_SPECIFIC_CHECKLISTS =
      Arrays.asList("28", "51", "52", "60", "57", "47", "31");
  public static final List<String> LARGE_SCALE_DATA_CHECKLISTS =
      Arrays.asList("3", "55", "24", TSA_UNANNOTATED_TEMPLATE_ID, TSA_ANNOTATED_TEMPLATE_ID);
  public static final List<String> BARCODE_COMPLIANT_TEMPLATE_IDs = Arrays.asList("33");
  public static final List<String> MIMARKS_COMPLIANT_TEMPLATE_IDs = Arrays.asList("11");
  public static final List<String> MIENS_TEMPLATE_IDs = Arrays.asList("11");
  public static final List<String> NEW_TEMPLATE_IDS =
      Arrays.asList(
          "31",
          NCRNA_TEMPLATE_ID,
          MOBILE_ELEMENT_TEMPLATE_ID,
          "57",
          MULTI_GENE_MARKER_TEMPLATE_ID,
          MULTI_EXON_TEMPLATE_ID,
          "60");
}
