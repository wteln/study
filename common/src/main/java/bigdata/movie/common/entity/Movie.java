package bigdata.movie.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@TableName("movie")
@Data
public class Movie implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String genres;

    @TableField("isDel")
    private boolean isDel;
}
