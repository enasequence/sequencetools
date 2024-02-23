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
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.BufferedReader;
import java.io.RandomAccessFile;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;

public class EmblLineReader extends LineReader {
  public EmblLineReader(BufferedReader reader) {
    super(reader);
  }

  public EmblLineReader(BufferedReader reader, String fileId) {
    super(reader, fileId);
  }

  public EmblLineReader(RandomAccessFile raf, String fileId) {
    super(raf, fileId);
  }

  private static final int DEFAULT_TAG_WIDTH = 5;

  @Override
  protected int getTagWidth(String line) {
    return Math.min(DEFAULT_TAG_WIDTH, line.length());
  }

  private static final Pattern TAG = Pattern.compile("^([A-Z]{2,3}(:? \\*)?).*$");

  private static final int TAG_GROUP = 1;

  // TODO: move to ThreadLocal
  private final Matcher m = TAG.matcher("");

  // Allow EmblLineReader to be used without being initialised by EntryReader.
  private Function<String, Boolean> isValidTag = (tag) -> EntryReader.isValidTag(tag);
  // Allow EmblLineReader to be used without being initialised by EntryReader.
  private Function<String, Boolean> isSkipTag = (tag) -> EntryReader.isSkipTag(tag);

  @Override
  protected boolean isTag(String line) {
    if (line.startsWith(EmblTag.TERMINATOR_TAG)) {
      return true;
    }

    if (m.reset(line).find()) {
      return isValidTag.apply(m.group(TAG_GROUP));
    }

    return false;
  }

  @Override
  protected boolean isSkipLine(String line) {
    if (m.reset(line).find()) {
      if (isSkipTag.apply(m.group(TAG_GROUP))) {
        return true;
      }
    }

    return line.startsWith(EmblTag.XX_TAG);
  }

  public void setIsValidTag(Function<String, Boolean> isValidTag) {
    this.isValidTag = isValidTag;
  }

  public void setIsSkipTag(Function<String, Boolean> isSkipTag) {
    this.isSkipTag = isSkipTag;
  }
}
