package uk.ac.ebi.embl.api.entry.genomeassembly;

import uk.ac.ebi.embl.api.validation.Origin;

public class GCSEntry
{
	private Origin origin;
	private String analysisId;

	public String getAnalysisId()
	{
		return analysisId;
	}

	public void setAnalysisId(String analysisId)
	{
		this.analysisId = analysisId;
	}

	public Origin getOrigin()
	{
		return origin;
	}

	public void setOrigin(Origin origin)
	{
		this.origin = origin;
	}
}
