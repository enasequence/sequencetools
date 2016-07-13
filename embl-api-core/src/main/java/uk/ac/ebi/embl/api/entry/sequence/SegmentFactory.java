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
package uk.ac.ebi.embl.api.entry.sequence;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import uk.ac.ebi.embl.api.entry.location.CompoundLocation;
import uk.ac.ebi.embl.api.entry.location.LocalBase;
import uk.ac.ebi.embl.api.entry.location.LocalRange;
import uk.ac.ebi.embl.api.entry.location.RemoteBase;
import uk.ac.ebi.embl.api.entry.location.RemoteRange;
import uk.ac.ebi.embl.api.entry.location.Location;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtils;
import uk.ac.ebi.embl.api.validation.dao.EntryDAOUtilsImpl;
import uk.ac.ebi.embl.api.validation.helper.location.LocationToStringCoverter;

public class SegmentFactory {
		
	private Connection connection;
	private EntryDAOUtils entryDAOUtils;
	public SegmentFactory()
	{
		this(null);
	}
	
	public SegmentFactory(Connection connection)
	{
		this.connection=connection;
	}
	
	public Segment createSegment(Sequence sequence, LocalBase localBase) {
		if (localBase.isComplement()) {
			return new Segment(localBase, 
					sequence.getReverseComplementSequenceByte(localBase));
		}
		else {
			return new Segment(localBase, sequence.getSequenceByte(localBase));
		}
	}
  
	
	public Segment createSegment(Sequence sequence, LocalRange localRange) {
		if (localRange.isComplement()) {
			return new Segment(localRange, 
					sequence.getReverseComplementSequenceByte(localRange));
		}
		else {
			return new Segment(localRange, sequence.getSequenceByte(localRange));
		}
	}
	
	
	public Segment createSegment(Sequence sequence, CompoundLocation<Location> compoundLocation) throws SQLException, IOException {

		if(compoundLocation==null)
			return null;
		
		if(compoundLocation.hasRemoteLocation()&&connection==null&&entryDAOUtils==null)
		{
			return null;
		}
		
		ByteBuffer segementBuffer=ByteBuffer.wrap(new byte[(int)compoundLocation.getLength()]);
		
		for (Location location : compoundLocation.getLocations()) {
			Segment segment = null;			
			if (location instanceof LocalBase) {
				segment = createSegment(sequence, (LocalBase)location);
			}
			else if (location instanceof LocalRange) {
				segment = createSegment(sequence, (LocalRange)location);
			}
			else if (location instanceof RemoteBase) {
				segment = createSegment((RemoteBase)location);
			}
			else if (location instanceof RemoteRange) {
				segment = createSegment((RemoteRange)location);
			}
			if (segment != null && segment.getSequenceByte() != null) {
				byte[] segByte=segment.getSequenceByte();
				segementBuffer.put(segByte);
			}
		}
		
		byte[]  segmentSeq=((ByteBuffer) ByteBuffer.wrap( Arrays.copyOf( segementBuffer.array(), segementBuffer.position() ) )).array();
		if(segmentSeq.length==0)
		{
			segmentSeq=null;
		}
		if (compoundLocation.isComplement()) {
			return new Segment(compoundLocation,
					(new ReverseComplementer()).reverseComplementByte(segmentSeq));
		}
		else {
			return new Segment(compoundLocation, segmentSeq);
		}
	}
	
	// TODO createSegment for RemoteBase
	public Segment createSegment(RemoteBase remoteBase) throws SQLException, IOException {
		
		if (connection == null&&entryDAOUtils==null)
			return null;
		else
		{
			if (remoteBase != null)
			{
				String accession = remoteBase.getAccession() + (remoteBase.getVersion() == null ? "" : ("." + remoteBase.getVersion().toString()));
				if(entryDAOUtils==null)
				{
					setEntryDAOUtils(new EntryDAOUtilsImpl(connection));
				}
				byte[] subSequence=entryDAOUtils.getSubSequence(accession, remoteBase.getBeginPosition(), (remoteBase.getEndPosition()-remoteBase.getBeginPosition())+1);
				
				if(subSequence==null)
				{
					throw new SQLException("Invalid Accession "+accession+" , which does not exist in database");
				}
				
				if(remoteBase.getLength()!=subSequence.length)
				{
					StringBuilder lcoationString=new StringBuilder();
					LocationToStringCoverter.renderLocation(lcoationString, remoteBase, false, false);
					throw new SQLException("invalid Remote Base:"+ lcoationString.toString() +", not within the entry \""+ remoteBase.getAccession()+"\" sequence length");
				}
				if(remoteBase.isComplement())
				{
					ReverseComplementer reversecomplementer=new ReverseComplementer();
					return new Segment(remoteBase,reversecomplementer.reverseComplementByte(subSequence));
				}
				return new Segment(
						remoteBase,subSequence);

			}
		}
		return null;	
	}

	// TODO createSegment for RemoteRange
	public Segment createSegment(RemoteRange remoteRange)
			throws SQLException, IOException
	{
		if (connection == null&&entryDAOUtils==null)
			return null;
		else
		{
			if (remoteRange != null)
			{
				String accession = remoteRange.getAccession() + (remoteRange.getVersion() == null ? "" : ("." + remoteRange.getVersion().toString()));
				if(entryDAOUtils==null)
				{
					setEntryDAOUtils(new EntryDAOUtilsImpl(connection));
				}
				byte[] subSequence=entryDAOUtils.getSubSequence(accession, remoteRange.getBeginPosition(), (remoteRange.getEndPosition()-remoteRange.getBeginPosition())+1);
				
				if(subSequence==null)
				{
					throw new SQLException("Invalid Accession "+accession+" , which does not exist in database");
				}
				
				if(remoteRange.getLength()!=subSequence.length)
				{
					StringBuilder lcoationString=new StringBuilder();
					LocationToStringCoverter.renderLocation(lcoationString, remoteRange, false, false);
					throw new SQLException("invalid Remote Location range:"+ lcoationString.toString() +", not within the entry \""+ remoteRange.getAccession()+"\" sequence length");
				}
				if(remoteRange.isComplement())
				{
					ReverseComplementer reversecomplementer=new ReverseComplementer();
					return new Segment(remoteRange,reversecomplementer.reverseComplementByte(subSequence));
				}
				return new Segment(
						remoteRange,subSequence);

			}
		}
		return null;
	}
	
	public void setEntryDAOUtils(EntryDAOUtils entryDAOUtils)//it is useful for test cases
	{
		this.entryDAOUtils = entryDAOUtils;
	}
 
}
