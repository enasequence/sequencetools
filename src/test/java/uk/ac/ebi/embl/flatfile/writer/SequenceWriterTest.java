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
package uk.ac.ebi.embl.flatfile.writer;

import java.io.IOException;
import java.io.StringWriter;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblSequenceWriter;
import uk.ac.ebi.embl.flatfile.writer.embl.EmblWriterTest;

public class SequenceWriterTest extends EmblWriterTest {

  public void testWrite_Sequence() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "atgtgcctacgcccctgaagttttgcttgccgtctatcgatagcatacagcacttatcgattgtctagcctttatatttgacttccagctgacgggcggcaattatattgaatttgttgtggttttcggtggagcgcggtactttcgatgaaaacaagaagatggaacagcacaaaagttggctggtctataccatcggcatgctcacggcgttcctttgcggcgccgtgctctttctgattggcttctttccggcctcctactcggtggcggagaaggaaagcaccgtgccagagggccggcccaccgctctgcttggcatggagtgagtttatgagctgataattgcgtgagatagcgcccctataatgctcgttaccctttacagactgacgccgccgccacctgcctatgactcctttgtgctgttactggtcgacgcgttgcgcgatgattttcccgatgccacgtctatgccggtggcttattctagggcctgc"
                .getBytes()));
    StringWriter writer = new StringWriter();
    assertTrue(new EmblSequenceWriter(entry, entry.getSequence()).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "SQ   Sequence 500 BP; 93 A; 131 C; 133 G; 143 T; 0 other;\n"
            + "     atgtgcctac gcccctgaag ttttgcttgc cgtctatcga tagcatacag cacttatcga        60\n"
            + "     ttgtctagcc tttatatttg acttccagct gacgggcggc aattatattg aatttgttgt       120\n"
            + "     ggttttcggt ggagcgcggt actttcgatg aaaacaagaa gatggaacag cacaaaagtt       180\n"
            + "     ggctggtcta taccatcggc atgctcacgg cgttcctttg cggcgccgtg ctctttctga       240\n"
            + "     ttggcttctt tccggcctcc tactcggtgg cggagaagga aagcaccgtg ccagagggcc       300\n"
            + "     ggcccaccgc tctgcttggc atggagtgag tttatgagct gataattgcg tgagatagcg       360\n"
            + "     cccctataat gctcgttacc ctttacagac tgacgccgcc gccacctgcc tatgactcct       420\n"
            + "     ttgtgctgtt actggtcgac gcgttgcgcg atgattttcc cgatgccacg tctatgccgg       480\n"
            + "     tggcttattc tagggcctgc                                                   500\n",
        writer.toString());
  }

  public void testWrite_LongSequence() throws IOException {
    SequenceFactory sequenceFactory = new SequenceFactory();
    entry.setSequence(
        sequenceFactory.createSequenceByte(
            "argtgcctacgcccctgaagttttgcttgccgtctatcgatagcatacagcacttatcgattgtctagcctttatatttgacttccagctgacgggcggcaattatattgaatttgttgtggttttcggtggagcgcggtactttcgatgaaaacaagaagatggaacagcacaaaagttggctggtctataccatcggcatgctcacggcgttcctttgcggcgccgtgctctttctgattggcttctttccggcctcctactcggtggcggagaaggaaagcaccgtgccagagggccggcccaccgctctgcttggcatggagtgagtttatgagctgataattgcgtgagatagcgcccctataatgctcgttaccctttacagactgacgccgccgccacctgcctatgactcctttgtgctgttactggtcgacgcgttgcgcgatgattttcccgatgccacgtctatgccggatgtgcctacgcccctgaagttttgcttgccgtctatcgatagcatacagcacttatcgattgtctagcctttatatttgacttccagctgacgggcggcaattatattgaatttgttgtggttttcggtggagcgcggtactttcgatgaaaacaagaagatggaacagcacaaaagttggctggtctataccatcggcatgctcacggcgttcctttgcggcgccgtgctctttctgattggcttctttccggcctcctactcggtggcggagaaggaaagcaccgtgccagagggccggcccaccgctctgcttggcatggagtgagtttatgagctgataattgcgtgagatagcgcccctataatgctcgttaccctttacagactgacgccgccgccacctgcctatgactcctttgtgctgttactggtcgacgcgttgcgcgatgattttcccgatgccacgtctatgccggggttttcggtggagcgcggtactttcgatgaaaacaagaagatggaacagcacaaaagttggctggtctataccatcggcatgctcacggcgttcctttgcggcgccgtgctctttctgattggcttctttccggcctcctactcggtggcggagaaggaaagcaccgtgccagagggccggcccaccgctctgcttggcatggagtgagtttatgagctgataattgcgtgagatagcgcccctataatgctcgttaccctttacagactgacgccgccgccacctgcctatgactcctttgtgctgttactggtcgacgcgttgcgcgatgattttcccgatgccacgtctatgccggttgtgctgttactggtcgacgcgttgcgcgatgattttcccgatgccacgtctatgccggtggcttattctagggcct"
                .getBytes()));
    StringWriter writer = new StringWriter();
    assertTrue(new EmblSequenceWriter(entry, entry.getSequence()).write(writer));
    // System.out.print(writer.toString());
    assertEquals(
        "SQ   Sequence 1398 BP; 257 A; 371 C; 379 G; 390 T; 1 other;\n"
            + "     argtgcctac gcccctgaag ttttgcttgc cgtctatcga tagcatacag cacttatcga        60\n"
            + "     ttgtctagcc tttatatttg acttccagct gacgggcggc aattatattg aatttgttgt       120\n"
            + "     ggttttcggt ggagcgcggt actttcgatg aaaacaagaa gatggaacag cacaaaagtt       180\n"
            + "     ggctggtcta taccatcggc atgctcacgg cgttcctttg cggcgccgtg ctctttctga       240\n"
            + "     ttggcttctt tccggcctcc tactcggtgg cggagaagga aagcaccgtg ccagagggcc       300\n"
            + "     ggcccaccgc tctgcttggc atggagtgag tttatgagct gataattgcg tgagatagcg       360\n"
            + "     cccctataat gctcgttacc ctttacagac tgacgccgcc gccacctgcc tatgactcct       420\n"
            + "     ttgtgctgtt actggtcgac gcgttgcgcg atgattttcc cgatgccacg tctatgccgg       480\n"
            + "     atgtgcctac gcccctgaag ttttgcttgc cgtctatcga tagcatacag cacttatcga       540\n"
            + "     ttgtctagcc tttatatttg acttccagct gacgggcggc aattatattg aatttgttgt       600\n"
            + "     ggttttcggt ggagcgcggt actttcgatg aaaacaagaa gatggaacag cacaaaagtt       660\n"
            + "     ggctggtcta taccatcggc atgctcacgg cgttcctttg cggcgccgtg ctctttctga       720\n"
            + "     ttggcttctt tccggcctcc tactcggtgg cggagaagga aagcaccgtg ccagagggcc       780\n"
            + "     ggcccaccgc tctgcttggc atggagtgag tttatgagct gataattgcg tgagatagcg       840\n"
            + "     cccctataat gctcgttacc ctttacagac tgacgccgcc gccacctgcc tatgactcct       900\n"
            + "     ttgtgctgtt actggtcgac gcgttgcgcg atgattttcc cgatgccacg tctatgccgg       960\n"
            + "     ggttttcggt ggagcgcggt actttcgatg aaaacaagaa gatggaacag cacaaaagtt      1020\n"
            + "     ggctggtcta taccatcggc atgctcacgg cgttcctttg cggcgccgtg ctctttctga      1080\n"
            + "     ttggcttctt tccggcctcc tactcggtgg cggagaagga aagcaccgtg ccagagggcc      1140\n"
            + "     ggcccaccgc tctgcttggc atggagtgag tttatgagct gataattgcg tgagatagcg      1200\n"
            + "     cccctataat gctcgttacc ctttacagac tgacgccgcc gccacctgcc tatgactcct      1260\n"
            + "     ttgtgctgtt actggtcgac gcgttgcgcg atgattttcc cgatgccacg tctatgccgg      1320\n"
            + "     ttgtgctgtt actggtcgac gcgttgcgcg atgattttcc cgatgccacg tctatgccgg      1380\n"
            + "     tggcttattc tagggcct                                                    1398\n",
        writer.toString());
  }

  public void testWrite_NoSequence() throws IOException {
    entry.setSequence(null);
    StringWriter writer = new StringWriter();
    assertFalse(new EmblSequenceWriter(entry, entry.getSequence()).write(writer));
    assertEquals("", writer.toString());
  }
}
