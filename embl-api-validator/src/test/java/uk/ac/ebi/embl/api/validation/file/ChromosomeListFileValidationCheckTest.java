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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.ChromosomeListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

@Description("")
public class ChromosomeListFileValidationCheckTest  extends SubmissionValidationTest
{

	@Before
	public void init() throws SQLException
	{   
		options = new SubmissionOptions();
		options.isRemote = true;
	}

	@Test
	public void testvalidChromosomeList() throws ValidationEngineException
	{
		validateMaster(Context.genome);
		SubmissionFile file=initSubmissionTestFile("chromosome_list.txt",SubmissionFile.FileType.CHROMOSOME_LIST);
		ChromosomeListFileValidationCheck check = new ChromosomeListFileValidationCheck(options);
		assertTrue(check.check(file));
		assertTrue(check.getChromosomeQualifeirs().size()==2);
		List<Qualifier> qualifiers =new ArrayList<Qualifier>();
		for(String key:check.getChromosomeQualifeirs().keySet())
		{
			for(Qualifier qual:check.getChromosomeQualifeirs().get(key))
			{
				qualifiers.add(qual);
			}
		}

		assertEquals("segment: II",qualifiers.get(0).getName()+": "+qualifiers.get(0).getValue());
		assertEquals("organelle: mitochondrion",qualifiers.get(1).getName()+": "+qualifiers.get(1).getValue());


	}

	@Test
	public void testInvalidChromosomeList() throws ValidationEngineException
	{
		validateMaster(Context.genome);
		SubmissionFile file=initSubmissionTestFile("invalid_chromosome_list.txt",SubmissionFile.FileType.CHROMOSOME_LIST);
		ChromosomeListFileValidationCheck check = new ChromosomeListFileValidationCheck(options);
		assertTrue(!check.check(file));
		assertTrue(check.getMessageStats().get("InvalidNoOfFields")!=null);
	}


}
