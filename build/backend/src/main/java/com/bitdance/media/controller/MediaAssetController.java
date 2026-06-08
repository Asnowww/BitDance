package com.bitdance.media.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.media.dto.MediaAssetDto;
import com.bitdance.media.service.MediaAssetService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
public class MediaAssetController {

    private final MediaAssetService service;

    public MediaAssetController(MediaAssetService service) {
        this.service = service;
    }

    @PostMapping(value = "/h5/media-assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaAssetDto> upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String bizType
    ) {
        return ApiResponse.ok(service.upload(CurrentUser.getId(), file, bizType));
    }

    @GetMapping("/public/media-assets/{id}/content")
    public ResponseEntity<Resource> content(@PathVariable Long id) {
        MediaAssetDto detail = service.detail(id);
        MediaType type = MediaType.parseMediaType(detail.mimeType());
        return ResponseEntity.ok().contentType(type).body(service.content(id));
    }
}
