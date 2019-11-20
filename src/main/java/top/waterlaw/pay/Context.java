package top.waterlaw.pay;

import java.math.BigDecimal;

public class Context {
    private Stratygy stratygy;

    public BigDecimal calRecharge(Integer channelId, Integer goodsId) throws Exception {
        StrategryFactory factory = StrategryFactory.getInstance();
        Stratygy stratygy = factory.create(channelId);
        return stratygy.calRecharge(channelId, goodsId);
    }

    //- 支付接口---->类型---->ICBC,ABNK

    //- 如果使用 Spring 需要再配置 BeanUtil
}
