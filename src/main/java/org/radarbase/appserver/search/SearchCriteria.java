package org.radarbase.appserver.search;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
public class SearchCriteria implements Serializable {
    @Serial
    private static final long serialVersionUID = 327842183571958L;
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
