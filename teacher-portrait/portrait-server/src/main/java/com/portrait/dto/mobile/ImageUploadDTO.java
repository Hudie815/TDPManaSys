package com.portrait.dto.mobile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * 图片上传 DTO
 * 
 * 支持移动端图片压缩上传，减少原图传输开销
 */
@Data
@Schema(description = "图片上传请求")
public class ImageUploadDTO {
    
    @NotNull(message = "图片文件不能为空")
    @Schema(description = "图片文件")
    private MultipartFile file;
    
    @Schema(description = "图片类型（avatar/document/photo）")
    private String type;
    
    @Schema(description = "最大宽度（像素），用于压缩", defaultValue = "800")
    private Integer maxWidth = 800;
    
    @Schema(description = "最大高度（像素），用于压缩", defaultValue = "800")
    private Integer maxHeight = 800;
    
    @Schema(description = "图片质量（0.1-1.0），1.0 为原图", defaultValue = "0.8")
    private Float quality = 0.8f;
    
    @Schema(description = "是否保持原图比例", defaultValue = "true")
    private Boolean keepAspectRatio = true;
    
    @Schema(description = "输出格式（jpg/png/webp）", defaultValue = "jpg")
    private String outputFormat = "jpg";
    
    @Schema(description = "关联的业务 ID")
    private Long relatedId;
    
    @Schema(description = "关联的业务类型（user/project/patent）")
    private String relatedType;
    
    /**
     * 创建头像上传配置
     */
    public static ImageUploadDTO avatar(MultipartFile file) {
        ImageUploadDTO dto = new ImageUploadDTO();
        dto.setFile(file);
        dto.setType("avatar");
        dto.setMaxWidth(200);
        dto.setMaxHeight(200);
        dto.setQuality(0.85f);
        dto.setOutputFormat("jpg");
        return dto;
    }
    
    /**
     * 创建文档图片上传配置
     */
    public static ImageUploadDTO document(MultipartFile file) {
        ImageUploadDTO dto = new ImageUploadDTO();
        dto.setFile(file);
        dto.setType("document");
        dto.setMaxWidth(1200);
        dto.setMaxHeight(1200);
        dto.setQuality(0.8f);
        dto.setOutputFormat("jpg");
        return dto;
    }
    
    /**
     * 创建移动端压缩配置（默认）
     */
    public static ImageUploadDTO mobile(MultipartFile file) {
        ImageUploadDTO dto = new ImageUploadDTO();
        dto.setFile(file);
        dto.setType("mobile");
        dto.setMaxWidth(800);
        dto.setMaxHeight(800);
        dto.setQuality(0.7f);
        dto.setOutputFormat("jpg");
        return dto;
    }
}