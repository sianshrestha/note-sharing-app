package com.sian.noteshare.repository;

import com.sian.noteshare.entity.Bookmark;
import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Bookmark entity.
 * Manages the many-to-many relationship between Users and Notes.
 */
@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * Retrieves all bookmarks associated with a specific user.
     */
    List<Bookmark> findByUser(User user);

    /**
     * Checks if a specific user has already bookmarked a specific note.
     */
    Optional<Bookmark> findByUserAndNote(User user, Note note);
}