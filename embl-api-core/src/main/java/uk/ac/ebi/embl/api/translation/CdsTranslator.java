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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.feature.CdsFeature;
import uk.ac.ebi.embl.api.entry.feature.PeptideFeature;
import uk.ac.ebi.embl.api.entry.qualifier.CodonQualifier;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.qualifier.TranslExceptQualifier;
import uk.ac.ebi.embl.api.entry.sequence.Segment;
import uk.ac.ebi.embl.api.entry.sequence.SegmentFactory;
import uk.ac.ebi.embl.api.validation.EntryValidations;
import uk.ac.ebi.embl.api.validation.ExtendedResult;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationException;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.helper.taxon.TaxonHelper;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureAminoAcidCheck;
import uk.ac.ebi.embl.api.RepositoryException;
import uk.ac.ebi.ena.taxonomy.taxon.Taxon;

public class CdsTranslator {

	private Severity severity=null;
	private  boolean acceptTranslation=false;
    private Translator translator;
    private EmblEntryValidationPlanProperty planProperty;

    public CdsTranslator(EmblEntryValidationPlanProperty planProperty) {
        this.planProperty=planProperty;
            }
    public CdsTranslator(EmblEntryValidationPlanProperty planProperty, Translator translator) {
        this.planProperty=planProperty;
        this.translator = translator;
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

        createTranslator(cds, entry, validationResult);

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


        if (extendedTranslatorResult.count(Severity.ERROR) > 0) {
            return extendedTranslatorResult;
        }

		// Note that if the compound location is a global complement
		// then the left and right partiality are reversed between CdsFeature
		// and Translator.

        boolean fixedLeftPartial = translator.isLeftPartial();
		boolean fixedRightPartial = translator.isRightPartial();
		if (isSwapPartiality(fixedLeftPartial, fixedRightPartial, cds.getLocations().isComplement())) {
			fixedLeftPartial = !fixedLeftPartial;
			fixedRightPartial = !fixedRightPartial;
		}
		if (translationResult.isFixedLeftPartial()) {
			cds.getLocations().setLeftPartial(fixedLeftPartial);
		}
		if (translationResult.isFixedRightPartial()) {
			cds.getLocations().setRightPartial(fixedRightPartial);
		}

		if(translationResult.isFixedPseudo()) {
		    cds.addQualifier(new QualifierFactory().createQualifier(Qualifier.PSEUDO_QUALIFIER_NAME));
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

		Set<String> fixes = translator.getFixes();
		if(fixes != null && !fixes.isEmpty()) {
		    fixes.forEach(f -> extendedTranslatorResult.append(EntryValidations.createMessage(cds.getOrigin(), Severity.FIX, f)));
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

	private void createTranslator(CdsFeature cds, Entry entry, ExtendedResult<TranslationResult> validationResult) {
        translator = new Translator();
        if (planProperty.isFixMode.get()) {
            //translator.setFixDegenarateStartCodon(true);//Not implemented
            translator.setFixNoStartCodonMake5Partial(true);
            translator.setFixCodonStartNotOneMake5Partial(true);
            translator.setFixNoStopCodonMake3Partial(true);
            //translator.setFixDeleteTrailingBasesAfterStopCodon(true); //Not implemented
            translator.setFixValidStopCodonRemove3Partial(true);
            translator.setFixNonMultipleOfThreeMake3And5Partial(true);
            translator.setFixInternalStopCodonMakePseudo(true);
        }

        /**
         * set up the translator with the details from the cds feature
         */
        try {
            validationResult.append(configureFromFeature(cds, planProperty.taxonHelper.get(), entry));
        } catch (ValidationException e) {
            reportException(
                    validationResult,
                    e,
                    entry,
                    cds);
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


    public ValidationResult configureFromFeature(CdsFeature feature,
                                                 TaxonHelper taxHelper, Entry entry) throws ValidationException
    {
        ValidationResult validationResult = new ValidationResult();
        Integer featureTranslationTable = null;
        if (feature != null)
            featureTranslationTable = feature.getTranslationTable();

        Integer translationTable = null;
        ValidationMessage<Origin> infoMessage = null;
        translator.setPeptideFeature(feature instanceof PeptideFeature);
        SourceFeature sourceFeature = entry.getPrimarySourceFeature();
        if (sourceFeature != null)
        {
            Taxon taxon = null;
            if (sourceFeature.getTaxon().getTaxId() != null)
            {
                taxon = taxHelper.getTaxonById(sourceFeature.getTaxon()
                        .getTaxId());
            } else if (sourceFeature.getTaxon().getScientificName() != null)
            {
                taxon = taxHelper.getTaxonByScientificName(sourceFeature
                        .getTaxon().getScientificName());
            }

            // Classified organism

            if (taxon != null)
            {
                String organelle = sourceFeature
                        .getSingleQualifierValue("organelle");
                if (StringUtils.equals(organelle, "mitochondrion")
                        || StringUtils.equals(organelle,
                        "mitochondrion:kinetoplast"))
                {
                    translationTable = taxon.getMitochondrialGeneticCode();
                    infoMessage = EntryValidations.createMessage(
                            entry.getOrigin(), Severity.INFO,
                            "CDSTranslator-12", organelle, translationTable);
                } else if (StringUtils.contains(organelle, "plastid"))
                {
                    translationTable = TranslationTable.PLASTID_TRANSLATION_TABLE;
                    infoMessage = EntryValidations.createMessage(
                            entry.getOrigin(), Severity.INFO,
                            "CDSTranslator-12", organelle, translationTable);
                } else
                {
                    translationTable = taxon.getGeneticCode();
                    infoMessage = EntryValidations.createMessage(
                            entry.getOrigin(), Severity.INFO,
                            "CDSTranslator-11", translationTable);
                }
            }

            else
            {
                if (featureTranslationTable != null)
                {
                    translationTable = featureTranslationTable;
                    infoMessage = EntryValidations.createMessage(
                            entry.getOrigin(), Severity.INFO,
                            "CDSTranslator-13", translationTable);
                } else
                {
                    String organelle = sourceFeature
                            .getSingleQualifierValue("organelle");
                    if (StringUtils.equals(organelle, "mitochondrion"))
                    {
                        translationTable = 2;
                        infoMessage = EntryValidations
                                .createMessage(entry.getOrigin(),
                                        Severity.INFO, "CDSTranslator-14",
                                        organelle, translationTable);
                    } else if (StringUtils.equals(organelle,
                            "mitochondrion:kinetoplast"))
                    {
                        translationTable = 4;
                        infoMessage = EntryValidations
                                .createMessage(entry.getOrigin(),
                                        Severity.INFO, "CDSTranslator-14",
                                        organelle, translationTable);
                    } else if (StringUtils.contains(organelle, "plastid"))
                    {
                        translationTable = TranslationTable.PLASTID_TRANSLATION_TABLE;
                        infoMessage = EntryValidations
                                .createMessage(entry.getOrigin(),
                                        Severity.INFO, "CDSTranslator-14",
                                        organelle, translationTable);
                    } else
                    {
                        translationTable = TranslationTable.DEFAULT_TRANSLATION_TABLE;
                        infoMessage = EntryValidations.createMessage(
                                entry.getOrigin(), Severity.INFO,
                                "CDSTranslator-15", translationTable);
                    }

                }
            }
        }

        if (featureTranslationTable != null
                && !featureTranslationTable.equals(translationTable))
        {
            validationResult.append(EntryValidations.createMessage(
                    entry.getOrigin(), Severity.ERROR, "CDSTranslator-10",
                    featureTranslationTable, translationTable));
            translationTable = featureTranslationTable;
        } else if (infoMessage != null)
        {
            validationResult.append(infoMessage);

        }
        if (translationTable == null)
        {
            if (feature != null)// CDSfeature
                validationResult.append(EntryValidations.createMessage(
                        feature.getOrigin(), Severity.INFO, "CDSTranslator-8",
                        TranslationTable.DEFAULT_TRANSLATION_TABLE));
            else
                // non-cds feature
                validationResult.append(EntryValidations.createMessage(
                        entry.getOrigin(), Severity.INFO, "CDSTranslator-8"));
            // Get the default translation table.
            translationTable = TranslationTable.DEFAULT_TRANSLATION_TABLE;
        }
        translator.setTranslationTable(translationTable);
        if (feature == null) // if it is not CDSfeature ex:tRNA
            return validationResult;
        if (!translationTable
                .equals(TranslationTable.DEFAULT_TRANSLATION_TABLE))
        {
            // Set feature translation table.
            if (!translator.isPeptideFeature())
                feature.setTranslationTable(translationTable);
        }

        // Set the start codon.
        Integer startCodon = feature.getStartCodon();
        if (startCodon != null)
        {
            translator.setCodonStart(startCodon);
        }


        CompoundLocation compoundLocation = feature.getLocations();

        // Note that if the compound location is a global complement
        // then the left and right partiality are reversed between CdsFeature
        // and Translator.
        if (isSwapPartiality(compoundLocation.isLeftPartial(), compoundLocation.isRightPartial(), compoundLocation.isComplement())) {
            translator.setLeftPartial(compoundLocation.isRightPartial());
            translator.setRightPartial(compoundLocation.isLeftPartial());
        } else {
            translator.setLeftPartial(compoundLocation.isLeftPartial());
            translator.setRightPartial(compoundLocation.isRightPartial());
        }

        // Set a pseudo translation.
        translator.setNonTranslating(feature.isPseudo());
        // Set an exceptional translation.
        translator.setException(feature.isException());

        // Set translation exceptions.
        List<TranslExceptQualifier> translExceptQualifiers = new ArrayList<TranslExceptQualifier>();
        feature.getComplexQualifiers(Qualifier.TRANSL_EXCEPT_QUALIFIER_NAME,
                translExceptQualifiers);

        for (TranslExceptQualifier qualifier : translExceptQualifiers)
        {
            Long beginPosition = qualifier.getLocations().getMinPosition();
            Long endPosition = qualifier.getLocations().getMaxPosition();

            Integer relativeBeginPos = feature.getLocations()
                    .getRelativeIntPosition(beginPosition);
            Integer relativeEndPos = feature.getLocations()
                    .getRelativeIntPosition(endPosition);
            if (relativeBeginPos != null && relativeEndPos != null)// EMD-5594
            {
                if (beginPosition < endPosition
                        && relativeBeginPos > relativeEndPos)
                {
                    Integer temp = relativeBeginPos;
                    relativeBeginPos = relativeEndPos;
                    relativeEndPos = temp;
                }
            }
            if (relativeBeginPos == null)
            {
                validationResult.append(EntryValidations.createMessage(
                        feature.getOrigin(), Severity.ERROR, "CDSTranslator-6",
                        beginPosition.toString()));
            } else if (relativeEndPos == null)
            {
                validationResult.append(EntryValidations.createMessage(
                        feature.getOrigin(), Severity.ERROR, "CDSTranslator-7",
                        endPosition.toString()));
            } else
            {
                translator.addTranslationException(relativeBeginPos, relativeEndPos,
                        qualifier.getAminoAcid().getLetter());
            }
        }

        // Set codon exceptions.
        List<CodonQualifier> codonQualifiers = new ArrayList<CodonQualifier>();
        feature.getComplexQualifiers(Qualifier.CODON_QUALIFIER_NAME,
                codonQualifiers);
        for (CodonQualifier qualifier : codonQualifiers)
        {
            translator.addCodonException(qualifier.getCodon(), qualifier.getAminoAcid()
                    .getLetter());
        }

        return validationResult;
    }

    public void setLeftPartial(boolean partial) {
        translator.setLeftPartial(partial);
    }

    public void translateCodons(byte[] sequenceString, TranslationResult translatorResult) throws ValidationException {
        translator.translateCodons(sequenceString, translatorResult);
    }

    public ExtendedResult<TranslationResult> translate(byte[] sequence, Origin origin) {
        return translator.translate(sequence, origin);
    }

    private boolean isSwapPartiality(boolean isLeftpartial, boolean isRightPartial, boolean isComplement) {
        return isComplement && isLeftpartial != isRightPartial;
    }
}
