/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.flatfile.writer.genbank;

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.qualifier.Qualifier;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.flatfile.GenbankPadding;
import uk.ac.ebi.embl.flatfile.writer.QualifierWriter;

public class GenbankQualifierWriterTest extends GenbankWriterTest {

  public void testWrite_Break() throws IOException {
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    Feature feature = featureFactory.createCdsFeature();
    feature.addQualifier(
        qualifierFactory.createQualifier(
            "replace",
            "testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak"));
    feature.addQualifier(
        qualifierFactory.createQualifier(
            "rpt_unit_seq",
            "testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak"));
    feature.addQualifier(
        qualifierFactory.createQualifier(
            "PCR_primers",
            "testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak"));
    feature.addQualifier(
        qualifierFactory.createQualifier(
            "translation",
            "testbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak"));
    StringWriter writer = new StringWriter();
    for (Qualifier qualifier : feature.getQualifiers()) {
      assertTrue(
          new QualifierWriter(entry, qualifier, wrapType, GenbankPadding.QUALIFIER_PADDING)
              .write(writer));
    }
    // System.out.print(writer.toString());
    assertEquals(
        "                     /replace=\"testbreaktestbreaktestbreaktestbreaktestbreaktes\n"
            + "                     tbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbre\n"
            + "                     aktestbreaktestbreaktestbreaktestbreaktestbreaktestbreakte\n"
            + "                     stbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbr\n"
            + "                     eaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreak\"\n"
            + "                     /rpt_unit_seq=\"testbreaktestbreaktestbreaktestbreaktestbre\n"
            + "                     aktestbreaktestbreaktestbreaktestbreaktestbreaktestbreakte\n"
            + "                     stbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbr\n"
            + "                     eaktestbreaktestbreaktestbreaktestbreaktestbreaktestbreakt\n"
            + "                     estbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestb\n"
            + "                     reak\"\n"
            + "                     /PCR_primers=\"testbreaktestbreaktestbreaktestbreaktestbrea\n"
            + "                     ktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktes\n"
            + "                     tbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbre\n"
            + "                     aktestbreaktestbreaktestbreaktestbreaktestbreaktestbreakte\n"
            + "                     stbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbr\n"
            + "                     eak\"\n"
            + "                     /translation=\"testbreaktestbreaktestbreaktestbreaktestbrea\n"
            + "                     ktestbreaktestbreaktestbreaktestbreaktestbreaktestbreaktes\n"
            + "                     tbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbre\n"
            + "                     aktestbreaktestbreaktestbreaktestbreaktestbreaktestbreakte\n"
            + "                     stbreaktestbreaktestbreaktestbreaktestbreaktestbreaktestbr\n"
            + "                     eak\"\n",
        writer.toString());
  }

  public void testWrite_Wrap() throws IOException {
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    Feature feature = featureFactory.createCdsFeature();
    feature.addQualifier(
        qualifierFactory.createQualifier(
            "note",
            "this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note this is a note "));
    feature.addQualifier(
        qualifierFactory.createQualifier(
            "note",
            "thisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanotethisisanote"));
    StringWriter writer = new StringWriter();
    for (Qualifier qualifier : feature.getQualifiers()) {
      assertTrue(
          new QualifierWriter(entry, qualifier, wrapType, GenbankPadding.QUALIFIER_PADDING)
              .write(writer));
    }
    // System.out.print(writer.toString());
    assertEquals(
        "                     /note=\"this is a note this is a note this is a note this\n"
            + "                     is a note this is a note this is a note this is a note\n"
            + "                     this is a note this is a note this is a note this is a\n"
            + "                     note this is a note this is a note this is a note this is\n"
            + "                     a note this is a note this is a note \"\n"
            + "                     /note=\"thisisanotethisisanotethisisanotethisisanotethisisa\n"
            + "                     notethisisanotethisisanotethisisanotethisisanotethisisanot\n"
            + "                     ethisisanotethisisanotethisisanotethisisanotethisisanoteth\n"
            + "                     isisanotethisisanotethisisanote\"\n",
        writer.toString());
  }

