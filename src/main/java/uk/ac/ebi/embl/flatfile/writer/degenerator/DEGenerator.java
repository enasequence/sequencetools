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
package uk.ac.ebi.embl.flatfile.writer.degenerator;

import static uk.ac.ebi.embl.api.entry.feature.Feature.MISC_FEATURE_NAME;
import static uk.ac.ebi.embl.api.entry.qualifier.Qualifier.*;
import static uk.ac.ebi.embl.api.validation.SequenceEntryUtils.*;
import static uk.ac.ebi.embl.api.validation.Severity.WARNING;

import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 19-Oct-2010 Time: 13:09:33 To change this template
 * use File | Settings | File Templates.
 */
public class DEGenerator {

  private static final String NO_CDS_GENE_MESSAGE =
      "mRNA mol_type with no CDS or Gene feature - cant make body?!";
  private static final String NO_GENE_OR_PRODUCT_MESSAGE =
      "no gene or product qualifiers on CDS or gene features - cant make body?!";
  private static final String COMPOUND_LOCATION_MISC_FEATURE_MESSAGE =
      "Misc feature has compound location";
  private static final String NO_DATACLASS_MESSAGE = "Entry has no dataclass";

  public static ValidationResult writeDE(Entry entry) {

    ValidationResult result = new ValidationResult();

    if (entry == null) {
      return result;
    }

    DEData deData = new DEData(entry);

    /*PREFIX*/
    String prefix = "";
    if (deData.isTPA()) {
      prefix = "TPA";
    }

    /*ORGANISM*/
    String organism = "";
    SourceFeature primarySource = entry.getPrimarySourceFeature();
    if (primarySource != null) {
      Qualifier organismQualifier = getQualifier(ORGANISM_QUALIFIER_NAME, primarySource);
      if (organismQualifier != null) {
        organism = organismQualifier.getValue().concat(" ");
      }

      if (isQualifierAvailable(PROVIRAL_QUALIFIER_NAME, primarySource)) {
        Qualifier noteQualifier = getQualifier(Qualifier.NOTE_QUALIFIER_NAME, primarySource);
        if (noteQualifier != null && noteQualifier.getValue().endsWith("endogenous retrovirus")) {
          organism = noteQualifier.getValue().concat(" ");
        }
      } else if (isQualifierAvailable(DB_XREF_QUALIFIER_NAME, primarySource)) {
        // todo look for taxid and get name from service
      }
    }

    /*ORGANELLE*/
    String organelle = "";
    if (isQualifierAvailable(ORGANELLE_QUALIFIER_NAME, primarySource)) {
      String organelleQual = getQualifier(ORGANELLE_QUALIFIER_NAME, primarySource).getValue();
      if (organelleQual.indexOf(":") != -1) {
        // strip stuff before the ":"
        organelleQual =
            organelleQual.substring((organelleQual.indexOf(":") + 1), organelleQual.length());
      }
      organelleQual = organelleQual.replace("mitochondrion", "mitochondrial");
      organelle = organelle.concat(organelleQual).concat(" ");
    }

    if (isQualifierAvailable(PLASMID_QUALIFIER_NAME, primarySource)) {
      String plasmidString = getQualifier(PLASMID_QUALIFIER_NAME, primarySource).getValue();
      organelle = organelle.concat("plasmid ");
      organelle = organelle.concat(plasmidString).concat(" ");
    }

    /*BODY*/
    String body = "";
    String mol_type = deData.getMol_type();

    String dataClass = entry.getDataClass();
    if (dataClass == null) {
      result.append(
          ValidationMessage.message(Severity.ERROR, NO_DATACLASS_MESSAGE)
              .append(entry.getOrigin()));
      return result;
    }

    //        System.out.println("dataClass = " + dataClass);

    /** EST/GSS/STS 3.1 */
    if (dataClass.equals(Entry.EST_DATACLASS)
        || dataClass.equals(Entry.GSS_DATACLASS)
        || dataClass.equals(Entry.STS_DATACLASS)) {

      body = dataClass;
      String uniquifierString =
          DEData.getUniquifierString(
              Arrays.asList(ISOLATE_QUALIFIER_NAME, CLONE_QUALIFIER_NAME, MAP_QUALIFIER_NAME),
              primarySource);
      body = body.concat(uniquifierString);

      /** circular replicon body 3.2 */
    } else if (deData.isCircular()) {

      body = "complete sequence";
      if (deData.hasChromosome()) {
        body = "chromosome " + deData.getChromosome() + " complete sequence";
      } else if (deData.hasSegment()) {
        body = "segment " + deData.getSegment() + " complete sequence";
      }

      String uniquifierString =
          DEData.getUniquifierString(
              Arrays.asList(ISOLATE_QUALIFIER_NAME, STRAIN_QUALIFIER_NAME), primarySource);
      body = body.concat(uniquifierString);

      /** normal coding mRNA 3.3 */
    } else if (deData.isMolType(Sequence.MRNA_MOLTYPE) && deData.isSingleCdsOrGeneFeature()) {

      body = "mRNA for ";
      Feature feature = deData.getSingleCdsOrGeneFeature();

      if (feature != null) {

        body = body.concat(createmRNAProductStringFromFeature(feature, result, entry));

      } else {
        result.append(
            ValidationMessage.message(WARNING, NO_CDS_GENE_MESSAGE).append(entry.getOrigin()));
      }

      /** polycistronic coding mRNA 3.4 */
    } else if (deData.isMolType(Sequence.MRNA_MOLTYPE) && deData.isMultiCdsOrGeneFeature()) {
      body = "polycistronic mRNA for ";

      List<Feature> relevantFeatures = deData.getMultiCdsOrGeneFeatures();

      for (Feature feature : relevantFeatures) {
        body = body.concat(createmRNAProductStringFromFeature(feature, result, entry));
        body = body.concat(", ");
      }
      body = StringUtils.removeEnd(body, ", ");

      /** non-standard 3.5 */
    } else if (getFeatures(MISC_FEATURE_NAME, entry).size() > 0) {

      for (Feature misc_feature : getFeatures(MISC_FEATURE_NAME, entry)) {
        if (deData.isFeatureSequenceLength(misc_feature)) {
          if (isQualifierAvailable(NOTE_QUALIFIER_NAME, misc_feature)) {
            body = getQualifier(NOTE_QUALIFIER_NAME, misc_feature).getValue();
          }
        }
      }

      /** 3.5.5) rRNA entries and 3.5.7) rRNA gene entries */
    } else if ((deData.isMolType(Sequence.RRNA_MOLTYPE)
            || deData.isMolType(Sequence.GENOMIC_DNA_MOLTYPE))
        && deData.hasSequenceLengthrRna()) {

      boolean rRna = deData.isMolType(Sequence.RRNA_MOLTYPE);

      Feature rRnaFeature = deData.getSequenceLengthrRna();

      if (isQualifierAvailable(Qualifier.GENE_QUALIFIER_NAME, rRnaFeature)) {
        String geneQualValue = getQualifierValue(Qualifier.GENE_QUALIFIER_NAME, rRnaFeature);
        if (DEData.isFeaturePartial(rRnaFeature)) {
          if (rRna) {
            body = "partial " + geneQualValue;
          } else {
            body = "partial gene for " + geneQualValue;
          }
        } else {
          if (rRna) {
            body = geneQualValue;
          } else {
            body = "gene for " + geneQualValue;
          }
        }
      } else {
        if (rRna) {
          body = "16s gene";
        } else {
          body = "gene";
        }
      }

      String uniquifierString =
          DEData.getUniquifierString(
              Arrays.asList(ISOLATE_QUALIFIER_NAME, CLONE_QUALIFIER_NAME), primarySource);
      body = body.concat(uniquifierString);

      /** 3.6) normal coding genomic DNA (1 CDS) */
    } else if (deData.isMolType(Sequence.GENOMIC_DNA_MOLTYPE)
        && deData.isSingleCdsOrGeneFeature()) {

      Feature feature = deData.getSingleCdsOrGeneFeature();

      if (feature != null) {
        body = body.concat(createSTDProductStringFromFeature(feature, result, entry));
      }

      String uniquifierString =
          DEData.getUniquifierString(Arrays.asList(ISOLATE_QUALIFIER_NAME), primarySource);
      body = body.concat(uniquifierString);

      /** 3.7) normal coding genomic DNA (>1 CDS) */
    } else if (deData.isMolType(Sequence.GENOMIC_DNA_MOLTYPE) && deData.isMultiCdsOrGeneFeature()) {

      List<Feature> relevantFeatures = deData.getMultiCdsOrGeneFeatures();

      for (Feature feature : relevantFeatures) {
        body = body.concat(createSTDProductStringFromFeature(feature, result, entry));
        body = body.concat(", ");
      }
      body = StringUtils.removeEnd(body, ", ");

      String uniquifierString =
          DEData.getUniquifierString(Arrays.asList(ISOLATE_QUALIFIER_NAME), primarySource);
      body = body.concat(uniquifierString);

    } else {
      System.out.println("No body for " + entry.getPrimaryAccession());
    }

    String deString = prefix + organism + organelle + body;

    System.out.println(entry.getPrimaryAccession());
    System.out.println("original " + entry.getDescription().getText());
    System.out.println("new      " + deString);

    entry.setDescription(new Text(deString.trim()));

    return result;
  }

