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
package uk.ac.ebi.embl.api.validation.fixer.entry;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.embl.api.validation.ValidationMessageManager;

import static org.junit.Assert.assertTrue;

public class AccessionFixTest {

    private final AccessionFix check = new AccessionFix();

    @Before
    public void setUp() {
        ValidationMessageManager
                .addBundle(ValidationMessageManager.STANDARD_FIXER_BUNDLE);
    }

    @Test
    public void testCheck_NoEntry() {
        assertTrue(check.check(null).isValid());
    }
}
