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
package uk.ac.ebi.embl.api.validation.fixer.sequence;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

import uk.ac.ebi.embl.api.entry.Entry;
import uk.ac.ebi.embl.api.entry.location.Gap;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteLocation;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.entry.sequence.Segment;
import uk.ac.ebi.embl.api.entry.sequence.SegmentFactory;
import uk.ac.ebi.embl.api.validation.*;
import uk.ac.ebi.embl.api.validation.annotation.Description;
import uk.ac.ebi.embl.api.validation.check.entry.EntryValidationCheck;

@Description("Constructed Sequence from CO line")
public class ContigstosequenceFix extends
		EntryValidationCheck
{

	protected final static String CONTIGSTOSEQUENCEFIX_ID = "ContigstosequenceFix";
		
	public ValidationResult check(Entry entry) throws ValidationEngineException
	{

		if(entry==null||FileType.AGP.equals(getEmblEntryValidationPlanProperty().fileType.get()))
		{
			return result;
		}
		if(entry.getSequence()!=null&&entry.getSequence().getSequenceByte()!=null)
		{
			return result;
		}
		if(entry.getSequence()==null||entry.getSequence().getContigs()==null||entry.getSequence().getContigs().size()==0)
		{
			return result;
		}
		
		if(getEntryDAOUtils()==null||FileType.AGP.equals(getEmblEntryValidationPlanProperty().fileType.get()))
			return result;
		
		SegmentFactory segmentFactory=new SegmentFactory();
		segmentFactory.setEntryDAOUtils(getEntryDAOUtils());
	
		try{
			
			int sequenceLength=0;
			
			sequenceLength=(int)entry.getSequence().getLength();
			if(sequenceLength==0)
			sequenceLength=(int)entry.getIdLineSequenceLength();
		ByteBuffer segmentBuffer=ByteBuffer.wrap(new byte[sequenceLength]);
		
		entry.setNonExpandedCON(true);
		
		for (Location contig : entry.getSequence().getContigs())
		{  Segment segment = null;			
			
		    if(contig instanceof RemoteLocation)
			{
				if(contig instanceof RemoteRange)
				{
				   segment=segmentFactory.createSegment((RemoteRange)contig);
				   segmentBuffer.put(segment.getSequenceByte());
				}
				else if(contig instanceof RemoteBase)
				{
					segment=segmentFactory.createSegment((RemoteBase)contig);
					segmentBuffer.put(segment.getSequenceByte());
				}
			}
			else if(contig instanceof Gap)
			{
			     segmentBuffer.put(((Gap) contig).getSequenceByte());
			}
		  }
		 entry.getSequence().setSequence(segmentBuffer);
		 reportMessage(Severity.FIX, entry.getOrigin(),CONTIGSTOSEQUENCEFIX_ID);
		 
		 return result;
		}catch(SQLException | IOException e)
		{
			throw new ValidationEngineException(e);
		}
	}

}
