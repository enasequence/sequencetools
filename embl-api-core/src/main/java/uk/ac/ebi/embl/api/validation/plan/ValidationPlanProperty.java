package uk.ac.ebi.embl.api.validation.plan;

public class ValidationPlanProperty<T>
{
	private T value;

	ValidationPlanProperty(T value)
	{
		this.value=value;
	}
	
	public void set(T value) 
	{
		this.value = value;
	}

	public T get()
	{
 		return value;
	}
}
