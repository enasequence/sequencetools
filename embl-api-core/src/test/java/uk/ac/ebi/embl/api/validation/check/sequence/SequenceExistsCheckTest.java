package uk.ac.ebi.embl.api.validation.check.sequence;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.check.sequence.SequenceExistsCheck;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class SequenceExistsCheckTest
{
	private Entry entry;
	private EntryFactory entryFactory;
	private LocationFactory locationFactory;
	private SequenceExistsCheck check;
	private EmblEntryValidationPlanProperty property;
	
	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		entryFactory = new EntryFactory();
		locationFactory = new LocationFactory();
		entry = entryFactory.createEntry();
		entry.setDataClass(Entry.STANDARD_DATACLASS);
		property=new EmblEntryValidationPlanProperty();
		check = new SequenceExistsCheck();
        check.setEmblEntryValidationPlanProperty(property);
	}
	
	@Test
	public void testCheck_NoEntry()
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_NoSequence()
	{
		assertTrue(!check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_scope() throws SQLException
	{
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check.setEmblEntryValidationPlanProperty(property);
		assertTrue(!check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_Coline() throws SQLException
	{
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check.setEmblEntryValidationPlanProperty(property);	
		Collection<Location> locations = new ArrayList<Location>();
		locations.add(locationFactory.createLocalRange(5l, 10l));
		SequenceFactory sequenceFactory=new SequenceFactory();
		Sequence sequence=sequenceFactory.createSequence();
		entry.setSequence(sequence);
		entry.getSequence().addContigs(locations);
		assertTrue(!check.check(entry).isValid());
	}
	
}
