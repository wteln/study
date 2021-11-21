package bigdata.movie.common.enums;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public enum Age {
  level1(1, "under 18"),
  level2(18, "18-24"),
  level3(25, "25-34"),
  level4(35, "35-44"),
  level5(45, "45-49"),
  level6(50, "50-55"),
  level7(56, "56+");

  private final String desc;
  private final int mark;

  Age(int mark, String desc) {
    this.desc = desc;
    this.mark = mark;
  }

  public String getDesc() {
    return desc;
  }

  public int getMark() {
    return mark;
  }

  public static Age fromMark(int mark) {
    for (Age value : Age.values()) {
      if(value.getMark() == mark) {
        return value;
      }
    }
    throw new IllegalArgumentException("not a valid mark");
  }
}
