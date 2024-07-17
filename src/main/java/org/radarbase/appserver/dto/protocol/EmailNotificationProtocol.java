package org.radarbase.appserver.dto.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yatharthranjan
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailNotificationProtocol {
    
    @JsonProperty("enabled")
    private boolean enabled = false;

    @JsonProperty("title")
    private LanguageText title;

    @JsonProperty("text")
    private LanguageText body;
    
}
