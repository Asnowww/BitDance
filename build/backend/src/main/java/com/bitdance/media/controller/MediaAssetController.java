package com.bitdance.media.controller;

import com.bitdance.common.web.ApiResponse;
import com.bitdance.iam.security.CurrentUser;
import com.bitdance.media.domain.MediaAsset;
import com.bitdance.media.dto.MediaAssetDto;
import com.bitdance.media.service.MediaAssetService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class MediaAssetController {

    private final MediaAssetService service;

    public MediaAssetController(MediaAssetService service) {
        this.service = service;
    }

    @PostMapping(value = "/h5/media-assets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MediaAssetDto> upload(
        @RequestPart("file") MultipartFile file,
        @RequestParam(required = false) String bizType,
        @RequestParam(required = false) Boolean publicAsset
    ) {
        return ApiResponse.ok(service.upload(CurrentUser.getId(), file, bizType, publicAsset));
    }

    @GetMapping("/public/media-assets/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long id) {
        MediaAsset asset = service.loadContent(id);
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(asset.getMimeType()))
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "inline; filename=\"" + asset.getFileName().replace("\"", "") + "\"")
            .body(asset.getContent());
    }
}
