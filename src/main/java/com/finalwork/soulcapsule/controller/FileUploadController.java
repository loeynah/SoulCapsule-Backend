package com.finalwork.soulcapsule.controller;

import com.finalwork.soulcapsule.common.ApiResult;
import com.finalwork.soulcapsule.service.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ApiResult<String> upload(@RequestPart("file") MultipartFile file,
                                    HttpServletRequest request) {
        try {
            String storedName = fileStorageService.store(file);
            String imageUrl = buildPublicUrl(request, storedName);
            return ApiResult.success("上传成功", imageUrl);
        } catch (IllegalArgumentException e) {
            return ApiResult.fail(e.getMessage());
        } catch (Exception e) {
            return ApiResult.fail("图片上传失败，请稍后重试");
        }
    }

    private String buildPublicUrl(HttpServletRequest request, String storedName) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String baseUrl;
        if (("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443)) {
            baseUrl = scheme + "://" + host;
        } else {
            baseUrl = scheme + "://" + host + ":" + port;
        }
        return baseUrl + "/uploads/" + storedName;
    }
}
