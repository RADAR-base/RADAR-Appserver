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
import org.radarbase.appserver.validation.CheckExactlyOneNotNull;

import jakarta.validation.constraints.NotNull;
import java.util.Arrays;

/**
 * @author yatharthranjan
 */
@Data
@CheckExactlyOneNotNull(fieldNames = {"amount", "randomAmountBetween"})
public class RepeatProtocol {
    @NotNull
    private String unit;

    private Integer amount;

    private Integer[] randomAmountBetween;

    private String dayOfWeek;

    @SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
    public Integer[] getRandomAmountBetween() {
        if (this.randomAmountBetween == null) return null;
        return Arrays.copyOf(this.randomAmountBetween, this.randomAmountBetween.length);
    }

    public RepeatProtocol setRandomAmountBetween(Integer[] randomAmountBetween) {
        this.randomAmountBetween = Arrays.copyOf(randomAmountBetween, randomAmountBetween.length);
        return this;
    }
}

