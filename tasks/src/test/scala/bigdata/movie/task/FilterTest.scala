package bigdata.movie.task

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object FilterTest {
  def main(args: Array[String]): Unit = {
    val path = "C:\\Users\\Administrator\\Desktop\\ml-latest\\tags.csv"
    Source.fromFile(path)
      .getLines()
      .foreach(line => {
        val arr = parseCsvLine(line)
        if(arr.length != 4) {
          println(s"$line ---> ${arr.mkString("%")}")
        }
      })

  }

  def parseCsvLine(line: String): ArrayBuffer[String] = {
    val arrBuffer = ArrayBuffer.empty[String]
    val sps = line.split("\"")
    for(i <- 0 until sps.length) {
      if(i % 2 == 0) {
        val s = sps(i).stripPrefix(",").stripSuffix(",")
        arrBuffer.appendAll(s.split(","))
      } else {
        arrBuffer.append(sps(i))
      }
    }
    arrBuffer
  }
}
