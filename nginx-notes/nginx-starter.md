## Nginx 入门

参考 [Nginx入门必须懂3大功能配置 - Web服务器/反向代理/负载均衡 | 技术蛋老师](https://www.bilibili.com/video/BV1TZ421b7SD)

从零开始编写一份 Nginx 的配置文件



### 基础配置文件

备份 /etc/nginx/nginx.conf 文件

```bash
mv /etc/nginx/nginx.conf /etc/nginx/nginx.conf.bak
```

新建空白配置文件

```bash
touch /etc/nginx/nginx.conf
```

先使用 `nginx -t` 检查配置文件，一开始会提示缺少 `events`。

`events` 用于告知 Nginx 如何处理连接

```nginx
events {}
```

重新检查配置文件，并立即加载

```bash
nginx -t
nginx -s reload
```

发现可以正常启动，但无法访问 http://localhost/ 的任何页面。这是因为缺少 `http` 块。

在 `http` 块中还需要定义 `server` 块，`server` 块中需要设置监听的端口和对应的 ip 地址或域名。

```nginx
events {}

http {
  server {
    listen 80;
    server_name localhost;
  }
}
```

重新加载 `nginx -s reload`，再次访问 http://localhost/ 就会出现 404。这表示 Nginx 已经开始监听 localhost 的 80 端口，只是还未指定响应的内容，因此 Nginx 就响应默认的 404 页面。

Nginx 可以像 API 那样返回指定的数据，而不一定是页面

```nginx
events {}

http {
  server {
    listen 80;
    server_name localhost;
    return 200 "welcome.\n";
  }
}
```

使用 curl 可以快速的查看状态码和响应信息

```bash
curl -i localhost
```



接下来就可以开始设置返回自定义页面

先创建自定义的文件夹，存放网页文件

```bash
mkdir -p /var/www/localhost
```

在文件夹下创建 index.html，放入自定义内容

```html
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title></title>
    <link href="style.css" rel="stylesheet">
  </head>
  <body>
    <h1>NGINX</h1>
  </body>
</html>
```



回到 Nginx 配置文件中指定网页根目录，使用 Nginx 的 `root` 指令

```nginx
events {}

http {
  server {
    listen 80;
    server_name localhost;
    
    root /var/www/localhost;
  }
}
```

> 需要删除原来 return 的配置，否则返回的是 return 的内容。



### 处理非默认文件名称

在实际使用时，创建的可能并不是 index.html，可能是其他的名称，如 about.html。此时访问 http://localhost/ 就会出现 403 的错误。

index.html 是 Nginx 会默认查找的文件，如果需要指定其他文件名，需要使用 `index` 指令

```nginx
events {}

http {
  server {
    listen 80;
    server_name localhost;
    
    root /var/www/localhost;
    index about.html;
  }
}
```

重新加载配置，就能成功访问到 about.html 的内容。



### 处理 MIME 类型文件

一个网页会包含多个文件。

创建一个 css/style.css 文件，为 about.html 增加背景颜色

```css
body {
  background-color: #67c6e3;
}
```

重新加载配置后，访问页面发现背景颜色没有生效，查看 Content-Type 为 text/plain，类型发生了错误。

这里需要使用位于 /etc/nginx 下的 mime.types 文件，里面定义了各种内容类型对应的文件后缀，方便 Nginx 处理 MIME 类型。

只需要在配置文件中使用 `include` 指令 引入 mime.types 即可

```nginx
events {}

http {
  
  include mime.types;
  
  server {
    listen 80;
    server_name localhost;
    
    root /var/www/localhost;
    index about.html;
  }
}
```

重新加载配置再访问 http://localhost/ 后，发现 style.css 文件的 Content-Type 为 text/css，背景颜色也正确加载。

> 如果浏览器缓存了 css 文件，导致背景颜色一直不刷新，可以手动删除浏览器缓存。

可以发现 `include`  指令写在 `http` 块内，`server` 块外，表示这个 `include` 可以影响所有 `server` 块，无需重复编写。



### 多文件拆分

 `include` 指令能够引入其他文件，因此可以将 nginx.conf 中的配置拆分到多个单独的文件中。

在 /etc/nginx 目录下创建 conf.d 目录，用于存放多个配置文件。

这些配置文件可以在 nginx 中通过 `include` 指定全部引入

```nginx
events {}

http {
  
  include mime.types;
  include conf.d/*.conf;
}
```

`server` 块的内容剪切到 conf.d 目录下的 default.conf 中

```nginx
server {
  listen 80;
  server_name localhost;

  root /var/www/localhost;
  index about.html;
}
```

加载配置重新访问 http://localhost/，能够正常访问。



### location 指令

#### 前缀匹配

将 about.html 重命名为 index.html，删除 conf.d/default.conf 配置文件中的 `index` 指令配置，并使用 `location` 配置。

```nginx
server {
  listen 80;
  server_name localhost;

  location / {
    root /var/www/localhost;
  }
}
```

重新加载配置文件，访问 http://localhost/ 没有问题。

将 `location /` 改为 `location /app`

```nginx
server {
  listen 80;
  server_name localhost;

  location /app {
    root /var/www/localhost;
  }
}
```

重新加载配置再访问会出现 404。因为在 /var/www/localhost 下面没有 /app 这个文件，Nginx 找不到对应的文件就会返回 404。



`location` 指令不单要匹配 URI，还要匹配对应路径的文件。

在 /var/www/localhost 下新建一个 app 文件夹，在 app 文件夹内创建 index.html。

使用 curl 请求 http://localhost/app 会出现 301 状态码，在请求地址结尾加上斜杠 `/` 或 `/index.html` 就能成功访问。

因为在 `location` 指令里面指定了 `/app`，访问 http://localhost/app 会查找根路径下有没有 app 这个文件，也就会说 app 被认为是一个文件，所以请求会出错。

但是 http://localhost/app/ 和 http://localhost/app/index.html 都可以成功访问，这是因为 location 和匹配路径参数之间还可以设置参数，默认情况下不设置这个参数，Nginx 会匹配以 "app" 开头的 URI 和文件路径。



对于 http://localhost/apple/ 和 http://localhost/apple/index.html，甚至 http://localhost/apple/ppa/。如果确实存在对应的文件路径，那么 `loaction /app` 都能成功匹配，因为前缀都包含 "app" 这个字符串。 

虽然这样设置更灵活，但是不安全，容易暴露不能公开的文件。



#### 精确匹配

Nginx 可以将 URI 和文件路径精确匹配，需要在 location 后加上 `=` 参数，并明确指定文件路径。

```nginx
server {
  listen 80;
  server_name localhost;

  location = /app/index.html {
    root /var/www/localhost;
  }
}
```

此时，只有访问 http://localhost/app/index.html 才能成功。

但是这样的形式又显得不灵活。还可以使用正则表达式。



#### 正则表达式

只需要在 location 和路径之间写入 `～`，就能启用正则表达式。

```nginx
server {
  listen 80;
  server_name localhost;

  location ～ /app/[1-9].html {
    root /var/www/localhost;
  }
}
```

可以匹配以数字 1-9 命名的 html后缀文件。

但是在文件名大小写不一致的情况下，就无法统一识别所有文件，因为 `～` 是区分字母大小写的。

在 `～` 后加上 `*` 表示不区分大小写

```nginx
server {
  listen 80;
  server_name localhost;

  location ~* /app/index.html {
    root /var/www/localhost;
  }
}
```

但是用户实际访问还是要与文件大小写名称保持一致，这里只是用正则表达式包含了不区分大小的文件路径，但不是将 URI 和 文件路径不区分大小写进行匹配。



#### 优先前缀匹配

优先前缀匹配和前缀匹配类似，使用 `^~`，优先级高于普通前缀匹配。



#### 优先级

从高到低匹配

1.`=` 精确匹配

2.`^~` 优先前缀

3.`~` 和 `～*` 正则

4.空格 普通前缀



### 临时重定向

仅凭路径匹配还不够，经常还需要把匹配到的路径的 URI 进行临时重定向的操作。

配置如下

```nginx
server {
  listen 80;
  server_name localhost;
  root /var/www/localhost;

  location /temp {
    return 307 /app/index.html;
  }
}
```

当用户访问 /temp 的时候会被重定向到根路径下的 /app/index.html。

使用 curl -i 测试，还可以在响应首部看到 Location 指向了下一个地址 http://localhost/app/index.html



不过重定向会改变 URI 地址，用户可以感知到变化。

为了使重定向过渡平滑，可以不使用 `location` 指令，使用 `rewrite` 指令重写路径

```nginx
server {
  listen 80;
  server_name localhost;
  root /var/www/localhost;
  
  rewrite /temp /app/index.html;
}
```

再次访问状态码就是 200，响应首部，也没有了 Location，和直接访问资源没有什么区别。



重定向和重写主要是针对单个文件路径的，如果需要多个文件，可以使用 `try_files`。



### try_files 指令

`try_files` 可以让 Nginx 选择多个文件，还可以结合变量 `$uri` 使用。

```nginx 
server {
  listen 80;
  server_name localhost;
  root /var/www/localhost;
  index index.html;

  location / {
    add_header X-debug-uri "$uri";
    try_files $uri $uri/ =404;
  }
}
```

> 这里为了方便观察 `$uri` 的作用，使用自定义响应首部记录 `$uri` 的值

这里是先尝试访问用户的文件路径，如果没有再访问对应的文件夹，如果有的话，就访问里面的 index.html。如果前面的都访问失败了，就返回 404。

```bash
curi -i localhost/index.html
```

响应的 X-debug-uri 为 /index.html，匹配的是 `try_files` 的第一个参数 `$uri`。

```bash
curi -i localhost/app/
```

响应的 X-debug-uri 为 /app/index.html，匹配的是 `try_files` 的第二个参数 `$uri/` 以及 `index` 指令指定的 index.html。

```bash
curi -i localhost/xxx/
```

没有 X-debug-uri，返回的是 `try_files` 的第三个参数 `=404`，因为不存在 /xxx/index.html 这个资源。



### 自定义 404 页面

返回的 404 是 Nginx 默认的 404 页面，需要自定义 404 页面，可以使用 `error_page`。

在根路径 /var/www/localhost 下创建 404.html

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title></title>
    <link href="css/style.css" rel="stylesheet" />
  </head>
  <body>
    <h1>404 NOT FOUND?</h1>
  </body>
</html>
```

在配置文件中指定 `error_page`

```nginx
server {
  listen 80;
  server_name localhost;
  root /var/www/localhost;
  index index.html;
  error_page 404 /404.html;

  location / {
    add_header X-debug-uri "$uri";
    try_files $uri $uri/ =404;
  }
}
```

其他状态的页面也可以自定义



### 反向代理

启动两个项目，一个项目监听 3000 端口，一个项目监听 3001 端口。

 使用 Nginx 的 `proxy_pass` 设置反向代理

```nginx
server {
  listen 80;
  server_name localhost;
  root /var/www/localhost;
  index index.html;
  error_page 404 /404.html;
  
  location /app1 {
    proxy_pass http://localhost:3000;
  }
  
  location /appp2 {
    proxy_pass http://localhost:3001;
  }
}
```

访问指定的路径就会转发到指定端口运行的项目。



### 负载均衡

当网站流量增多的时候，你可能会增加服务器来进行分流，此时就可以使用 Nginx 的负载均衡功能。

使用 `upstream` 块设置上游服务器集群，名称为 backend-servers，在里面写入可分配的服务器地址。

```nginx
upstream backend-servers {
  server localhost:3000;
  server localhost:3001;
}
server {
  listen 80;
  server_name localhost;
  root /var/www/localhost;
  index index.html;
  error_page 404 /404.html;

  location / {
    proxy_pass http://backend-servers;
  }
}
```

在 location 块内指定 proxy_pass 为上游服务器集群的名称即可。Nginx 会自动进行负载均衡，将流量分配到集群里面的服务器。

此时访问 http://localhost/，不断刷新就会把流量依次分配到不同的服务器上。



不过现实中的服务器的性能不完全一样，需要把更多的流量分配给高配置的服务器，低配的服务器则分配较少的流量。这可以通过设置 `weight` 实现

```nginx
upstream backend-servers {
  server localhost:3000 weight=2;
  server localhost:3001 weight=6;
}
server {
  listen 80;
  server_name localhost;
  root /var/www/localhost;
  index index.html;
  error_page 404 /404.html;

  location / {
    proxy_pass http://backend-servers;
  }
}
```

`weight` 的数值相对越大，被分配到的次数就会越多。