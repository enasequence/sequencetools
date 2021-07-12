package uk.ac.ebi.embl.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

public class CommonUtil {

    public static BufferedReader bufferedReaderFromFile(File file) throws FileNotFoundException, IOException {
        if (file.getName().matches("^.+\\.(gz|gzip)$")) {
            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file));
            return new BufferedReader(new InputStreamReader(gzip));
        } else if (file.getName().matches("^.+\\.(bz2|bzip2)$")) {
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(new FileInputStream(file));
            return new BufferedReader(new InputStreamReader(bzIn));
        } else {
            return new BufferedReader(new FileReader(file));
        }
    }
}
