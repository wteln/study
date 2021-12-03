package bigdata.movie.task

import bigdata.movie.common.entity.{Movie, Rate, Tag}
import org.apache.spark.rdd.RDD

object Source extends SparkApplication {
  private val movieFile = "/ml-latest/movies.csv"
  private val rateFile = "/ml-latest/ratings.csv"
  private val tagFile = "/ml-latest/tags.csv"

  def movie: RDD[Movie] = {
    spark.read.format("csv").option("header", "true").load(movieFile).rdd
      .map(r => {
        val movie = new Movie
        movie.setId(r.get(0).toString.toLong)
        movie.setTitle(r.getAs(1))
        movie.setGenres(r.getAs(2))
        movie
      })
  }

  def rate: RDD[Rate] = {
    spark.read.format("csv")
      .option("quote", "\"")
      .option("header", "true").load(rateFile).rdd
      .map(r => {
        val rate = new Rate
        rate.setUserId(r.get(0).toString.toLong)
        rate.setMovieId(r.get(1).toString.toLong)
        rate.setRate(r.get(2).toString.toDouble)
        rate.setTs(r.get(3).toString.toLong)
        rate
      }
      )
  }

  def tag: RDD[Tag] = {
    spark.read.format("csv")
      .option("quote", "\"")
      .option("mode", "DROPMALFORMED")
      .option("header", "true").load(tagFile).rdd
      .map(r => {
        val tag = new Tag
        tag.setUserId(r.get(0).toString.toLong)
        tag.setMovieId(r.get(1).toString.toLong)
        tag.setTag(r.getAs(2))
        tag.setTs(r.get(3).toString.toLong)
        tag
      }
      )
  }

  def avgRate: RDD[(Long, Double)] = {
    rate.map(r => (r.getMovieId, (r.getRate, 1)))
      .reduceByKey { case (r1, r2) => (r1._1 + r2._1, r1._2 + r2._2) }
      .map { case (mid, (sum, cnt)) => (mid, sum / cnt) }
  }

  def sumTag: RDD[(Long, String)] = {
    tag.map(t => (t.getMovieId, Set(t.getTag)))
      .reduceByKey((s1, s2) => s1 ++ s2)
      .map { case (mid, s) => (mid, s.mkString("|")) }
  }

  private def parseCsvWithExpect(line: String, size: Int): Array[String] = {
    val arr = line.split(",")
    if (arr.size != size) {
      throw new IllegalArgumentException(line)
    }
    arr;
  }
}