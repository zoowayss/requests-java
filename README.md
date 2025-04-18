# Java Requests

一个类似 Python requests 的 Java HTTP 客户端库。简单优雅的 HTTP 请求库。

## 特性

- 优雅简单的 API，类似 Python requests
- 支持常见 HTTP 方法（GET、POST、PUT、DELETE）
- 支持 Form 表单和 JSON 请求体
- 支持代理（HTTP 和 SOCKS）
- 自动 JSON 响应解析
- 支持自定义请求头
- 支持流式处理（下载大文件、处理长连接）
- 支持连接和读取超时设置
- 支持链式调用（构建器模式）
- 易于使用和集成

## 安装

在 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>io.github.zoowayss</groupId>
    <artifactId>requests-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 使用示例

### 使用构建器模式（推荐）

```java
// 基本 GET 请求
Response response=Requests.builder("https://api.example.com/users")
        .method("GET")
        .execute();

// 完整的 POST 请求示例
        Response response=Requests.builder("https://api.example.com/users")
        .method("POST")
        .headers(headers)
        .json(user)
        .proxy(proxy)          // 可选的代理设置
        .connectTimeout(5000)  // 可选的连接超时
        .readTimeout(30000)    // 可选的读取超时
        .execute();

// 流式下载示例
        Response response=Requests.builder("https://example.com/large-file")
        .method("GET")
        .stream()             // 启用流式处理
        .proxy(proxy)         // 可选的代理
        .execute();

// 灵活的代理配置
        Response response=Requests.builder("https://api.example.com")
        .method("GET")
        .proxy(new ProxyConfig("proxy.example.com",8080)
        .credentials("username","password"))
        .execute();
```

### 基本 GET 请求

```java
Response response=Requests.get("https://ipinfo.io");
        if(response.isOk()){
        Map<String, Object> data=response.getJson();
        System.out.println(data);
        }
```

### 带请求头的 GET 请求

```java
Map<String, String> headers=new HashMap<>();
        headers.put("Authorization","Bearer your-token");
        Response response=Requests.get("https://api.example.com/users",headers);
```

### 设置超时时间

```java
// 设置全局超时时间
Requests.setConnectTimeout(5000);  // 连接超时：5秒
        Requests.setReadTimeout(30000);    // 读取超时：30秒

// 为单个请求设置超时时间
        Response response=Requests.get("https://api.example.com/users",headers,5000,30000);

// POST请求设置超时
        Response response=Requests.post("https://api.example.com/users",headers,data,json,5000,30000);

// 流式请求设置超时
        Response response=Requests.getStream("https://api.example.com/download",headers,5000,30000);
```

### POST 请求 - JSON

```java
User user=new User("test","test@example.com");
        Response response=Requests.post("https://api.example.com/users",null,null,user);
```

### POST 请求 - 表单数据

```java
Map<String, Object> formData=new HashMap<>();
        formData.put("username","test");
        formData.put("password","123456");
        Response response=Requests.post("https://api.example.com/login",formData);
```

### 使用代理

```java
// HTTP 代理
ProxyConfig proxy=new ProxyConfig("proxy.example.com",8080);

// SOCKS 代理
        ProxyConfig socksProxy=new ProxyConfig("socks.example.com",1080,Proxy.Type.SOCKS);

// 带认证的代理
        ProxyConfig authProxy=new ProxyConfig("proxy.example.com",8080)
        .credentials("username","password");

// 设置全局代理
        Requests.setGlobalProxy(proxy);

// 为单个请求设置代理
        Response response=Requests.get("https://api.example.com/users",null,proxy);
```

### 解析响应

```java
				// 解析为特定类
        User user = response.getJson(User.class);

        // 解析为 Map
        Map<String, Object> data = response.getJson();

        // 解析为列表
        List<User> users = response.jsonList(User.class);

        // 解析复杂泛型类型
        TypeReference<List<Map<String, User>>> typeRef = new TypeReference<List<Map<String, User>>>() {};
        List<Map<String, User>> result = response.getJson(typeRef);
```

### 流式处理

#### 下载大文件

```java
try(Response response=Requests.getStream("https://example.com/large-file.zip")){
            if(response.isOk()){
                response.saveToFile("large-file.zip");
            }
        }
```

#### 按块处理数据

```java
try(Response response=Requests.getStream("https://api.example.com/stream")){
            if(response.isOk()){
                response.stream(chunk->{
                    // 处理每一块数据
                    System.out.println("收到 "+chunk.length+" 字节");
                });
            }
        }
```

#### 按行处理数据

```java
try(Response response=Requests.getStream("https://api.example.com/logs")){
            if(response.isOk()){
                response.streamLines(line->{
                    // 处理每一行数据
                    System.out.println(line);
                });
            }
        }
```

#### 自定义流处理

```java
try(Response response=Requests.getStream("https://api.example.com/data")){
            if(response.isOk()){
                InputStream inputStream=response.getInputStream();
                // 使用输入流进行自定义处理
            }
        }
```

### 错误处理

```java
try {
            Response response = Requests.get("https://api.example.com/users");
            if (!response.isOk()) {
                System.err.println("请求失败: " + response.getStatusCode());
                System.err.println("错误信息: " + response.getText());
            }
        } catch (IOException e) {
            if (e instanceof java.net.SocketTimeoutException) {
                System.err.println("请求超时");
            } else {
                System.err.println("请求异常: " + e.getMessage());
            }
        }
```

## 注意事项

1. 流式处理时请使用 try-with-resources 语句确保资源正确关闭
2. 使用代理时建议先测试代理的可用性
3. 处理大文件时推荐使用流式 API
4. JSON 解析失败时会抛出 IOException，请做好异常处理
5. 对于需要长期保持的连接，建议使用流式 API
6. 默认连接超时时间为 10 秒，读取超时时间为 30 秒
7. 超时时间可以全局设置，也可以针对单个请求设置
8. 推荐使用构建器模式来创建请求，这样可以更灵活地配置请求参数

## 许可证

本项目采用 Apache License 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。 