  public void testWrite_NoValueQualifiers() throws IOException {
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    Feature feature = featureFactory.createCdsFeature();
    feature.addQualifier(qualifierFactory.createQualifier("pseudo", null));
    feature.addQualifier(qualifierFactory.createQualifier("focus", null));
    StringWriter writer = new StringWriter();
    for (Qualifier qualifier : feature.getQualifiers()) {
      assertTrue(
          new QualifierWriter(entry, qualifier, wrapType, GenbankPadding.QUALIFIER_PADDING)
              .write(writer));
    }
    // System.out.print(writer.toString());
    assertEquals(
        "                     /pseudo\n" + "                     /focus\n", writer.toString());
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
    for (Qualifier qualifier : feature.getQualifiers()) {
      assertTrue(
          new QualifierWriter(entry, qualifier, wrapType, GenbankPadding.QUALIFIER_PADDING)
              .write(writer));
    }
    // System.out.print(writer.toString());
    assertEquals(
        "                     /mod_base=test\n"
            + "                     /compare=test\n"
            + "                     /rpt_unit_range=test\n"
            + "                     /citation=test\n"
            + "                     /codon=test\n"
            + "                     /codon_start=test\n"
            + "                     /cons_splice=test\n"
            + "                     /direction=test\n"
            + "                     /estimated_length=test\n"
            + "                     /label=test\n"
            + "                     /number=test\n"
            + "                     /rpt_type=test\n"
            + "                     /transl_except=test\n"
            + "                     /transl_table=test\n"
            + "                     /tag_peptide=test\n",
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
    for (Qualifier qualifier : feature.getQualifiers()) {
      assertTrue(
          new QualifierWriter(entry, qualifier, wrapType, GenbankPadding.QUALIFIER_PADDING)
              .write(writer));
    }
    // System.out.print(writer.toString());
    assertEquals(
        "                     /frequency=\"test\"\n"
            + "                     /function=\"test\"\n"
            + "                     /rpt_family=\"test\"\n"
            + "                     /cell_line=\"test\"\n"
            + "                     /isolate=\"test\"\n"
            + "                     /strain=\"test\"\n"
            + "                     /tissue_lib=\"test\"\n"
            + "                     /cultivar=\"test\"\n"
            + "                     /clone=\"test\"\n"
            + "                     /locus_tag=\"test\"\n"
            + "                     /old_locus_tag=\"test\"\n"
            + "                     /rpt_unit_seq=\"test\"\n"
            + "                     /collected_by=\"test\"\n"
            + "                     /collection_date=\"test\"\n"
            + "                     /identified_by=\"test\"\n"
            + "                     /clone_lib=\"test\"\n"
            + "                     /EC_number=\"test\"\n"
            + "                     /map=\"test\"\n"
            + "                     /allele=\"test\"\n"
            + "                     /bound_moiety=\"test\"\n"
            + "                     /cell_type=\"test\"\n"
            + "                     /chromosome=\"test\"\n"
            + "                     /country=\"test\"\n"
            + "                     /db_xref=\"test\"\n"
            + "                     /dev_stage=\"test\"\n"
            + "                     /ecotype=\"test\"\n"
            + "                     /exception=\"test\"\n"
            + "                     /experiment=\"test\"\n"
            + "                     /gene=\"test\"\n"
            + "                     /haplotype=\"test\"\n"
            + "                     /inference=\"test\"\n"
            + "                     /isolation_source=\"test\"\n"
            + "                     /lab_host=\"test\"\n"
            + "                     /lat_lon=\"test\"\n"
            + "                     /mol_type=\"test\"\n"
            + "                     /note=\"test\"\n"
            + "                     /operon=\"test\"\n"
            + "                     /organelle=\"test\"\n"
            + "                     /organism=\"test\"\n"
            + "                     /PCR_conditions=\"test\"\n"
            + "                     /PCR_primers=\"test\"\n"
            + "                     /phenotype=\"test\"\n"
            + "                     /pop_variant=\"test\"\n"
            + "                     /plasmid=\"test\"\n"
            + "                     /product=\"test\"\n"
            + "                     /protein_id=\"test\"\n"
            + "                     /replace=\"test\"\n"
            + "                     /segment=\"test\"\n"
            + "                     /serotype=\"test\"\n"
            + "                     /serovar=\"test\"\n"
            + "                     /sex=\"test\"\n"
            + "                     /specimen_voucher=\"test\"\n"
            + "                     /standard_name=\"test\"\n"
            + "                     /sub_clone=\"test\"\n"
            + "                     /sub_species=\"test\"\n"
            + "                     /sub_strain=\"test\"\n"
            + "                     /tissue_type=\"test\"\n"
            + "                     /translation=\"test\"\n"
            + "                     /variety=\"test\"\n"
            + "                     /mobile_element_type=\"test\"\n"
            + "                     /func_characterised=\"test\"\n"
            + "                     /GO_term=\"test\"\n"
            + "                     /culture_collection=\"test\"\n"
            + "                     /bio_material=\"test\"\n"
            + "                     /ncRNA_class=\"test\"\n"
            + "                     /anticodon=\"test\"\n",
        writer.toString());
  }

  public void testWrite_NoQualifiers() throws IOException {
    StringWriter writer = new StringWriter();
    assertFalse(
        new QualifierWriter(entry, null, wrapType, GenbankPadding.QUALIFIER_PADDING).write(writer));
    // System.out.print(writer.toString());
    assertEquals("", writer.toString());
  }
}
