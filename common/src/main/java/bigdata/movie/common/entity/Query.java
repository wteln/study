package bigdata.movie.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@TableName("query")
@ToString
public class Query {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField("minRate")
    private double minRate;

    @TableField("maxRate")
    private double maxRate;

    private String category;

    private String tag;

    @TableField("startTime")
    private LocalDateTime startTime;

    @TableField("endTime")
    private LocalDateTime endTime;
}
