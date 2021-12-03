package bigdata.movie.task

import bigdata.movie.common.entity.Movie
import org.apache.spark.rdd.RDD
import org.apache.spark.rdd.RDD.rddToPairRDDFunctions

object Filter extends SparkApplication {

  def main(args: Array[String]): Unit = {
    val opt = parseArgument(args)
    val movieRdd = Source.movie
    val rateRdd: RDD[(Long, Double)] = Source.avgRate

    val tagRdd: RDD[(Long, String)] = Source.sumTag

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
        f"${movie.getId}, ${movie.getTitle}, ${movie.getGenres}, $rate%.1f, $tag)"
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
}

case class FilterOptions(name: String = null,
                         minRate: Double = 0.0,
                         maxRate: Double = 5.0,
                         category: String = null,
                         tag: String = null,
                         outputPath: String = null)
