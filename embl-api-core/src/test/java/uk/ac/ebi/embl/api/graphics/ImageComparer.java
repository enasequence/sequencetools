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
package uk.ac.ebi.embl.api.graphics;

import uk.ac.ebi.embl.api.checksum.MD5Checksum;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Vector;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: lbower
 * Date: 03-Sep-2010
 * Time: 13:44:15
 * To change this template use File | Settings | File Templates.
 */
public class ImageComparer {

    public boolean compareImage(BufferedImage image, String fileName, String expectedChecksums[]) throws Exception {

        /**
         * we need to store the output somewhere - use the users home dir
         */
        String homeDir = System.getProperty("user.home");
        String outputDirString = homeDir + "/embl-api-core/uk/ac/ebi/embl/api/graphics/view/";

        File outputDir = new File(outputDirString);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        String testOutputFile = outputDirString + fileName;

        /**
         * write the test image to the temp dir
         */
        ImageIO.write(image, "png", new File(testOutputFile));

        /*
        InputStream referenceFileInputStream =
                this.getClass().getResourceAsStream("/uk/ac/ebi/embl/api/graphics/view/" + fileName);
        if (referenceFileInputStream == null) {
        	return false;
        }

        byte[] referenceByteArray = inputStreamToByteArray(referenceFileInputStream);
        String referenceChecksum = (new MD5Checksum()).getChecksum(new ByteArrayInputStream(referenceByteArray));
        */
        FileInputStream testFileInputSteam = new FileInputStream(testOutputFile);

        byte[] testByteArray = inputStreamToByteArray(testFileInputSteam);
        String actualChecksum = (new MD5Checksum()).getChecksum(new ByteArrayInputStream(testByteArray));

        for(String expectedChecksum : expectedChecksums) {
        	if (expectedChecksum.equals(actualChecksum)) {
        		return true;
        	}
        }
        	
        return false;
    }

    private byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }
}
