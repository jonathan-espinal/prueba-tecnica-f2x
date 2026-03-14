package com.f2x.prueba.application.dto.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class UpdateClientRequestDto extends BaseClientRequestDto {
    public UpdateClientRequestDto() {
        super();
    }
}
