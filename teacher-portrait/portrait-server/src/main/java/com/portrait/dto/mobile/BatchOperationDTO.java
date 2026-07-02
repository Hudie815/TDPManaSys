package com.portrait.dto.mobile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量操作 DTO
 * 
 * 用于移动端减少请求次数，支持批量增删改操作
 */
@Data
@Schema(description = "批量操作请求")
public class BatchOperationDTO {
    
    @NotEmpty(message = "操作列表不能为空")
    @Schema(description = "操作类型（batch_create/batch_update/batch_delete）")
    private String operation;
    
    @Schema(description = "目标实体类型（vertical_project/horizontal_project/patent/paper/software/competition）")
    private String entityType;
    
    @Schema(description = "数据列表（JSON 字符串或对象列表）")
    private List<Object> items;
    
    @Schema(description = "ID 列表（用于批量删除）")
    private List<Long> ids;
    
    /**
     * 批量创建操作
     */
    public static BatchOperationDTO create(String entityType, List<Object> items) {
        BatchOperationDTO dto = new BatchOperationDTO();
        dto.setOperation("batch_create");
        dto.setEntityType(entityType);
        dto.setItems(items);
        return dto;
    }
    
    /**
     * 批量更新操作
     */
    public static BatchOperationDTO update(String entityType, List<Object> items) {
        BatchOperationDTO dto = new BatchOperationDTO();
        dto.setOperation("batch_update");
        dto.setEntityType(entityType);
        dto.setItems(items);
        return dto;
    }
    
    /**
     * 批量删除操作
     */
    public static BatchOperationDTO delete(String entityType, List<Long> ids) {
        BatchOperationDTO dto = new BatchOperationDTO();
        dto.setOperation("batch_delete");
        dto.setEntityType(entityType);
        dto.setIds(ids);
        return dto;
    }
}