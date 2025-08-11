package com.sian.noteshare.repository;


import com.sian.noteshare.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;


@Repository
public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {
    Page<Note> findByUploadedByUsernameContainingIgnoreCase(String username, Pageable pageable);
    Page<Note> findBySubjectContainingIgnoreCase(String subject, Pageable pageable);
    Page<Note> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
