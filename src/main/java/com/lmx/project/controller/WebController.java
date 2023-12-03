package com.lmx.project.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebController {

    @Value("${resource.path}")
    private String resource;


    @GetMapping()
    public String index() {
        return "forward:/catalogue";
    }


    /**
     * 获取文件目录以及请求转发
     *
     * @param map
     * @param request
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping(value = "/catalogue/**", produces = "text/html;charset=UTF-8")
    public String catalogue(Model map, HttpServletRequest request) throws UnsupportedEncodingException {
//        map.addAttribute("msg", "hello,world");
//
        String currnetPath = "/catalogue";
        String fileEncoding = "file.encoding";
        String uri = request.getRequestURI();
        String path = URLDecoder.decode(uri, StandardCharsets.UTF_8.toString());

        String encoding = System.getProperty(fileEncoding);

        File file;

        if (path != null) {
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


    /**
     * 获取资源路径
     *
     * @param path
     * @return
     * @throws UnsupportedEncodingException
     */
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

    /**
     * 拼接url编码后的字符串
     *
     * @param pre
     * @param encodingStr
     * @throws UnsupportedEncodingException
     */
    private static void addEncoding(StringBuilder pre, String encodingStr) throws UnsupportedEncodingException {
        String encode = URLEncoder.encode(encodingStr, StandardCharsets.UTF_8.toString());
        pre.append("/").append(encode);
    }

    /**
     * 获取文档树
     *
     * @param map
     * @param encoding
     * @param file
     */
    private static void getFileTree(Model map, String encoding, File file) {
        File[] files = file.listFiles();
        List<String> list = null;
        if (files != null) {
            list = Arrays.stream(files).map(f -> {
                try {
                    return new String(f.getName().getBytes(encoding), StandardCharsets.UTF_8);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }

            }).collect(Collectors.toList());
        }
        map.addAttribute("files", list);
        System.out.println(list);
    }
}