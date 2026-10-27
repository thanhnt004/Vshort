package org.example.infrashtructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "hashtags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HashtagJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "usage_count")
    private Long usageCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Quan hệ n-n với Video
    @ManyToMany(mappedBy = "hashtags")
    private Set<VideoJpaEntity> videos = new HashSet<>();

    public HashtagJpaEntity(String hashTag) {
        this.name = hashTag;
        this.usageCount = 0L;
        this.createdAt = LocalDateTime.now();
    }
}