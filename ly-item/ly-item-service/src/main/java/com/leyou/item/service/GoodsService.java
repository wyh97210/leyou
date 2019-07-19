package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.page.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper  skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    public PageResult<Spu> querySpuBoByPage(String key, Boolean saleable, Integer page, Integer rows) {

        // 分页条件
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Spu.class);
        //搜索字段过滤
        Example.Criteria criteria = example.createCriteria();
        // 搜索条件
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        example.setOrderByClause("last_update_time DESC");

        // 执行查询
        List<Spu> spus = this.spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        loadCategoryAndBrandName(spus);
        //解析分页结果
        PageInfo<Spu> pageInfo = new PageInfo<>(spus);



        return new PageResult<>(pageInfo.getTotal(), spus);

    }
    private void loadCategoryAndBrandName(List<Spu> spus){
        //解析分lei和品牌的名称
        for(Spu spu :spus){
            // 查询分类名称
            List<String> names = categoryService.queryNamesByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names, "/"));

            // 查询品牌的名称
            spu.setBname(this.brandMapper.selectByPrimaryKey(spu.getBrandId()).getName());

        }
    }
    /**
     * 商品的新增
     * @param spu
     * @return
     */
    public void saveGoods(Spu spu) {

        //新增spu
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setId(null);
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if (count!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增spudetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        //新增sku和stock
        saveSkuAndStock(spu);



    }

    private void saveSkuAndStock(Spu spu) {
        List<Stock> stocks=new ArrayList<>();
        int count;//新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
           count = skuMapper.insert(sku);
           if (count!=1){
               throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
           }
            //新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
             stocks.add(stock);
        }
        count = stockMapper.insertList(stocks);
        if(count!=stocks.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    /**
     * 商品的更新
     * @param spu
     * @return
     */
    @Transactional
    public void updateGoods(Spu spu) {
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        //查询sku
        List<Sku> skus = skuMapper.select(sku);
        if(!CollectionUtils.isEmpty(skus)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count!=1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);

        }
        //修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count!=1) {
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        saveSkuAndStock(spu);
        //发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }
    /**
     * 根据spuId查询spuDetai
     * @param spuId
     * @return
     */
    public SpuDetail querySpuDetailBySpuId(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.SPUDETAIL_NOT_FOUND);


        }
        return spuDetail;
    }
    /**
     * 根据spuId查询sku
     * @param spuId
     * @return
     */
    public List<Sku> querySkuBySpuId(Long spuId) {
        Sku record = new Sku();
        record.setSpuId(spuId);

        List<Sku> skus = skuMapper.select(record);
        if(CollectionUtils.isEmpty(skus)){
            throw new LyException(ExceptionEnum.SKU_NOT_FOUND);
        }
        skus.forEach(sku ->{
            Stock stock = stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        });
        return skus;
    }

    /**
     * 根据id查询spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {

        Spu spu = spuMapper.selectByPrimaryKey(id);
        System.out.println("skus6666"+querySkuBySpuId(id));
        System.out.println(querySpuDetailBySpuId(id));
       spu.setSkus(querySkuBySpuId(id));
       spu.setSpuDetail(querySpuDetailBySpuId(id));
        return spu;
    }
}
