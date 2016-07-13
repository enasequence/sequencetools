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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.StringWriter;

import uk.ac.ebi.embl.flatfile.writer.FeatureLocationWriter;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.Base;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.location.Range;
import uk.ac.ebi.embl.api.entry.location.LocalRange;

public class EmblFeatureLocationWriterTest extends EmblWriterTest {

	public void testWrite_PartialLocation() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", false);
		feature.getLocations().setLeftPartial(true);
		feature.getLocations().setRightPartial(true);
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1l, 210l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(493l, 660l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(752l, 970l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1058l, 1144l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1522l, 1627l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1701l, 1936l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(2077l, 2166l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		// System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            order(<1..210,493..660,752..970,1058..1144,1522..1627,\n" +
				"FT                   1701..1936,2077..>2166)\n",
				writer.toString());

        locationFactory = new LocationFactory();
        featureFactory = new FeatureFactory();
        feature = featureFactory.createFeature("mRNA", false);
        feature.getLocations().setLeftPartial(true);
        feature.getLocations().setRightPartial(true);
        LocalRange range = locationFactory.createLocalRange(1l, 6l);
        feature.getLocations().addLocation(range);
        entry.addFeature(feature);
        writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
        // System.out.print(writer.toString());
        assertEquals("FT   mRNA            <1..>6\n", writer.toString());

	}

	public void testWrite_NonPartialLocation() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", false);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1l, 210l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(493l, 660l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(752l, 970l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1058l, 1144l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1522l, 1627l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1701l, 1936l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(2077l, 2166l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		// System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            order(1..210,493..660,752..970,1058..1144,1522..1627,\n" +
				"FT                   1701..1936,2077..2166)\n",
				writer.toString());
	}

	public void testWrite_ComplementPartialLocation() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", false);
		feature.getLocations().setLeftPartial(true);
		feature.getLocations().setRightPartial(true);
		Range range = locationFactory.createLocalRange(1l, 210l);
		range.setComplement(true);
		feature.getLocations().addLocation(range);
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(493l, 660l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(752l, 970l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1058l, 1144l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1522l, 1627l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1701l, 1936l));
		range = locationFactory.createLocalRange(2077l, 2166l);
		range.setComplement(true);
		feature.getLocations().addLocation(range);
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		// System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            order(complement(1..>210),493..660,752..970,1058..1144,\n" +
				"FT                   1522..1627,1701..1936,complement(<2077..2166))\n",
				writer.toString());
	}	
	
	public void testWrite_ComplementLocation() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", true);
		feature.getLocations().setComplement(true);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1l, 210l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(493l, 660l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(752l, 970l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1058l, 1144l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1522l, 1627l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1701l, 1936l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(2077l, 2166l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		//System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            complement(join(1..210,493..660,752..970,1058..1144,\n" +
				"FT                   1522..1627,1701..1936,2077..2166))\n",
				writer.toString());
	}

	
	public void testWrite_ComplementOfComplementLocation() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", true);
		feature.getLocations().setComplement(true);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		Range range = locationFactory.createLocalRange(1l, 210l);
		range.setComplement(true);
		feature.getLocations().addLocation(range);
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(493l, 660l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(752l, 970l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1058l, 1144l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1522l, 1627l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1701l, 1936l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(2077l, 2166l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		//System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            complement(join(complement(1..210),493..660,752..970,\n" +
				"FT                   1058..1144,1522..1627,1701..1936,2077..2166))\n",
				writer.toString());
	}	

	public void testWrite_BaseLocation() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", true);
		feature.getLocations().setComplement(true);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		Base base = locationFactory.createLocalBase(123l);
		base.setComplement(true);
		feature.getLocations().addLocation(base);
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(493l, 660l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(752l, 970l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1058l, 1144l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1522l, 1627l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1701l, 1936l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(2077l, 2166l));
		feature.getLocations().addLocation(locationFactory.createLocalBase(99999l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		//System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            complement(join(complement(123),493..660,752..970,\n" +
				"FT                   1058..1144,1522..1627,1701..1936,2077..2166,99999))\n",
				writer.toString());
	}
	
	public void testWrite_BetweenLocation() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", false);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		feature.getLocations().addLocation(
				locationFactory.createLocalBetween(5493l, 54932l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(752l, 970l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1058l, 1144l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1522l, 1627l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1701l, 1936l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(2077l, 2166l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		// System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            order(5493^54932,752..970,1058..1144,1522..1627,1701..1936,\n" +
				"FT                   2077..2166)\n",
				writer.toString());
	}	
		
	public void testWrite_RemoteLocation() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", false);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		feature.getLocations().addLocation(
			locationFactory.createRemoteRange("A00001", 1, 1l, 210l));
		feature.getLocations().addLocation(
			locationFactory.createRemoteBase("A00002", 4, 5493l));
		feature.getLocations().addLocation(
				locationFactory.createRemoteBetween("A00002", 4, 5493l, 54932l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(752l, 970l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1058l, 1144l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1522l, 1627l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(1701l, 1936l));
		feature.getLocations().addLocation(
			locationFactory.createLocalRange(2077l, 2166l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		//System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            order(A00001.1:1..210,A00002.4:5493,A00002.4:5493^54932,\n" +
				"FT                   752..970,1058..1144,1522..1627,1701..1936,2077..2166)\n",
				writer.toString());
	}	
	
	public void testWrite_SingleBase() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", true);
		feature.getLocations().setComplement(false);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		Base base = locationFactory.createLocalBase(123l);
		base.setComplement(false);
		feature.getLocations().addLocation(base);
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		//System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            123\n",
				writer.toString());
	}	

	public void testWrite_SingleRange() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", true);
		feature.getLocations().setComplement(false);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		feature.getLocations().addLocation(locationFactory.createLocalRange(123l, 333l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		//System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            123..333\n",
				writer.toString());
	}	

	public void testWrite_SingleBetween() throws IOException {
		LocationFactory locationFactory = new LocationFactory();
		FeatureFactory featureFactory = new FeatureFactory();
		Feature feature = featureFactory.createFeature("mRNA", true);
		feature.getLocations().setComplement(false);
		feature.getLocations().setLeftPartial(false);
		feature.getLocations().setRightPartial(false);
		feature.getLocations().addLocation(locationFactory.createLocalBetween(123l, 124l));
		entry.addFeature(feature);		
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, feature, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		//System.out.print(writer.toString());
		assertEquals(
				"FT   mRNA            123^124\n",
				writer.toString());
	}	
	
	public void testWrite_NoFeature() throws IOException {
		StringWriter writer = new StringWriter();
		new FeatureLocationWriter(entry, null, wrapType, 
				EmblPadding.FEATURE_PADDING, 
				EmblPadding.QUALIFIER_PADDING).write(writer);
		//System.out.print(writer.toString());
		assertEquals(
				"",
				writer.toString());
	}	
}
