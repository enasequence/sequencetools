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
package uk.ac.ebi.embl.api.translation;

import java.util.Collection;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.PeptideFeature;
import uk.ac.ebi.embl.api.entry.sequence.Segment;
import uk.ac.ebi.embl.api.entry.sequence.SegmentFactory;
import uk.ac.ebi.embl.api.validation.EntryValidations;
import uk.ac.ebi.embl.api.validation.ExtendedResult;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureAminoAcidCheck;
import uk.ac.ebi.embl.api.RepositoryException;

public class CdsTranslator {

	private Severity severity=null;
	private  boolean acceptTranslation=false;
    private Translator translator;
   	EmblEntryValidationPlanProperty planProperty;

   	private void createTranslator() {
		translator = new Translator();
		if (planProperty.isFixMode.get()) {
			translator.setFixDegenarateStartCodon(true);
			translator.setFixMissingStartCodon(true);
			translator.setFixRightPartialCodon(true);
			translator.setFixRightPartialStopCodon(true);
		}
	}
    
    public CdsTranslator(EmblEntryValidationPlanProperty planProperty) {
        this.planProperty=planProperty;
            }
	
	/**
     * returns an ExtendedValidationResult
     *
     * @param cds
     * @param entry
     * @return
     */
   
	public ExtendedResult<TranslationResult> translate(CdsFeature cds, Entry entry) throws RepositoryException {
    
        ExtendedResult<TranslationResult> validationResult = new ExtendedResult<TranslationResult>();
        if(cds == null || cds.getLocations() == null)
            return validationResult;
        
    	if((Entry.CON_DATACLASS.equals(entry.getDataClass()) && (entry.getSequence() == null || entry.getSequence().getSequenceByte() == null)) )
    		return validationResult;

        // Check translation amino acids.
		if (cds.getTranslation() != null) {
			CdsFeatureAminoAcidCheck validationCheck = new CdsFeatureAminoAcidCheck();
			validationResult.append(validationCheck.check(cds));
		}

        if(entry.getSequence() == null || entry.getSequence().getSequenceByte() == null){
            validationResult.append(EntryValidations.createMessage(cds.getOrigin(), Severity.ERROR, "CDSTranslator-5"));
        }

        if (validationResult.count(Severity.ERROR) > 0) {
			return validationResult;
		}

		// Check exceptional translation.
		if (cds.isException() && cds.getTranslation() == null) {
			// Missing exceptional translation.
            validationResult.append(EntryValidations.createMessage(cds.getOrigin(), Severity.ERROR, "CDSTranslator-1"));
        }
		if (validationResult.count(Severity.ERROR) > 0) {
			return validationResult;
		}

        createTranslator();

        /**
         * set up the translator with the details from the cds feature
         */
        try {
            validationResult.append(translator.configureFromFeature(cds, planProperty.taxonHelper.get(), entry));
        } catch (ValidationException e) {
            reportException(
                    validationResult,
                    e,
                    entry,
                    cds);
        }

        if(!checkLocations(entry.getSequence().getLength(), cds.getLocations())){
            validationResult.append(EntryValidations.createMessage(cds.getOrigin(), Severity.ERROR, "CDSTranslator-4"));
        }

		if (validationResult.count(Severity.ERROR) > 0) {
			return validationResult;
		}

		try
		{
		SegmentFactory factory=new SegmentFactory(planProperty.enproConnection.get()); 
		Segment segment = factory.createSegment(entry.getSequence(), cds.getLocations());
		if(segment==null)
		{
			return validationResult;
		}
		ExtendedResult<TranslationResult> extendedTranslatorResult = translator.translate(segment.getSequenceByte(), entry.getOrigin());
		
		for (ValidationMessage<Origin> message : extendedTranslatorResult.getMessages()) {
			message.append(cds.getOrigin());
// 			message.append(EntryOrigin.origin(entry));
		}
		extendedTranslatorResult.append(validationResult);

		TranslationResult translationResult = extendedTranslatorResult.getExtension();
		String expectedTranslation = cds.getTranslation();
		String conceptualTranslation = translationResult.getConceptualTranslation();

//        try {
//            System.out.println("table = " + cds.getTranslationTable());
//            System.out.println("sequence = " + segment.getSequence());
//            System.out.println("conceptualTranslation = " + conceptualTranslation);
//        } catch (ValidationException e) {
//            e.printStackTrace();
//        }

        if (extendedTranslatorResult.count(Severity.ERROR) > 0) {
            return extendedTranslatorResult;
        }

        // Apply partiality fixes.

		// TODO: the isReversedPartiality decision should be made
		// once and kept in a member variable to avoid risk of
		// problems.

		// Note that if the compound location is a global complement
		// and if one (but not both) ends are partial then the
		// left and right partiality is reversed between CdsFeature
		// and Translator.
		boolean isReversedPartiality =
			cds.getLocations().isComplement() &&
			cds.getLocations().isLeftPartial() !=
			cds.getLocations().isRightPartial();

        boolean fixedLeftPartial = translator.isLeftPartial();
		boolean fixedRightPartial = translator.isRightPartial();
		if (isReversedPartiality) {
			fixedLeftPartial = !fixedLeftPartial;
			fixedRightPartial = !fixedRightPartial;
		}
		if (translationResult.isFixedLeftPartial()) {
			cds.getLocations().setLeftPartial(fixedLeftPartial);
		}
		if (translationResult.isFixedRightPartial()) {
			cds.getLocations().setRightPartial(fixedRightPartial);
		}

        if (expectedTranslation == null ||
			expectedTranslation.length() == 0 ) {
        	if(!(cds instanceof PeptideFeature))
			cds.setTranslation(conceptualTranslation); // Accept the translation.
		}
		else if (acceptTranslation 
				&& !translator.equalsTranslation(expectedTranslation, conceptualTranslation)) {
			cds.setTranslation(conceptualTranslation); // Accept the translation.
			// Add warning
			extendedTranslatorResult.append(EntryValidations.createMessage(cds.getOrigin(), Severity.WARNING,
                    "CDSTranslator-2"));
		}
		else if (!cds.isException() && !cds.isPseudo() &&
                !translator.equalsTranslation(expectedTranslation, conceptualTranslation)) {
			// Reject the translation.
			extendedTranslatorResult.append(EntryValidations.createMessage(cds.getOrigin(), Severity.ERROR,
                    "CDSTranslator-2"));
		}
		else if (cds.isException() && translator.equalsTranslation(expectedTranslation, conceptualTranslation)) {
			extendedTranslatorResult.append(EntryValidations.createMessage(cds.getOrigin(), Severity.WARNING,
                    "CDSTranslator-3"));
		}
		
		if (severity != null) {
			Collection<ValidationMessage<Origin>> messages = 
				extendedTranslatorResult.getMessages();
			for (ValidationMessage<Origin> message : messages) {
				message.setSeverity(severity);
			}
		}
		
		return extendedTranslatorResult;
		
		}
		catch(Exception e)
		{
			throw new RepositoryException(e);
		}
	}

