package uk.ac.ebi.embl.api.validation.helper;

public class FlatFileComparatorException extends Throwable
{
    private static final long serialVersionUID = 1L;
	
    public 
    FlatFileComparatorException( String message ) 
    {
        super( message );
    }
    
    public 
    FlatFileComparatorException( Throwable throwable ) 
    {
        super( throwable );
    }

    public 
    FlatFileComparatorException( String message, Throwable throwable ) 
    {
        super( message, throwable );
    }
}
