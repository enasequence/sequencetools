package uk.ac.ebi.embl.api.validation;

import java.util.HashMap;

public class CVTable<IdType, ValueType>
{
	private HashMap<ValueType, IdType> cvTable = new HashMap<ValueType, IdType>();
	
	public IdType getId(ValueType value)
	{
		return cvTable.get(value);
	}
	
	public void put(IdType id, ValueType value)
	{
		cvTable.put(value, id);
	}
}
