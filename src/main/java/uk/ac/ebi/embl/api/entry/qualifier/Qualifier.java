/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.api.entry.qualifier;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import uk.ac.ebi.embl.api.storage.CachedFileDataManager;
import uk.ac.ebi.embl.api.storage.DataManager;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.check.CheckFileManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class Qualifier implements HasOrigin, Serializable, Comparable<Qualifier> {

  private static final long serialVersionUID = -3946561979283315312L;

  public static final String ORGANISM_QUALIFIER_NAME = "organism";
  public static final String TRANSLATION_QUALIFIER_NAME = "translation";
  public static final String PROTEIN_ID_QUALIFIER_NAME = "protein_id";
  public static final String CODON_QUALIFIER_NAME = "codon";
  public static final String TRANSL_EXCEPT_QUALIFIER_NAME = "transl_except";
  public static final String TRANSL_TABLE_QUALIFIER_NAME = "transl_table";
  public static final String EXCEPTION_QUALIFIER_NAME = "exception";
  public static final String PSEUDO_QUALIFIER_NAME = "pseudo";
  public static final String PSEUDOGENE_QUALIFIER_NAME = "pseudogene";
  public static final String PARTIAL_QUALIFIER_NAME = "partial";
  public static final String CODON_START_QUALIFIER_NAME = "codon_start";
  public static final String CITATION_QUALIFIER_NAME = "citation";
  public static final String FOCUS_QUALIFIER_NAME = "focus";
  public static final String TRANSGENIC_QUALIFIER_NAME = "transgenic";
  public static final String DB_XREF_QUALIFIER_NAME = "db_xref";
  public static final String MOL_TYPE_QUALIFIER_NAME = "mol_type";
  public static final String GENE_QUALIFIER_NAME = "gene";
  public static final String GENE_SYNONYM_NAME = "gene_synonym";
  public static final String LOCUS_TAG_QUALIFIER_NAME = "locus_tag";
  public static final String RIBOSOMAL_SLIPPAGE_QUALIFIER_NAME = "ribosomal_slippage";
  public static final String ESTIMATED_LENGTH_QUALIFIER_NAME = "estimated_length";
  public static final String NOTE_QUALIFIER_NAME = "note";
  public static final String MOBILE_ELEMENT_NAME = "mobile_element";
  public static final String LABEL_QUALIFIER_NAME = "label";
  public static final String REPLACE_QUALIFIER_NAME = "replace";
  public static final String COMPARE_QUALIFIER_NAME = "compare";
  public static final String COLLECTION_DATE_QUALIFIER_NAME = "collection_date";
  public static final String ANTICODON_QUALIFIER_NAME = "anticodon";
  public static final String RPT_UNIT_RANGE_QUALIFIER_NAME = "rpt_unit_range";
  public static final String TAG_PEPTIDE_QUALIFIER_NAME = "tag_peptide";
  public static final String PROVIRAL_QUALIFIER_NAME = "proviral";
  public static final String ORGANELLE_QUALIFIER_NAME = "organelle";
  public static final String PLASMID_QUALIFIER_NAME = "plasmid";
  public static final String PRODUCT_QUALIFIER_NAME = "product";
  public static final String ISOLATE_QUALIFIER_NAME = "isolate";
  public static final String ISOLATION_SOURCE_QUALIFIER_NAME = "isolation_source";
  public static final String CLONE_QUALIFIER_NAME = "clone";
  public static final String MAP_QUALIFIER_NAME = "map";
  public static final String STRAIN_QUALIFIER_NAME = "strain";
  public static final String CHROMOSOME_QUALIFIER_NAME = "chromosome";
  public static final String SEGMENT_QUALIFIER_NAME = "segment";
  public static final String EC_NUMBER_QUALIFIER_NAME = "EC_number";
  public static final String OLD_LOCUS_TAG = "old_locus_tag";
  public static final String PCR_PRIMERS_QUALIFIER_NAME = "PCR_primers";
  public static final String LAT_LON_QUALIFIER_NAME = "lat_lon";
  public static final String SATELLITE_QUALIFIER_NAME = "satellite";
  public static final String NCRNA_CLASS_QUALIFIER_NAME = "ncRNA_class";
  public static final String GAP_TYPE_QUALIFIER_NAME = "gap_type";
  public static final String LINKAGE_EVIDENCE_QUALIFIER_NAME = "linkage_evidence";
  public static final String NUMBER_QUALIFIER_NAME = "number";
  public static final String EXPERIMENT_QUALIFIER_NAME = "experiment";
  public static final String TYPE_MATERIAL_QUALIFIER_NAME = "type_material";
  public static final String GERMLINE_QUALIFIER_NAME = "germline";
  public static final String MACRONUCLEAR_QUALIFIER_NAME = "macronuclear";
  public static final String REARRANGED_QUALIFIER_NAME = "rearranged";
  public static final String ENVIRONMENTAL_SAMPLE_QUALIFIER_NAME = "environmental_sample";
  public static final String HOST_QUALIFIER_NAME = "host";
  public static final String OPERON_QUALIFIER_NAME = "operon";
  public static final String COUNTRY_QUALIFIER_NAME = "country";
  public static final String REGULATORY_CLASS_QUALIFIER_NAME = "regulatory_class";
  public static final String ALTITUDE_QUALIFIER_NAME = "altitude";
  public static final String ARTIFICIAL_LOCATION = "artificial_location";
  public static final String TRANS_SPLICING = "trans_splicing";
  public static final String RIBOSOMAL_SLIPPAGE = "ribosomal_slippage";
  public static final String METAGENOME_SOURCE_QUALIFIER_NAME = "metagenome_source";
  public static final String SUBMITTER_SEQID_QUALIFIER_NAME = "submitter_seqid";
  public static final String SEROVAR_QUALIFIER_NAME = "serovar";
  public static final String SEROTYPE_QUALIFIER_NAME = "serotype";
  public static final String CIRCULAR_RNA_QUALIFIER_NAME = "circular_RNA";
  public static final String SUB_SPECIES = "sub_species";

  private static final HashSet<String> QUOTED_QUALS = new HashSet<String>();

  private static final HashMap<String, Integer> ORDER_QUALS = new HashMap<String, Integer>();

  private static final Integer DEFAULT_ORDER_QUALS = Integer.MAX_VALUE;

  static {
    DataManager dataManager = new CachedFileDataManager();
    CheckFileManager tsvFileManager = new CheckFileManager();

    DataSet dataSet =
        dataManager.getDataSet(
            tsvFileManager.filePath(
                GlobalDataSetFile.FEATURE_QUALIFIER_VALUES.getFileName(), false));
    if (dataSet != null) {

      for (DataRow row : dataSet.getRows()) {
        String name = row.getString(0);
        String order = row.getString(5);
        String quoted = row.getString(3);

        if (quoted.equals("Y")) {
          QUOTED_QUALS.add(name);
        }

        ORDER_QUALS.put(name, Integer.parseInt(order));
      }
    }
  }

  private Origin origin;
  private String id;
  private String name;
  private String value;

  protected Qualifier(String name, String value) {
    this.name = name;
    this.value = value;
  }

  protected Qualifier(String name) {
    this(name, null);
  }

  public Origin getOrigin() {
    return origin;
  }

  public void setOrigin(Origin origin) {
    this.origin = origin;
  }

  public boolean isValueQuoted() {
    return QUOTED_QUALS.contains(name);
  }

  public String getId() {
    return id;
  }

  public void setId(Object id) {
    if (id != null) {
      this.id = id.toString();
    } else {
      this.id = null;
    }
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return this.value;
  }

  public String getValueRemoveTabs() {
    String returnedValue = null;
    if (this.value != null) {
      returnedValue = this.value.replaceAll("\\r\\n|\\r|\\n", "");
      returnedValue = returnedValue.replaceAll("\\t", " ");
    }
    return returnedValue;
  }

  public boolean isValue() {
    if (value == null || value.trim().isEmpty()) return false;
    else return true;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setName(String name) {
    this.name = name;
  }

  protected void throwValueException() throws ValidationException {
    throw new ValidationException(
        ValidationMessage.error("Qualifier", getName(), getValue()).append(origin));
  }

  @Override
  public int hashCode() {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append(this.id);
    builder.append(this.name);
    builder.append(this.value);
    return builder.toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Qualifier) {
      final Qualifier other = (Qualifier) obj;
      final EqualsBuilder builder = new EqualsBuilder();
      builder.append(this.id, other.id);
      builder.append(this.name, other.name);
      builder.append(this.value, other.value);
      return builder.isEquals();
    } else {
      return false;
    }
  }

  public int compareTo(Qualifier o) {
    // The natural order of the qualifiers is the order in
    // which they should appear in the flat file.
    if (this.equals(o)) {
      return 0;
    }
    final CompareToBuilder builder = new CompareToBuilder();
    Integer thisOrder = ORDER_QUALS.get(this.name);
    if (thisOrder == null) {
      thisOrder = DEFAULT_ORDER_QUALS;
    }
    Integer otherOrder = ORDER_QUALS.get(o.name);
    if (otherOrder == null) {
      otherOrder = DEFAULT_ORDER_QUALS;
    }
    builder.append(thisOrder, otherOrder);
    return builder.toComparison();
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.append("id", id);
    builder.append("name", name);
    builder.append("value", value);
    return builder.toString();
  }
}
