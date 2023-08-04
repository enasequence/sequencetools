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
package uk.ac.ebi.embl.api.validation.helper;

import java.io.*;
import java.util.zip.GZIPInputStream;
import uk.ac.ebi.embl.api.validation.FileType;

/** Created by IntelliJ IDEA. User: lbower Date: 08-Feb-2012 Time: 16:08:06 */
public class FileUtils {

  public static final String embl_filetoken = "ID";
  public static final String genbank_filetoken = "LOCUS";
  public static final String gff_filetoken = "##gff-version 3";
  public static final String fasta_filetoken = ">";

  public static void copyFile(File src, File dst) throws IOException, SecurityException {

    InputStream in = new FileInputStream(src);
    OutputStream out = new FileOutputStream(dst);

    // Transfer bytes from in to out
    byte[] buf = new byte[1024];
    int len;
    while ((len = in.read(buf)) > 0) {
      out.write(buf, 0, len);
    }
    in.close();
    out.close();
  }

  public static String readFile(InputStream inputStream) throws IOException {

    if (inputStream != null) {
      BufferedInputStream stream = new BufferedInputStream(inputStream);
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      StringBuffer fileData = new StringBuffer(1000);

      char[] buf = new char[1024];
      int numRead = 0;
      while ((numRead = reader.read(buf)) != -1) {
        String readData = String.valueOf(buf, 0, numRead);
        fileData.append(readData);
        buf = new char[1024];
      }
      reader.close();

      return fileData.toString();
    }
    return null;
  }

  public static void moveFile(String oldFileString, String newFileDirString) throws Exception {
    File oldFile = new File(oldFileString);
    File newFileDir = new File(newFileDirString);

    if (!oldFile.exists()) {
      throw new Exception("file does not exist " + oldFileString);
    }

    if (!newFileDir.isDirectory()) {
      throw new Exception("copy file is not a directory " + newFileDirString);
    }

    boolean success = oldFile.renameTo(new File(newFileDir, oldFile.getName()));

    if (!success) {
      throw new Exception("problem moving file\n " + oldFileString + " to \n " + newFileDirString);
    }
  }

  public static String getdeCompressedFile(String file) throws IOException {
    if (file == null) {
      return null;
    }
    File zipFile = new File(file);
    if (!zipFile.exists()) {
      return null;
    }

    if (!zipFile.getName().matches("^.+\\.gz$")) {
      return file;
    }
    byte[] buffer = new byte[1024];
    GZIPInputStream zis = new GZIPInputStream(new FileInputStream(zipFile));
    File tempFile =
        new File(
            zipFile.getParent()
                + File.separator
                + zipFile.getName().substring(0, zipFile.getName().lastIndexOf(".gz")));
    if (!tempFile.exists()) {
      tempFile.createNewFile();
    }

    FileOutputStream fos = new FileOutputStream(tempFile);
    int len;
    while ((len = zis.read(buffer)) > 0) {
      fos.write(buffer, 0, len);
    }

    fos.close();

    zis.close();

    return tempFile.getAbsolutePath();
  }

  public static FileType getFileType(File formatFile) throws IOException {
    BufferedReader fileFormatReader = new BufferedReader(new FileReader(formatFile));
    // FINDING THE TYPE OF FILE BY READING THE FIRST LINE OF THE FILE
    String firstLineofFile = fileFormatReader.readLine();
    fileFormatReader.close();
    if (firstLineofFile.startsWith(embl_filetoken)) {
      return FileType.EMBL;
    } else if (firstLineofFile.startsWith(genbank_filetoken)) {
      return FileType.GENBANK;
    } else if (firstLineofFile.startsWith(gff_filetoken)) {
      return FileType.GFF3;
    } else if (firstLineofFile.startsWith(fasta_filetoken)) {
      return FileType.FASTA;
    }
    return null;
  }
}
