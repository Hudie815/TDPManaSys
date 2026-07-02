package com.portrait.dto.mobile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 批量操作结果 DTO
 * 
 * 用于返回批量操作的结果统计
 */
@Data
@Schema(description = "批量操作结果")
public class BatchResultDTO {
    
    @Schema(description = "操作类型")
    private String operation;
    
    @Schema(description = "目标实体类型")
    private String entityType;
    
    @Schema(description = "成功数量")
    private int successCount;
    
    @Schema(description = "失败数量")
    private int failedCount;
    
    @Schema(description = "失败原因列表")
    private List<FailureDetail> failures;
    
    @Schema(description = "处理耗时（毫秒）")
    private long processingTime;
    
    /**
     * 失败详情
     */
    @Data
    @Schema(description = "失败详情")
    public static class FailureDetail {
        @Schema(description = "失败的索引或ID")
        private Object identifier;
        
        @Schema(description = "失败原因")
        private String reason;
        
        @Schema(description = "错误消息")
        private String message;
    }
    
    /**
     * 创建成功结果
     */
    public static BatchResultDTO success(String operation, String entityType, int successCount) {
        BatchResultDTO result = new BatchResultDTO();
        result.setOperation(operation);
        result.setEntityType(entityType);
        result.setSuccessCount(successCount);
        result.setFailedCount(0);
        result.setProcessingTime(0);
        return result;
    }
    
    /**
     * 创建部分成功结果
     */
    public static BatchResultDTO partialSuccess(String operation, String entityType, 
            int successCount, int failedCount, List<FailureDetail> failures) {
        BatchResultDTO result = new BatchResultDTO();
        result.setOperation(operation);
        result.setEntityType(entityType);
        result.setSuccessCount(successCount);
        result.setFailedCount(failedCount);
        result.setFailures(failures);
        return result;
    }
}