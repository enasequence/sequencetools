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
import uk.ac.ebi.embl.api.validation.check.genomeassembly.GenomeAssemblyValidationCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.UnlocalisedListChromosomeValidationCheck;
import uk.ac.ebi.embl.api.validation.check.genomeassembly.UnlocalisedListObjectNameValidationCheck;
import uk.ac.ebi.embl.api.validation.fixer.entry.*;
import uk.ac.ebi.embl.api.validation.fixer.feature.*;
import uk.ac.ebi.embl.api.validation.fixer.sequence.*;
import uk.ac.ebi.embl.api.validation.fixer.sourcefeature.*;

@SuppressWarnings("unchecked")
public enum GenomeAssemblyValidationUnit
{
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
				
			
			
	
	List<Class<? extends GenomeAssemblyValidationCheck<?>>> checks;

	GenomeAssemblyValidationUnit(Class<? extends GenomeAssemblyValidationCheck<?>>... checks)
	{
		this.checks = Arrays.asList(checks);
	}

	List<Class<? extends GenomeAssemblyValidationCheck<?>>> getValidationUnit()
	{
		return this.checks;
	}
}
