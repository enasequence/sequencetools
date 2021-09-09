package uk.ac.ebi.embl.api.validation;

import org.junit.Test;
import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class GenomeUtilsTest {

    Map<String, AssemblySequenceInfo> sequenceInfo = new LinkedHashMap();
    Set<String> agpPlacedComponents = new HashSet<>();

    @Test
    public void testInvalidAssemblyLevel() {
        sequenceInfo.put("contig1", new AssemblySequenceInfo(100, -1, "contig1"));
        assertThrows("Unexpected assembly level", ValidationEngineException.class, () -> GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents));
    }

    @Test
    public void testContigsOnly() throws ValidationEngineException {
        sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1"));
        sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2"));
        sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3"));
        assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 100 + 150 + 200);
    }

    @Test
    public void testChromosomesOnly() throws ValidationEngineException {
        sequenceInfo.put("chr1", new AssemblySequenceInfo(100, 2, "chr1"));
        assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 100);
    }

    @Test
    public void testContigsScaffolds_PlacedContigs() throws ValidationEngineException {
        sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1"));
        sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2"));
        sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3"));

        // AGP
        // scaffold1 contig1
        // scaffold1 gap
        // scaffold1 contig2
        // scaffold2 contig3
        sequenceInfo.put("scaffold1", new AssemblySequenceInfo(250, 1, "scaffold1"));
        sequenceInfo.put("scaffold2", new AssemblySequenceInfo(200, 1, "scaffold2"));

        agpPlacedComponents.add("contig1");
        agpPlacedComponents.add("contig2");
        agpPlacedComponents.add("contig3");

        assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 250 + 200);
    }

    @Test
    public void testContigsScaffolds_PlacedContigsAndScaffolds() throws ValidationEngineException {
        sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1"));
        sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2"));
        sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3"));
        sequenceInfo.put("contig4", new AssemblySequenceInfo(300, 0, "contig4"));

        // AGP
        // scaffold1 contig1
        // scaffold1 gap
        // scaffold1 contig2
        // scaffold2 contig3
        // scaffold3 scaffold2
        sequenceInfo.put("scaffold1", new AssemblySequenceInfo(250, 1, "scaffold1"));
        sequenceInfo.put("scaffold2", new AssemblySequenceInfo(200, 1, "scaffold2"));
        sequenceInfo.put("scaffold3", new AssemblySequenceInfo(200, 1, "scaffold3"));

        agpPlacedComponents.add("contig1");
        agpPlacedComponents.add("contig2");
        agpPlacedComponents.add("contig3");
        agpPlacedComponents.add("scaffold2");

        assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 250 + 200 + 300);
    }

    @Test
    public void testContigsScaffoldsChromosomes() throws ValidationEngineException {
        sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1")); //placed
        sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2")); //placed
        sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3")); //placed
        sequenceInfo.put("contig4", new AssemblySequenceInfo(300, 0, "contig4")); //placed

        // AGP
        // scaffold1 contig1
        // scaffold1 gap
        // scaffold1 contig2
        // scaffold2 contig3
        // chr1 contig4
        // chr1 scaffold2
        sequenceInfo.put("scaffold1", new AssemblySequenceInfo(250, 1, "scaffold1")); //unplaced
        sequenceInfo.put("scaffold2", new AssemblySequenceInfo(200, 1, "scaffold2")); //placed

        sequenceInfo.put("chr1", new AssemblySequenceInfo(250, 2, "chr1"));

        agpPlacedComponents.add("contig1");
        agpPlacedComponents.add("contig2");
        agpPlacedComponents.add("contig3");
        agpPlacedComponents.add("contig4");
        agpPlacedComponents.add("scaffold2");

        assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 250 + 250);
    }

    @Test
    public void testContigsChromosomes() throws ValidationEngineException {
        sequenceInfo.put("contig1", new AssemblySequenceInfo(100, 0, "contig1"));
        sequenceInfo.put("contig2", new AssemblySequenceInfo(150, 0, "contig2"));
        sequenceInfo.put("contig3", new AssemblySequenceInfo(200, 0, "contig3"));
        sequenceInfo.put("contig4", new AssemblySequenceInfo(300, 0, "contig4"));

        // AGP
        // chr1 contig1
        // chr1 contig2
        // chr1 contig3
        // chr1 contig4
        sequenceInfo.put("chr1", new AssemblySequenceInfo(600, 2, "chr1"));

        agpPlacedComponents.add("contig1");
        agpPlacedComponents.add("contig2");
        agpPlacedComponents.add("contig3");
        agpPlacedComponents.add("contig4");

        assertTrue(GenomeUtils.calculateGenomeSize(sequenceInfo, agpPlacedComponents) == 600);
    }
}
