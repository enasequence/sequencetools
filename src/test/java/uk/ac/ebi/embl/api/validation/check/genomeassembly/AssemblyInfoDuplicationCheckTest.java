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
package uk.ac.ebi.embl.api.validation.check.genomeassembly;

import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AssemblyInfoDuplicationCheckTest
{
	private AssemblyInfoEntry assemblyEntry;
	private AssemblyInfoDuplicationCheck check;
	private EmblEntryValidationPlanProperty planProperty;

	@Before
	public void setUp() throws SQLException
	{
		assemblyEntry = new AssemblyInfoEntry();
		check = new AssemblyInfoDuplicationCheck();
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
		planProperty=new EmblEntryValidationPlanProperty();
		check.setEmblEntryValidationPlanProperty(planProperty);
	}

	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}
	
   @Test
   public void testCheck_noDuplication() throws SQLException, ValidationEngineException
   {    
	   assemblyEntry.setAnalysisId("ERZ0001");
	   EntryDAOUtils entryDAOUtils = createMock(EntryDAOUtils.class);
	   EraproDAOUtils eraDAOUtils = createMock(EraproDAOUtils.class);
       expect(entryDAOUtils.isAssemblyUpdateSupported(assemblyEntry.getAnalysisId())).andReturn(true);
       expect(eraDAOUtils.isAssemblyDuplicate(assemblyEntry.getAnalysisId())).andReturn(new ArrayList<String>());
	   replay(entryDAOUtils);
	   replay(eraDAOUtils);
	   check.setEntryDAOUtils(entryDAOUtils);
	   assertTrue(check.check(assemblyEntry).isValid());
   }
   
   @Test
   public void testCheck_withDuplication() throws SQLException, ValidationEngineException
   {    
	   assemblyEntry.setAnalysisId("ERZ0001");
	   EntryDAOUtils entryDAOUtils = createMock(EntryDAOUtils.class);
	   EraproDAOUtils eraDAOUtils = createMock(EraproDAOUtils.class);
	   ArrayList<String> assemblies= new ArrayList<String>();
	   assemblies.add("ERZ09878");
	   expect(entryDAOUtils.isAssemblyUpdateSupported(assemblyEntry.getAnalysisId())).andReturn(true);
       expect(eraDAOUtils.isAssemblyDuplicate(assemblyEntry.getAnalysisId())).andReturn(assemblies);
	   replay(entryDAOUtils);
	   replay(eraDAOUtils);
	   check.setEntryDAOUtils(entryDAOUtils);
	   check.setEraproDAOUtils(eraDAOUtils);
	   ValidationResult result= check.check(assemblyEntry);
	   assertEquals(1, result.count("AssemblyInfoDuplicationCheck", Severity.ERROR));
   }
   
   @Test
   public void testCheck_notUpdate() throws SQLException, ValidationEngineException
   {    
	   assemblyEntry.setAnalysisId("ERZ0001");
	   EntryDAOUtils entryDAOUtils = createMock(EntryDAOUtils.class);
	   expect(entryDAOUtils.isAssemblyUpdateSupported(assemblyEntry.getAnalysisId())).andReturn(false);
	   replay(entryDAOUtils);
	   check.setEntryDAOUtils(entryDAOUtils);
	   assertTrue(check.check(assemblyEntry).isValid());
   }
	
	
}
