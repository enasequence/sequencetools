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
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.validation.Severity;
import uk.ac.ebi.embl.flatfile.EmblTag;
import uk.ac.ebi.embl.flatfile.reader.EntryReader;
import uk.ac.ebi.embl.flatfile.reader.FeatureReader;
import uk.ac.ebi.embl.flatfile.reader.LineReader;
import uk.ac.ebi.embl.flatfile.reader.SequenceReader;
import uk.ac.ebi.embl.flatfile.validation.FlatFileValidations;

public class 
EmblEntryReader extends EntryReader
{

  // private static List<String> AT_LEAST_ONCE_BLOCKS = Arrays.asList( EmblTag.RN_TAG, EmblTag.RT_TAG, EmblTag.RL_TAG );

    private static List<String> EXACTLY_ONCE_BLOCKS  = Arrays.asList( EmblTag.ID_TAG, EmblTag.AC_TAG, EmblTag.DE_TAG );

    private static List<String> NONE_OR_ONCE_BLOCKS  = Arrays.asList( EmblTag.ST_STAR_TAG, EmblTag.DT_TAG, EmblTag.KW_TAG, EmblTag.PR_TAG, EmblTag.SQ_TAG, EmblTag.AH_TAG, EmblTag.CO_TAG, EmblTag.MASTER_CON_TAG, EmblTag.MASTER_WGS_TAG,
         EmblTag.MASTER_TPA_TAG);
    private boolean skipSourceFeature=false;


    public enum 
    Format
    {
        EMBL_FORMAT, 
        CDS_FORMAT, 
        MASTER_FORMAT, 
        EPO_FORMAT,
        NCR_FORMAT,
        ASSEMBLY_FILE_FORMAT
   };

//TODO: delete!
   public 
   EmblEntryReader( BufferedReader reader )
   {

		this(reader, Format.EMBL_FORMAT, null);
	}
			
	public 
	EmblEntryReader( BufferedReader reader,
	         Format         format, 
	         String         fileId )
	{
	
		super(new EmblLineReader(reader, fileId));

		addBlockReaders(format);
	}
   

	public 
	EmblEntryReader( BufferedReader reader,
					 Charset        charset,
	                 Format         format, 
	                 String         fileId )
	{
	
		super(new EmblLineReader(reader, charset, fileId));
	
		addBlockReaders(format);
	}
    
    public 
    EmblEntryReader( BufferedReader reader, 
                     Format         format, 
                     String         fileId,
                     boolean ignoreParseError)
    {

      super(new EmblLineReader(reader, fileId).setIgnoreParseError(ignoreParseError));

      addBlockReaders(format);
   }
    
    public 
    EmblEntryReader( BufferedReader reader, 
    				 Charset        charset,
                     Format         format, 
                     String         fileId,
                     boolean ignoreParseError)
    {

      super(new EmblLineReader(reader, charset, fileId).setIgnoreParseError(ignoreParseError));

      addBlockReaders(format);
   }

    public EmblEntryReader(RandomAccessFile raf) {

      this(raf, Format.EMBL_FORMAT, null);
   }

   public EmblEntryReader(RandomAccessFile raf, Format format, String fileId) {
      super(new EmblLineReader(raf, fileId));
      addBlockReaders(format);
   }


    private void 
    addBlockReaders( Format format )
    {
   
        if( format.equals( Format.EMBL_FORMAT ) )
        {
         addBlockReader(new IDReader(lineReader));
         addBlockReader(new ACReader(lineReader));
         addBlockReader(new DEReader(lineReader));
         addBlockReader(new KWReader(lineReader));
         addBlockReader(new DTReader(lineReader));
         addBlockReader(new PRReader(lineReader));
         addBlockReader(new ACStarReader(lineReader));
         addBlockReader(new STStarReader(lineReader));
         addBlockReader(new COReader(lineReader));
         addBlockReader(new SQReader(lineReader));
         addBlockReader(new AHReader(lineReader));
         addBlockReader(new FHReader(lineReader));
         addBlockReader(new CCReader(lineReader));
         addBlockReader(new DRReader(lineReader));
         addBlockReader(new OSReader(lineReader));
         addBlockReader(new OCReader(lineReader));
         addBlockReader(new OGReader(lineReader));
         addBlockReader(new ASReader(lineReader));
         addBlockReader(new RAReader(lineReader));
         addBlockReader(new RCReader(lineReader));
         addBlockReader(new RGReader(lineReader));
         addBlockReader(new RLReader(lineReader));
         addBlockReader(new RNReader(lineReader));
         addBlockReader(new RPReader(lineReader));
         addBlockReader(new RTReader(lineReader));
         addBlockReader(new RXReader(lineReader));
        } else if( format.equals( Format.MASTER_FORMAT ) )
        {
         addBlockReader(new IDReader(lineReader));
         addBlockReader(new ACReader(lineReader));
         addBlockReader(new DEReader(lineReader));
         addBlockReader(new KWReader(lineReader));
         addBlockReader(new DTReader(lineReader));
         addBlockReader(new PRReader(lineReader));
         addBlockReader(new FHReader(lineReader));
         addBlockReader(new CCReader(lineReader));
         addBlockReader(new DRReader(lineReader));
         addBlockReader(new OSReader(lineReader));
         addBlockReader(new OCReader(lineReader));
         addBlockReader(new OGReader(lineReader));
         addBlockReader(new RAReader(lineReader));
         addBlockReader(new RCReader(lineReader));
         addBlockReader(new RGReader(lineReader));
         addBlockReader(new RLReader(lineReader));
         addBlockReader(new RNReader(lineReader));
         addBlockReader(new RPReader(lineReader));
         addBlockReader(new RTReader(lineReader));
         addBlockReader(new RXReader(lineReader));
         addBlockReader(new MasterWGSReader(lineReader));
         addBlockReader(new MasterCONReader(lineReader));
         addBlockReader(new MasterTPAReader(lineReader));
         addBlockReader(new MasterTSAReader(lineReader));
        } else if( format.equals( Format.CDS_FORMAT ) )
        {
         addBlockReader(new IDReader(lineReader));
         addBlockReader(new PAReader(lineReader));
         addBlockReader(new DTReader(lineReader));
         addBlockReader(new DEReader(lineReader));
         addBlockReader(new KWReader(lineReader));
         addBlockReader(new SQReader(lineReader));
         addBlockReader(new FHReader(lineReader));
         addBlockReader(new DRReader(lineReader));
         addBlockReader(new OSReader(lineReader));
         addBlockReader(new OCReader(lineReader));
         addBlockReader(new OGReader(lineReader));
         addBlockReader(new OXReader(lineReader));
         addBlockReader(new PRReader(lineReader));
         addBlockReader(new RAReader(lineReader));
         addBlockReader(new RCReader(lineReader));
         addBlockReader(new RGReader(lineReader));
         addBlockReader(new RLReader(lineReader));
         addBlockReader(new RNReader(lineReader));
         addBlockReader(new RPReader(lineReader));
         addBlockReader(new RTReader(lineReader));
         addBlockReader(new RXReader(lineReader));
        } else if( format.equals( Format.EPO_FORMAT ) )
        {
         addBlockReader(new IDReader(lineReader));
         addBlockReader(new ACReader(lineReader));
         addBlockReader(new SQReader(lineReader));
         addBlockReader(new FHReader(lineReader));
         addBlockReader(new OSReader(lineReader));
         addBlockReader(new RAReader(lineReader));
         addBlockReader(new RLReader(lineReader));
         addBlockReader(new RNReader(lineReader));
         addBlockReader(new RTReader(lineReader));
      } else if( format.equals( Format.NCR_FORMAT ) )
        {
         addBlockReader(new IDReader(lineReader,true));
         addBlockReader(new PAReader(lineReader));
         addBlockReader(new ACReader(lineReader));
         addBlockReader(new DEReader(lineReader));
         addBlockReader(new KWReader(lineReader));
         addBlockReader(new DTReader(lineReader));
         addBlockReader(new PRReader(lineReader));
         addBlockReader(new ACStarReader(lineReader));
         addBlockReader(new STStarReader(lineReader));
         addBlockReader(new COReader(lineReader));
         addBlockReader(new SQReader(lineReader));
         addBlockReader(new AHReader(lineReader));
         addBlockReader(new FHReader(lineReader));
         addBlockReader(new CCReader(lineReader));
         addBlockReader(new DRReader(lineReader));
         addBlockReader(new OSReader(lineReader));
         addBlockReader(new OCReader(lineReader));
         addBlockReader(new OGReader(lineReader));
         addBlockReader(new ASReader(lineReader));
         addBlockReader(new RAReader(lineReader));
         addBlockReader(new RCReader(lineReader));
         addBlockReader(new RGReader(lineReader));
         addBlockReader(new RLReader(lineReader));
         addBlockReader(new RNReader(lineReader));
         addBlockReader(new RPReader(lineReader));
         addBlockReader(new RTReader(lineReader));
         addBlockReader(new RXReader(lineReader));
        } else if( format.equals( Format.ASSEMBLY_FILE_FORMAT ) )
        {
            addBlockReader(new IDReader(lineReader));
            addSkipTagCounterHolder(new ACReader(lineReader));
            addSkipTagCounterHolder(new PRReader(lineReader));
            addSkipTagCounterHolder(new DEReader(lineReader));
            addSkipTagCounterHolder(new KWReader(lineReader));
            addBlockReader(new DTReader(lineReader));
            addBlockReader(new ACStarReader(lineReader));
            addSkipTagCounterHolder(new STStarReader(lineReader));
            addBlockReader(new COReader(lineReader));
            addBlockReader(new SQReader(lineReader));
            addBlockReader(new AHReader(lineReader));
            addBlockReader(new FHReader(lineReader));
            addSkipTagCounterHolder(new CCReader(lineReader));
            addSkipTagCounterHolder(new DRReader(lineReader));
            addSkipTagCounterHolder(new OSReader(lineReader));
            addSkipTagCounterHolder(new OCReader(lineReader));
            addSkipTagCounterHolder(new OGReader(lineReader));
            addBlockReader(new ASReader(lineReader));
            addSkipTagCounterHolder(new RAReader(lineReader));
            addSkipTagCounterHolder(new RCReader(lineReader));
            addSkipTagCounterHolder(new RGReader(lineReader));
            addSkipTagCounterHolder(new RLReader(lineReader));
            addSkipTagCounterHolder(new RNReader(lineReader));
            addSkipTagCounterHolder(new RPReader(lineReader));
            addSkipTagCounterHolder(new RTReader(lineReader));
            addSkipTagCounterHolder(new RXReader(lineReader));
            skipSourceFeature=true;
         }
        

      /**
       * Have to add the line types separately as are not registered with the
       * main hash of readers.
       */
      getBlockCounter().put(EmblTag.FT_TAG, 0);

        if( !format.equals( Format.MASTER_FORMAT ) )
        {
         getBlockCounter().put(EmblTag.SQ_TAG, 0);
      }
   }

   @Override
    protected boolean 
    readFeature( LineReader lineReader, Entry entry ) throws IOException
    {

        if( lineReader.getCurrentTag().equals( EmblTag.FT_TAG ) )
        {
         append((new FeatureReader(lineReader,skipSourceFeature)).read(entry));
         Integer count = getBlockCounter().get(EmblTag.FT_TAG);
         getBlockCounter().put(EmblTag.FT_TAG, ++count);
         return true;
      }
      return false;
   }

   @Override
    protected boolean 
    readSequence( LineReader lineReader, Entry entry ) throws IOException
    {

        if( lineReader.getActiveTag().equals( EmblTag.SQ_TAG ) && !lineReader.isCurrentTag() )
        {
         append((new SequenceReader(lineReader)).read(entry));
         return true;
      }
      return false;
   }


    protected void 
    checkBlockCounts( Entry entry )
    {
      boolean citationExists = true;

        if( entry.getDataClass() != null )
        {
            if( entry.getDataClass().equals( Entry.CON_DATACLASS ) && getBlockCounter().get( EmblTag.CO_TAG ) == null )
            {
                validationResult.append( FlatFileValidations.message( lineReader, Severity.ERROR, "FF.11" ) );
         }
            if( !entry.getDataClass().equals( Entry.CON_DATACLASS ) && getBlockCounter().get( EmblTag.SQ_TAG ) == null )
            {
                validationResult.append( FlatFileValidations.message( lineReader, Severity.ERROR, "FF.12" ) );
         }
      }

        for( String tag : getBlockCounter().keySet() )
        {
         Integer count = getBlockCounter().get(tag);

          /*  if( AT_LEAST_ONCE_BLOCKS.contains( tag ) && count < 1 )
            {
                if( tag.equals( EmblTag.RL_TAG ) || tag.equals( EmblTag.RN_TAG ) || tag.equals( EmblTag.RT_TAG ) )
               citationExists = false;
            else
                    validationResult.append( FlatFileValidations.message( lineReader, Severity.ERROR, "FF.7", tag ) );
         }*/

            if( EXACTLY_ONCE_BLOCKS.contains( tag ) && count != 1 )
            {
                validationResult.append( FlatFileValidations.message( lineReader, Severity.ERROR, "FF.5", tag ) );
         }

            if( NONE_OR_ONCE_BLOCKS.contains( tag ) && count > 1 )
            {
                validationResult.append( FlatFileValidations.message( lineReader, Severity.ERROR, "FF.9", tag ) );
         }
      }
      if (!citationExists)
            validationResult.append( FlatFileValidations.message( lineReader, Severity.ERROR, "FF.13" ) );

   }
}
