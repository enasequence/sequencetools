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
package uk.ac.ebi.embl.flatfile;

public abstract class GenbankPadding {

  public static final String LOCUS_PADDING = "LOCUS       ";
  public static final String DEFINITION_PADDING = "DEFINITION  ";
  public static final String ACCESSION_PADDING = "ACCESSION   ";
  public static final String VERSION_PADDING = "VERSION     ";
  public static final String KEYWORDS_PADDING = "KEYWORDS    ";
  public static final String SOURCE_PADDING = "SOURCE      ";
  public static final String ORGANISM_PADDING = "  ORGANISM  ";
  public static final String REFERENCE_PADDING = "REFERENCE   ";
  public static final String AUTHORS_PADDING = "  AUTHORS   ";
  public static final String CONSRTM_PADDING = "  CONSRTM   ";
  public static final String TITLE_PADDING = "  TITLE     ";
  public static final String JOURNAL_PADDING = "  JOURNAL   ";
  public static final String PUBMED_PADDING = "   PUBMED   ";
  public static final String REMARK_PADDING = "  REMARK    ";
  public static final String COMMENT_PADDING = "COMMENT     ";
  public static final String PROJECT_PADDING = "PROJECT     ";
  public static final String DBLINK_PADDING = "DBLINK      ";
  public static final String CONTIG_PADDING = "CONTIG      ";
  public static final String PRIMARY_PADDING = "PRIMARY     ";
  public static final String ORIGIN_PADDING = "ORIGIN";
  public static final String BLANK_PADDING = "            ";
  public static final String FEATURE_PADDING = "     ";
  public static final String QUALIFIER_PADDING = "                     ";
  public static final String ERROR_MSG_PADDING = "ERROR  ";
}
