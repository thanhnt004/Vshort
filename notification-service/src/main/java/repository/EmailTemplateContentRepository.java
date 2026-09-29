package repository;

import entity.EmailTemplate;
import entity.EmailTemplateContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EmailTemplateContentRepository extends JpaRepository<EmailTemplateContent, UUID> {
    @Query("SELECT c FROM EmailTemplateContent c JOIN c.template t WHERE t.code = :code AND c.locale = :locale")
    Optional<EmailTemplateContent> findByTemplateCodeAndLocale(@Param("code") String code, @Param("locale") String locale);
}
