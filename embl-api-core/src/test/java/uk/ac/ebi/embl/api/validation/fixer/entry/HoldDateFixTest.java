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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertTrue;

public class HoldDateFixTest
{

	private Entry entry;
	private HoldDateFix check;
	public EntryFactory entryFactory;
    private EmblEntryValidationPlanProperty property;
	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
        property=new EmblEntryValidationPlanProperty();
		entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		check = new HoldDateFix();
		property.validationScope.set(ValidationScope.ASSEMBLY_MASTER);
		check.setEmblEntryValidationPlanProperty(property);
	}

	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}

	@Test
	public void testCheck_NoHolddate()
	{
		assertTrue(check.check(entry).isValid());
	}

	@Test
	public void testCheck_pastDate()
	{
		 Calendar calendar = Calendar.getInstance();
	     calendar.set(Calendar.YEAR, 1970);
	     Date pastDate = calendar.getTime();
	     entry.setHoldDate(pastDate);
	     ValidationResult validationResult = check.check(entry);
		 assertTrue(!validationResult.getMessages(Severity.FIX).isEmpty());
		
	}

	@Test
	public void testCheck_futureDate()
	{
		 Calendar calendar = Calendar.getInstance();
	     int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
	     calendar.set(Calendar.DAY_OF_YEAR, dayOfYear + 1);
	     Date futureDate = calendar.getTime();
	     entry.setHoldDate(futureDate);
	     ValidationResult validationResult = check.check(entry);
	     assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}
	@Test
	public void testCheck_pastDate_non_assembly_scope() throws SQLException
	{
		property.validationScope.set(ValidationScope.EMBL);
		check.setEmblEntryValidationPlanProperty(property);
		 Calendar calendar = Calendar.getInstance();
	     int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
	     calendar.set(Calendar.DAY_OF_YEAR, dayOfYear + 1);
	     Date futureDate = calendar.getTime();
	     entry.setHoldDate(futureDate);
	     ValidationResult validationResult = check.check(entry);
	     assertTrue(validationResult.getMessages(Severity.FIX).isEmpty());
	}
	
	
	
}