  private static String createmRNAProductStringFromFeature(
      Feature feature, ValidationResult result, Entry entry) {
    String resultString = "";
    String geneString = DEData.getGeneString(feature);
    String productString = DEData.getProductString(feature);

    if (productString != null && geneString != null) {
      resultString = productString + "(" + geneString + " gene)";
    } else if (productString != null) {
      resultString = productString;
    } else if (geneString != null) {
      resultString = geneString + " gene mRNA";
    } else {
      result.append(
          ValidationMessage.message(WARNING, NO_GENE_OR_PRODUCT_MESSAGE).append(entry.getOrigin()));
    }

    return resultString;
  }

  private static String createSTDProductStringFromFeature(
      Feature feature, ValidationResult result, Entry entry) {
    String resultString = "";
    String geneString = DEData.getGeneString(feature);
    String productString = DEData.getProductString(feature);

    if (productString != null && geneString != null) {
      resultString = geneString + " gene for " + productString;
    } else if (productString != null) {
      resultString = "gene for " + productString;
    } else if (geneString != null) {
      resultString = geneString + " gene";
    } else {
      result.append(
          ValidationMessage.message(WARNING, NO_GENE_OR_PRODUCT_MESSAGE).append(entry.getOrigin()));
    }

    return resultString;
  }
}
