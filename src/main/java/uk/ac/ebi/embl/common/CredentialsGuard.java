/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package uk.ac.ebi.embl.common;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

/**
 * This class is used to encrypt and decrypt the credentials. This class will be obfuscated for
 * making it less readable
 */
public class CredentialsGuard {

  static StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();

  static {
    if (!encryptor.isInitialized()) {
      encryptor.setPassword(getPass());
    }
  }

  public static String enc(String pass) {
    return encryptor.encrypt(pass);
  }

  public static String dec(String pass) {
    return encryptor.decrypt(pass);
  }

  public static String getPass() {
    String password = System.getenv("ENCRYPTION_KEY");
    if (password == null) {
      password = "testtest";
    }
    return password;
  }
}
