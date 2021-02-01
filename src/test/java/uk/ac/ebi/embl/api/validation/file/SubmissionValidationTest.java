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
package uk.ac.ebi.embl.api.validation.file;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.check.file.MasterEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparator;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorOptions;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile.FileType;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

public abstract class SubmissionValidationTest {

	protected SubmissionOptions options =null;

	public SubmissionValidationTest() {
		ValidationMessageManager.addBundle(ValidationMessageManager.GENOMEASSEMBLY_VALIDATION_BUNDLE);	
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);		
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
	}
	protected  SubmissionFile 
	initSubmissionTestFile(String fileName,FileType fileType)
	{
		URL url = SubmissionValidationTest.class.getClassLoader().getResource( "uk/ac/ebi/embl/api/validation/file/"+ fileName);
		if (url != null)
		{
			fileName = url.getPath().replaceAll("%20", " ");
		}
		SubmissionFile file = new SubmissionFile(fileType, new File(fileName));
		return file;
	}
	
	protected  File 
	initFile(String fileName)
	{
		URL url = SubmissionValidationTest.class.getClassLoader().getResource( "uk/ac/ebi/embl/api/validation/file/"+ fileName);
		if (url != null)
		{
			fileName = url.getPath().replaceAll("%20", " ");
		}
		
		return new File(fileName);
	}
	
	protected  SubmissionFile 
	initSubmissionFixedTestFile(String fileName,FileType fileType)
	{
		URL url = SubmissionValidationTest.class.getClassLoader().getResource( "uk/ac/ebi/embl/api/validation/file/"+ fileName);
		if (url != null)
		{
			fileName = url.getPath().replaceAll("%20", " ");
		}
		SubmissionFile file = new SubmissionFile(fileType, new File(fileName),new File(fileName+".fixed"));
		return file;
	}
	
	protected  SubmissionFile 
	initSubmissionFixedSequenceTestFile(String fileName,FileType fileType)
	{
		URL url = SubmissionValidationTest.class.getClassLoader().getResource( "uk/ac/ebi/embl/api/validation/file/"+ fileName);
		if (url != null)
		{
			fileName = url.getPath().replaceAll("%20", " ");
		}
		SubmissionFile file = new SubmissionFile(fileType, new File(fileName),new File(fileName+".sequence"));
		return file;
	}
	
	protected SourceFeature getSource()
	{
		SourceFeature source = new FeatureFactory().createSourceFeature();
		source.addQualifier(Qualifier.ORGANISM_QUALIFIER_NAME,"Micrococcus sp. 5");
		source.addQualifier(Qualifier.STRAIN_QUALIFIER_NAME,"PR1");
		return source;
	}
	
	protected AssemblyInfoEntry getAssemblyinfoEntry()
	{
		AssemblyInfoEntry infoEntry= new AssemblyInfoEntry();
		infoEntry.setMinGapLength(3);
		infoEntry.setProjectId("PRJEB0");
		infoEntry.setBiosampleId("SMEA091");
		infoEntry.setName("assembly");
		infoEntry.setProgram("sdfsfg");
		infoEntry.setPlatform("sdfsgf");
		return infoEntry;
	}
	
	protected void validateMaster(Context context) throws ValidationEngineException
	{
		SubmissionOptions options = new SubmissionOptions();
		
		options.assemblyInfoEntry = Optional.of(getAssemblyinfoEntry());
		options.source = Optional.of(getSource());
		options.isWebinCLI =true;
		options.context =Optional.of(context);
		options.getEntryValidationPlanProperty().validationScope.set(ValidationScope.ASSEMBLY_MASTER);
		MasterEntryValidationCheck check = new MasterEntryValidationCheck(options);
		check.check();
    }
	
	protected boolean compareOutputFixedFiles(File file) throws FlatFileComparatorException
	{
		FlatFileComparatorOptions options=new FlatFileComparatorOptions();
		FlatFileComparator comparator=new FlatFileComparator(options);
		return comparator.compare(file.getAbsolutePath()+".expected", file.getAbsolutePath()+".fixed");
	}
	protected boolean compareOutputSequenceFiles(File file) throws FlatFileComparatorException
	{
		FlatFileComparatorOptions options=new FlatFileComparatorOptions();
		FlatFileComparator comparator=new FlatFileComparator(options);
		return comparator.compare(file.getAbsolutePath()+".sequence.expected", file.getAbsolutePath()+".sequence");
	}
	
}
