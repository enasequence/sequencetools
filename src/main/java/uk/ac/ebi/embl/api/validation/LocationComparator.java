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
package uk.ac.ebi.embl.api.validation;

import uk.ac.ebi.embl.api.entry.location.Location;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 11-Jun-2010
 * Time: 16:29:45
 * To change this template use File | Settings | File Templates.
 */
public class LocationComparator implements Comparator<Location> {
    public static final int START_LOCATION = 1;
    public static final int END_LOCATION = 2;
    private int sortField = START_LOCATION;

    public LocationComparator(int sortField) {
        this.sortField = sortField;
    }

    public int compare(Location location1, Location location2) {
        if(sortField == START_LOCATION){
            return location1.getBeginPosition().compareTo(location2.getBeginPosition());
        }else if(sortField == END_LOCATION){
            return location1.getEndPosition().compareTo(location2.getEndPosition());
        }

        return 0;
    }
}
