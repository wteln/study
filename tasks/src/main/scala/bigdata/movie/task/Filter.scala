package bigdata.movie.task

import bigdata.movie.common.entity.{Movie, Rate, Tag}
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

object Filter extends SparkApplication {
  private val movieFile = "/ml-latest/movies.csv"
  private val rateFile = "/ml-latest/ratings.csv"
  private val tagFile = "/ml-latest/tags.csv"

  def main(args: Array[String]): Unit = {
    val opt = parseArgument(args)
    val movieRdd = sc.textFile(movieFile)
      .flatMap(row => {
        val sps = row.split(",")
        if(sps(0).equals("movieId")) {
          Iterator.empty
        } else {
          val movie = new Movie
          movie.setId(sps(0).toLong)
          movie.setTitle(sps(1))
          movie.setGenres(sps(2))
          Iterator(movie)
        }
      })
    val rateRdd: RDD[(Long, Double)] = sc.textFile(rateFile)
        .flatMap(row => {
          val sps = row.split(",")
          if(sps(0).equals("userId")) {
            Iterator.empty
          } else {
            val rate = new Rate
            rate.setUserId(sps(0).toLong)
            rate.setMovieId(sps(1).toLong)
            rate.setRate(sps(2).toDouble)
            rate.setTs(sps(3).toLong)
            Iterator(rate)
          }
        }).map(r=>(r.getMovieId, (r.getRate, 1)))
      .reduceByKey{case (r1, r2)=>(r1._1 + r2._1, r1._2+r2._2)}
      .map{case (mid, (sum, cnt)) => (mid, sum / cnt)}

    val tagRdd: RDD[(Long, String)] = sc.textFile(tagFile)
      .flatMap(row=>{
        try {
          val sps = parseCsvWithExpect(row, 4)
          if (sps(0).equals("userId")) {
            Iterator.empty
          } else {
            val tag = new Tag
            tag.setUserId(sps(0).toLong)
            tag.setMovieId(sps(1).toLong)
            tag.setTag(sps(2))
            tag.setTs(sps(3).toLong)
            Iterator(tag)
          }
        }catch {
          case e: Exception => Iterator.empty
        }
      }).map(t => (t.getMovieId, Set(t.getTag)))
      .reduceByKey((s1,s2) => s1 ++ s2)
      .map{ case (mid, s) => (mid, s.mkString("|"))}

    val filteredMovieRdd: RDD[(Long, Movie)] = if(opt.name != null || opt.category != null) {
      movieRdd.filter(movie=>{
        var m = true
        if(opt.name != null && opt.name.nonEmpty) {
          m = movie.getTitle.matches(s".*${opt.name}.*") & m
        }
        if(opt.category != null && opt.category.nonEmpty) {
          m = movie.getGenres.matches(s".*${opt.category}.*") & m
        }
        m
      }).keyBy(_.getId)
    } else movieRdd.keyBy(_.getId)

    val filteredRateRdd: RDD[(Long, Double)] = if(opt.minRate > 0 || opt.maxRate < 5) {
      rateRdd.filter{case (_, rate) => rate >= opt.minRate && rate <= opt.maxRate}
    } else rateRdd

    val filteredTagRdd: RDD[(Long, String)] = if(opt.tag != null) {
      tagRdd.filter{case (_, tag) => tag.matches(s".*${opt.tag}.*")}
    } else tagRdd

    val result = filteredMovieRdd
      .join(filteredRateRdd)
      .join(filteredTagRdd)
      .map{ case (_, ((movie, rate), tag)) =>
        f"${movie.getId}, ${movie.getTitle}, ${movie.getGenres}, ${rate}%.1f, ${tag})"
      }
    result.saveAsTextFile(opt.outputPath)
  }

  def parseArgument(args: Array[String]): FilterOptions = {
    import scopt.OParser
    val builder = OParser.builder[FilterOptions]
    val parser1 = {
      import builder._
      OParser.sequence(
        programName("filter"),

        opt[String]( "name")
          .action((x, c) => c.copy(name = x))
          .text("movie name"),
        opt[Double]("minRate")
          .action((x, c) => c.copy(minRate = x))
          .text("min rate"),
        opt[Double]("maxRate")
          .action((x, c) => c.copy(maxRate = x))
          .text("max rate"),
        opt[String]("category")
          .action((x, c) => c.copy(category = x)),
        opt[String]("tag")
          .action((x, c) => c.copy(tag = x)),
        opt[String]("output")
          .required()
          .action((x, c) => c.copy(outputPath = x))
      )
    }
    OParser.parse(parser1, args, FilterOptions()) match {
      case Some(config) => config
      case _ =>
        throw new RuntimeException()
    }
  }

  def parseCsvWithExpect(line: String, size: Int): Array[String] = {
    val arr = line.split(",")
    if(arr.size != size) {
      throw new IllegalArgumentException(line)
    }
    arr;
  }
}

case class FilterOptions(name: String = null,
                         minRate: Double = 0.0,
                         maxRate: Double = 5.0,
                         category: String = null,
                         tag: String = null,
                         outputPath: String = null)
