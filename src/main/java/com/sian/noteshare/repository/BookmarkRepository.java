package com.sian.noteshare.repository;

import com.sian.noteshare.entity.Bookmark;
import com.sian.noteshare.entity.Note;
import com.sian.noteshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    List<Bookmark> findByUser(User user);
    Optional<Bookmark> findByUserAndNote(User user, Note note);

}
