package com.bitdance.community.repository;

import com.bitdance.community.domain.ContentPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContentPostRepository extends JpaRepository<ContentPost, Long> {

    @Query("""
        select p from ContentPost p
        where p.postStatus = 'published'
          and p.visibility in ('public','followers')
          and (:danceStyleId is null or p.danceStyleId = :danceStyleId)
        order by p.publishedAt desc, p.id desc
        """)
    Page<ContentPost> recommend(@Param("danceStyleId") Long danceStyleId, Pageable pageable);

    @Query(
        value = """
        select p from ContentPost p
        where p.postStatus = 'published'
          and p.visibility in ('public','followers')
          and (:danceStyleId is null or p.danceStyleId = :danceStyleId)
        order by
          case when p.authorUserId in :followeeIds then 0 else 1 end,
          p.publishedAt desc,
          p.id desc
        """,
        countQuery = """
        select count(p) from ContentPost p
        where p.postStatus = 'published'
          and p.visibility in ('public','followers')
          and (:danceStyleId is null or p.danceStyleId = :danceStyleId)
        """
    )
    Page<ContentPost> recommendPrioritized(
        @Param("danceStyleId") Long danceStyleId,
        @Param("followeeIds") List<Long> followeeIds,
        Pageable pageable
    );

    @Query("""
        select p from ContentPost p
        where p.postStatus = 'published'
          and p.visibility in ('public','followers')
          and p.authorUserId in :followeeIds
          and (:danceStyleId is null or p.danceStyleId = :danceStyleId)
        order by p.publishedAt desc, p.id desc
        """)
    Page<ContentPost> followingFeed(
        @Param("followeeIds") List<Long> followeeIds,
        @Param("danceStyleId") Long danceStyleId,
        Pageable pageable
    );

    @Query("""
        select p from ContentPost p
        where p.id in (
            select t.contentPostId from ContentPostTopic t where t.topicTagId = :topicId
        )
          and p.postStatus = 'published'
          and p.visibility = 'public'
        order by p.publishedAt desc, p.id desc
        """)
    Page<ContentPost> byTopic(@Param("topicId") Long topicId, Pageable pageable);

    @Query("""
        select p from ContentPost p
        where p.id in (
            select t.contentPostId from ContentPostTopic t where t.topicTagId = :topicId
        )
          and p.postStatus = 'published'
          and p.visibility = 'public'
        order by
          (select count(l.userId) from ContentLike l where l.contentPostId = p.id) desc,
          (select count(c.id) from ContentComment c where c.contentPostId = p.id and c.commentStatus = 'published') desc,
          p.publishedAt desc,
          p.id desc
        """)
    Page<ContentPost> byTopicHot(@Param("topicId") Long topicId, Pageable pageable);

    @Query("""
        select p from ContentPost p
        where p.postStatus = 'published'
          and p.visibility = 'public'
          and lower(p.contentText) like lower(concat('%', :q, '%'))
        order by p.publishedAt desc, p.id desc
        """)
    Page<ContentPost> search(@Param("q") String q, Pageable pageable);

    @Query("""
        select p from ContentPost p
        where p.postStatus = 'published'
          and p.visibility = 'public'
          and p.authorUserId = :authorUserId
        order by p.publishedAt desc, p.id desc
        """)
    Page<ContentPost> publicPostsByAuthor(
        @Param("authorUserId") Long authorUserId,
        Pageable pageable
    );

    @Query("""
        select p from ContentPost p
        where p.postStatus in ('published','draft')
          and p.authorUserId = :authorUserId
        order by p.publishedAt desc, p.id desc
        """)
    Page<ContentPost> postsByAuthorForOwner(
        @Param("authorUserId") Long authorUserId,
        Pageable pageable
    );
}
