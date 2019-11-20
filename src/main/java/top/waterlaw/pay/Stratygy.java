package top.waterlaw.pay;

import java.math.BigDecimal;

public interface Stratygy {

    BigDecimal calRecharge(Integer channelId, Integer goodsId);
}
