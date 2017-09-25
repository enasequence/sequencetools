package uk.ac.ebi.embl.api.validation.check.sequence;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.api.validation.ValidationScope;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.plan.EmblEntryValidationPlanProperty;

public class AnnotationOnlySequenceCheckTest
{
	private Entry entry;
	private EntryFactory entryFactory;
	private AnnotationOnlySequenceCheck check;
	private EmblEntryValidationPlanProperty property;
	private EntryDAOUtils entryDAOUtils;
	
	@Before
	public void setUp() throws SQLException
	{
		ValidationMessageManager.addBundle(ValidationMessageManager.STANDARD_VALIDATION_BUNDLE);
		entryFactory = new EntryFactory();
		entry = entryFactory.createEntry();
		entry.setSequence(new SequenceFactory().createSequence());
		entry.setSubmitterAccession("contig1");
		property=new EmblEntryValidationPlanProperty();
		property.isAssembly.set(true);
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		property.analysis_id.set("ERZ0001");
		check = new AnnotationOnlySequenceCheck();
        check.setEmblEntryValidationPlanProperty(property);
		entryDAOUtils=createMock(EntryDAOUtils.class);
	}
	
	@Test
	public void testCheck_NoEntry() throws ValidationEngineException
	{
		assertTrue(check.check(null).isValid());
	}
	
	@Test
	public void testCheck_withSequence() throws ValidationEngineException
	{
		entry.getSequence().setSequence(ByteBuffer.wrap("aaa".getBytes()));
		assertTrue(check.check(entry).isValid());
	}
	
	
	@Test
	public void testCheck_withCOline() throws SQLException, ValidationEngineException
	{
		property.validationScope.set(ValidationScope.ASSEMBLY_CONTIG);
		check.setEmblEntryValidationPlanProperty(property);	
		Collection<Location> locations = new ArrayList<Location>();
		locations.add(new LocationFactory().createLocalRange(5l, 10l));
		entry.getSequence().addContigs(locations);
		assertTrue(check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_noSequenceExistsandNoassembly_levelExists() throws ValidationEngineException, SQLException, IOException
	{
		expect(entryDAOUtils.isAssemblyLevelExists(check.getEmblEntryValidationPlanProperty().analysis_id.get(),0)).andReturn(false);
		expect(entryDAOUtils.getPrimaryAcc(check.getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession(), 2)).andReturn("A00001");
		expect(entryDAOUtils.getSequence("A00001")).andReturn(null);	
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		assertTrue(check.check(entry).isValid());
	}
	
	@Test
	public void testCheck_nodatabaseSequenceExistsandassembly_levelExists() throws ValidationEngineException, SQLException, IOException
	{
		expect(entryDAOUtils.isAssemblyLevelExists(check.getEmblEntryValidationPlanProperty().analysis_id.get(),0)).andReturn(true);
		expect(entryDAOUtils.getPrimaryAcc(check.getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession(), 0)).andReturn(null);
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		ValidationResult result=check.check(entry);
		assertTrue(!result.isValid());
		assertEquals(1,result.count("AnnotationOnlySequenceCheck_1", Severity.ERROR));
	}
	
	@Test
	public void testCheck_databaseSequenceExistsandassembly_levelExists() throws ValidationEngineException, SQLException, IOException
	{
		expect(entryDAOUtils.isAssemblyLevelExists(check.getEmblEntryValidationPlanProperty().analysis_id.get(),0)).andReturn(true);
		expect(entryDAOUtils.getPrimaryAcc(check.getEmblEntryValidationPlanProperty().analysis_id.get(),entry.getSubmitterAccession(), 0)).andReturn("A00001");
		expect(entryDAOUtils.getSequence("A00001")).andReturn("gttttgtttgatggagaattgcgcagaggggttatatctgcgtgaggatctgt".getBytes());
		replay(entryDAOUtils);
		check.setEntryDAOUtils(entryDAOUtils);
		assertTrue(check.check(entry).isValid());
	}
}
