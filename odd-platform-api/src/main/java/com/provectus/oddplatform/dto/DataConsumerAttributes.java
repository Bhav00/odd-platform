package com.provectus.oddplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DataConsumerAttributes extends DataEntityAttributes {
    @JsonProperty("input_list")
    private Set<String> inputListOddrn;

    @Override
    public Set<String> getDependentOddrns() {
        return inputListOddrn;
    }
}