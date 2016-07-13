	/*******************************************************************************
	 * Copyright 2012-2013 EMBL-EBI, Hinxton outstation
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
package uk.ac.ebi.embl.api.entry;

import uk.ac.ebi.embl.api.validation.Origin;

public class AgpRow
{
	/*
	 * SCAFFOLD_NAME                VARCHAR2(100) 
       SCAFFOLD_ACC        NOT NULL VARCHAR2(18)  
       SCAFFOLD_START               NUMBER(15)    
       SCAFFOLD_END                 NUMBER(15)    
       COMPONENT_ORDER              NUMBER(15)    
       COMPONENT_TYPE_ID            CHAR(1)       
       CONTIG_NAME                  VARCHAR2(100) 
       CONTIG_ACC                   VARCHAR2(18)  
       CONTIG_START                 NUMBER(15)    
       CONTIG_END                   NUMBER(15)    
       ORIENTATION                  CHAR(1)       
       IS_GAP                       CHAR(1)       
       GAP_LENGTH                   NUMBER(15)    
       GAP_TYPE_ID                  NUMBER(5)     
       LINKAGE_EVIDENCE_ID          NUMBER(5)     
       ANALYSIS_ID                  VARCHAR2(15)  
	 */

    	private String object;//SCAFFOLD_NAME
		private Long object_beg;//SCAFFOLD_START
		private Long object_end;//SCAFFOLD_END
		private Integer part_number;//COMPONENT_ORDER
		private String component_type_id;//COMPONENT_TYPE_ID
		private String component_id;//CONTIG_NAME
		private Long gap_length=null;//GAP_LENGTH
		private Long component_beg=null;//CONTIG_START
		private String gap_type;//GAP_TYPE_ID
		private Long component_end=null;//CONTIG_END
		private String orientation;//ORIENTATION
		private String linkageevidence;//LINKAGE_EVIDENCE_ID
		private String component_acc;//CONTIG_ACC
		private String object_acc;//SCAFFOLD_ACC
		private Origin origin;
		
		public Origin getOrigin()
		{
			return origin;
		}
		
		public void setOrigin(Origin origin)
		{
			this.origin = origin;
		}
			
		public String getObject()
		{
			return object;
		}
		
		public void setObject(String object)
		{
			this.object = object;
		}
		
		public Long getObject_beg()
		{
			return object_beg;
		}
		
		public void setObject_beg(long object_beg)
		{
			this.object_beg = object_beg;
		}
		
		public Long getObject_end()
		{
			return object_end;
		}
		
		public void setObject_end(Long object_end)
		{
			this.object_end = object_end;
		}
		
		public Integer getPart_number()
		{
			return part_number;
		}
		
		public void setPart_number(Integer part_number)
		{
			this.part_number = part_number;
		}
		
		public String getComponent_type_id()
		{
			return component_type_id;
		}
		
		public void setComponent_type_id(String component_type_id)
		{
			this.component_type_id = component_type_id;
		}
		
		public String getComponent_id()
		{
			return component_id;
		}
		
		public void setComponent_id(String component_id)
		{
			this.component_id = component_id;
		}
		
		public Long getGap_length()
		{
			return gap_length;
		}
		
		public void setGap_length(Long gap_length)
		{
			this.gap_length = gap_length;
		}
		
		public Long getComponent_beg()
		{
			return component_beg;
		}
		
		public void setComponent_beg(Long component_beg)
		{
			this.component_beg = component_beg;
		}
		
		public String getGap_type()
		{
			return gap_type;
		}
		
		public void setGap_type(String gap_type)
		{
			this.gap_type = gap_type;
		}
		
		public Long getComponent_end()
		{
			return component_end;
		}
		
		public void setComponent_end(Long component_end)
		{
			this.component_end = component_end;
		}
		
		public String getOrientation()
		{
			return orientation;
		}
		
		public void setOrientation(String orientation)
		{
			this.orientation = orientation;
		}
		
		public String getLinkageevidence()
		{
			return linkageevidence;
		}
		
		public void setLinkageevidence(String linkageevidence)
		{
			this.linkageevidence = linkageevidence;
		}
		
		public String getComponent_acc()
		{
			return component_acc;
		}
		
		public void setComponent_acc(String component_acc)
		{
			this.component_acc = component_acc;
		}
		
		public boolean isGap()
		{
			return (component_type_id.equals("N") || component_type_id.equals("U"));
		}
		
		public boolean isNegativeContig()
		{
			return this.orientation=="-";
		}
		
		public String getObject_acc() {
			return object_acc;
		}

		public void setObject_acc(String object_acc) {
			this.object_acc = object_acc;
		}

		public boolean isValid()
		{
			if(object==null||object_beg==null||object_end==null||part_number==null||component_type_id==null)
				return false;
			
			if(isGap())
			{
				if(gap_length==null||gap_type==null)
				{
                    return false;
				}
			}
			else
			{
				if(component_id==null||component_beg==null||component_end==null||orientation==null)
				{
                   return false;
				}
			}
			return true;
		}
		
		@Override
		public String toString() {
			return "ScaffoldRecord [object=" + object + ", object_beg="
					+ object_beg + ", object_end=" + object_end + ", part_number="
					+ part_number + ", component_type=" + component_type_id
					+ ", component_id=" + component_id + ", gap_length="
					+ gap_length + ", component_beg=" + component_beg
					+ ", gap_type=" + gap_type + ", component_end=" + component_end
					+ ", orientation=" + orientation
					+ ", linkageevidence=" + linkageevidence + ", component_acc="
					+ component_acc + ", scaffold_acc="+object_acc+", is_gap="+isGap()+"]";
		}
	}


