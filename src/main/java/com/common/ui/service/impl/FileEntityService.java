/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.common.ui.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.common.ui.service.EntityService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件服务抽象类
 *
 * @version V1.0
 **/
public class FileEntityService<T> implements EntityService<T> {
    private Path filePath;
    private List<T> entityList = new ArrayList<>();
    private Class<T> clazz;

    private static final Logger logger = LoggerFactory.getLogger(FileEntityService.class);

    public FileEntityService(String fileName, Class<T> clazz) {
        filePath = Paths.get(fileName);
        this.clazz = clazz;

        readFromFile();
    }

    private void readFromFile() {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (IOException e) {
            logger.error("读取文件内容失败", e);
            return;
        }
        entityList = lines.stream()
                .filter(StringUtils::isNotBlank)
                .map(line -> JSONObject.parseObject(line, clazz))
                .collect(Collectors.toList());
    }

    @Override
    public void save(T entity) {
        if (null == entity) {
            return;
        }

        entityList.add(entity);

        saveToFile();
    }

    /**
     * 将列表保存到文件中
     */
    protected void saveToFile() {
        List<String> contents = entityList.stream().map(JSONObject::toJSONString).collect(Collectors.toList());
        try {
            Files.write(filePath, contents);
        } catch (IOException e) {
            logger.error("保存到文件失败", e);
        }
    }

    @Override
    public List<T> list() {
        return entityList;
    }

    @Override
    public void delete(T t) {
        Iterator<T> it = entityList.iterator();
        while (it.hasNext()) {
            T t1 = it.next();

            if (equals(t1, t)) {
                it.remove();
                break;
            }
        }

        this.saveToFile();
    }

    protected Boolean equals(T t1, T t2) {
        return t1.equals(t2);
    }
}
