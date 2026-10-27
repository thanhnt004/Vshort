package org.example.application.dto.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadPartModel {

    private Integer partNumber;
    private String etag;
}