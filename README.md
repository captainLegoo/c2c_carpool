# Carpool

In the same city carpooling software, drivers and passengers can post itineraries, and the system will match similar routes.

The driver invites the passenger to go with him. After the passenger agrees, an order is formed. After delivery, the passenger pays the driver face to face and the transaction is completed.



## 1. Config docker

**mysql**

```sh
docker run -id \
-p 3306:3306 \
--name=mysql \
-v $PWD/conf:/etc/mysql/conf.d \
-v $PWD/logs:/logs \
-v $PWD/data:/var/lib/mysql \
-e MYSQL_ROOT_PASSWORD=root \
-d \
mysql:5.7.25
```



**RabbitMQ**

```sh
docker run \
 -e RABBITMQ_DEFAULT_USER=guest \
 -e RABBITMQ_DEFAULT_PASS=guest \
 -v mq-plugins:/plugins \
 --name mq \
 --hostname mq1 \
 -p 15672:15672 \
 -p 5672:5672 \
 -d \
 rabbitmq:3.8-management
```



**Minio**

```sh
docker run -p9005:9000 -p9006:9090 \
--name minio -d \
-e "MINIO_ACCESS_KEY=minioadmin" \
-e "MINIO_SECRET_KEY=minioadmin" \
-v /opt/data/minio/data:/data \
-v /opt/data/minio/config:/root/.minio \
minio/minio:RELEASE.2023-12-02T10-51-33Z  \
 server /data --console-address ":9090" --address ":9000"
```



Create a bucket named hitch and set access permissions

![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/864fc04540bf4a318bf321cb32c6b3a2.png#pic_center)



**Redis**

```sh
docker run \
--restart=always \
--name redis \
-p 6379:6379 -d \
redis:6.0.6
```



**Nacos**

```sh
docker run \
--env MODE=standalone \
--name nacos \
--restart=always \
-d \
-p 8848:8848 \
nacos/nacos-server:2.0.1
```



**Mongo**

```sh
docker run -d \
--name mongo \
-p 27017:27017 \
mongo:4.4
```



## 2. Config backend

![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/63a3c2498b244e798d07a14a178f943e.png)



### 2.1. Structure

**Backend**: Springcloud microservice architecture, providing interfaces required by the frontend

- account: account service, user registration and login, personal center information maintenance
- commons: some public jar packages, introduced by other projects through maven
- gateway: gateway, all front-end page requests must be routed to the back-end service through the gateway
- modules: some PO, VO and publicly used objects, jar packages, introduced by maven from other projects
- notice: messaging service, sending and receiving messages within the site
- order: order service, generate orders
- payment: payment service, maintain payment information
- storage: storage service, data reading and writing in the project need to go through this microservice
- stroke: stroke service, maintain stroke data

**Web**: front-end h5 interface. You need to use Openresty or Nginx for browser access.



### 2.2. Start backend

Modify the corresponding IP and password in the yml files under all projects



### 2.3. Start frontend

Download and install Openresty or Nginx locally

Find nginx.conf in the project root directory and modify the following parts. Be sure to point to the directory of the web folder in your local code.
_Note: If it is a mac, the slash needs to be replaced with /_

![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/ff4cce9aa6094fd4963d62554cc2fcfe.png#pic_center)



After modification, overwrite `nginx.conf` in the `conf` file in the `openresty` installation directory

![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/03733b24499548f683656287aee481aa.png#pic_center)





Enter the `Openresty` installation directory from the command line and enter `nginx` to start:

```sh
start nginx
```



![在这里插入图片描述](https://img-blog.csdnimg.cn/direct/1df477ad5d014c8ca021dfdaf49e8062.png)



Visit http://localhost/web

