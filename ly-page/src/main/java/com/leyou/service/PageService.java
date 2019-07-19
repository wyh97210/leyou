package com.leyou.service;

import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecificationClient;
import com.leyou.item.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
@Slf4j
@Service
public class PageService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private TemplateEngine templateEngine;


    public Map<String,Object> loadModel(Long spuId) {
        Map<String,Object> model=new  HashMap<>();

        //
        Spu spu = goodsClient.querySpuById(spuId);
        System.out.println(spu);
        //
        List<Sku> skus = spu.getSkus();
        System.out.println(skus);
        //
        SpuDetail detail = spu.getSpuDetail();
        System.out.println(detail);
        //
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        System.out.println(brand);
        //
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        System.out.println(categories);
        //
        List<SpecGroup> specs = specificationClient.queryGroupByCid(spu.getCid3());
        System.out.println(specs);
        model.put("title",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());
        model.put("skus",skus);
        model.put("detail",detail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("specs",specs);
        System.out.println(model);
        return model;
    }
    public void createHtml(Long spuId){
        //上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File dest = new File("D:/wyh/nginx-1.16.0/html/item", spuId + ".html");
        if (dest.exists()){
            dest.delete();
        }
        try(PrintWriter writer=new PrintWriter(dest,"UTF-8")) {
            //生成html
            templateEngine.process("item",context,writer);
        }catch (Exception e){
            log.error("静态页面异常",e);
        }

    }

    public void deleteHtml(Long spuId) {
        File dest = new File("D:/wyh/nginx-1.16.0/html/item", spuId + ".html");
        if (dest.exists()){
            dest.delete();
        }
    }
}
