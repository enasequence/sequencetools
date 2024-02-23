/*
 * Copyright 2019-2024 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.api.validation;

/**
 * Created by IntelliJ IDEA. User: lbower Date: 10-Aug-2010 Time: 11:50:42 To change this template
 * use File | Settings | File Templates.
 */
public class TestOrigin implements Origin {
  private static final long serialVersionUID = 1L;

  @Override
  public String getOriginText() {
    return "this is a test origin";
  }
}
