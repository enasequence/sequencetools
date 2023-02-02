package uk.ac.ebi.embl.api.validation.plan;

import uk.ac.ebi.embl.api.validation.EmblEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.entries.NonUniqueSubmitterAccessionCheck;
import uk.ac.ebi.embl.api.validation.check.entry.*;
import uk.ac.ebi.embl.api.validation.check.feature.*;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListChromosomeLocationCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListChromosomeNameCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListChromosomeTypeCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.ChromosomeListObjectNameCheck;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceExistsCheck;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceLengthCheck;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceToGapFeatureBasesCheck;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.*;
import uk.ac.ebi.embl.api.validation.fixer.entry.*;
import uk.ac.ebi.embl.api.validation.fixer.feature.*;
import uk.ac.ebi.embl.api.validation.fixer.sequence.ContigstosequenceFix;
import uk.ac.ebi.embl.api.validation.fixer.sequence.Mol_typeFix;
import uk.ac.ebi.embl.api.validation.fixer.sequence.SequenceBasesFix;
import uk.ac.ebi.embl.api.validation.fixer.sequence.SequenceToGapFeatureBasesFix;
import uk.ac.ebi.embl.api.validation.fixer.sourcefeature.*;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public enum ValidationUnit
	{
		SOURCE_FEATURE_CHECKS
				(
						OrganismAndRequiredQualifierCheck.class,
						SourceFeatureOnlyCheck.class,
						SourceFeatureQualifierCheck.class,
						Type_materialQualifierCheck.class,
						CollectionDateQualifierCheck.class,
						MoleculeTypeAndOrganismCheck.class,
						MoleculeTypeAndSourceQualifierCheck.class,
						SequenceCoverageCheck.class,
						QualifierCheck.class,
						ExclusiveQualifiersCheck.class,
						QualifierAndRequiredQualifierinFeatureCheck.class,
						QualifierPatternAndQualifierCheck.class,
						QualifierValueNotQualifierCheck.class,
						QualifierValueNotQualifierPatternCheck.class,
						QualifierValueRequiredQualifierStartsWithValueCheck.class,
						UnbalancedParenthesesCheck.class,
						ChromosomeSourceQualifierCheck.class
				),
		SOURCE_FEATURE_FIXES
				(
						CollectionDateQualifierFix.class,
						Isolation_sourceQualifierFix.class,
						HostQualifierFix.class,
						SourceQualifierMissingFix.class,
						SourceQualifierFix.class,
						StrainQualifierValueFix.class,
						Lat_lonValueFix.class,
						CountryQualifierFix.class,
						MoleculeTypeAndQualifierFix.class,
						ExclusiveQualifierTransformToNoteQualifierFix.class,
						FeatureQualifierRenameFix.class,
						TaxonomicDivisionNotQualifierFix.class,
						QualifierWithinQualifierFix.class,
						Mol_typeFix.class,
						DescriptionCheck.class
				),
	        SEQUENCE_ENTRY_CHECKS
	        (
	        		NonUniqueSubmitterAccessionCheck.class,//exclude for master and include for assemblies
					SubmitterAccessionCheck.class,//exclude for master
					AssemblySecondarySpanCheck.class,//exclude for all assemblies
					DataclassCheck.class,//exclude for assembly master 
					KWCheck.class,//exclude for assembly master
					EntryProjectIdCheck.class,//exclude for all assemblies
					HoldDateCheck.class,//include for all
					//ReferenceCheck.class,
					MasterEntrySourceCheck.class,//include only for assembly master
					HostQualifierCheck.class,//include for all
					OrganismAndRequiredQualifierCheck.class, //include for all
					SourceFeatureOnlyCheck.class,//include for all
					SourceFeatureQualifierCheck.class,//include for all
					Type_materialQualifierCheck.class,//include for all
					CollectionDateQualifierCheck.class,//include for all
					MoleculeTypeAndOrganismCheck.class,//exclude for assembly master
					MoleculeTypeAndSourceQualifierCheck.class,//exclude for assembly master
					SequenceCoverageCheck.class,//exclude for assembly master
					QualifierCheck.class, //include for all
					GapFeatureBasesCheck.class,//include for all
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
					PseudogeneValueCheck.class,
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
					UnbalancedParenthesesCheck.class,//include for all
					Assembly_gapFeatureCheck.class,//exclude for master
					CitationQualifierCheck.class,//include for all
					PropeptideLocationCheck.class,//exclude for master
					//FeatureAndSourceQualifierCheck.class, //this check no longer exists
					OrganismAndPermittedQualifierCheck.class,//include for all
					SourceQualifierPatternAndFeatureCheck.class,//include for all
					TaxonomicDivisionQualifierCheck.class,//include for all
					AntiCodonTranslationCheck.class,//exclude for master
					GapFeatureBasesCheck.class,//exclude for master
					GapFeatureLocationsCheck.class,//exclude for master
					MoleculeTypeAndFeatureCheck.class,//include for all
					PeptideFeatureCheck.class,//exclude for master
					CdsFeatureTranslationCheck.class,//exclude for master
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
	        		LocustagExistsCheck.class,//exclude for master
	        		AssemblyLevelDataclassCheck.class,//exclude for master
	        		AssemblyTopologyCheck.class,//exclude for master
	        		ChromosomeSourceQualifierCheck.class,//exclude for master
	        		EC_numberFormatCheck.class,//include for all
					ScaffoldComponentCheck.class, // only for scaffolds
					CircularRNAQualifierCheck.class
	        ),
	        SEQUENCE_ENTRY_FIXES
	        (
	        		HoldDateFix.class,//include for all
					ReferencePositionFix.class,//include for all
					DataclassFix.class,//include for all
					DivisionFix.class, //include for all
					TPA_dataclass_Fix.class,//exclude for master
					AccessionFix.class,//exclude for master
					NonAsciiCharacterFix.class,//include for all
					CollectionDateQualifierFix.class,//include for all
					Isolation_sourceQualifierFix.class,//include for all
					HostQualifierFix.class,//include for all
					SourceQualifierMissingFix.class,//include for all
					SourceQualifierFix.class,
					StrainQualifierValueFix.class,//include for all
					Lat_lonValueFix.class,//include for all
					CountryQualifierFix.class,//include for all
					MoleculeTypeAndQualifierFix.class,//include for all
					CDS_RNA_LocusFix.class,//exclude for master
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
			   		TaxonomicDivisionNotQualifierFix.class,//include for all
					AnticodonQualifierFix.class,//exclude for master
	    			AgptoConFix.class,//excude for master
	    	        ContigstosequenceFix.class,//exclude for master
					SequenceBasesFix.class,//exclude for master
					Mol_typeFix.class,//exclude for master
					SequenceToGapFeatureBasesFix.class,//exclude for master
	    		    AssemblyTopologyFix.class,//exclude for master
					//AssemblyLevelSubmitterReferenceFix.class,//exclude for master
	    		    JournalFix.class,//include for all
	    		    AssemblyLevelEntryNameFix.class,
	    		    GaptoAssemblyGapFeatureFix.class,//exclude for master
					Linkage_evidenceFix.class,//exclude for master
					DescriptionCheck.class,//only for template submissions
					QualifierRemovalFix.class 
	        ),
			CHROMOSOME_LIST_CHECKS
			(
				ChromosomeListChromosomeLocationCheck.class,
				ChromosomeListChromosomeNameCheck.class,
				ChromosomeListChromosomeTypeCheck.class,
				ChromosomeListObjectNameCheck.class
			),
			UNLOCALISED_LIST_CHECKS
			(
				//UnlocalisedListChromosomeValidationCheck.class,
				//UnlocalisedListObjectNameValidationCheck.class
			);
				
			
			
	
	List<Class<? extends EmblEntryValidationCheck<?>>> checks;

	ValidationUnit(Class<? extends EmblEntryValidationCheck<?>>... checks)
	{
		this.checks = Arrays.asList(checks);
	}

	public List<Class<? extends EmblEntryValidationCheck<?>>> getValidationUnit()
	{
		return this.checks;
	}
}
