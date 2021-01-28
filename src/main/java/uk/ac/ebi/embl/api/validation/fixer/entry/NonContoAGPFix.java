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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import uk.ac.ebi.embl.api.entry.AgpRow;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.GlobalDataSets;
import uk.ac.ebi.embl.api.validation.SequenceEntryUtils;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Description("")
public class NonContoAGPFix extends EntryValidationCheck {
    public ValidationResult check(Entry entry) throws ValidationEngineException {

        if (entry == null)
            return result;
        result = new ValidationResult();
        List<AgpRow> agpRows = new ArrayList<>();
        if (entry.getSequence() != null && entry.getSequence().getSequenceByte() != null && SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry).size() != 0)//non-CON entries
        {
            List<Feature> assemblyGapFeatures = SequenceEntryUtils.getFeatures(Feature.ASSEMBLY_GAP_FEATURE_NAME, entry);
            assemblyGapFeatures = getSortedAssemblyGapFeatures(assemblyGapFeatures);

            Long prevGapEnd = 0l;
            int componentOrder = 1;
            String contig_type = Utils.getComponentTypeId(entry);
            long seqLength = entry.getSequence().getLength();

            if (assemblyGapFeatures.size() == 1) {
                Feature assemblyGapFeature = assemblyGapFeatures.get(0);
                Long gapBegin = assemblyGapFeature.getLocations().getMinPosition();
                Long gapEnd = assemblyGapFeature.getLocations().getMaxPosition();
                agpRows.add(generateComponentAgpRow(prevGapEnd, gapBegin, componentOrder++, contig_type, entry));
                agpRows.add(generateGapAgpRow(assemblyGapFeature, componentOrder++, entry));
                agpRows.add(generateComponentAgpRow(gapEnd, seqLength + 1, componentOrder++, contig_type, entry));
            } else {
                for (int i = 0; i < assemblyGapFeatures.size(); i++) {
                    Feature assemblyGapFeature = assemblyGapFeatures.get(i);
                    Long gapBegin = assemblyGapFeature.getLocations().getMinPosition();
                    Long gapEnd = assemblyGapFeature.getLocations().getMaxPosition();

                    if (i == 0) {
                        if (gapBegin != 1) {
                            agpRows.add(generateComponentAgpRow(prevGapEnd, gapBegin, componentOrder++, contig_type, entry));
                        }
                        agpRows.add(generateGapAgpRow(assemblyGapFeature, componentOrder++, entry));

                    } else if (i == (assemblyGapFeatures.size() - 1)) {
                        agpRows.add(generateComponentAgpRow(prevGapEnd, gapBegin, componentOrder++, contig_type, entry));
                        agpRows.add(generateGapAgpRow(assemblyGapFeature, componentOrder++, entry));
                        agpRows.add(generateComponentAgpRow(gapEnd, seqLength + 1, componentOrder++, contig_type, entry));
                    } else {
                        agpRows.add(generateComponentAgpRow(prevGapEnd, gapBegin, componentOrder++, contig_type, entry));
                        agpRows.add(generateGapAgpRow(assemblyGapFeature, componentOrder++, entry));

                    }
                    prevGapEnd = gapEnd;
                }
            }
        }


        entry.getSequence().addAgpRows(agpRows);

        return result;
    }

    private AgpRow generateGapAgpRow(Feature gapFeature, int order, Entry scaffoldEntry) throws ValidationEngineException {
        AgpRow agpRow = new AgpRow();
        agpRow.setObject(scaffoldEntry.getSubmitterAccession());
        long gapBegin = gapFeature.getLocations().getMinPosition();
        long gapEnd = gapFeature.getLocations().getMaxPosition();
        if (gapEnd == 0 && gapBegin != 0) {
            gapEnd = gapBegin;
        }

        List<Qualifier> gapQualifiers = gapFeature.getQualifiers();
        List<String> linkageEvidences = new ArrayList<>();

        for (int i = 0; i < gapQualifiers.size(); ++i) {
            Qualifier gapQualifier = gapQualifiers.get(i);
            if (Qualifier.LINKAGE_EVIDENCE_QUALIFIER_NAME.equals(gapQualifier.getName())) {
                linkageEvidences.add(GlobalDataSets.linkageEvidence.get(gapQualifier.getValue()));
            }
            if (Qualifier.GAP_TYPE_QUALIFIER_NAME.equals(gapQualifier.getName())) {
                if (GlobalDataSets.gapType.containsKey(gapQualifier.getValue())) {
                    agpRow.setGap_type(GlobalDataSets.gapType.get(gapQualifier.getValue()));
                } else {
                    throw new ValidationEngineException(String.format("Generating gap AGP row:Invalid gap type %s", gapQualifier.getValue()), ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
                }
            }
            if (Qualifier.ESTIMATED_LENGTH_QUALIFIER_NAME.equals(gapQualifier.getName())) {
                if ("unknown".equalsIgnoreCase(gapQualifier.getValue())) {
                    agpRow.setComponent_type_id("U");
                } else {
                    agpRow.setComponent_type_id("N");
                }
            }
        }
        agpRow.setLinkageevidence(linkageEvidences);

        agpRow.setObject_acc(scaffoldEntry.getPrimaryAccession() + "." + scaffoldEntry.getSequence().getVersion());
        agpRow.setObject(scaffoldEntry.getSubmitterAccession());
        agpRow.setObject_beg(gapBegin);
        agpRow.setObject_end(gapEnd);
        agpRow.setPart_number(order);
        agpRow.setGap_length((gapEnd - gapBegin) + 1);
        return agpRow;
    }

    private AgpRow generateComponentAgpRow(Long prevGapEnd, Long gapBegin, int order, String contig_type, Entry scaffoldEntry) {
        AgpRow agpRow = new AgpRow();
        agpRow.setObject(scaffoldEntry.getSubmitterAccession());
        agpRow.setObject_acc(scaffoldEntry.getPrimaryAccession() + "." + scaffoldEntry.getSequence().getVersion());
        agpRow.setObject(scaffoldEntry.getSubmitterAccession());
        agpRow.setObject_beg(prevGapEnd + 1);
        agpRow.setObject_end(gapBegin - 1);
        agpRow.setPart_number(order);
        agpRow.setComponent_type_id(contig_type);
        agpRow.setComponent_id(scaffoldEntry.getSubmitterAccession());
        agpRow.setComponent_acc(scaffoldEntry.getPrimaryAccession() + "." + scaffoldEntry.getSequence().getVersion());
        agpRow.setComponent_beg(prevGapEnd + 1);
        agpRow.setComponent_end(gapBegin - 1);
        agpRow.setOrientation("+");
        return agpRow;
    }

    public List<Feature> getSortedAssemblyGapFeatures(List<Feature> assemblyGapFeature) {
        Collections.sort(assemblyGapFeature, (feature1, feature2) -> (feature1.getLocations().getMinPosition() < feature2.getLocations().getMinPosition()) ? -1 : 1);
        return assemblyGapFeature;
    }
}
