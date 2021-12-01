### 准备
1. 下载代码
```shell
git clone git@github.com:wteln/study.git ## for ssh, 必须先在github上配置ssh-key
git clone https://github.com/wteln/study.git ## for http，push时需要输入用户名和密码
```

2. 准备数据文件
将ml-latest文件夹复制到项目根目录
```shell
cp -r {ml-latest path} ./
```

3. 配置mysql环境变量
```shell
export MYSQL_USER={username}   ## mysql用户名
export MYSQL_PASSWORD={password} $$ 密码
```

### 安装
sh install.sh