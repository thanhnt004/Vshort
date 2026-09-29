package entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "email_template_variables", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"template_id", "variable_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailTemplateVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private EmailTemplate template;

    @Column(name = "variable_name", nullable = false, length = 100)
    private String variableName;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_required")
    @Builder.Default
    private Boolean isRequired = true;
}