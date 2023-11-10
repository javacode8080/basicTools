# 指定基础镜像
FROM java:8-alpine

COPY ./exceltosql-0.0.1-SNAPSHOT.jar /tmp/exceltosql-0.0.1-SNAPSHOT.jar

# 暴露端口
EXPOSE 8421
# 入口，java项目的启动命令
ENTRYPOINT java -jar /tmp/exceltosql-0.0.1-SNAPSHOT.jar