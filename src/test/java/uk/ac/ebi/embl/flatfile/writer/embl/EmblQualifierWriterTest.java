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

import uk.ac.ebi.embl.flatfile.writer.QualifierWriter;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.flatfile.EmblPadding;

public class EmblQualifierWriterTest extends EmblWriterTest {

	public void testWrite_Break() throws IOException {
		FeatureFactory featureFactory = new FeatureFactory();		
		QualifierFactory qualifierFactory = new QualifierFactory();
		Feature feature = featureFactory.createCdsFeature();
 		feature.addQualifier(qualifierFactory.createQualifier("replace",
 			"testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak"));
 		feature.addQualifier(qualifierFactory.createQualifier("rpt_unit_seq",
			"testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak"));
 		feature.addQualifier(qualifierFactory.createQualifier("PCR_primers",
			"testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak"));
 		feature.addQualifier(qualifierFactory.createQualifier("translation",
			"testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak"));
		StringWriter writer = new StringWriter();
		for( Qualifier qualifier : feature.getQualifiers() ) {
			assertTrue(new QualifierWriter(entry, qualifier, wrapType,
	        		EmblPadding.QUALIFIER_PADDING).write(writer));
		}
		//System.out.print(writer.toString());
		assertEquals(
			"FT                   /replace=\"testbreaktestbreaktestbreaktestbreaktestbreaktest\n" +
			"FT                   breaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak\n" +
			"FT                   testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestb\n" +
			"FT                   reaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreakt\n" +
			"FT                   estbreaktestbreaktestbreaktestbreaktestbreaktestbreak\"\n" +
			"FT                   /rpt_unit_seq=\"testbreaktestbreaktestbreaktestbreaktestbrea\n" +
			"FT                   ktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktest\n" +
			"FT                   breaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak\n" +
			"FT                   testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestb\n" +
			"FT                   reaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak\"\n" +
			"FT                   /PCR_primers=\"testbreaktestbreaktestbreaktestbreaktestbreak\n" +
			"FT                   testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestb\n" +
			"FT                   reaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreakt\n" +
			"FT                   estbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbr\n" +
			"FT                   eaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak\"\n" +
			"FT                   /translation=\"testbreaktestbreaktestbreaktestbreaktestbreak\n" +
			"FT                   testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestb\n" +
			"FT                   reaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreakt\n" +
			"FT                   estbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbr\n" +
			"FT                   eaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak\"\n",
			writer.toString());	
	} 		
	
	public void testWrite_Wrap() throws IOException {
		FeatureFactory featureFactory = new FeatureFactory();		
		QualifierFactory qualifierFactory = new QualifierFactory();
		Feature feature = featureFactory.createCdsFeature();
 		feature.addQualifier(qualifierFactory.createQualifier("note",
 			"this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note "));
 		feature.addQualifier(qualifierFactory.createQualifier("note",
			"thisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanote"));
		StringWriter writer = new StringWriter();
		for( Qualifier qualifier : feature.getQualifiers() ) {
			assertTrue(new QualifierWriter(entry, qualifier, wrapType,
	        		EmblPadding.QUALIFIER_PADDING).write(writer));
		}
		//System.out.print(writer.toString());
		assertEquals(
				"FT                   /note=\"this is a note this is a note this is a note this is\n" +
				"FT                   a note this is a note this is a note this is a note this is\n" +
				"FT                   a note this is a note this is a note this is a note this is\n" +
				"FT                   a note this is a note this is a note this is a note this is\n" +
				"FT                   a note this is a note \"\n" +
				"FT                   /note=\"thisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanote\"\n",
			writer.toString());	
	} 		
	
