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
package uk.ac.ebi.embl.api.entry;

import uk.ac.ebi.embl.api.validation.ValidationEngineException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

public class PolySampleInfo implements Serializable {
  private static final long serialVersionUID = 1L;
  public static final String polysampleInfo = "polysample.info";
  private long sequenceLength;
  private int assemblyLevel;
  private String accession;

  public PolySampleInfo(long sequenceLength, String accession) {
    this.sequenceLength = sequenceLength;
    this.accession = accession;
  }

  public long getSequenceLength() {
    return sequenceLength;
  }

  public int getAssemblyLevel() {
    return assemblyLevel;
  }

  public String getAccession() {
    return accession;
  }

  public void setSequenceLength(long sequenceLength) {
    this.sequenceLength = sequenceLength;
  }

  public void setAssemblyLevel(int assemblyLevel) {
    this.assemblyLevel = assemblyLevel;
  }

  public void setAccession(String accession) {
    this.accession = accession;
  }

  public static void writeMapObject(
          Map<String, String> polysampleInfo, String outputDir, String fileName)
      throws ValidationEngineException {

    try {
      Files.deleteIfExists(Paths.get(outputDir + File.separator + fileName));
    } catch (Exception e) {
      throw new ValidationEngineException(
          "Failed to delete sequence info file: " + e.getMessage(), e);
    }
    try (ObjectOutputStream oos =
        new ObjectOutputStream(new FileOutputStream(outputDir + File.separator + fileName))) {
      oos.writeObject(polysampleInfo);

    } catch (Exception e) {
      throw new ValidationEngineException(
          "Assembly sequence registration failed: " + e.getMessage(), e);
    }
  }

  public static Map<String, String> getMapObject(String inputDir, String fileName)
      throws ValidationEngineException {
    Map<String, String> infoObject = new LinkedHashMap<>();

    if (!Files.exists(Paths.get(inputDir + File.separator + fileName))) return infoObject;

    try (ObjectInputStream oos =
        new ObjectInputStream(new FileInputStream(inputDir + File.separator + fileName))) {
      infoObject = (LinkedHashMap<String, String>) oos.readObject();

    } catch (Exception e) {
      throw new ValidationEngineException(
          "Failed to read assembly sequence information: " + e.getMessage(), e);
    }

    return infoObject;
  }

  public static void writeObject(Object o, String outputDir, String fileName)
      throws ValidationEngineException {

    try {
      Files.deleteIfExists(Paths.get(outputDir + File.separator + fileName));
    } catch (Exception e) {
      throw new ValidationEngineException(
          "Failed to delete file: " + fileName + "\n" + e.getMessage(), e);
    }
    try (ObjectOutputStream oos =
        new ObjectOutputStream(new FileOutputStream(outputDir + File.separator + fileName))) {
      oos.writeObject(o);

    } catch (Exception e) {
      throw new ValidationEngineException("Assembly names registration failed: " + e.getMessage());
    }
  }

  public static Object getObject(String inputDir, String fileName)
      throws ValidationEngineException {
    Object infoObject = null;

    try (ObjectInputStream oos =
        new ObjectInputStream(new FileInputStream(inputDir + File.separator + fileName))) {
      infoObject = oos.readObject();

    } catch (Exception e) {
      throw new ValidationEngineException(
          "Failed to read assembly names information: " + fileName + "\n" + e.getMessage(), e);
    }

    return infoObject;
  }

  @Override
  public String toString() {
    return "AssemblySequenceInfo{"
        + "sequenceLength="
        + sequenceLength
        + ", assemblyLevel="
        + assemblyLevel
        + ", accession='"
        + accession
        + '\''
        + '}';
  }
}
