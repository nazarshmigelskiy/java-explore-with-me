package ru.practicum.explorewithme.comment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {
    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

    List<Comment> findAllByAuthorId(Long authorId, Pageable pageable);

    List<Comment> findAllByEventId(Long eventId, Pageable pageable);
}
