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
package uk.ac.ebi.embl.api.validation.plan;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.validation.EmblEntryValidationCheck;
import uk.ac.ebi.embl.api.validation.ValidationCheck;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationPlanResult;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.CdsFeatureTranslationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureLocationCheck;
import uk.ac.ebi.embl.api.validation.check.feature.FeatureValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FastaFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.FlatfileFileValidationCheck;
import uk.ac.ebi.embl.api.validation.check.file.SubmissionFile;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceValidationCheck;
import uk.ac.ebi.embl.api.validation.check.sourcefeature.ChromosomeSourceQualifierCheck;
import uk.ac.ebi.embl.api.validation.fixer.entry.AssemblyLevelEntryNameFix;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SubmissionValidationPlan extends ValidationPlan
{
	public SubmissionValidationPlan(EmblEntryValidationPlanProperty property) {
		super(property);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ValidationPlanResult execute(Object target) throws ValidationEngineException {
		
		ValidationCheck check =null;
		if(target instanceof List)
		{
			for(Object o : (List)target)
			{
				if(o instanceof SubmissionFile)
				{
					switch((SubmissionFile)o)
					{
					case FASTA :
						 check = new FastaFileValidationCheck();
						 break;
					case FLATFILE:
						check = new FlatfileFileValidationCheck();
						break;
					}
				}
				
				check.check(o);
			}
		}
		return null;
	}
}
