package uk.ac.ebi.embl.api.validation.fixer.genomeassembly;

import org.junit.Test;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyInfoEntry;
import uk.ac.ebi.embl.api.entry.genomeassembly.AssemblyType;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.api.validation.ValidationResult;

import static org.junit.Assert.assertEquals;

public class AssemblyTypeFixTest  {

    AssemblyInfoEntry entry = new AssemblyInfoEntry();
    AssemblyTypeFix fix;

    @Test
    public void testAssembyTypeCaseFixedValue() {
        entry.setAssemblyType("metagenome-assembled genome (mag)");
        fix = new AssemblyTypeFix();
        ValidationResult result = fix.check(entry);
        assertEquals(1, result.getMessages("AssemblyinfoAssemblyTypeFix").size());
        assertEquals(AssemblyType.METAGENOME_ASSEMBLEDGENOME.getFixedValue(), entry.getAssemblyType());

        entry.setAssemblyType("Covid-19 outbreak");
        fix = new AssemblyTypeFix();
        result = fix.check(entry);
        assertEquals(1, result.getMessages("AssemblyinfoAssemblyTypeFix").size());
        assertEquals(AssemblyType.COVID_19_OUTBREAK.getFixedValue(), entry.getAssemblyType());

        entry.setAssemblyType("Environmental single-cell amplified genome (SAG)");
        fix = new AssemblyTypeFix();
        result = fix.check(entry);
        assertEquals(1, result.getMessages("AssemblyinfoAssemblyTypeFix").size());
        assertEquals(AssemblyType.ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME.getFixedValue(), entry.getAssemblyType());
    }

    @Test
    public void testAssembyTypeCaseNoFix() {
        entry.setAssemblyType("Metagenome-Assembled Genome (MAG)");
        fix = new AssemblyTypeFix();
        ValidationResult result = fix.check(entry);
        assertEquals(0, result.getMessages(Severity.FIX).size());
        assertEquals(AssemblyType.METAGENOME_ASSEMBLEDGENOME.getFixedValue(), entry.getAssemblyType());

        entry.setAssemblyType("COVID-19 outbreak");
        fix = new AssemblyTypeFix();
        result = fix.check(entry);
        assertEquals(0, result.getMessages(Severity.FIX).size());
        assertEquals(AssemblyType.COVID_19_OUTBREAK.getFixedValue(), entry.getAssemblyType());
        assertEquals("COVID-19 outbreak", AssemblyType.COVID_19_OUTBREAK.getFixedValue(), entry.getAssemblyType());

        entry.setAssemblyType("Environmental Single-Cell Amplified Genome (SAG)");
        fix = new AssemblyTypeFix();
        result = fix.check(entry);
        assertEquals(0, result.getMessages(Severity.FIX).size());
        assertEquals(AssemblyType.ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME.getFixedValue(), entry.getAssemblyType());
        assertEquals("Environmental Single-Cell Amplified Genome (SAG)", AssemblyType.ENVIRONMENTALSINGLE_CELLAMPLIFIEDGENOME.getFixedValue());
    }

}