package uk.ac.ebi.embl.api.validation;

final public class 
DefaultOrigin implements Origin
{
	private static final long serialVersionUID = 1L;
	final private String origin_text;
	
	
	public
	DefaultOrigin( String origin_text )
	{
		this.origin_text = origin_text;
	}
	
	
	@Override public String 
	getOriginText() 
	{
		return origin_text;
	}
	
	
	@Override public String
	toString()
	{
		return getOriginText();
	}
}
