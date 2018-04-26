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
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoAnalysisIdCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoCoverageCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoDuplicationCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoMinGapLengthCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoNameCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoPlatformCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoProgramCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoProjectIdheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoSamplewithDifferentProjectCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.AssemblyInfoSubmissionIdCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListAnalysisIdCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListChromosomeLocationCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListChromosomeNameCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListChromosomeTypeCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListObjectNameCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.UnlocalisedListChromosomeValidationCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.UnlocalisedListObjectNameValidationCheck;
import uk.ac.ebi.embl.api.validation.fixer.entry.*;
import uk.ac.ebi.embl.api.validation.fixer.feature.*;
import uk.ac.ebi.embl.api.validation.fixer.sequence.*;
import uk.ac.ebi.embl.api.validation.fixer.sourcefeature.*;

@SuppressWarnings("unchecked")
public enum ValidationUnit
{
	        SEQUENCE_ENTRY_CHECKS
	        (
	        		Entry_NameCheck.class,//exclude for master and include for assemblies
					AssemblySecondarySpanCheck.class,//exclude for all assemblies
					DataclassCheck.class,//exclude for assembly master 
					KWCheck.class,//exclude for assembly master
					EntryProjectIdCheck.class,//exclude for all assemblies
					HoldDateCheck.class,//include for all
					//ReferenceCheck.class,
					MasterEntryDescriptionCheck.class,//include only for assembly master
					MasterEntrySourceCheck.class,//include only for assembly master
					ExclusiveSourceQualifiersCheck.class,//include for all
					HostQualifierCheck.class,//include for all
					OrganismAndRequiredQualifierCheck.class, //include for all
					OrganismNotQualifierCheck.class,//include for all
					OrganismPatternAndQualifierValueCheck.class,//include for all
					SingleSourceQualifiersCheck.class,//include for all
					SourceFeatureOnlyCheck.class,//include for all
					SourceFeatureQualifierCheck.class,//include for all
					Type_materialQualifierCheck.class,//include for all
					CollectionDateQualifierCheck.class,//include for all
					Isolation_sourceQualifierCheck.class,//include for all
					MoleculeTypeAndOrganismCheck.class,//exclude for assembly master
					MoleculeTypeAndSourceQualifierCheck.class,//exclude for assembly master
					SequenceCoverageCheck.class,//exclude for assembly master
					QualifierCheck.class, //include for all
					GapFeatureBasesCheck.class,//include for all
					GapFeatureLocationsCheck.class,//include for all
					Assembly_gapFeatureCheck.class,//include for all
					WGSGapCheck.class,//include for all
					ExclusiveQualifiersCheck.class,//include for all
					QualifierAndRequiredQualifierinFeatureCheck.class,//include for all
					DuplicateFeatureCheck.class, //include for all
					ProteinIdExistsCheck.class,//exclude for assembly master
					EntryFeatureLocationCheck.class,//exclude for assembly master
					ExonFeaturesIntervalCheck.class,//exclude for assembly master
					FeaturewithRemoteLocationCheck.class,//exclude for assembly master
					GeneAssociationCheck.class,//exclude for assembly master
					GeneFeatureLocusTagCheck.class,//exclude for assembly master
					LocusTagAssociationCheck.class,//exclude for assembly master
					LocusTagPrefixCheck.class,//exclude for assembly master
					OperonFeatureCheck.class,//exclude for assembly master
					QualifierAndRequiredQualifierinEntryCheck.class,//include for all
					QualifierPatternAndFeatureCheck.class,//include for all
					QualifierValueNotQualifierEntryCheck.class,//include for all
					QualifierValueRequiredQualifierValueEntryCheck.class,//include for all
					AntiCodonQualifierCheck.class,//exclude for assembly master
					TranslExceptQualifierCheck.class,//exclude for assembly master
					CdsFeatureAminoAcidCheck.class,//exclude for assembly master
					DeprecatedQualifiersCheck.class,//include for all
					EC_numberandProductValueCheck.class,//exclude for assembly master
					EstimatedLengthCheck.class,//exclude for assembly master
					ExclusiveQualifiersWithSameValueCheck.class,//include for all
					FeatureKeyCheck.class,//include for all
					FeatureLengthCheck.class,//include for all
					FeatureLocationCheck.class,//include for all
					FeatureLocationTypeCheck.class, // separate the sourcefeature
													// location check
					FeatureQualifiersRequiredCheck.class,//include for all
					IntronLengthWithinCDSCheck.class,//exclude for master
					NcRNAQualifierValueAndQualifierPatternCheck.class,//exclude for master
					PCRPrimersQualifierCheck.class,//exclude for master
					QualifierCheck.class, //include for all
					QualifierPatternAndQualifierCheck.class,//include for all
					QualifierValueNotQualifierCheck.class,//include for all
					QualifierValueNotQualifierPatternCheck.class,//include for all
					QualifierValueRequiredQualifierStartsWithValueCheck.class,//include for all
					QualifierValueRequiredQualifierValueCheck.class,//include for all
					SimpleFeatureLocationCheck.class,//include for all
					UnbalancedParenthesesCheck.class,//include for all
					Assembly_gapFeatureCheck.class,//exclude for master
					WGSGapCheck.class,//exclude for master
					EC_numberCheck.class,//exclude for master
					CitationQualifierCheck.class,//include for all
					PropeptideLocationCheck.class,//exclude for master
					//FeatureAndSourceQualifierCheck.class, //this check no longer exists
					OrganismAndPermittedQualifierCheck.class,//include for all
					RRNAQualifierValueOrOrganismAndQualifierValueCheck.class,//include for all
					SourceQualifierPatternAndFeatureCheck.class,//include for all
					TaxonomicDivisionNotQualifierCheck.class,//include for all
					TaxonomicDivisionQualifierCheck.class,//include for all
					AntiCodonTranslationCheck.class,//exclude for master
					FeatureAndMoleculeTypeCheck.class,//include for all
					GapFeatureBasesCheck.class,//exclude for master
					GapFeatureLocationsCheck.class,//exclude for master
					LocusTagCoverageCheck.class,//exclude for master
					MoleculeTypeAndFeatureCheck.class,//include for all
					PeptideFeatureCheck.class,//exclude for master
					CdsFeatureTranslationCheck.class,//exclude for master
					AnnotationOnlySequenceCheck.class,//exclude for master
					EntryContigsCheck.class, // CO line property will be
											// moved to sequence //exclude for master
					EntryMolTypeCheck.class,//include for all
					SequenceBasesCheck.class,//exclude for master
					SequenceExistsCheck.class,//exclude for master
					SequenceLengthCheck.class,//exclude for master
					MoleculeTypeAndDataclassCheck.class,//exclude for master
					SequenceToGapFeatureBasesCheck.class,//exclude for master
					AGPValidationCheck.class,//exclude for master
					AsciiCharacterCheck.class,//include for all
					NCBIQualifierCheck.class,//exclude for master
					EntryNameExistsCheck.class,//exclude for master
	        		LocustagExistsCheck.class,//exclude for master
	        		AssemblyLevelDataclassCheck.class,//exclude for master
	        		AssemblyTopologyCheck.class,//exclude for master
	        		AssemblyLevelEntryNameCheck.class,//exclude for master
	        		ChromosomeSourceQualifierCheck.class//exclude for master
	        ),
	        SEQUENCE_ENTRY_FIXES
	        (
	        		HoldDateFix.class,//include for all
					ReferencePositionFix.class,//include for all
					DataclassFix.class,//include for all
					TPA_dataclass_Fix.class,//exclude for master
					SubmitterAccessionFix.class,//exclude for master
					CollectionDateQualifierFix.class,//include for all
					Isolation_sourceQualifierFix.class,//include for all
					HostQualifierFix.class,//include for all
					SourceQualifierMissingFix.class,//include for all
					StrainQualifierValueFix.class,//include for all
					Lat_lonValueFix.class,//include for all
					CountryQualifierFix.class,//include for all
					MoleculeTypeAndQualifierFix.class,//include for all
					CDS_RNA_LocusFix.class,//exclude for master
					GaptoAssemblyGapFeatureFix.class,//exclude for master
					GeneAssociatedwithFeatureFix.class,//exclude for master
					GeneAssociationFix.class,//exclude for master
					GeneSynonymFix.class,//exclude for master
					LocusTagAssociationFix.class,//exclude for master
					EC_numberfromProductValueFix.class,//exclude for master
					ExclusiveQualifierTransformToNoteQualifierFix.class,//include for all
					ExperimentQualifierFix.class,//exclude for master
					FeatureLocationFix.class,//exclude for master
					FeatureQualifierDuplicateValueFix.class,//include for all
					FeatureQualifierRenameFix.class,//include for all
					FeatureRenameFix.class,//exclude for master
					ObsoleteFeaturetoNewFeatureFix.class,//exclude for master
					Linkage_evidenceFix.class,//exclude for master
					ObsoleteFeatureFix.class,//exclude for master
					QualifierValueFix.class,//include for all
					EC_numberValueFix.class,//exclude for master
					QualifierWithinQualifierFix.class,//include for all
					Transl_exceptLocationFix.class,//exclude for master
					ProteinIdRemovalFix.class,//exclude for master
					LocusTagValueFix.class,//exclude for master
					Linkage_evidenceFix.class,//exclude for master
			   		TaxonomicDivisionNotQualifierFix.class,//include for all
					AnticodonQualifierFix.class,//exclude for master
				    AgpComponentAccessionFix.class,//exclude for master
	    			AgptoConFix.class,//excude for master
	    	        ContigstosequenceFix.class,//exclude for master
	                AnnotationOnlySequenceFix.class,//exclude for master
					SequenceBasesFix.class,//exclude for master
					Mol_typeFix.class,//exclude for master
					SequenceToGapFeatureBasesFix.class,//exclude for master
	    		    AssemblyTopologyFix.class,//exclude for master
					AssemblySourceQualiferFix.class,//exclude for master
					AssemblyLevelSubmitterReferenceFix.class,//exclude for master
					AssemblyFeatureRemoteLocationFix.class,//exclude for master
	    		    JournalFix.class//include for all
	        ),
	
	// GCS entry validation checks
			ASSEMBLYINFO_CHECKS
			(
			AssemblyInfoAnalysisIdCheck.class,
			AssemblyInfoCoverageCheck.class,
			AssemblyInfoDuplicationCheck.class,
			AssemblyInfoMinGapLengthCheck.class,
			AssemblyInfoNameCheck.class,
			AssemblyInfoPlatformCheck.class,
			AssemblyInfoProgramCheck.class,
			AssemblyInfoProjectIdheck.class,
			AssemblyInfoSamplewithDifferentProjectCheck.class,
			AssemblyInfoSubmissionIdCheck.class
			),
			CHROMOSOME_LIST_CHECKS
			(
				ChromosomeListAnalysisIdCheck.class,
				ChromosomeListChromosomeLocationCheck.class,
				ChromosomeListChromosomeNameCheck.class,
				ChromosomeListChromosomeTypeCheck.class,
				ChromosomeListObjectNameCheck.class
			),
			UNLOCALISED_LIST_CHECKS
			(
				UnlocalisedListChromosomeValidationCheck.class,
				UnlocalisedListObjectNameValidationCheck.class
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
