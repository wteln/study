package bigdata.movie.task

import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession

class SparkApplication {
  protected lazy val sc: SparkContext = SparkContext
    .getOrCreate()
  protected lazy val spark: SparkSession = SparkSession
    .builder()
    .appName("Spark SQL basic example")
    .config("spark.some.config.option", "some-value")
    .getOrCreate()
}
