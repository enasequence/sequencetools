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

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.feature.FeatureFactory;
import uk.ac.ebi.embl.api.entry.location.LocationFactory;
import uk.ac.ebi.embl.api.entry.qualifier.QualifierFactory;
import uk.ac.ebi.embl.flatfile.EmblPadding;
import uk.ac.ebi.embl.flatfile.writer.FeatureWriter;

public class EmblFeatureWriterTest extends EmblWriterTest {

  public void testWrite_FeatureAndQualifiers() throws IOException {
    FeatureFactory featureFactory = new FeatureFactory();
    Feature feature = featureFactory.createCdsFeature();
    LocationFactory locationFactory = new LocationFactory();
    feature.getLocations().addLocation(locationFactory.createLocalRange(3514L, 4041L, false));
    QualifierFactory qualifierFactory = new QualifierFactory();
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
    feature.addQualifier(
        qualifierFactory.createQualifier(
            "PCR_primers",
            "fwd_name: 27F, fwd_seq: agagtttgatcctggctcag, rev_name: 1492R, rev_seq: acggctaccttgttacgactt"));
    StringWriter writer = new StringWriter();
    boolean sortQualifiers = true;
    new FeatureWriter(
            entry,
            feature,
            sortQualifiers,
            wrapType,
            EmblPadding.FEATURE_PADDING,
            EmblPadding.QUALIFIER_PADDING)
        .write(writer);
    // System.out.print(writer.toString());
    assertEquals(
        "FT   CDS             3514..4041\n"
            + "FT                   /product=\"hypothetical protein\"\n"
            + "FT                   /PCR_primers=\"fwd_name: 27F, fwd_seq: agagtttgatcctggctcag,\n"
            + "FT                   rev_name: 1492R, rev_seq: acggctaccttgttacgactt\"\n"
            + "FT                   /note=\"ORF 5\"\n"
            + "FT                   /db_xref=\"InterPro:IPR001964\"\n"
            + "FT                   /db_xref=\"UniProtKB/Swiss-Prot:P09511\"\n"
            + "FT                   /protein_id=\"CAA31466.1\"\n"
            + "FT                   /translation=\"MEEDDHAGKHDALSALSQWLWSKPLGQHNADLDDDEEVTTGQEEL\n"
            + "FT                   FLPEEQVRARHLFSQKTISREVPAEQSRSGRVYQTARHSLMECSRPTMSIKSQWSFWSS\n"
            + "FT                   SPKPLPKIPVPSLTSWTHTVNSTPFPQLSTSSGSQSPGKGRLQRLTSTERNGTTLPRTN\n"
            + "FT                   SGSSTKAMVLHR\"\n",
        writer.toString());
  }
}
