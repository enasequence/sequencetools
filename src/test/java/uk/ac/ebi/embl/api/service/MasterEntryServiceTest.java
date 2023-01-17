package uk.ac.ebi.embl.api.service;

import org.junit.Test;
import org.mockito.Mockito;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.Text;
import uk.ac.ebi.embl.api.validation.ValidationEngineException;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EraproDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.submission.Context;
import uk.ac.ebi.embl.api.validation.submission.SubmissionOptions;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class MasterEntryServiceTest {

    @Test // SPLIT AT ANY SPLICHAR FOUND BETWEEN DEFAULT OPTIMAL LENGTH AND MAX LENGTH
    public void formatCommentWithoutBreak() throws  ValidationEngineException, SQLException {
        MasterEntryService masterEntryService = new MasterEntryService();

        String comment =
                "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage " +
                        "Illumina HiSeqX data from a 10X Genomics Chromium library generated at the Wellcome Sanger Institute," +
                        " as well as ~71x coverage HiSeqX data from a Hi-C library prepared by Arima Genomics. " +
                        "An initial PacBio assembly was made using Falcon-unzip, and retained haplotigs were " +
                        "identified using purge_haplotigs. The primary contigs were then scaffolded using the " +
                        "10X data with scaff10x, then scaffolded further using the Hi-C data with SALSA2. " +
                        "Polishing and gap-filling of both the primary scaffolds and haplotigs was performed " +
                        "using the PacBio reads and Arrow, followed by two rounds of Illumina polishing using " +
                        "the 10X data and freebayes. Finally, the assembly was manually improved using gEVAL to " +
                        "correct mis-joins and improve concordance with the raw data. Chromosomes are named according " +
                        "to synteny with the GCA_003309015.1 assembly of Sparus aurata.";

        Entry masterEntry = new Entry();

        masterEntry.setComment(new Text(comment));

        SubmissionOptions submissionOptions = new SubmissionOptions();
        submissionOptions.context = Optional.of(Context.genome);
        submissionOptions.analysisId = Optional.of("analysisId");

        EraproDAOUtils eraproDAOUtils = Mockito.mock(EraproDAOUtilsImpl.class);
        Mockito.when(eraproDAOUtils.createMasterEntry(Mockito.anyString(), Mockito.any())).thenReturn(masterEntry);
        masterEntryService.getMasterEntryFromSubmittedXml(submissionOptions, eraproDAOUtils);


        String expectedComment = "The assembly fSpaAur1.1 is based on ~56x PacBio Sequel data, ~62x coverage\n" +
                "Illumina HiSeqX data from a 10X Genomics Chromium library generated at the\n" +
                "Wellcome Sanger Institute, as well as ~71x coverage HiSeqX data from a Hi-C\n" +
                "library prepared by Arima Genomics. An initial PacBio assembly was made\n" +
                "using Falcon-unzip, and retained haplotigs were identified using\n" +
                "purge_haplotigs. The primary contigs were then scaffolded using the 10X\n" +
                "data with scaff10x, then scaffolded further using the Hi-C data with\n" +
                "SALSA2. Polishing and gap-filling of both the primary scaffolds and\n" +
                "haplotigs was performed using the PacBio reads and Arrow, followed by two\n" +
                "rounds of Illumina polishing using the 10X data and freebayes. Finally, the\n" +
                "assembly was manually improved using gEVAL to correct mis-joins and improve\n" +
                "concordance with the raw data. Chromosomes are named according to synteny\n" +
                "with the GCA_003309015.1 assembly of Sparus aurata.";
        assertEquals(expectedComment, masterEntry.getComment().getText());

    }

    @Test //CAN'T SPLIT (SPLIT AT DEFAULT MAX LANGTH)
    public void formatCommentWithoutBreakAndSpace() throws  ValidationEngineException, SQLException {
        MasterEntryService masterEntryService = new MasterEntryService();

        String comment =
                "A123456789B123456789C123456789D123456789E123456789F123456789G123456789H123456789I123456789J123456789K123456789L123456789";

        Entry masterEntry = new Entry();

        masterEntry.setComment(new Text(comment));

        SubmissionOptions submissionOptions = new SubmissionOptions();
        submissionOptions.context = Optional.of(Context.genome);
        submissionOptions.analysisId = Optional.of("analysisId");

        EraproDAOUtils eraproDAOUtils = Mockito.mock(EraproDAOUtilsImpl.class);
        Mockito.when(eraproDAOUtils.createMasterEntry(Mockito.anyString(), Mockito.any())).thenReturn(masterEntry);
        masterEntryService.getMasterEntryFromSubmittedXml(submissionOptions, eraproDAOUtils);


        String expectedComment = "A123456789B123456789C123456789D123456789E123456789F123456789G123456789H123456789I123456789J123456789K123456789L123456789";
        assertEquals(expectedComment, masterEntry.getComment().getText());

    }
}