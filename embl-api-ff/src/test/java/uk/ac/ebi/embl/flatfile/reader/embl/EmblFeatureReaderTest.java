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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import uk.ac.ebi.embl.flatfile.reader.FeatureReader;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.Join;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.LocalBetween;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.Order;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteBetween;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.validation.FlatFileOrigin;
import uk.ac.ebi.embl.api.validation.Origin;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationMessage;
import uk.ac.ebi.embl.api.validation.ValidationResult;
import uk.ac.ebi.embl.flatfile.reader.ReaderOptions;

public class EmblFeatureReaderTest extends EmblReaderTest {

	public void testReadOnlySourceFeature() throws IOException {
		initLineReader(
				"FT   ncRNA           DP001173.2:3219018..3219088\n" +
					  "FT                   /gene=\"MIR216B\"\n" +
					  "FT                   /product=\"mir-216b\"\n" +
				      "FT                   /note=\"ortholog of zebra finch tgu-mir-216b\"\n" +
				      "FT                   /ncRNA_class=\"miRNA\"\n"+
				      "FT   source          156 \n"+
			          "FT                   /mol_type=\"mRNA\"");
		ReaderOptions rO = new ReaderOptions();
		rO.setParseSourceOnly(true);
		lineReader.setReaderOptions(rO);

		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertNotNull(entry.getFeatures());
		assertEquals(0, entry.getFeatures().size());

		result = (new FeatureReader(lineReader)).read(entry);
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		assertEquals("source", entry.getFeatures().get(0).getName());
		assertEquals("mRNA", entry.getSequence().getMoleculeType());
	}

