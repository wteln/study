package bigdata.movie.common.entity;

import bigdata.movie.common.enums.Age;
import bigdata.movie.common.enums.Gender;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@TableName(value = "user", autoResultMap = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String username;
  private String password;
  private Gender gender;

  private int age;

  private String occupation;

  @TableField("zipCode")
  private String zipCode;

  @TableField("isDel")
  private boolean del;

  public static final Map<Integer, String> ageDescs = new HashMap<Integer, String>() {{
    put(1, "under 18");
    put(18, "18-24");
    put(25, "25-34");
    put(35, "35-44");
    put(45, "45-49");
    put(50, "50-55");
    put(56, "56+");
  }};

  public static final Map<String, String> occupationDescs = new HashMap<String, String>() {{
    put("0", "other or not specified");
    put("1", "academic/educator");
    put("2", "artist");
    put("3", "clerical/admin");
    put("4", "college/grad student");
    put("5", "customer service");
    put("6", "doctor/health care");
    put("7", "executive/managerial");
    put("8", "farmer");
    put("9", "homemaker");
    put("10", "K-12 student");
    put("11", "lawyer");
    put("12", "programmer");
    put("13", "retired");
    put("14", "sales/marketing");
    put("15", "scientist");
    put("16", "self-employed");
    put("17", "technician/engineer");
    put("18", "tradesman/craftsman");
    put("19", "unemployed");
    put("20", "writer");
  }};

  public String getAgeDesc() {
    return ageDescs.get(age);
  }

  public String getOccDesc() {
    return occupationDescs.get(occupation);
  }
}
