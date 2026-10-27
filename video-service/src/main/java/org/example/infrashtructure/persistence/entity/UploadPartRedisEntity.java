package org.example.infrashtructure.persistence.entity;

import lombok.*;

// Không cần @RedisHash và @Id vì nó nằm bên trong UploadSessionRedisEntity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadPartRedisEntity {
    private Integer partNumber;
    private String etag;
}