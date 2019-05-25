package com.dzhy.manage.service.produce;

import com.dzhy.manage.common.Result;
import com.dzhy.manage.dao.OutputMapper;
import com.dzhy.manage.dao.ProductMapper;
import com.dzhy.manage.dao.ProductSukMapper;
import com.dzhy.manage.entity.*;
import com.dzhy.manage.enums.ResultEnum;
import com.dzhy.manage.exception.GeneralException;
import com.dzhy.manage.utils.CommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName AbstractUpdateService
 * @Description 进度更新 service
 * @Author alex
 * @Date 2019-05-25
 **/
@Service
@Slf4j
public abstract class AbstractUpdateService {

    private  OutputMapper outputMapper;
    private ProductMapper productMapper;
    private ProductSukMapper productSukMapper;

    @Autowired
    public void setOutputMapper(OutputMapper outputMapper) {
        this.outputMapper = outputMapper;
    }

    @Autowired
    public void setProductSukMapper(ProductSukMapper productSukMapper) {
        this.productSukMapper = productSukMapper;
    }

    @Autowired
    public void setProductMapper(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public abstract Result update(Produce origin, int value, String comment, int flag);

    public abstract Result fix(Produce origin, int value, String comment);

    private Output getOutput(int monthInt, int productId, int sukId) {
        Output output = outputMapper.selectByMonthAndProductIdAndSukId(monthInt, productId, sukId);
        if (output != null) {
            return output;
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            throw new GeneralException(ResultEnum.NOT_FOUND.getMessage() + "-产品ID" + productId);
        }
        ProductSuk suk = productSukMapper.selectByPrimaryKey(sukId);
        if (suk == null) {
            throw new GeneralException(ResultEnum.NOT_FOUND.getMessage() + "-SukID" + sukId);
        }
            output = Output.builder()
                    .month(monthInt)
                    .productId(productId)
                    .outputName(product.getProductName() + ":" + suk.getSukName())
                    .sukId(sukId)
                    .sukPrice(suk.getPrice())
                    .build();
        try {
            int count = outputMapper.insertSelective(output);
            log.info("insert output, count:{} outputId:{}", count, output.getOutputId());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new GeneralException(ResultEnum.ADD_ERROR.getMessage() + "产值");
        }
        return output;
    }

    private ProduceRecord makeRecord(int productId, int sukId,
                                     String colName1, int value1,
                                     String colName2, int value2,
                                     String colName3, int value3,
                                     String comment) {
        return ProduceRecord.builder()
                .userId(CommonUtil.getUserIdFromContext())
                .productId(productId)
                .sukId(sukId)
                .colName1(colName1)
                .value1(value1)
                .colName2(colName2)
                .value2(value2)
                .colName3(colName3)
                .value3(value3)
                .comments(CommonUtil.getUserNameFromContext() + ":" + comment)
                .build();
    }
}