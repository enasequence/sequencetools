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
package uk.ac.ebi.embl.flatfile.writer.embl;

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.Text;

public class CCWriterTest extends EmblWriterTest {

  public void testWriteComment() throws IOException {
    entry.setComment(
        new Text(
            "-------------- Genome Center\n"
                + "     Center: NIH Intramural Sequencing Center\n"
                + "     Center code: NISC\n"
                + "\n"
                + "     Web site: http://www.nisc.nih.gov\n"
                + "     Contact: nisc_zoo@nhgri.nih.gov\n"
                + "All clones contained in this assembly were sequenced by the\nNIH Intramural Sequencing Center (NISC).  This multi-clone\nDNA sequence was assembled by NISC staff."));
    StringWriter writer = new StringWriter();
    assertTrue(new CCWriter(entry).write(writer));
    assertEquals(
        "CC   -------------- Genome Center\n"
            + "CC        Center: NIH Intramural Sequencing Center\n"
            + "CC        Center code: NISC\n"
            + "CC   \n"
            + "CC        Web site: http://www.nisc.nih.gov\n"
            + "CC        Contact: nisc_zoo@nhgri.nih.gov\n"
            + "CC   All clones contained in this assembly were sequenced by the\n"
            + "CC   NIH Intramural Sequencing Center (NISC).  This multi-clone\n"
            + "CC   DNA sequence was assembled by NISC staff.\n",
        writer.toString());
  }

  public void testWriteCommentNoBreaksShorterThanOptimalLineLength() throws IOException {
    entry.setComment(
        new Text(
            "-------------- Genome Center\n"
                + "     Center: NIH Intramural Sequencing Center\n"
                + "All-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC)."));
    StringWriter writer = new StringWriter();
    CCWriter ccWriter = new CCWriter(entry);
    assertTrue(ccWriter.write(writer));
    assertEquals(
        "CC   -------------- Genome Center\n"
            + "CC        Center: NIH Intramural Sequencing Center\n"
            + "CC   All-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).\n",
        writer.toString());
  }

  public void testWriteCommentWithBreaksShorterThanOptimalLineLength() throws IOException {
    entry.setComment(
        new Text(
            "-------------- Genome Center\n"
                + "     Center: NIH Intramural Sequencing Center\n"
                + "All-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center (NISC)."));
    StringWriter writer = new StringWriter();
    CCWriter ccWriter = new CCWriter(entry);
    assertTrue(ccWriter.write(writer));
    assertEquals(
        "CC   -------------- Genome Center\n"
            + "CC        Center: NIH Intramural Sequencing Center\n"
            + "CC   All-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center "
            + "(NISC).\n",
        writer.toString());
  }

  public void testWriteCommentNoBreaksLongerThanOptimalLineLength() throws IOException {
    entry.setComment(
        new Text(
            "-------------- Genome Center\n"
                + "     Center: NIH Intramural Sequencing Center\n"
                + "1-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-1"
                + "2-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-2"
                + "3-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-3"
                + "4-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-4"
                + "5-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-5"));
    StringWriter writer = new StringWriter();
    CCWriter ccWriter = new CCWriter(entry);
    assertTrue(ccWriter.write(writer));
    assertEquals(
        "CC   -------------- Genome Center\n"
            + "CC        Center: NIH Intramural Sequencing Center\n"
            + "CC   1-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-12-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Int\n"
            + "CC   ramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-23-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-3\n"
            + "CC   4-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Intramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-45-clones-contained-in-this-assembly-were-sequenced-by-the-NIH-Int\n"
            + "CC   ramural-Sequencing-Center-(NISC).-This-multi-clone-DNA-sequence-5\n",
        writer.toString());
  }

  public void testWriteNoComment() throws IOException {
    entry.setComment(null);
    StringWriter writer = new StringWriter();
    assertFalse(new CCWriter(entry).write(writer));
    assertEquals("", writer.toString());
  }
}
