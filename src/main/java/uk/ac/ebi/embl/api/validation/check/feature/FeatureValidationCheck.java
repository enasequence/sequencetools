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
package uk.ac.ebi.embl.api.validation.check.feature;

import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;

public abstract class FeatureValidationCheck implements EmblEntryValidationCheck<Feature> {

//	private String entryId;

	protected ValidationResult result;
    private boolean isPopulated;
    private EmblEntryValidationPlanProperty property;
	private EntryDAOUtils entryDAOUtils;
	private EraproDAOUtils eraproDAOUtils;


    public FeatureValidationCheck() {
	}

//	public String getEntryId() {
//		return entryId;
//	}

//	public void setEntryId(String entryId) {
//		this.entryId = entryId;
//	}

    public boolean isPopulated() {
        return isPopulated;
    }

    public void setPopulated() {
        //override to do any processing of data post-population
        isPopulated = true;
    }

    /**
	 * Creates an error validation message for the feature and adds it to 
	 * the validation result. 
	 * 
     * @param origin the origin
     * @param messageKey a message key
     * @param params message parameters
     */
	protected ValidationMessage<Origin> reportError(Origin origin, String messageKey,
			Object... params) {
		return reportMessage(Severity.ERROR, origin, messageKey, params);
	}

    /**
	 * Creates an error validation message for the feature and adds it to 
	 * the validation result. If there are locus_tag or gene qualifiers the values of these will be added
     * to the message as a curator comment.
	 *
     * @param origin the origin
     * @param messageKey a message key
     * @param params message parameters
     */
	protected ValidationMessage<Origin> reportFeatureError(Origin origin, String messageKey, Feature feature,
			Object... params) {
        ValidationMessage<Origin> message = reportMessage(Severity.ERROR, origin, messageKey, params);

        appendLocusTadAndGeneIDToMessage(feature, message);

        return message;
    }

    /**
     * If a feature had locus_tag or gene qualifiers - appends the value of these to the message as a curator
     * comment. Useful for some submitters who want more of a handle on the origin than just a line number.
     *
     * @param feature
     * @param message
     */
    public static void appendLocusTadAndGeneIDToMessage(Feature feature, ValidationMessage<Origin> message) {
        if (SequenceEntryUtils.isQualifierAvailable(Qualifier.LOCUS_TAG_QUALIFIER_NAME, feature)) {
            Qualifier locusTag = SequenceEntryUtils.getQualifier(Qualifier.LOCUS_TAG_QUALIFIER_NAME, feature);
            if (locusTag.isValue()) {
                message.appendCuratorMessage("locus tag = " + locusTag.getValue());
            }
        }

        if (SequenceEntryUtils.isQualifierAvailable(Qualifier.GENE_QUALIFIER_NAME, feature)) {
            Qualifier geneName = SequenceEntryUtils.getQualifier(Qualifier.GENE_QUALIFIER_NAME, feature);
            if (geneName.isValue()) {
                message.appendCuratorMessage("gene = " + geneName.getValue());
            }
        }
    }

    /**
	 * Creates a warning validation message for the feature and adds it to 
	 * the validation result. 
	 * 
     * @param origin the origin
     * @param messageKey a message key
     * @param params message parameters
     */
	protected ValidationMessage<Origin> reportWarning(Origin origin, String messageKey,
			Object... params) {
		return reportMessage(Severity.WARNING, origin, messageKey, params);
	}

	/**
	 * Creates a validation message for the feature and adds it to 
	 * the validation result.
	 * 
	 * @param severity message severity
     * @param origin the origin
     * @param messageKey a message key
     * @param params message parameters
     */
	protected ValidationMessage<Origin> reportMessage(Severity severity, Origin origin,
			String messageKey, Object... params) {
        ValidationMessage<Origin> message = EntryValidations.createMessage(origin, severity, messageKey, params);
        message.getMessage();
//        System.out.println("message = " + message.getMessage());
        result.append(message);
        return message;
    }

	 @Override
		public void setEmblEntryValidationPlanProperty(
				EmblEntryValidationPlanProperty property) throws SQLException
		{
			this.property=property;
		}

		@Override
		public EmblEntryValidationPlanProperty getEmblEntryValidationPlanProperty()
		{
			return property;
		}

		@Override
		public void setEntryDAOUtils(EntryDAOUtils entryDAOUtils)
		{
			this.entryDAOUtils=entryDAOUtils;
			
		}

		@Override
		public EntryDAOUtils getEntryDAOUtils()
		{
			// TODO Auto-generated method stub
			return entryDAOUtils;
		}
	    
		@Override
		public EraproDAOUtils getEraproDAOUtils()
		{
			// TODO Auto-generated method stub
			return eraproDAOUtils;
		}
	    
		@Override
		public void setEraproDAOUtils(EraproDAOUtils eraproDAOUtils)
		{
			this.eraproDAOUtils=eraproDAOUtils;
			
		}
}
