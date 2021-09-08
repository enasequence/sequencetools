package uk.ac.ebi.embl.api.validation;

import uk.ac.ebi.embl.api.entry.AssemblySequenceInfo;

import java.util.*;

public class GenomeUtils {

    public final static Long COVID_19_OUTBREAK_GENOME_MAX_SIZE = 31000L; //bp

    public static Long calculateGenomeSize(Map<String, AssemblySequenceInfo> sequenceInfo, Set<String> agpPlacedComponents) throws ValidationEngineException {
        long genomeSize = 0;
        for (Map.Entry<String, AssemblySequenceInfo> entry : sequenceInfo.entrySet()) {
            AssemblySequenceInfo info = entry.getValue();

            if (info.getAssemblyLevel() == 0 || info.getAssemblyLevel() == 1) {
                if (!agpPlacedComponents.contains(entry.getKey())) {
                    genomeSize += info.getSequenceLength();
                }
            } else if (info.getAssemblyLevel() == 2) {
                genomeSize += info.getSequenceLength();
            } else throw new ValidationEngineException("Unexpected assembly level", ValidationEngineException.ReportErrorType.VALIDATION_ERROR);
        }
        return(genomeSize);
    }

}
