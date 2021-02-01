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

import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Optional;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.MasterEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparator;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorException;
import uk.ac.ebi.embl.api.validation.helper.FlatFileComparatorOptions;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblEntryWriter;

@Description("")
public class MasterEntryValidationCheckTest extends SubmissionValidationTest
{
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void init() throws SQLException
	{   
		options = new SubmissionOptions();
		options.isWebinCLI = true;
	}

	@Test
	public void  testMasterEntrywithoutAssemblyInfo() throws ValidationEngineException
	{
		options.source= Optional.of(getSource());
		MasterEntryValidationCheck check = new MasterEntryValidationCheck(options);
		thrown.expect(ValidationEngineException.class);
		thrown.expectMessage("SubmissionOption assemblyInfoEntry must be given to generate master entry");
		check.check();

	}

	@Test
	public void  testMasterEntrywithoutSource() throws ValidationEngineException
	{
		options.assemblyInfoEntry= Optional.of(getAssemblyinfoEntry());
		MasterEntryValidationCheck check = new MasterEntryValidationCheck(options);
		thrown.expect(ValidationEngineException.class);
		thrown.expectMessage("SubmissionOption source must be given to generate master entry");
		check.check();

	}

	@Test
	public void  testMasterEntryGenomecreation() throws ValidationEngineException,FlatFileComparatorException, IOException
	{
		options.assemblyInfoEntry= Optional.of(getAssemblyinfoEntry());
		options.source= Optional.of(getSource());
		options.context = Optional.of(Context.genome);
		File file=initFile("genome_master.txt.expected");
		options.processDir = Optional.of(file.getParent());
		MasterEntryValidationCheck check = new MasterEntryValidationCheck(options);
		check.check();
		Entry entry =check.getMasterEntry();
		PrintWriter writer = new PrintWriter(options.processDir.get()+File.separator+"master.dat");
		EmblEntryWriter entryWriter= new EmblEntryWriter(entry);
		entryWriter.write(writer);
		FlatFileComparatorOptions coptions = new FlatFileComparatorOptions();
		FlatFileComparator comparator = new FlatFileComparator(coptions);
		assertTrue(comparator.compare(file.getAbsolutePath(), options.processDir.get()+File.separator+"master.dat"));
	}

	@Test
	public void  testMasterEntryTranscriptomcreation() throws ValidationEngineException, FlatFileComparatorException, IOException
	{
		options.assemblyInfoEntry= Optional.of(getAssemblyinfoEntry());
		options.source= Optional.of(getSource());
		options.context = Optional.of(Context.transcriptome);
		File file=initFile("transcriptom_master.txt.expected");
		options.processDir = Optional.of(file.getParent());
		MasterEntryValidationCheck check = new MasterEntryValidationCheck(options);
		check.check();
		Entry entry =check.getMasterEntry();
		PrintWriter writer = new PrintWriter(options.processDir.get()+File.separator+"master.dat");
		EmblEntryWriter entryWriter= new EmblEntryWriter(entry);
		entryWriter.write(writer);
		FlatFileComparatorOptions coptions = new FlatFileComparatorOptions();
		FlatFileComparator comparator = new FlatFileComparator(coptions);
		assertTrue(comparator.compare(file.getAbsolutePath(), options.processDir.get()+File.separator+"master.dat"));
	}
}
