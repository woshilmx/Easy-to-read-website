# 简易阅读网站设计与实现

## 需求

- 我从网上找的关于设计模式的资料，但是是html格式的，手头上也没有很好的软件能够较为方便的管理这些文件，所以自己做了一个简易版的网站，也算一个小工具了，来进行阅读，后续如果还有类似的**文档集合**，直接上传到网站对应的资源目录下，即可方便管理。
- 虽然需求比较简单，但是还是踩了一点坑，特此记录一下。

## 技术选型

- SpringBoot2.5+SpringSecurity权限管理+Thymeleaf模版引擎

## 呈现效果

- 登录

![image-20231203220424312](https://lmxboke.oss-cn-beijing.aliyuncs.com/image-20231203220424312.png)

![image-20231203220440213](https://lmxboke.oss-cn-beijing.aliyuncs.com/image-20231203220440213.png)

![image-20231203220453914](https://lmxboke.oss-cn-beijing.aliyuncs.com/image-20231203220453914.png)

## 具体实现

#### 登录

采用了SpringSecurity框架，实现简易版的登录

- 引入依赖

~~~xmk
 <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
~~~

- 编写配置文件

~~~java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 任何请求都需要鉴权
        http.authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
            // 采用表单登录的方式
                .formLogin()
                .and()
            // 方式csrf攻击
                .csrf()
                .and()
            // 解决跨域
                .cors();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        
        auth.inMemoryAuthentication().withUser("xxxx")
                .password(passwordEncoder().encode("xxxx"))
                .roles("ADMIN")
                .and()
                .passwordEncoder(passwordEncoder()); // 设置密码校验器
    }
~~~

在此因为是仅供自己使用，基于内存的方式保存了用户名与密码

### 资源映射

~~~java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${resource.path}")
    private String resource;

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        System.out.println(resource);
        registry.addResourceHandler("/resource/**").addResourceLocations("file:" + resource);
    }
}
~~~

- 在配置文件中指明了资源目录的地址

#### **注意1：**

~~~yml
resource:
  path: E:\javaProject2023\Boke\resourse\
~~~

**对于这种目录的配置，最后一定要使用\或者/结尾，否则会提示找不到资源**

#### **注意2**

在本项目中，最开始使用的SpringBoot版本为2.7，但是2.7版本中对于url路径中存在中文的情况，无法找到资源，通过查阅资料发现，这是2.7版本中的bug，后来将版本切换为**2.5**，这个问题就消失了

### 构建展示页面

- 由于本项目比较简单，采用了Thymeleaf模版引擎的方式，进行编写。

- 在resources目录下，**新建templates目录**，在该目录下存放编写的页面模版，这是Thymeleaf默认配置的资源路径

![image-20231203221728879](https://lmxboke.oss-cn-beijing.aliyuncs.com/image-20231203221728879.png)

- 编写页面

~~~html
<!DOCTYPE html>
<html lang="en">
<head>
    <!--注意:这个meta节点最后有个结束的'/',一定要注意,否则会产生异常!!!后续我们再分析如何解决这个异常-->
    <meta charset="UTF-8"/>
    <title>index</title>
</head>
<body>
<li th:each="file : ${files}">
    <a th:href="@{|${#request.getRequestURL().toString()}/${file}|}" th:text="${file}"></a>
    <!--    <span th:text="${#request.getRequestURL()}"></span>-->
</li>
</body>
</html>
~~~

a标签中href执行的属性为当前url路径，加上资源文件夹中的**文件名称**

#### 编写Controller

~~~java

@GetMapping(value = "/catalogue/**", produces = "text/html;charset=UTF-8")
    public String catalogue(Model map, HttpServletRequest request) throws UnsupportedEncodingException {
//        map.addAttribute("msg", "hello,world");
//
        String currnetPath = "/catalogue";
        String fileEncoding = "file.encoding";
        // 获取请求路径
        String uri = request.getRequestURI();
        String path = URLDecoder.decode(uri, StandardCharsets.UTF_8.toString());

        String encoding = System.getProperty(fileEncoding);

        File file;

        if (path != null) {
            // 获取目录结果
            path = path.replace(currnetPath, "");
            file = new File(resource, path);
        } else {
            file = new File(resource);
        }


//        文件直接重定向到资源，如果是目录则获取资源的目录结构
        if (file.isDirectory()) {
            getFileTree(map, encoding, file);
            return "index";
        } else {
            String pre = getResourcePath(path);
            return pre;
        }
    }
~~~

- 这段代码的目的就是获取资源文件夹的目录结构，如果当前请求的是一个文件夹，那么返回的内容就是它所有的子文件夹，但是如果是一个文件，那么**直接重定向到资源目录中的文件位置**。

#### 注意3

@GetMapping(value = "/catalogue/**)这里的映射路径一定要写成这种形式，意思就是说以catalogue开头的所有路径，均会被转发到这个方法中，只有这样子，才能处理多级目录的情况.

例如，我的资源目录是这样子，设计模式/创建型/1.html。

那么在url路径中，如果不加`/**`，只有`/catalogue/设计模式`能被转发到这个方法，但是`/catalogue/设计模式/创建型`就无法被转发到这个方法。

#### 注意4

~~~java
 @NotNull
    private static String getResourcePath(String path) throws UnsupportedEncodingException {
        String[] split = path.split("/");
        StringBuilder pre = new StringBuilder("redirect:/resource");
        for (String s : split) {
            if (s != null && !"".equals(s)) {
                addEncoding(pre, s);
            }
        }
        return pre.toString();
    }
~~~

~~~java
 private static void addEncoding(StringBuilder pre, String encodingStr) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode(encodingStr, StandardCharsets.UTF_8.toString());
        pre.append("/").append(encode);
    }
~~~

- 这个方法是在进行请求转发到资源路径时，会记性URL编码，主要是处理url中出现中文的情况.
- 但是在URLEncoder.encode方法，是会对中文，符号，字母均进行编码，但是我们**只需要对中文进行编码**，**不能将符号`/`也编码，否则会出现找不到资源的情况**。

## 上线部署

- 打包jar包到服务器上
- 在项目目录下，**新建config目录，新建application.yml文件**

![image-20231203223619127](C:/Users/Lenovo/AppData/Roaming/Typora/typora-user-images/image-20231203223619127.png)

![image-20231203223627344](C:/Users/Lenovo/AppData/Roaming/Typora/typora-user-images/image-20231203223627344.png)

- 新建resources目录，存放我们的资源。
- 启动jar包

#### 注意5

- 说明：**SpringBoot2.5在部署时有个bug，需要在config目录下新建一个空的文件夹，否则无法运行。**

#### 注意6

- 在使用xftp7上传中文文件夹时，要将xftp7的默认编码方式改为utf8,否则会出现上传后，文件夹名称乱码的情况。
- 具体在xftp中文件名正常，但是在xshell中文件名乱码情况。

具体可参考这篇文章 https://blog.whsir.com/post-6560.html

