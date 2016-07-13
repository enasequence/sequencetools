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
package uk.ac.ebi.embl.gff3.reader;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 14-Sep-2010
 * Time: 10:46:58
 * To change this template use File | Settings | File Templates.
 */
public class GFF3TestRead {
    public static void main(String[] args) {
        new GFF3TestRead().readFile();
    }

    private void readFile() {
        try {
            File gffFile = new File("H:/temp/gffsmall.txt");
            BufferedReader fileReader = new BufferedReader(new FileReader(gffFile));
            GFF3FlatFileEntryReader reader = new GFF3FlatFileEntryReader(fileReader);
            reader.read();
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
