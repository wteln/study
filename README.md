### 安装准备
- 检查hdfs是否正常安装: jps查看是否有namenode和datanode进程，查看hdfs webui是否能成功打开（默认地址http://localhost:50070)
- 检查spark是否正常安装: jps查看是否有master和worker进程，查看spark ui是否能成功打开（默认地址http://localhost:8080)
- 检查node和npm
  ```shell
   which npm
  ```

### 安装和配置
```shell
git clone git@github.com:wteln/study.git ## for ssh, 必须先在github上配置ssh-key
git clone https://github.com/wteln/study.git ## for http，push时需要输入用户名和密码
```
下载项目后, 需先修改一些配置文件。
##### 配置hadoop环境
在项目根目录，运行：
```shell
cp {your_hadoop_install_path}/etc/hadoop/core-site.xml {your_hadoop_install_path}/etc/hadoop/hdfs-site.xml tasks/src/main/resources/
cp {your_hadoop_install_path}/etc/hadoop/core-site.xml {your_hadoop_install_path}/etc/hadoop/hdfs-site.xml server/src/main/resources/
```
hadoop上需预先建几个目录
```shell
{your_hadoop_install_path}/bin/hdfs dfs -put {dataset} /
{your_hadoop_install_path}/bin/hdfs dfs -mkdir -p /movie-manage/results/
```

##### 配置server
打开server/src/main/resources/application.yaml，配置如下几项：
```yaml
spring.datasource:
  url: jdbc:mysql://localhost:3306/movie # 如果mysql不在当前机器上，修改localhost为对应ip地址
  username: username  # 修改为你自己的用户名
  password: localhost # 修改为你自己的密码

spark:
  home: '/home/mhy/software/environment/spark-2.4.8-bin-hadoop2.7' # 修改为spark安装目录
  master: 'spark://DESKTOP-UR1BCC0.localdomain:7077' # 修改为spark master url，可以在SPARK webui上找到
  task-jar: '/mnt/d/projects/SparkWorks/w2/build/libs/tasks-all.jar' # 修改为{project_root}/build/libs/tasks-all.jar，project_root是项目根目录
  result-base: '/movie-manage/results/'
init.path: '/mnt/c/Users/Administrator/Desktop/ml-latest/' # 修改为本地数据集位置
```

### 编译运行
编译项目
```shell
./gradlew product
```
启动前端
```shell
cd webui
npm run serve -- --port 3000
```
启动server
```shell
java -jar build/libs/server.jar
```
初始化初始admin用户
```shell
wget http://localhost:5000/movie-manager/init/movie?type=user
```
输出有success字样表示成功。初始用户为admin,密码admin123