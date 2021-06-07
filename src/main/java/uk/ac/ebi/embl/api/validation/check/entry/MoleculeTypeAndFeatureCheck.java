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
package uk.ac.ebi.embl.api.validation.check.entry;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.storage.DataRow;
import uk.ac.ebi.embl.api.storage.DataSet;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.annotation.ExcludeScope;

@Description("Feature {0} is required when molecule type is {1}. \"Join\" locations are only permitted in CDS features on mRNA entries when \"ribosomal_slippage\" or \"exception\" qualifiers are present.")
@ExcludeScope(validationScope = {ValidationScope.NCBI, ValidationScope.NCBI_MASTER})
public class MoleculeTypeAndFeatureCheck extends EntryValidationCheck {


    /**
     * whether this is a 'new' entry in the database - default to true until we have a system for
     * checking this
     *
     */
    private boolean isNewEntry = true;
	
    private final static String MESSAGE_ID = "MoleculeTypeAndFeatureCheck-1";
	private final static String MRNA_CDS_ID = "MoleculeTypeAndFeatureCheck-2";
	private final static String MRNA_CDS_LOCATION_ID = "MoleculeTypeAndFeatureCheck-3";

	public MoleculeTypeAndFeatureCheck() {
	}

    public void setNewEntry(boolean newEntry) {
        isNewEntry = newEntry;
    }

    public ValidationResult check(Entry entry) {
        result = new ValidationResult();
        DataSet dataSet = GlobalDataSets.getDataSet(GlobalDataSetFile.MOLTYPE_FEATURE);

        String moleculeType = SequenceEntryUtils.getMoleculeType(entry);
		if (moleculeType == null) {
			return result;
		}

        /**
         * first check that mandatory qualifiers are present for this mol type
         */
        DataRow row = dataSet.findRow(1, moleculeType);
        if (row != null) {
            String expectedFeatureName = row.getString(0);
            if (!SequenceEntryUtils.isFeatureAvailable(expectedFeatureName, entry)) {
                reportError(entry.getOrigin(), MESSAGE_ID, row.getString(1), expectedFeatureName);
            }
        }

        /**
         * ...then check if is mRNA and has CDS, if so, do more checks
         */
        if (isNewEntry) {
            if (moleculeType.equals(Sequence.MRNA_MOLTYPE) &&
                    SequenceEntryUtils.isFeatureAvailable(Feature.CDS_FEATURE_NAME, entry)) {
              
                for (Feature cdsFeature : SequenceEntryUtils.getFeatures(Feature.CDS_FEATURE_NAME, entry)) {
					if (cdsFeature.getLocations().isComplement()) {
						reportError(entry.getOrigin(), MRNA_CDS_LOCATION_ID);

					}
                    if (cdsFeature.getLocations() instanceof  Join && cdsFeature.getLocations().getLocations().size() > 1) {
                        if (!SequenceEntryUtils.isQualifierAvailable(Qualifier.RIBOSOMAL_SLIPPAGE_QUALIFIER_NAME, cdsFeature) &&
                                !SequenceEntryUtils.isQualifierAvailable(Qualifier.EXCEPTION_QUALIFIER_NAME, cdsFeature)) {
                            reportError(entry.getOrigin(), MRNA_CDS_ID);
                        }
                    }
                }
            }
        }

        return result;
	}

}
