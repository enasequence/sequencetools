package uk.ac.ebi.embl.api.entry;

import java.sql.Date;

public class MasterEntryInfo
{
	private String studyID;
	private String analysisId;
	private int statusId;
	private int taxId;
	private Date hold_date;

	public Date getHold_date()
	{
		return hold_date;
	}

	public void setHold_date(Date hold_date)
	{
		this.hold_date = hold_date;
	}

	public String getStudyID()
	{
		return studyID;
	}

	public void setStudyID(String studyID)
	{
		this.studyID = studyID;
	}

	public String getAnalysisId()
	{
		return analysisId;
	}

	public void setAnalysisId(String analysisId)
	{
		this.analysisId = analysisId;
	}

	public int getStatusId()
	{
		return statusId;
	}

	public void setStatusId(int statusId)
	{
		this.statusId = statusId;
	}

	public int getTaxId()
	{
		return taxId;
	}

	public void setTaxId(int taxId)
	{
		this.taxId = taxId;
	}

}
