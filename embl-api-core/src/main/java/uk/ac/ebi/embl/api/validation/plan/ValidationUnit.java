package uk.ac.ebi.embl.api.validation.plan;

import java.util.Arrays;
import java.util.List;

import uk.ac.ebi.embl.api.validation.EmblEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.entries.Entry_NameCheck;
import uk.ac.ebi.embl.api.validation.check.entry.*;
import uk.ac.ebi.embl.api.validation.check.entry.SequenceBasesCheck;
import uk.ac.ebi.embl.api.validation.check.sequence.*;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.*;
import uk.ac.ebi.embl.api.validation.check.feature.*;
import uk.ac.ebi.embl.api.validation.fixer.entry.*;
import uk.ac.ebi.embl.api.validation.fixer.feature.*;
import uk.ac.ebi.embl.api.validation.fixer.sequence.*;
import uk.ac.ebi.embl.api.validation.fixer.sourcefeature.*;

@SuppressWarnings("unchecked")
public enum ValidationUnit
{
			ENTRY_SPECIFIC_HEADER_ONLY_CHECKS
			(
				Entry_NameCheck.class,
				AssemblySecondarySpanCheck.class,
				DataclassCheck.class,
				KWCheck.class
			),
			MASTER_HEADER_ONLY_CHECKS(
					CitationExistsCheck.class,
					EntryProjectIdCheck.class,
					HoldDateCheck.class,
					ReferenceCheck.class,
					MasterEntryDescriptionCheck.class
			),
			SOURCE_FEAURES_ONLY_CHECKS
			(
				MasterEntrySourceCheck.class,
				ExclusiveSourceQualifiersCheck.class,
				HostQualifierCheck.class,
				OrganismAndRequiredQualifierCheck.class,
				OrganismNotQualifierCheck.class,
				OrganismPatternAndQualifierValueCheck.class,
				SingleSourceQualifiersCheck.class,
				SourceFeatureOnlyCheck.class,
				SourceFeatureQualifierCheck.class,
				Type_materialQualifierCheck.class,
				CollectionDateQualifierCheck.class,
				Isolation_sourceQualifierCheck.class
			),
			SOURCE_DEPENDSON_SEQUENCE_CHECKS
			(
				MoleculeTypeAndOrganismCheck.class,
				MoleculeTypeAndSourceQualifierCheck.class,
				SequenceCoverageCheck.class
			),

			FASTA_AGP_FEATURE_CHECKS
			(
				QualifierCheck.class,
				GapFeatureBasesCheck.class,
				GapFeatureLocationsCheck.class,
				Assembly_gapFeatureCheck.class,
				WGSGapCheck.class
		     ),
		     
			NON_SOURCE_FEATURES_ONLY_CHECKS
			(
				DuplicateFeatureCheck.class, // need to separate source feature
												// duplication check
				ProteinIdExistsCheck.class,
				EntryFeatureLocationCheck.class,
				ExonFeaturesIntervalCheck.class,
				FeaturewithRemoteLocationCheck.class,
				GeneAssociationCheck.class,
				GeneFeatureLocusTagCheck.class,
				LocusTagAssociationCheck.class,
				LocusTagPrefixCheck.class,
				OperonFeatureCheck.class,
				QualifierAndRequiredQualifierinEntryCheck.class,
				QualifierPatternAndFeatureCheck.class,
				QualifierValueNotQualifierEntryCheck.class,
				QualifierValueRequiredQualifierValueEntryCheck.class,
				AntiCodonQualifierCheck.class,
				CdsFeatureAminoAcidCheck.class,
				DeprecatedQualifiersCheck.class,
				EC_numberandProductValueCheck.class,
				EstimatedLengthCheck.class,
				ExclusiveQualifiersCheck.class,
				ExclusiveQualifiersWithSameValueCheck.class,
				FeatureKeyCheck.class,
				FeatureLengthCheck.class,
				FeatureLocationCheck.class,
				FeatureLocationTypeCheck.class, // separate the sourcefeature
												// location check
				FeatureQualifiersRequiredCheck.class,
				IntronLengthWithinCDSCheck.class,
				NcRNAQualifierValueAndQualifierPatternCheck.class,
				PCRPrimersQualifierCheck.class,
				QualifierAndRequiredQualifierinFeatureCheck.class,
				QualifierCheck.class, // separate the source feature qualifier
										// checks
				QualifierPatternAndQualifierCheck.class,
				QualifierValueNotQualifierCheck.class,
				QualifierValueNotQualifierPatternCheck.class,
				QualifierValueRequiredQualifierStartsWithValueCheck.class,
				QualifierValueRequiredQualifierValueCheck.class,
				SimpleFeatureLocationCheck.class,
				UnbalancedParenthesesCheck.class,
				Assembly_gapFeatureCheck.class,
				WGSGapCheck.class,
				EC_numberCheck.class,
				CitationQualifierCheck.class,
				PropeptideLocationCheck.class,
				CdsFeatureCheck.class
			),
			NON_SOURCE_DEPENDSON_SOURCE_CHECKS
			(
				//FeatureAndSourceQualifierCheck.class, //this check no longer exists
				OrganismAndPermittedQualifierCheck.class,
				RRNAQualifierValueOrOrganismAndQualifierValueCheck.class,
				SourceQualifierPatternAndFeatureCheck.class,
				TaxonomicDivisionNotQualifierCheck.class,
				TaxonomicDivisionQualifierCheck.class
			),
			NON_SOURCE_DEPENDSON_SEQUENCE_CHECKS
			(
				AntiCodonTranslationCheck.class,
				FeatureAndMoleculeTypeCheck.class,
				GapFeatureBasesCheck.class,
				GapFeatureLocationsCheck.class,
				LocusTagCoverageCheck.class,
				MoleculeTypeAndFeatureCheck.class
			),
			NON_SOURCE_DEPENDSON_SEQUENCE_AND_SOURCE_CHECKS
			(
				PeptideFeatureCheck.class,
				CdsFeatureTranslationCheck.class
			),
			SEQUENCE_ONLY_CHECKS
			(
				EntryContigsCheck.class, // CO line property will be
										// moved to sequence
				EntryMolTypeCheck.class,
				SequenceBasesCheck.class,
				SequenceExistsCheck.class,
				SequenceLengthCheck.class,
				MoleculeTypeAndDataclassCheck.class,
				SequenceToGapFeatureBasesCheck.class,
				AGPValidationCheck.class
			),
			SEQUENCE_DEPENDSON_NON_SOURCE_FEATURES_CHECKS
			(
			),

