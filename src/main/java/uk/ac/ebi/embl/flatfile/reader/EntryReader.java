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
package uk.ac.ebi.embl.flatfile.reader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.EntryFactory;
import uk.ac.ebi.embl.api.entry.feature.Feature;
import uk.ac.ebi.embl.api.entry.sequence.SequenceFactory;

public abstract class EntryReader extends FlatFileEntryReader {

  protected EntryReader(LineReader lineReader) {
    super(lineReader);
    terminatingTags.add("//");
  }

  private final HashMap<String, FlatFileLineReader> reader =
      new HashMap<String, FlatFileLineReader>();

  public static ThreadLocal<HashMap<String, Integer>> blockCounterHolder =
      new ThreadLocal<HashMap<String, Integer>>() {
        public HashMap<String, Integer> initialValue() {
          return new HashMap<String, Integer>();
        }
      };

  public static ThreadLocal<List<String>> skipTagCounterHolder =
      new ThreadLocal<List<String>>() {
        public List<String> initialValue() {
          return new ArrayList<String>();
        }
      };

  public static HashMap<String, Integer> getBlockCounter() {
    return blockCounterHolder.get();
  }

  public static List<String> getSkipTagCounter() {
    return skipTagCounterHolder.get();
  }

  // TODO: check!
  private final HashSet<String> terminatingTags = new HashSet<String>();
  private boolean terminatingAtSourceFeature = false;

  private boolean isEntry;

  private Entry entry;

  protected boolean isCheckBlockCounts = true;

  protected boolean isIgnoreLocationParseError = false;
  public static boolean isOrigin = true;

  protected int currentEntryLine = 1;
  protected int nextEntryLine = currentEntryLine;

  /** @param nextEntryLine the nextEntryLine to set */
  public final void setNextEntryLine(int nextEntryLine) {
    this.nextEntryLine = nextEntryLine;
  }

  protected abstract boolean readFeature(LineReader lineReader, Entry entry) throws IOException;

  protected abstract boolean readSequence(LineReader lineReader, Entry entry) throws IOException;

  protected void addBlockReader(BlockReader blockReader) {
    reader.put(blockReader.getTag(), blockReader);
    HashMap<String, Integer> block = getBlockCounter();
    getBlockCounter().put(blockReader.getTag(), 0);
  }

  protected void addSkipTagCounterHolder(BlockReader blockReader) {
    reader.put(blockReader.getTag(), blockReader);
    getSkipTagCounter().add(blockReader.getTag());
  }

  private void resetSingleBlockReaders() {
    Iterator<String> itr = getBlockCounter().keySet().iterator();
    while (itr.hasNext()) {
      getBlockCounter().put(itr.next(), 0);
    }
  }

  public void addTerminatingTag(String tag) {
    terminatingTags.add(tag);
  }

  public boolean isTerminatingAtSourceFeature() {
    return terminatingAtSourceFeature;
  }

  public static boolean isValidTag(String tag) {
    return getBlockCounter().containsKey(tag);
  }

  public static boolean isSkipTag(String tag) {
    return getSkipTagCounter().contains(tag);
  }

  public void setTerminatingAtSourceFeature(boolean terminatingAtSourceFeature) {
    this.terminatingAtSourceFeature = terminatingAtSourceFeature;
  }

  public void skipLines() throws IOException {
    resetSingleBlockReaders();
    currentEntryLine = nextEntryLine;
    entry = (new EntryFactory()).createEntry();
    entry.setSequence((new SequenceFactory()).createSequence());

    isEntry = lineReader.skipToTerminatingFlag();
    nextEntryLine = lineReader.getCurrentLineNumber();
  }

  public void readLines() throws IOException {
    resetSingleBlockReaders();
    lineReader.readLine();
    isEntry = false;
    currentEntryLine = nextEntryLine;
    entry = (new EntryFactory()).createEntry();
    entry.setSequence((new SequenceFactory()).createSequence());
    OUTER:
    while (true) {
      if (!lineReader.isCurrentLine()) {
        break;
      }

      while (readFeature(lineReader, entry)) {
        if (terminatingAtSourceFeature) {
          List<Feature> list = entry.getFeatures();
          if (null != list) {
            for (Feature fea : list) {
              if (!Feature.SOURCE_FEATURE_NAME.equals(fea.getName())
                  || !lineReader.getNextLine().trim().startsWith(Feature.SOURCE_FEATURE_NAME))
                break OUTER;
            }
          }
        }
        lineReader.readLine();
      }
      try {
        while (readSequence(lineReader, entry)) {
          lineReader.readLine();
        }
      } catch (Exception e) {
        e.printStackTrace();
        String entryname =
            entry.getPrimaryAccession() == null
                ? entry.getSubmitterAccession()
                : entry.getPrimaryAccession();
        if (entryname == null)
          throw new IOException(
              "Invalid Sequence:Failed to read the Sequence at line :"
                  + lineReader.getCurrentLine());
        else
          throw new IOException("Invalid Sequence:Failed to read the Sequence of : " + entryname);
      }

      String tag = lineReader.getCurrentTag();

      if (terminatingTags.contains(tag)) { // tag.equals("//")) { // terminator tag
        lineReader.getCache().resetReferenceCache();
        lineReader.getCache().resetOrganismCache();
        isEntry = true;
        nextEntryLine = lineReader.getCurrentLineNumber();
        break;
      }

      FlatFileLineReader flatFileLineReader = reader.get(tag);

      if (flatFileLineReader != null) {

        if (getBlockCounter().containsKey(tag)) {
          Integer count = getBlockCounter().get(tag);
          getBlockCounter().put(tag, ++count);
        } else {
          warning("FF.8", tag);
        }

        append(flatFileLineReader.read(entry));
      } else {
        error("FF.6", lineReader.getCurrentRawLine());
        // Remove erroneous rows.
        while (lineReader.isNextLine() && lineReader.getNextTag().equals(tag)) {
          lineReader.readLine();
        }
      }
      lineReader.readLine();
    }

    if (isEntry && isCheckBlockCounts) {
      checkBlockCounts(entry);
    }
  }

  protected abstract void checkBlockCounts(Entry entry);

  public boolean isEntry() {
    return isEntry;
  }

  public Entry getEntry() {
    return entry;
  }

  public boolean isCheckBlockCounts() {
    return isCheckBlockCounts;
  }

  public void setCheckBlockCounts(boolean checkBlockCounts) {
    isCheckBlockCounts = checkBlockCounts;
  }

  public boolean isIgnoreLocationParseError() {
    return isIgnoreLocationParseError;
  }

  public void setIgnoreLocationParseError(boolean isIgnoreLocationParseError) {
    this.isIgnoreLocationParseError = isIgnoreLocationParseError;
  }

  /** Release resources used by the reader. Help the GC to cleanup the heap */
  public void close() {
    entry.close();
    getBlockCounter().clear();
    getSkipTagCounter().clear();
    getCache().resetOrganismCache();
    getCache().resetReferenceCache();
  }
}