	public void testWrite_NoValueQualifiers() throws IOException {
		FeatureFactory featureFactory = new FeatureFactory();		
		QualifierFactory qualifierFactory = new QualifierFactory();
		Feature feature = featureFactory.createCdsFeature();
 		feature.addQualifier(qualifierFactory.createQualifier("pseudo", null));
 		feature.addQualifier(qualifierFactory.createQualifier("focus", null));
		StringWriter writer = new StringWriter();
		for( Qualifier qualifier : feature.getQualifiers() ) {
			assertTrue(new QualifierWriter(entry, qualifier, wrapType,
	        		EmblPadding.QUALIFIER_PADDING).write(writer));
		}
		//System.out.print(writer.toString());
		assertEquals(
				"FT                   /pseudo\n" +
				"FT                   /focus\n",
			writer.toString());	
	} 		

	public void testWrite_NoQuoteQualifiers() throws IOException {
		FeatureFactory featureFactory = new FeatureFactory();		
		QualifierFactory qualifierFactory = new QualifierFactory();
		Feature feature = featureFactory.createCdsFeature(); 		
		feature.addQualifier(qualifierFactory.createQualifier("mod_base", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("compare", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("rpt_unit_range", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("citation", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("codon", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("codon_start", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("cons_splice", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("direction", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("estimated_length", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("label", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("number", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("rpt_type", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("transl_except", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("transl_table", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("tag_peptide", "test"));
 		
		StringWriter writer = new StringWriter();
		for( Qualifier qualifier : feature.getQualifiers() ) {
			assertTrue(new QualifierWriter(entry, qualifier, wrapType,
	        		EmblPadding.QUALIFIER_PADDING).write(writer));
		}
		// System.out.print(writer.toString());
		assertEquals(
				"FT                   /mod_base=test\n" +
				"FT                   /compare=test\n" +
				"FT                   /rpt_unit_range=test\n" +
				"FT                   /citation=test\n" +
				"FT                   /codon=test\n" +
				"FT                   /codon_start=test\n" +
				"FT                   /cons_splice=test\n" +
				"FT                   /direction=test\n" +
				"FT                   /estimated_length=test\n" +
				"FT                   /label=test\n" +
				"FT                   /number=test\n" +
				"FT                   /rpt_type=test\n" +
				"FT                   /transl_except=test\n" +
				"FT                   /transl_table=test\n" +
				"FT                   /tag_peptide=test\n",		
			writer.toString());	
	} 		
	
	public void testWrite_QuoteQualifiers() throws IOException {
		FeatureFactory featureFactory = new FeatureFactory();		
		QualifierFactory qualifierFactory = new QualifierFactory();
		Feature feature = featureFactory.createCdsFeature(); 		
		feature.addQualifier(qualifierFactory.createQualifier("frequency", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("function", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("rpt_family", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("cell_line", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("isolate", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("strain", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("tissue_lib", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("cultivar", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("clone", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("locus_tag", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("old_locus_tag", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("rpt_unit_seq", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("collected_by", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("collection_date", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("identified_by", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("clone_lib", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("EC_number", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("map", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("allele", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("bound_moiety", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("cell_type", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("chromosome", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("country", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("db_xref", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("dev_stage", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("ecotype", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("exception", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("experiment", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("gene", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("haplotype", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("inference", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("isolation_source", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("lab_host", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("lat_lon", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("mol_type", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("note", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("operon", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("organelle", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("organism", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("PCR_conditions", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("PCR_primers", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("phenotype", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("pop_variant", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("plasmid", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("product", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("protein_id", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("replace", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("segment", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("serotype", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("serovar", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("sex", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("specimen_voucher", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("standard_name", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("sub_clone", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("sub_species", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("sub_strain", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("tissue_type", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("translation", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("variety", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("mobile_element_type", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("func_characterised", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("GO_term", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("culture_collection", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("bio_material", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("ncRNA_class", "test"));
		feature.addQualifier(qualifierFactory.createQualifier("anticodon", "test"));
		StringWriter writer = new StringWriter();
		for( Qualifier qualifier : feature.getQualifiers() ) {
			assertTrue(new QualifierWriter(entry, qualifier, wrapType,
	        		EmblPadding.QUALIFIER_PADDING).write(writer));
		}
		//System.out.print(writer.toString());
		assertEquals(
				"FT                   /frequency=\"test\"\n" +
				"FT                   /function=\"test\"\n" +
				"FT                   /rpt_family=\"test\"\n" +
				"FT                   /cell_line=\"test\"\n" +
				"FT                   /isolate=\"test\"\n" +
				"FT                   /strain=\"test\"\n" +
				"FT                   /tissue_lib=\"test\"\n" +
				"FT                   /cultivar=\"test\"\n" +
				"FT                   /clone=\"test\"\n" +
				"FT                   /locus_tag=\"test\"\n" +
				"FT                   /old_locus_tag=\"test\"\n" +
				"FT                   /rpt_unit_seq=\"test\"\n" +
				"FT                   /collected_by=\"test\"\n" +
				"FT                   /collection_date=\"test\"\n" +
				"FT                   /identified_by=\"test\"\n" +
				"FT                   /clone_lib=\"test\"\n" +
				"FT                   /EC_number=\"test\"\n" +
				"FT                   /map=\"test\"\n" +
				"FT                   /allele=\"test\"\n" +
				"FT                   /bound_moiety=\"test\"\n" +
				"FT                   /cell_type=\"test\"\n" +
				"FT                   /chromosome=\"test\"\n" +
				"FT                   /country=\"test\"\n" +
				"FT                   /db_xref=\"test\"\n" +
				"FT                   /dev_stage=\"test\"\n" +
				"FT                   /ecotype=\"test\"\n" +
				"FT                   /exception=\"test\"\n" +
				"FT                   /experiment=\"test\"\n" +
				"FT                   /gene=\"test\"\n" +
				"FT                   /haplotype=\"test\"\n" +
				"FT                   /inference=\"test\"\n" +
				"FT                   /isolation_source=\"test\"\n" +
				"FT                   /lab_host=\"test\"\n" +
				"FT                   /lat_lon=\"test\"\n" +
				"FT                   /mol_type=\"test\"\n" +
				"FT                   /note=\"test\"\n" +
				"FT                   /operon=\"test\"\n" +
				"FT                   /organelle=\"test\"\n" +
				"FT                   /organism=\"test\"\n" +
				"FT                   /PCR_conditions=\"test\"\n" +
				"FT                   /PCR_primers=\"test\"\n" +
				"FT                   /phenotype=\"test\"\n" +
				"FT                   /pop_variant=\"test\"\n" +
				"FT                   /plasmid=\"test\"\n" +
				"FT                   /product=\"test\"\n" +
				"FT                   /protein_id=\"test\"\n" +
				"FT                   /replace=\"test\"\n" +
				"FT                   /segment=\"test\"\n" +
				"FT                   /serotype=\"test\"\n" +
				"FT                   /serovar=\"test\"\n" +
				"FT                   /sex=\"test\"\n" +
				"FT                   /specimen_voucher=\"test\"\n" +
				"FT                   /standard_name=\"test\"\n" +
				"FT                   /sub_clone=\"test\"\n" +
				"FT                   /sub_species=\"test\"\n" +
				"FT                   /sub_strain=\"test\"\n" +
				"FT                   /tissue_type=\"test\"\n" +
				"FT                   /translation=\"test\"\n" +
				"FT                   /variety=\"test\"\n" +
				"FT                   /mobile_element_type=\"test\"\n" +
				"FT                   /func_characterised=\"test\"\n" +
				"FT                   /GO_term=\"test\"\n" +
				"FT                   /culture_collection=\"test\"\n" +
				"FT                   /bio_material=\"test\"\n" +
				"FT                   /ncRNA_class=\"test\"\n" +
				"FT                   /anticodon=\"test\"\n" ,
				writer.toString());	
	} 	

	public void testWrite_NoQualifiers() throws IOException {
		StringWriter writer = new StringWriter();
		assertFalse(new QualifierWriter(entry, null, wrapType,
        		EmblPadding.QUALIFIER_PADDING).write(writer));
		//System.out.print(writer.toString());
		assertEquals(
				"",			
				writer.toString());	
	} 	
}
