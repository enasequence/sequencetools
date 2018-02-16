/*******************************************************************************
 * Copyright 2012 EMBL-EBI, Hinxton outstation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package uk.ac.ebi.embl.flatfile.reader.embl;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.BlockReader;
import uk.ac.ebi.embl.flatfile.reader.FeatureReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SequenceReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

public class EBISearchEmblEntryReader extends EmblEntryReader {

   private List<String> EXACTLY_ONCE_BLOCKS      = Arrays.asList(EmblTag.ID_TAG, EmblTag.AC_TAG, EmblTag.DE_TAG);

   private List<String> NONE_OR_ONCE_BLOCKS      = Arrays.asList(EmblTag.ST_STAR_TAG,
                                                                 EmblTag.DT_TAG,
                                                                 EmblTag.KW_TAG,
                                                                 EmblTag.PR_TAG,
                                                                 EmblTag.SQ_TAG,
                                                                 EmblTag.AH_TAG,
                                                                 EmblTag.CO_TAG,
                                                                 EmblTag.MASTER_CON_TAG,
                                                                 EmblTag.MASTER_WGS_TAG,
                                                                 EmblTag.MASTER_TPA_TAG);
   private boolean      skipSourceFeature        = false;

   private boolean      htmlEntValidationEnabled = true;
   private boolean      conSeqDatacheckEnabled   = true;

   public EBISearchEmblEntryReader(BufferedReader reader, Charset charset, EmblEntryReader.Format format, String fileId,
                    boolean ignoreParseError, boolean htmlEntityValidationEnabled) {

      super(reader, charset, format, fileId);

      this.htmlEntValidationEnabled = htmlEntityValidationEnabled;
      initializeBlockReaders(format);
   }
   
   private void initializeBlockReaders(Format format) {
      if (format.equals(Format.EMBL_FORMAT)) {
         addBlockReader(new OSTollerantReader(lineReader));
         addBlockReader(new RATollerantReader(lineReader));
      }
      else if (format.equals(Format.CDS_FORMAT)) {
         addBlockReader(new OSTollerantReader(lineReader));
         EXACTLY_ONCE_BLOCKS = Arrays.asList(EmblTag.ID_TAG, EmblTag.DE_TAG);
      }
      else if (format.equals(Format.NCR_FORMAT)) {
         addBlockReader(new OSTollerantReader(lineReader));
         EXACTLY_ONCE_BLOCKS = Arrays.asList(EmblTag.ID_TAG, EmblTag.DE_TAG);
      }
   }

   public boolean isConSeqDatacheckEnabled() {
      return conSeqDatacheckEnabled;
   }

   public void setConSeqDatacheckEnabled(boolean conSeqDatacheckEnabled) {
      this.conSeqDatacheckEnabled = conSeqDatacheckEnabled;
   }

   @Override
   protected BlockReader addBlockReader(BlockReader blockReader) {
      blockReader.enableHtmlEntityValidation(htmlEntValidationEnabled);
      return super.addBlockReader(blockReader);
   }

   @Override
   protected boolean readFeature(LineReader lineReader, Entry entry) throws IOException {

      if (lineReader.getCurrentTag().equals(EmblTag.FT_TAG)) {
         append((new FeatureReader(lineReader, skipSourceFeature, htmlEntValidationEnabled)).read(entry));
         Integer count = getBlockCounter().get(EmblTag.FT_TAG);
         getBlockCounter().put(EmblTag.FT_TAG, ++count);
         return true;
      }
      return false;
   }

   @Override
   protected boolean readSequence(LineReader lineReader, Entry entry) throws IOException {

      if (lineReader.getActiveTag().equals(EmblTag.SQ_TAG) && !lineReader.isCurrentTag()) {
         append((new SequenceReader(lineReader)).read(entry));
         return true;
      }
      return false;
   }

   protected void checkBlockCounts(Entry entry) {
      boolean citationExists = true;

      if (entry.getDataClass() != null) {
         if (conSeqDatacheckEnabled && entry.getDataClass().equals(Entry.CON_DATACLASS) && getBlockCounter().get(EmblTag.CO_TAG) == null) {
            validationResult.append(FlatFileValidations.message(lineReader, Severity.ERROR, "FF.11"));
         }
         if (!entry.getDataClass().equals(Entry.CON_DATACLASS) && getBlockCounter().get(EmblTag.SQ_TAG) == null) {
            validationResult.append(FlatFileValidations.message(lineReader, Severity.ERROR, "FF.12"));
         }
      }

      for (String tag : getBlockCounter().keySet()) {
         Integer count = getBlockCounter().get(tag);

         if (EXACTLY_ONCE_BLOCKS.contains(tag) && count != 1) {
            validationResult.append(FlatFileValidations.message(lineReader, Severity.ERROR, "FF.5", tag));
         }

         if (NONE_OR_ONCE_BLOCKS.contains(tag) && count > 1) {
            validationResult.append(FlatFileValidations.message(lineReader, Severity.ERROR, "FF.9", tag));
         }
      }

      if (!citationExists) {
         validationResult.append(FlatFileValidations.message(lineReader, Severity.ERROR, "FF.13"));
      }
   }
}
