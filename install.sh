#!/usr/bin/env bash

set -x

jps -l|grep "org.apache.spark.deploy"|awk '{print $1}'|xargs kill
jps -l|grep "org.apache.hadoop.hdfs.server"|awk '{print $1}'|xargs kill
jps -l|grep "server.jar"|awk '{print $1}'|xargs kill

set -e

BaseDir=$(cd `dirname $0`;pwd)
InstallDir=$BaseDir/install
SourceDir=$InstallDir/sources
DistDir=$InstallDir/dist
ConfDir=$BaseDir/conf

rm -rf $DistDir/*

ENV_FILE="${BaseDir}/env.sh"
echo "#!/usr/bin/env bash" > $ENV_FILE
echo "" >> $ENV_FILE

export PROJECT_HOME="$BaseDir"
echo export PROJECT_HOME="$BaseDir"

mkdir -p $SourceDir
mkdir -p $DistDir

### 下载需要的安装包
cd $SourceDir

spark_name="spark-2.4.8-bin-hadoop2.7"

if [ ! -f $spark_name.tgz ];then
  wget https://mirrors.tuna.tsinghua.edu.cn/apache/spark/spark-2.4.8/spark-2.4.8-bin-hadoop2.7.tgz
fi

hadoop_name="hadoop-2.10.1"
if [ ! -f $hadoop_name.tar.gz ];then
  wget https://mirrors.tuna.tsinghua.edu.cn/apache/hadoop/core/hadoop-2.10.1/hadoop-2.10.1.tar.gz
fi

node_name="node-v16.13.0-linux-x64"
if [ ! -f $node_name.tar.xz ];then
  wget https://nodejs.org/dist/v16.13.0/node-v16.13.0-linux-x64.tar.xz
fi

#### 配置安装环境
cd $DistDir
if [ ! -d $spark_name ];then
  tar -xzf $SourceDir/$spark_name.tgz
  cd $spark_name
  cp conf/slaves.template conf/slaves
  ./sbin/start-all.sh
fi

export SPARK_MASTER="spark://$(hostname):7077"
echo export SPARK_MASTER="spark://$(hostname).localdomain:7077" >> $ENV_FILE
export SPARK_HOME=$DistDir/$spark_name
echo export SPARK_HOME=$DistDir/$spark_name >> $ENV_FILE

cd $DistDir
export HADOOP_HOME=$DistDir/$hadoop_name
echo export HADOOP_HOME=$DistDir/$hadoop_name >> $ENV_FILE
if [ ! -d $hadoop_name ];then
  tar -xzf $SourceDir/$hadoop_name.tar.gz
  cd $HADOOP_HOME
  envsubst < $ConfDir/core-site.xml > $HADOOP_HOME/etc/hadoop/core-site.xml
  envsubst < $ConfDir/hdfs-site.xml > $HADOOP_HOME/etc/hadoop/hdfs-site.xml
  cp $ConfDir/hadoop-env.sh $HADOOP_HOME/etc/hadoop/
  echo "export JAVA_HOME=${JAVA_HOME}" >> $HADOOP_HOME/etc/hadoop/hadoop-env.sh
  mkdir -p $HADOOP_HOME/tmp
  mkdir -p $HADOOP_HOME/data/name
  mkdir -p $HADOOP_HOME/data/data
  $HADOOP_HOME/bin/hdfs namenode -format
  $HADOOP_HOME/sbin/start-dfs.sh
fi

cd $DistDir
export NODE_HOME=$DistDir/$node_name
echo export PATH="$DistDir/$node_name/bin:\$PATH" >> $ENV_FILE
if [ ! -d $NODE_HOME ];then
  tar -xf $SourceDir/$node_name.tar.xz
fi

### 初始化mysql
mysql_user="${MYSQL_USER}"
mysql_password="${MYSQL_PASSWORD}"
mysql_db="movie_filter"
mysql -u$mysql_user -p$mysql_password -e "create database if not exists $mysql_db"
mysql -u$mysql_user -p$mysql_password -D$mysql_db -e "source $BaseDir/server/src/main/resources/init.sql"

export MYSQL_USER="${mysql_user}"
export MYSQL_PASSWORD="${mysql_password}"
export MYSQL_DB="${mysql_db}"
echo export MYSQL_USER="${mysql_user}" >> $ENV_FILE
echo export MYSQL_PASSWORD="${mysql_password}" >> $ENV_FILE
echo export MYSQL_DB="${mysql_db}" >> $ENV_FILE

$HADOOP_HOME/bin/hdfs dfs -put $BaseDir/ml-latest /
cp $HADOOP_HOME/etc/hadoop/core-site.xml $PROJECT_HOME/tasks/src/main/resources/
cp $HADOOP_HOME/etc/hadoop/hdfs-site.xml $PROJECT_HOME/tasks/src/main/resources/
cp $HADOOP_HOME/etc/hadoop/core-site.xml $PROJECT_HOME/server/src/main/resources/
cp $HADOOP_HOME/etc/hadoop/hdfs-site.xml $PROJECT_HOME/server/src/main/resources/
envsubst < $ConfDir/application.yaml > $PROJECT_HOME/server/src/main/resources/application.yaml

chmod +x $PROJECT_HOME/gradlew
cd $PROJECT_HOME
./gradlew product

nohup java -jar $PROJECT_HOME/build/libs/server.jar > server.log 2>&1 &

wget http://localhost:5000/movie-manager/init/movie?type=user

cd $PROJECT_HOME/webui
npm install
npm run serve -- --port 3000

