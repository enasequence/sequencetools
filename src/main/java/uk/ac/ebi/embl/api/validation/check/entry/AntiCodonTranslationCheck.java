/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation.check.entry;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.qualifier.AnticodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.sequence.Segment;
import uk.ac.ebi.embl.api.entry.sequence.SegmentFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.translation.CdsTranslator;
import uk.ac.ebi.embl.api.translation.Codon;
import uk.ac.ebi.embl.api.translation.TranslationResult;
import uk.ac.ebi.embl.api.translation.Translator;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;
import uk.ac.ebi.embl.api.validation.check.feature.AntiCodonQualifierCheck;

@Description("Translate the anticodon")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class AntiCodonTranslationCheck extends EntryValidationCheck {
  private static final String ANTICODON_TRANSLATION_MESSAGE_ID = "AntiCodonTranslationCheck_1";
  private static final String ANTICODON_SEQUENCE_MESSAGE_ID = "AntiCodonTranslationCheck_2";

  public ValidationResult check(Entry entry) throws ValidationEngineException {
    result = new ValidationResult();
    AnticodonQualifier antiCodon = null;
    Translator translator = null;
    byte[] sequenceString = null;
    AntiCodonQualifierCheck anticodonCheck = new AntiCodonQualifierCheck();
    if (entry == null) {
      return result;
    }
    List<Feature> features = entry.getFeatures();
    List<CdsFeature> cdsFeatures = SequenceEntryUtils.getCDSFeatures(entry);
    if (features.size() == 0) {
      return result;
    }
    Feature anticodonFeature = null;

    try {
      for (Feature feature : entry.getFeatures()) {
        anticodonFeature = feature;
        if (SequenceEntryUtils.isQualifierAvailable(Qualifier.ANTICODON_QUALIFIER_NAME, feature)) {
          if (anticodonCheck.check(feature).isValid()
              && entry.getSequence().getSequenceByte() != null) {
            List<Qualifier> anticodonQualifiers =
                feature.getQualifiers(Qualifier.ANTICODON_QUALIFIER_NAME);
            for (Qualifier qualifier : anticodonQualifiers) {
              antiCodon = new AnticodonQualifier(qualifier.getValue());
              CompoundLocation<Location> antiCodonCompoundLocation = antiCodon.getLocations();
              if (antiCodonCompoundLocation != null) {
                for (Location anticodonLocation : antiCodonCompoundLocation.getLocations()) {
                  if (anticodonLocation instanceof RemoteLocation) return result;
                }
              }
              SegmentFactory factory = new SegmentFactory();
              Segment segment =
                  factory.createSegment(entry.getSequence(), antiCodonCompoundLocation);
              if (segment == null) continue;
              Sequence sequence = entry.getSequence();
              byte[] anticodonSeq =
                  antiCodon.getSequence() == null ? null : antiCodon.getSequence().getBytes();
              sequenceString = segment.getSequenceByte();

              if (sequence.getSequenceByte() == null) {
                return result;
              }
              boolean anticodonLocationComplement = antiCodonCompoundLocation.isComplement();
              for (Location antiCodonLocation : antiCodonCompoundLocation.getLocations()) {
                if (antiCodonLocation instanceof LocalRange) {
                  if (antiCodonLocation.isComplement()) {
                    anticodonLocationComplement = true;
                  }
                }
              }
              translator = new Translator();
              CdsTranslator trans =
                  new CdsTranslator(getEmblEntryValidationPlanProperty(), translator);

              /** set up the translator with the details from the cds feature */
              try {
                if (cdsFeatures.size() != 0) {
                  result.append(
                      trans.configureFromFeature(
                          cdsFeatures.get(0),
                          getEmblEntryValidationPlanProperty().taxonClient.get(),
                          entry));
                } else {
                  result.append(
                      trans.configureFromFeature(
                          null, getEmblEntryValidationPlanProperty().taxonClient.get(), entry));
                }
              } catch (ValidationException e) {
                reportException(result, e, entry, cdsFeatures.get(0));
              }
              trans.setFivePrimePartial(true);
              TranslationResult translatorResult = new TranslationResult();
              boolean featurelocationComplement = feature.getLocations().isComplement();
              for (Location flocation : feature.getLocations().getLocations()) {
                if (flocation.isComplement()) {
                  featurelocationComplement = true;
                  break;
                }
              }

              if (anticodonSeq != null && !Arrays.equals(anticodonSeq, sequenceString)) {
                reportError(
                    qualifier.getOrigin(),
                    ANTICODON_SEQUENCE_MESSAGE_ID,
                    antiCodon.getSequence(),
                    new String(sequenceString));
              } else {
                if (anticodonSeq == null) {
                  anticodonSeq = sequenceString;
                }

                if (Arrays.equals(anticodonSeq, "tca".getBytes())
                    && antiCodon
                        .getAminoAcid()
                        .getAbbreviation()
                        .equals("Sec")) // DO not check any translation
                {
                  return result;
                }
              }

              if ((!featurelocationComplement && !anticodonLocationComplement)
                  || (featurelocationComplement && anticodonLocationComplement)) {
                sequenceString = segment.getReverseComplementSequenceByte();
                trans.translateCodons(sequenceString, translatorResult);

              } else {
                sequenceString = segment.getSequenceByte();
                trans.translateCodons(sequenceString, translatorResult);
              }

              Vector<Codon> codons = translatorResult.getCodons();
              char resultaa = 0;
              for (Codon codon : codons) {
                resultaa = codon.getAminoAcid();
              }

              if (antiCodon.getAminoAcid().getLetter() != resultaa) {
                reportError(
                    qualifier.getOrigin(),
                    ANTICODON_TRANSLATION_MESSAGE_ID,
                    antiCodon.getAminoAcid().getAbbreviation(),
                    new String(segment.getSequenceByte()),
                    trans.getTranslationTable().getNumber());
              }
            }
          }
        }
      }
    } catch (ValidationException e) {
      reportException(result, e, entry, anticodonFeature);
    } catch (Exception e) {
      throw new ValidationEngineException(e);
    }
    return result;
  }

  private void reportException(
      ValidationResult result, ValidationException exception, Entry entry, Feature feature) {
    ValidationMessage<Origin> message = exception.getValidationMessage();
    message.append(feature.getOrigin());
    message.append(entry.getOrigin());
    result.append(message);
  }
}
