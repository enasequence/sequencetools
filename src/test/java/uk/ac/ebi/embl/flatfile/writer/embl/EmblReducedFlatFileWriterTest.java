/*
 * Copyright 2018-2023 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.feature.SourceFeature;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.api.entry.reference.Article;
import uk.ac.ebi.embl.api.entry.reference.Reference;
import uk.ac.ebi.embl.api.entry.reference.ReferenceFactory;
import uk.ac.ebi.embl.api.entry.sequence.Sequence.Topology;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.flatfile.FlatFileDateUtils;
import uk.ac.ebi.embl.flatfile.reader.embl.EmblLineReader;

public class EmblReducedFlatFileWriterTest extends EmblWriterTest {

  protected void setUp() throws Exception {
    super.setUp();
    initBaseEntry();
  }

  private void initBaseEntry() throws IOException {
    entry.setPrimaryAccession("DP000153");
    entry.setDataClass("CON");
    entry.setDivision("MAM");
    entry.setDescription(
        new Text(
            "Cloning and characterization of a cDNA encoding a novel subtype of rat thyrotropin-releasing hormone receptor."));
    entry.setComment(
        new Text(
            "Cloning\n and characterization\n of a cDNA encoding a novel subtype of rat\n thyrotropin-releasing hormone receptor"));
    addReference();
  }

  /**
   * test for CON with contigs + features + sequence
   *
   * @throws IOException
   */
  public void testCONContigsFeaturesSequence() throws IOException {
    addFeature();
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
    entry.getSequence().setVersion(1);
    entry.getSequence().setTopology(Topology.LINEAR);
    entry.getSequence().setMoleculeType("genomic RNA");
    entry.setIdLineSequenceLength(2);

    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange = locationFactory.createLocalRange(1L, 2L);
    entry.getSequence().addContig(localRange);

    StringWriter writer = new StringWriter();
    assertTrue(new EmblReducedFlatFileWriter(entry).write(writer));
    assertEquals(
        "ID   DP000153; SV 1; linear; genomic RNA; CON; MAM; 2 BP.\n"
            + "XX\n"
            + "FH   Key             Location/Qualifiers\n"
            + "FH\n"
            + "FT   source          1..4041\n"
            + "FT                   /submitter_seqid=\"\"\n"
            + "FT   CDS             3514..4041\n"
            + "FT                   /product=\"hypothetical protein\"\n"
            + "FT                   /note=\"ORF 5\"\n"
            + "FT                   /protein_id=\"CAA31466.1\"\n"
            + "FT                   /translation=\"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEEL\n"
            + "FT                   FLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFWSS\n"
            + "FT                   SPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLPRTN\n"
            + "FT                   SGSSTKAMVLHR\"\n"
            + "XX\n"
            + "CO   join(1..2)\n"
            + "XX\n"
            + "SQ   Sequence 2 BP; 2 A; 0 C; 0 G; 0 T; 0 other;\n"
            + "     aa                                                                        2\n"
            + "//\n",
        writer.toString());
  }

  /**
   * test for non-CON with features + sequence -> check flat file
   *
   * @throws IOException
   */
  public void testNonCONFeaturesSequence() throws IOException {
    entry.setDataClass("SET");
    addFeature();
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
    entry.getSequence().setVersion(1);
    entry.getSequence().setTopology(Topology.LINEAR);
    entry.getSequence().setMoleculeType("genomic RNA");
    entry.setIdLineSequenceLength(2);

    StringWriter writer = new StringWriter();
    assertTrue(new EmblReducedFlatFileWriter(entry).write(writer));
    assertEquals(
        "ID   DP000153; SV 1; linear; genomic RNA; SET; MAM; 2 BP.\n"
            + "XX\n"
            + "FH   Key             Location/Qualifiers\n"
            + "FH\n"
            + "FT   source          1..4041\n"
            + "FT                   /submitter_seqid=\"\"\n"
            + "FT   CDS             3514..4041\n"
            + "FT                   /product=\"hypothetical protein\"\n"
            + "FT                   /note=\"ORF 5\"\n"
            + "FT                   /protein_id=\"CAA31466.1\"\n"
            + "FT                   /translation=\"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEEL\n"
            + "FT                   FLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFWSS\n"
            + "FT                   SPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLPRTN\n"
            + "FT                   SGSSTKAMVLHR\"\n"
            + "XX\n"
            + "SQ   Sequence 2 BP; 2 A; 0 C; 0 G; 0 T; 0 other;\n"
            + "     aa                                                                        2\n"
            + "//\n",
        writer.toString());
  }

  /**
   * test for CON with NO contigs + features + sequence
   *
   * @throws IOException
   */
  public void testCONNoContigsFeaturesSequence() {
    addFeature();
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
    entry.getSequence().setVersion(1);
    entry.getSequence().setTopology(Topology.LINEAR);
    entry.getSequence().setMoleculeType("genomic RNA");
    entry.setIdLineSequenceLength(2);

    try {
      StringWriter writer = new StringWriter();
      new EmblReducedFlatFileWriter(entry).write(writer);
      fail(
          "Expected IOException: "
              + EmblReducedFlatFileWriter.REDUCED_FF_WRITE_FAILED_MISSING_CONTIGS);
    } catch (IOException exception) {
      assertTrue(
          exception
              .getMessage()
              .startsWith(EmblReducedFlatFileWriter.REDUCED_FF_WRITE_FAILED_MISSING_CONTIGS));
    }
  }

  /**
   * test for non-CON with contigs + features + sequence
   *
   * @throws IOException
   */
  public void testNonCONContigsFeaturesSequence() throws IOException {
    entry.setDataClass("SET");
    addFeature();
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
    entry.getSequence().setVersion(1);
    entry.getSequence().setTopology(Topology.LINEAR);
    entry.getSequence().setMoleculeType("genomic RNA");
    entry.setIdLineSequenceLength(2);

    LocationFactory locationFactory = new LocationFactory();
    LocalRange localRange = locationFactory.createLocalRange(1L, 2L);
    entry.getSequence().addContig(localRange);

    try {
      StringWriter writer = new StringWriter();
      new EmblReducedFlatFileWriter(entry).write(writer);
      fail(
          "Expected IOException: "
              + EmblReducedFlatFileWriter.REDUCED_FF_WRITE_FAILED_UNEXPECTED_CONTIGS);
    } catch (IOException exception) {
      assertTrue(
          exception
              .getMessage()
              .startsWith(EmblReducedFlatFileWriter.REDUCED_FF_WRITE_FAILED_UNEXPECTED_CONTIGS));
    }
  }

  /** test without sequence */
  public void testNoSequence() {
    addFeature();
    entry.setSequence(null);
    try {
      StringWriter writer = new StringWriter();
      new EmblReducedFlatFileWriter(entry).write(writer);
      fail(
          "Expected IOException: "
              + EmblReducedFlatFileWriter.REDUCED_FF_WRITE_FAILED_MISSING_SEQUENCE);
    } catch (IOException exception) {
      assertTrue(
          exception
              .getMessage()
              .startsWith(EmblReducedFlatFileWriter.REDUCED_FF_WRITE_FAILED_MISSING_SEQUENCE));
    }
  }

  /** test without features */
  public void testSourceFeatureOnly() throws IOException {
    entry.setDataClass("SET");
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(sequenceFactory.createSequenceByte("aa".getBytes()));
    entry.getSequence().setVersion(1);
    entry.getSequence().setTopology(Topology.LINEAR);
    entry.getSequence().setMoleculeType("genomic RNA");
    entry.setIdLineSequenceLength(2);

    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    LocationFactory locationFactory = new LocationFactory();
    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(qualifierFactory.createQualifier("organism", "Homo sapiens"));
    source.getLocations().addLocation(locationFactory.createLocalRange(1L, 4041L, false));
    entry.addFeature(source);

    StringWriter writer = new StringWriter();
    assertTrue(new EmblReducedFlatFileWriter(entry).write(writer));
    assertEquals(
        "ID   DP000153; SV 1; linear; genomic RNA; SET; MAM; 2 BP.\n"
            + "XX\n"
            + "FH   Key             Location/Qualifiers\n"
            + "FH\n"
            + "FT   source          1..4041\n"
            + "FT                   /submitter_seqid=\"\"\n"
            + "XX\n"
            + "SQ   Sequence 2 BP; 2 A; 0 C; 0 G; 0 T; 0 other;\n"
            + "     aa                                                                        2\n"
            + "//\n",
        writer.toString());
  }

  private void addReference() throws IOException {
    ReferenceFactory referenceFactory = new ReferenceFactory();
    Article article =
        referenceFactory.createArticle(
            "Cloning and characterization of a cDNA encoding a novel subtype of rat thyrotropin-releasing hormone receptor",
            "J. Biol. Chem.");
    article.setVolume("273");
    article.setIssue("48");
    article.setFirstPage("32281");
    article.setLastPage("32287");
    article.setYear(FlatFileDateUtils.getDay("10-SEP-1998"));

    EmblLineReader lineReader =
        new EmblLineReader(
            new BufferedReader(
                new StringReader(
                    "RA   Antonellis A., Ayele K., Benjamin B., Blakesley R.W., Boakye A., Bouffard G.G., Brinkley C., Brooks S., Chu G., Coleman H., Engle J., Gestole M., Greene A., Guan X., Gupta J., Haghighi P., Han J., Hansen N., Ho S.-L., Hu P., Hunter G., Hurle B., Idol J.R., Kwong P., Laric P., Larson S., Lee-Lin S.-Q., Legaspi R., Madden M., Maduro Q.L., Maduro V.B., Margulies E.H., Masiello C., Maskeri B., McDowell J., Mojidi H.A., Mullikin J.C., Oestreicher J.S., Park M., Portnoy M.E., Prasad A., Puri O., Reddix-Dugue N., Schandler K., Schueler M.G., Sison C., Stantripop S., Stephen E., Taye A., Thomas J.W., Thomas P.J., Tsipouri V., Ung L., Vogt J.L., Wetherby K.D., Young A., Green E.D.")));
    lineReader.getCache().setPublication(article);
    lineReader.readLine();
    article.setConsortium("Google consortium");
    Reference reference = referenceFactory.createReference(article, 1);
    reference.setComment("reference comment");
    entry.addReference(reference);
  }

  public void addFeature() {
    FeatureFactory featureFactory = new FeatureFactory();
    QualifierFactory qualifierFactory = new QualifierFactory();
    LocationFactory locationFactory = new LocationFactory();
    SourceFeature source = featureFactory.createSourceFeature();
    source.addQualifier(qualifierFactory.createQualifier("organism", "Homo sapiens"));
    source.getLocations().addLocation(locationFactory.createLocalRange(1L, 4041L, false));
    entry.addFeature(source);
    Feature feature = featureFactory.createCdsFeature();

    feature.getLocations().addLocation(locationFactory.createLocalRange(3514L, 4041L, false));

    feature.addQualifier(qualifierFactory.createQualifier("product", "hypothetical protein"));
    feature.addQualifier(qualifierFactory.createQualifier("note", "ORF 5"));
    feature.addQualifier(qualifierFactory.createQualifier("db_xref", "InterPro:IPR001964"));
    feature.addQualifier(
        qualifierFactory.createQualifier("db_xref", "UniProtKB/Swiss-Prot:P09511"));
    feature.addQualifier(qualifierFactory.createQualifier("protein_id", "CAA31466.1"));
    feature.addQualifier(
        qualifierFactory.createQualifier(
            "translation",
            "MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEELFLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFWSSSPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLPRTNSGSSTKAMVLHR"));
    entry.addFeature(feature);
  }
}
