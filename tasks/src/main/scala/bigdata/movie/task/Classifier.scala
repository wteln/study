package bigdata.movie.task

import bigdata.movie.common.entity.Movie
import org.apache.spark.rdd.RDD

object Classifier extends SparkApplication {
  def main(args: Array[String]): Unit = {
    val movie: RDD[(Long, Movie)] = Source.movie.keyBy(_.getId)
    movie.cache()
    movie.join(Source.avgRate)
      .map { case (l, (m, d)) => ((Math.floor(d * 10) / 10).formatted("%.1f"), m) }
      .aggregateByKey(0L)((c, _) => c + 1, _ + _)
      .save("/movie-manage/results/charts/rate.csv")

    movie.flatMap { case (l, movie) =>
      val gs = movie.getGenres.split("\\|")
      gs.map((_, movie))
    }.aggregateByKey(0L)((c, _) => c + 1, _ + _)
      .save("/movie-manage/results/charts/genre.csv")

    val tag: RDD[(Long, String)] = Source.tag.map(t => (t.getMovieId, t.getTag))
    tag.join(movie)
      .map{ case (_, (t, m)) => (t, m) }
      .aggregateByKey(0L)((c, _) => c + 1, _ + _)
      .save("/movie-manage/results/charts/tag.csv")

  }

  implicit class CsvRdd(rdd : RDD[(String, Long)]) {
    def save(path: String): Unit = {
      rdd.map { case (str, l) => s"$str >>>>> $l" }
        .saveAsTextFile(path)
    }
  }
}
