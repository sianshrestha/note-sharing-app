package com.sian.noteshare.repository;

import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for the Note entity.
 * Supports standard CRUD operations, pagination, and dynamic querying via JpaSpecificationExecutor.
 */
@Repository
public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

    /**
     * Finds notes uploaded by a specific user, matching the username ignoring case.
     */
    Page<Note> findByUploadedByUsernameContainingIgnoreCase(String username, Pageable pageable);

    /**
     * Finds notes where the subject contains the given keyword, ignoring case.
     */
    Page<Note> findBySubjectContainingIgnoreCase(String subject, Pageable pageable);

    /**
     * Finds notes where the title contains the given keyword, ignoring case.
     */
    Page<Note> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Retrieves all notes uploaded by a specific User entity.
     */
    List<Note> findByUploadedBy(User uploadedBy);
}