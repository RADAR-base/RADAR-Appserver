/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.dto.protocol;

import lombok.Data;
import java.util.Locale;

/** @author yatharthranjan */
@Data
public class LanguageText {

  private String en;

  private String it;

  private String nl;

  private String da;

  private String de;

  private String es;

  public String getText(String languageCode) {
    switch (languageCode.toLowerCase(Locale.getDefault())) {
        case "en":
            return en;
        case "it":
            return it;
        case "nl":
            return nl;
        case "da":
            return da;
        case "de":
            return de;
        case "es":
            return es;
        default:
            return "";
    }
  }
}