    public Translator getTranslator() {
        return translator;
    }

    /**
     * Checks that the locations are within the sequence length
     * @param length
     * @param locations
     * @return
     */
    private boolean checkLocations(long length, CompoundLocation<Location> locations) {
        for(Location location : locations.getLocations()){
        	if(location instanceof RemoteLocation)
        		continue;
            if(location.getIntBeginPosition() == null || 
                    location.getIntBeginPosition() < 0){
                return false;
            }
            if(location.getIntEndPosition() == null ||
                    location.getIntEndPosition() > length){
                return false;
            }
        }
        return true;
    }

    private void reportException(ValidationResult result,
                                 ValidationException exception,
                                 Entry entry,
                                 CdsFeature cds) {
		ValidationMessage<Origin> message = exception.getValidationMessage();
		message.append(cds.getOrigin());
		message.append(entry.getOrigin());
		result.append(message);
    }

    public Severity getSeverity() {
		return severity;
	}

	public void setSeverity(Severity severity) {
		this.severity = severity;
	}

	public boolean isAcceptTranslation() {
		return acceptTranslation;
	}

	public void setAcceptTranslation(boolean acceptTranslation) {
		this.acceptTranslation = acceptTranslation;
	}

    public TranslationTable getTranslationTable() {
        if (translator == null) {
            return null;
        } else {
            return translator.getTranslationTable();
        }
    }

}
