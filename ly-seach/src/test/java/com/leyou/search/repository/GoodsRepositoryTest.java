package com.leyou.search.repository;

import com.leyou.common.page.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {
    @Autowired
    private  GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SearchService serchService;
    @Test
    public  void testCreateIndex(){
        elasticsearchTemplate.createIndex(Goods.class);
        elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        int page=1;
        int rows=100;
        int size=0;
        do{
        //查询spu信息
        PageResult<Spu> result  = goodsClient.querySpuBoByPage(null, true, page, rows);
        List<Spu> spuList = result.getItems();
        if (CollectionUtils.isEmpty(spuList)){
            break;
        }
        //购建goods
        List<Goods> goodsList = spuList.stream().map(serchService::buildGoods).collect(Collectors.toList());
        //存入索引库
    goodsRepository.saveAll(goodsList);
        page++;
        size=spuList.size();
        }while (size==100);
    }
}