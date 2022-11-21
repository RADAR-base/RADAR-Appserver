package org.radarbase.appserver.search;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchCriteria {
    private String key;
    private String operation;
    private Object value;

    /***
     * Only AND supported in first instance. Later we can add a new query param that can provide this value
     */
    public boolean isOrPredicate() {
        return false;
    }
}
