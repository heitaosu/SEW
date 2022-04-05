package com.conpany.project;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.util.FileUtils;
import com.alibaba.excel.util.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ExcelTest {
    public static void write(){
        String fileName = "D:\\" + System.currentTimeMillis() + ".xlsx";
        System.out.println(fileName);
        EasyExcel.write(fileName).head(head()).sheet("数据").doWrite(data());
    }

    private static List<List<String>> head() {
        List<List<String>> list = ListUtils.newArrayList();
        List<String> head0 = ListUtils.newArrayList();
        head0.add("字符串" + System.currentTimeMillis());
        List<String> head1 = ListUtils.newArrayList();
        head1.add("数字" + System.currentTimeMillis());
        List<String> head2 = ListUtils.newArrayList();
        head2.add("日期" + System.currentTimeMillis());
        list.add(head0);
        list.add(head1);
        list.add(head2);
        return list;
    }

    private static List<List> data() {
        List<List> list = ListUtils.newArrayList();
        for (int i = 0; i < 10; i++) {
            List data = new ArrayList();
            data.add("字符串" + i);
            data.add(new Date());
            data.add(0.56);
            list.add(data);
        }
        return list;
    }

    public static void main(String[] args) throws IOException {
        boolean ss = StringUtils.isNumeric("444.7886");
        System.out.println(ss);

        File file = new File("D:\\51463460.001");
        byte[] bytes = FileUtils.readFileToByteArray(file);
        String str = new String(bytes);
        str = str.replaceAll( "\\s+", " ");
        str = str.replace(" ","_");
        String[] array = str.split("_");
        System.out.println("型号="+array[1]+",序列号="+array[3]+",安装方式="+array[15]);
        System.out.println(str);
    }
}
