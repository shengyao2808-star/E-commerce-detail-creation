package com.ecommerce.detail.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ecommerce.detail.ai.entity.TaskCostRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface TaskCostRecordMapper extends BaseMapper<TaskCostRecord> {

    @Select("SELECT COALESCE(SUM(cost_amount), 0) FROM task_cost_record WHERE deleted = 0 AND visual_plan_id = #{planId}")
    BigDecimal sumCostByVisualPlanId(@Param("planId") Long planId);

    @Select("SELECT tool_code, COALESCE(SUM(cost_amount), 0) as total FROM task_cost_record WHERE deleted = 0 GROUP BY tool_code")
    List<Map<String, Object>> sumCostGroupByTool();

    @Select("SELECT model_code, COALESCE(SUM(cost_amount), 0) as total FROM task_cost_record WHERE deleted = 0 GROUP BY model_code")
    List<Map<String, Object>> sumCostGroupByModel();

    @Select("SELECT task_type, task_id, tool_code, model_code, duration_ms, invoke_count, cost_amount, cost_currency, cost_source, visual_plan_id, batch_id FROM task_cost_record WHERE deleted = 0")
    List<Map<String, Object>> selectAllCostRecords();
}
