package com.sian.noteshare.repository;


import com.sian.noteshare.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUploadedById(Long userId);
    List<Note> findBySubjectContainingIgnoreCase(String subject);
    List<Note> findByTitleContainingIgnoreCase(String title);
}
