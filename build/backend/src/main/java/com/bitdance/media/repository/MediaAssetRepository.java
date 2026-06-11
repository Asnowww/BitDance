package com.bitdance.media.repository;

import com.bitdance.media.domain.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    Optional<MediaAsset> findByBucketNameAndObjectKey(String bucketName, String objectKey);
}
