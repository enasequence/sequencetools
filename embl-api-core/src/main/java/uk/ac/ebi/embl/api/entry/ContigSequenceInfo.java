package uk.ac.ebi.embl.api.entry;

public class ContigSequenceInfo
{
		private String primaryAccession;
		private int sequenceVersion;
		private long sequenceChksum;
		private int sequenceLength;

		public String getPrimaryAccession() 
		{
			return primaryAccession;
		}
		
		public void setPrimaryAccession(String primaryAccession) 
		{
			this.primaryAccession = primaryAccession;
		}
		
		public int getSequenceVersion() 
		{
			return sequenceVersion;
		}
		
		public void setSequenceVersion(int sequenceVersion)
		{			
			this.sequenceVersion = sequenceVersion;
		}
		
		public long getSequenceChksum() 
		{
			return sequenceChksum;
		}
		
		public void setSequenceChksum(long sequenceChksum) 
		{
			this.sequenceChksum = sequenceChksum;
		}

		public int getSequenceLength() 
		{
			return sequenceLength;
		}

		public void setSequenceLength(int sequenceLength) 
		{
			this.sequenceLength = sequenceLength;
		}
}