			MASTER_HEADER_ONLY_FIXES
			(
				HoldDateFix.class,
				ReferencePositionFix.class
			),
			
			ENTRY_SPECIFIC_HEADER_ONLY_FIXES
			(
				DataclassFix.class,
				TPA_dataclass_Fix.class

			),
			SOURCE_FEAURES_ONLY_FIXES
			(
				CollectionDateQualifierFix.class,
				Isolation_sourceQualifierFix.class,
				HostQualifierFix.class,
				SourceQualifierMissingFix.class,
				StrainQualifierValueFix.class
			),
			SOURCE_DEPENDSON_SEQUENCE_FIXES
			(
				MoleculeTypeAndQualifierFix.class
			),
			NON_SOURCE_FEATURES_ONLY_FIXES
			(
				CDS_RNA_LocusFix.class,
				GaptoAssemblyGapFeatureFix.class,
				GeneAssociatedwithFeatureFix.class,
				GeneAssociationFix.class,
				GeneSynonymFix.class,
				LocusTagAssociationFix.class,
				EC_numberfromProductValueFix.class,
				ExclusiveQualifierTransformToNoteQualifierFix.class,
				ExperimentQualifierFix.class,
				FeatureLocationFix.class,
				FeatureQualifierDuplicateValueFix.class,
				FeatureQualifierRenameFix.class,
				FeatureRenameFix.class,
				ObsoleteFeaturetoNewFeatureFix.class,
				Linkage_evidenceFix.class,
				ObsoleteFeatureFix.class,
				QualifierValueFix.class,
				EC_numberValueFix.class,
				QualifierWithinQualifierFix.class,
				Transl_exceptLocationFix.class,
				ProteinIdRemovalFix.class,
				LocusTagValueFix.class
			),
			
			FASTA_AGP_FEATURE_FIXES
			(
				Linkage_evidenceFix.class
		     ),
		     
			NON_SOURCE_DEPENDSON_SOURCE_FIXES
			(
				TaxonomicDivisionNotQualifierFix.class
			),
			NON_SOURCE_DEPENDSON_SEQUENCE_FIXES
			(
				AnticodonQualifierFix.class
			),
			NON_SOURCE_DEPENDSON_SEQUENCE_AND_SOURCE_FIXES
			(
			),
            AGP_SPECIFIC_FIXES
            (
    			 AgpComponentAccessionFix.class,
    			 AgptoConFix.class
    	    ),
            SEQUENCE_ONLY_FIXES
			(
                ContigstosequenceFix.class,
				SequenceBasesFix.class,
				Mol_typeFix.class,
				SequenceToGapFeatureBasesFix.class
    		),
			
			ASSEMBLY_LEVEL_FIXES
			(
				AssemblyTopologyFix.class,
				AssemblySourceQualiferFix.class,
				AssemblyLevelSequenceFix.class,
				AssemblyLevelSubmitterReferenceFix.class
    		 ),
    	    ASSEMBLY_LEVEL_CHECKS
            (
        		EntryNameExistsCheck.class,
        		LocustagExistsCheck.class,
        		AssemblyLevelDataclassCheck.class,
        		AssemblyTopologyCheck.class,
        		AssemblyLevelSequenceCheck.class,
        		AssemblyLevelEntryNameCheck.class,
        		ChromosomeSourceQualifierCheck.class
		   ),
			SEQUENCE_DEPENDSON_NON_SOURCE_FEATURES_FIXES
			(
			);
			
	
	List<Class<? extends EmblEntryValidationCheck<?>>> checks;

	ValidationUnit(Class<? extends EmblEntryValidationCheck<?>>... checks)
	{
		this.checks = Arrays.asList(checks);
	}

	List<Class<? extends EmblEntryValidationCheck<?>>> getValidationUnit()
	{
		return this.checks;
	}
}
