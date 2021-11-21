#!/usr/bin/env bash

SPARK_HOME=""
SPARK_MASTER=""
TASK_JAR_PATH=""

"$SPARK_HOME"/bin/spark-submit \
  --class bigdata.movie.task.Filter \
  --master "$SPARK_MASTER" \
  "$TASK_JAR_PATH $*"

