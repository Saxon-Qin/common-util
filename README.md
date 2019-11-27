个人总结的常用辅助类包，包括：
- LocalCache：一个轻量级的线程安全本地内存缓存，当应用重启后被清空，因此不能存储需要可以作为Redis等分布式缓存的一种补充；主要提供以下功能：
    - 根据Key/Value的方式缓存应用级的数据，当应用重启后清空；
    - 指定缓存数据的存活时间，当数据缓存超过存活时间后将会被清除；
    - 根据Key从缓存中获取数据；
    最好是使用一个Configuration将其托管到Spring容器中去，然后在需要使用的地方通过@Resource方式注入进去使用。
    一个简单的使用示例：
    ```java
    @Resource
    private LocalCache localCache;

    public void test() {
        // 缓存数据，一分钟过期
        String key = "test";
        localCache.cacheWithTimeoutInMinutes(key, "test", 1);

        // 查询缓存的数据
        Optional<String> value = localCache.get(key);
        value.ifPresent(v -> ...);

        // 查询缓存数据，如果不存在则使用默认值
        String value = localCache.get(key, "test");

        // 查询缓存数据，如果不存在则使用生成器生成值然后返回
        String value = localCache.get(key, () -> "test");

        // 查询缓存数据，如果不存在，则使用生成器生成并进行缓存，然后返回生成的值
        String value = localCache.getOrCache(key, () -> "test");

    }
    ```

- MapEnhancer，map增强器，通过Builder方式使用Map，省去往Map中写值时的重复代码，使用方法如下：
    ```java
    Map<String, Object> map = MapEnhancer.of(12)
        .put("test", 1)
        .put("test1", 2)
        .put("test2", 3)
        .putNotNull("test3", null)
        .putIf("test4", "test4Value", () -> return false)
        .build();
    ```

- SingleValue, 单值对象，主要是使用在Lambda语句中，因为直接的Object变量传入到Lambda语句中必须是Final类型的，所以可以通过SingleValue进行一下包装；
    如要在forEach中进行计数：
    ```java
        SingleValue<Long> singleValue = new SingleValue<Long>().setValue(maxLineId);
        productionLines.forEach(line -> line
                .setLineId(singleValue.getAndSet(singleValue.getValue() + 1))
                .setUpdateTime(nowTime));
    ```

- BiValue，双值对象，用于某些需要两个值的场景下，遍历List并且根据List中的Map统计两个值时：
   ```java
       BiValue<Float, Float> biValue = new BiValue<>();
       deviceTypeDataList.forEach(m -> {
           float partInventoryCount = MapUtils.getFloatValue(m, "partInventoryCount");
           float safeInventoryCount = MapUtils.getFloatValue(m, "safeInventoryCount");
           biValue.changeKey(partInventoryCount, v -> v + partInventoryCount)
                   .changeValue(safeInventoryCount, v -> v + safeInventoryCount);
       });
   ```

- Excel导入导出包，使用POI方式导入导出2003或者2007格式的Excel文件；
  使用方式：
    ```java
        // 定义Columns
        List<ExcelColumn<ProductionLineDTO>> columns = Arrays.asList(
                    new ExcelColumn<ProductionLineDTO>().setTitle("省份*").setNullable(false).setDataHandler((line, cell) -> {
                        String value = cell.getStringCellValue();
                        if (StringUtils.isNotBlank(value) && value.length() > 2) {
                            line.setProvinceCode(value.substring(0, 2)).setProvinceName(value.substring(2));
                        }
                    }),
                    new ExcelColumn<ProductionLineDTO>().setTitle("客户").setNullable(false),
                    new ExcelColumn<ProductionLineDTO>("产线品牌", "lineBrand", 64L).setNullable(false),
                    new ExcelColumn<ProductionLineDTO>("生产线名称", "lineName", 255L).setNullable(false),
                    new ExcelColumn<ProductionLineDTO>("生产线类型*", "lineType", 64L).setSelectedValues(Arrays.asList("流水线", "固定模台线")),
                    new ExcelColumn<ProductionLineDTO>("设计产能", "lineDesignCapacity").setType(ExcelColumnType.DECIMAL).setNullable(false),
                    new ExcelColumn<ProductionLineDTO>("模台数量", "modelCount").setType(ExcelColumnType.NUMBER),
                    new ExcelColumn<ProductionLineDTO>("养护窑舱位数", "warehousePosition").setType(ExcelColumnType.DECIMAL),
                    new ExcelColumn<ProductionLineDTO>("生产线位置", "lineAddress", 1000L).setNullable(false),
                    new ExcelColumn<ProductionLineDTO>("经度", "longitude").setType(ExcelColumnType.DECIMAL),
                    new ExcelColumn<ProductionLineDTO>("纬度", "latitude").setType(ExcelColumnType.DECIMAL)
            );

        // 生成导入模板
        Workbook workbook = ExcelUtils.createXSSFEmptyWorkbook(columns);

        // 读取内容
        List<ProductionLineDTO> productionLines = ExcelUtils.read(file, columns, ProductionLineDTO.class);

        // 根据数据生成Workbook
        Workbook workbook = ExcelUtils.dataToXSSFWorkbook(dataList, Test.class, columns);
    ```
    ### 参考 CSDN博主：icarusliu81
    ####  博主github: https://github.com/icarusliu/lcommon