	public void testRead_LocalSingleBase() throws IOException {
		initLineReader("FT   source          156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(156), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalSingleBaseComplement() throws IOException {
		initLineReader("FT   source          complement(156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(156), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_LocalSingleBaseLeftPartial() throws IOException {
		initLineReader("FT   source          <156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(156), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalSingleBaseRightPartial() throws IOException {
		initLineReader("FT   source          complement(<156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertTrue(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(156), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_RemoteSingleBaseWithoutVersion() throws IOException {
		initLineReader("FT   source          A00001:156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof RemoteBase);
		assertEquals(new Long(156), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertEquals("A00001", ((RemoteBase) location).getAccession());
		assertNull(((RemoteBase) location).getVersion());
		assertFalse(location.isComplement());
	}

	public void testRead_RemoteSingleBaseWithVersion() throws IOException {
		initLineReader("FT   source          A00001.13:156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof RemoteBase);
		assertEquals(new Long(156), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertEquals("A00001", ((RemoteBase) location).getAccession());
		assertEquals(new Integer(13), ((RemoteBase) location).getVersion());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalRange() throws IOException {
		initLineReader("FT   source          4..156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalRangeComplement() throws IOException {
		initLineReader("FT   source          complement(4..156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_LocalRangeLeftPartial() throws IOException {
		initLineReader("FT   source          <4..156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalRangeRightPartial() throws IOException {
		initLineReader("FT   source          4..>156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertTrue(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalRangeRightPartial2() throws IOException {
		initLineReader("FT   source          complement(<4..156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertTrue(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_LocalRangeLeftPartial2() throws IOException {
		initLineReader("FT   source          complement(4..>156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_RemoteRangeWithoutVersion() throws IOException {
		initLineReader("FT   source          complement(A0001:4..156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A0001", ((RemoteRange) location).getAccession());
		assertNull(((RemoteRange) location).getVersion());
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_RemoteRangeWithLeftPartial() throws IOException {
		initLineReader("FT   source          A0001:<4..156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A0001", ((RemoteRange) location).getAccession());
		assertNull(((RemoteRange) location).getVersion());
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_RemoteRangeWithVersion() throws IOException {
		initLineReader("FT   source          complement(A0001.1:4..156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A0001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(1), ((RemoteRange) location).getVersion());
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_LocalBetween() throws IOException {
		initLineReader("FT   source          4^156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBetween);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalBetweenComplement() throws IOException {
		initLineReader("FT   source          complement(4^156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBetween);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_LocalBetweenLeftPartial() throws IOException {
		initLineReader("FT   source          <4^156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBetween);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalBetweenRightPartial() throws IOException {
		initLineReader("FT   source          4^>156 \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertTrue(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBetween);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_LocalBetweenRightPartial2() throws IOException {
		initLineReader("FT   source          complement(<4^156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertTrue(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBetween);
		assertEquals(new Long(4), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_LocalBetweenLeftPartial2() throws IOException {
		initLineReader("FT   source          complement(155^>156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBetween);
		assertEquals(new Long(155), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_RemoteBetweenWithoutVersion() throws IOException {
		initLineReader("FT   source          complement(A0001:155^156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof RemoteBetween);
		assertEquals("A0001", ((RemoteBetween) location).getAccession());
		assertNull(((RemoteBetween) location).getVersion());
		assertEquals(new Long(155), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_RemoteBetweenWithVersion() throws IOException {
		initLineReader("FT   source          complement(A0001.1:155^156) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(1, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof RemoteBetween);
		assertEquals("A0001", ((RemoteBetween) location).getAccession());
		assertEquals(new Integer(1), ((RemoteBetween) location).getVersion());
		assertEquals(new Long(155), location.getBeginPosition());
		assertEquals(new Long(156), location.getEndPosition());
		assertTrue(location.isComplement());
	}

	public void testRead_Join() throws IOException {
		initLineReader("FT   source          join(1,2..34,complement(34..45),\n"
				+ "FT                   A00001.5:34..45) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(1), location.getBeginPosition());
		assertEquals(new Long(1), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2), location.getBeginPosition());
		assertEquals(new Long(34), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertTrue(location.isComplement());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A00001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(5), ((RemoteRange) location).getVersion());
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_JoinComplement() throws IOException {
		initLineReader("FT   source          complement(join(1,2..34,complement(34..45),\n"
				+ "FT                   A00001.5:34..45)) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertTrue(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(1), location.getBeginPosition());
		assertEquals(new Long(1), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2), location.getBeginPosition());
		assertEquals(new Long(34), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertTrue(location.isComplement());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A00001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(5), ((RemoteRange) location).getVersion());
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_JoinComplement2() throws IOException {
		initLineReader("FT   mRNA            complement(join(<1594..1883,1927..2065,2746..2749,\n"
				+ "FT                   3182..>3228)) \n");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("mRNA", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertTrue(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertTrue(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(1594), location.getBeginPosition());
		assertEquals(new Long(1883), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(1927), location.getBeginPosition());
		assertEquals(new Long(2065), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2746), location.getBeginPosition());
		assertEquals(new Long(2749), location.getEndPosition());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(3182), location.getBeginPosition());
		assertEquals(new Long(3228), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_JoinLeftPartial() throws IOException {
		initLineReader("FT   source          join(<1,2..34,complement(34..45),\n"
				+ "FT                   A00001.5:34..45) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(1), location.getBeginPosition());
		assertEquals(new Long(1), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2), location.getBeginPosition());
		assertEquals(new Long(34), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertTrue(location.isComplement());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A00001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(5), ((RemoteRange) location).getVersion());
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_JoinRightPartial() throws IOException {
		initLineReader("FT   source          join(1,2..34,complement(34..45),\n"
				+ "FT                   A00001.5:34..>45) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Join<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertTrue(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(1), location.getBeginPosition());
		assertEquals(new Long(1), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2), location.getBeginPosition());
		assertEquals(new Long(34), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertTrue(location.isComplement());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A00001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(5), ((RemoteRange) location).getVersion());
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_Order() throws IOException {
		initLineReader("FT   source          order(1,2..34,complement(34..45),\n"
				+ "FT                   A00001.5:34..45) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Order<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(1), location.getBeginPosition());
		assertEquals(new Long(1), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2), location.getBeginPosition());
		assertEquals(new Long(34), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertTrue(location.isComplement());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A00001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(5), ((RemoteRange) location).getVersion());
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_OrderComplement() throws IOException {
		initLineReader("FT   source          complement(order(1,2..34,complement(34..45),\n"
				+ "FT                   A00001.5:34..45)) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Order<?>);
		assertTrue(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(1), location.getBeginPosition());
		assertEquals(new Long(1), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2), location.getBeginPosition());
		assertEquals(new Long(34), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertTrue(location.isComplement());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A00001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(5), ((RemoteRange) location).getVersion());
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_OrderLeftPartial() throws IOException {
		initLineReader("FT   source          order(<1,2..34,complement(34..45),\n"
				+ "FT                   A00001.5:34..45) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Order<?>);
		assertFalse(locations.isComplement());
		assertTrue(locations.isLeftPartial());
		assertFalse(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(1), location.getBeginPosition());
		assertEquals(new Long(1), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2), location.getBeginPosition());
		assertEquals(new Long(34), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertTrue(location.isComplement());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A00001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(5), ((RemoteRange) location).getVersion());
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_OrderRightPartial() throws IOException {
		initLineReader("FT   source          order(1,2..34,complement(34..45),\n"
				+ "FT                   A00001.5:34..>45) \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		CompoundLocation<Location> locations = feature.getLocations();
		assertTrue(locations instanceof Order<?>);
		assertFalse(locations.isComplement());
		assertFalse(locations.isLeftPartial());
		assertTrue(locations.isRightPartial());
		assertEquals(4, locations.getLocations().size());
		Location location = locations.getLocations().get(0);
		assertTrue(location instanceof LocalBase);
		assertEquals(new Long(1), location.getBeginPosition());
		assertEquals(new Long(1), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(1);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(2), location.getBeginPosition());
		assertEquals(new Long(34), location.getEndPosition());
		assertFalse(location.isComplement());
		location = locations.getLocations().get(2);
		assertTrue(location instanceof LocalRange);
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertTrue(location.isComplement());
		location = locations.getLocations().get(3);
		assertTrue(location instanceof RemoteRange);
		assertEquals("A00001", ((RemoteRange) location).getAccession());
		assertEquals(new Integer(5), ((RemoteRange) location).getVersion());
		assertEquals(new Long(34), location.getBeginPosition());
		assertEquals(new Long(45), location.getEndPosition());
		assertFalse(location.isComplement());
	}

	public void testRead_QualifierNoValue() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /note \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertNull(qualifier.getValue());
	}

	public void testRead_QualifierUnquotedValue() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /note=\"value value\"\n"
				+ "FT                   /mol_type=\"mRNA\"");//qualifier values must be quoted,if they are given as qualifier.isquoted
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals("value value", qualifier.getValue());
	}

	public void testRead_QualifierQuotedValue() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /note=\"value value\" \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals("value value", qualifier.getValue());
	}

	public void testRead_QualifierLongQuotedValue() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /note=\"value1 value2\n"
				+ "FT                   value3 value4\n"
				+ "FT                   value5 value6\n"
				+ "FT                   value7 value8\" \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals("value1 value2 value3 value4 value5 value6 value7 value8",
				qualifier.getValue());
	}

	public void testRead_QualifierLongQuotedValueWithForwardSlash()
			throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /note=\"value1 value2\n"
				+ "FT                   value3 value4\n"
				+ "FT                   /value5 value6\n"
				+ "FT                   /note value8\" \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals("value1 value2 value3 value4 /value5 value6 /note value8",
				qualifier.getValue());
	}

	public void testRead_QualifierLongQuotedValueWithForwardSlash2()
			throws IOException {
		initLineReader("FT   misc_feature    423526..424566\n"
				+ "FT                   /note=\"HMMPfam hit to PF00724, NADH:flavin oxidoreductase\n"
				+ "FT                   / NADH oxidas, score 9.5e-89\"\n");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("misc_feature", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals(
				"HMMPfam hit to PF00724, NADH:flavin oxidoreductase / NADH oxidas, score 9.5e-89",
				qualifier.getValue());
	}

	public void testRead_QualifierLongQuotedValueWithForwardSlash3()
			throws IOException {
		initLineReader("FT   misc_feature    423526..424566\n"
				+ "FT                   /note=\"HMMPfam hit to PF00724, NADH:flavin oxidoreductase\n"
				+ "FT                   /NADH oxidas, score 9.5e-89\"\n");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("misc_feature", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals(
				"HMMPfam hit to PF00724, NADH:flavin oxidoreductase /NADH oxidas, score 9.5e-89",
				qualifier.getValue());
	}

	public void testRead_QualifierLongQuotedValueWithForwardSlash4()
			throws IOException {
		initLineReader("FT   misc_feature    423526..424566\n"
				+ "FT                   /note=\"HMMPfam hit to PF00724, NADH:flavin oxidoreductase\n"
				+ "FT                   /gene oxidas, score 9.5e-89\"\n");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("misc_feature", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals(
				"HMMPfam hit to PF00724, NADH:flavin oxidoreductase /gene oxidas, score 9.5e-89",
				qualifier.getValue());
	}

	public void testRead_QualifierLongUnquotedValue() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /note=\"value1 value2\n"
				+ "FT                   value3 value4\n"
				+ "FT                   value5 value6\n"
				+ "FT                   value7 value8\" \n"
				+ "FT                   /mol_type=\"mRNA\"");//qualifier values must be quoted,if they are given as qualifier.isquoted
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals("value1 value2 value3 value4 value5 value6 value7 value8",
				qualifier.getValue());
	}

	public void testRead_MultipleQualifierValue() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /note=\"value value\"\n"
				+ "FT                   /note=\"value value\"\n"
				+ "FT                   /note=\"value value\"\n"
				+ "FT                   /note=\"value value\" \n"
				+ "FT                   /mol_type=\"mRNA\"");//Quotes should be balanced ,since qualifier values may contain other qualifiers
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(4, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals("value value", qualifier.getValue());
		qualifier = qualifiers.get(1);
		assertEquals("note", qualifier.getName());
		assertEquals("value value", qualifier.getValue());
		qualifier = qualifiers.get(2);
		assertEquals("note", qualifier.getName());
		assertEquals("value value", qualifier.getValue());
		qualifier = qualifiers.get(3);
		assertEquals("note", qualifier.getName());
		assertEquals("value value", qualifier.getValue());
	}

	public void testRead_QualifierReplace() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /replace=\"value1 value2\n"
				+ "FT                   value3 value4\n"
				+ "FT                   value5 value6\n"
				+ "FT                   value7 value8\" \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("replace", qualifier.getName());
		assertEquals("value1 value2value3 value4value5 value6value7 value8",
				qualifier.getValue());
	}

	public void testRead_QualifierRptUnitSeq() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /rpt_unit_seq=\"value1 value2\n"
				+ "FT                   value3 value4\n"
				+ "FT                   value5 value6\n"
				+ "FT                   value7 value8\" \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("rpt_unit_seq", qualifier.getName());
		assertEquals("value1 value2value3 value4value5 value6value7 value8",
				qualifier.getValue());
	}

	public void testRead_QualifierPcrPrimers() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /PCR_primers=\"value1 value2\n"
				+ "FT                   value3 value4\n"
				+ "FT                   value5 value6\n"
				+ "FT                   value7 value8\" \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("PCR_primers", qualifier.getName());
		assertEquals("value1 value2value3 value4value5 value6value7 value8",
				qualifier.getValue());
	}

	public void testRead_QualifierTranslation() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /translation=\"value1 value2\n"
				+ "FT                   value3 value4\n"
				+ "FT                   value5 value6\n"
				+ "FT                   value7 value8\" \n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("translation", qualifier.getName());
		assertEquals("value1 value2value3 value4value5 value6value7 value8",
				qualifier.getValue());
	}

	public void testRead_QualifierLineEndWithDash() throws IOException {
		initLineReader("FT   source          156\n"
				+ "FT                   /note=\"Predicted tRNA(5-methylaminomethyl-2-\n"
				+ "FT                   thiouridylate) methyltransferase, contains the PP-loop\n"
				+ "FT                   ATPase domain\"\n"
				+ "FT                   /mol_type=\"mRNA\"");
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		Feature feature = entry.getFeatures().get(0);
		assertEquals("source", feature.getName());
		List<Qualifier> qualifiers = feature.getQualifiers();
		assertEquals(1, qualifiers.size());
		Qualifier qualifier = qualifiers.get(0);
		assertEquals("note", qualifier.getName());
		assertEquals(
				"Predicted tRNA(5-methylaminomethyl-2-thiouridylate) methyltransferase, contains the PP-loop ATPase domain",
				qualifier.getValue());
	}

	public void testRead_Origin() throws IOException {
		initLineReader("FT   source          156..\n"
				+ "FT                   160\n"
				+ "FT                   /note=\"value value\n"
				+ "FT                   value value\"\n"
				+ "FT                   /note=\"value value\"\n"
				+ "FT                   /note=\"value value\"\n"
				+ "FT                   /note=\"value value\"\n"
				+ "FT                   /mol_type=\"mRNA\"");//Quotes should be balanced ,since qualifier values may contains other qualifiers
		ValidationResult result = (new FeatureReader(lineReader)).read(entry);
		Collection<ValidationMessage<Origin>> messages = result.getMessages();
		for (ValidationMessage<Origin> message : messages) {
			System.out.println(message.getMessage());
		}
		assertEquals(0, result.count(Severity.ERROR));
		assertNotNull(entry.getFeatures());
		assertEquals(1, entry.getFeatures().size());
		FlatFileOrigin origin = (FlatFileOrigin) entry.getFeatures().get(0)
				.getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(2, origin.getLastLineNumber());
		origin = (FlatFileOrigin) entry.getFeatures().get(0).getLocations()
				.getOrigin();
		assertEquals(1, origin.getFirstLineNumber());
		assertEquals(2, origin.getLastLineNumber());
		List<Qualifier> qualifiers = entry.getFeatures().get(0).getQualifiers();
		assertEquals(4, qualifiers.size());
		origin = (FlatFileOrigin) qualifiers.get(0).getOrigin();
		assertEquals(3, origin.getFirstLineNumber());
		assertEquals(4, origin.getLastLineNumber());
		origin = (FlatFileOrigin) qualifiers.get(1).getOrigin();
		assertEquals(5, origin.getFirstLineNumber());
		assertEquals(5, origin.getLastLineNumber());
		origin = (FlatFileOrigin) qualifiers.get(2).getOrigin();
		assertEquals(6, origin.getFirstLineNumber());
		assertEquals(6, origin.getLastLineNumber());
		origin = (FlatFileOrigin) qualifiers.get(3).getOrigin();
		assertEquals(7, origin.getFirstLineNumber());
		assertEquals(7, origin.getLastLineNumber());
	}
}
