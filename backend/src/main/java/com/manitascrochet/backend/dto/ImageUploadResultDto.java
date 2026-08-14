package com.manitascrochet.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageUploadResultDto {
    String url;
    String fileId;
}
