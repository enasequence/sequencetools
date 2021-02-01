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

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.file.ChromosomeListFileValidationCheck;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionFile;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Description("")
public class ChromosomeListFileValidationCheckTest  extends SubmissionValidationTest
{

	@Before
	public void init()
	{   
		options = new SubmissionOptions();
		options.isWebinCLI = true;
	}

	@Test
	public void testvalidChromosomeList() throws ValidationEngineException
	{
		validateMaster(Context.genome);
		SubmissionFile file=initSubmissionTestFile("chromosome_list.txt",SubmissionFile.FileType.CHROMOSOME_LIST);
		ChromosomeListFileValidationCheck check = new ChromosomeListFileValidationCheck(options);
		assertTrue(!check.check(file).hasError());
		assertEquals(check.getChromosomeQualifeirs().size(),2);
		List<Qualifier> qualifiers =new ArrayList<>();
		for(String key:check.getChromosomeQualifeirs().keySet())
		{
			for(Qualifier qual:check.getChromosomeQualifeirs().get(key).setAndGetQualifiers(false))
			{
				qualifiers.add(qual);
			}
		}
		
		assertTrue(qualifiers.contains(new QualifierFactory().createQualifier(Qualifier.SEGMENT_QUALIFIER_NAME,"II")));
		assertTrue(qualifiers.contains(new QualifierFactory().createQualifier(Qualifier.ORGANELLE_QUALIFIER_NAME,"mitochondrion")));
	}

	@Test
	public void testInvalidChromosomeList() throws ValidationEngineException
	{
		validateMaster(Context.genome);
		SubmissionFile file=initSubmissionTestFile("invalid_chromosome_list.txt",SubmissionFile.FileType.CHROMOSOME_LIST);
		ChromosomeListFileValidationCheck check = new ChromosomeListFileValidationCheck(options);
		assertTrue(!check.check(file).hasError());
		assertTrue(check.getMessageStats().get("InvalidNoOfFields")!=null);
	}


}
