package com.zyj.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.zyj.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000119605017";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDB/FbWQvJ3l/RYFFhF4rDaOxE2zNEbbfGu2rH3ZCE0FQ+mRpRqbWHECXHfh2WHptrZoLKG/MVU/SOjz5jhdJ7kN1Ou/T05rybMPDUb1ZSiM+CaY4L4PK3bhzNCiueU07QdoOZ8JioM5JHgd2GQX4DDJPvN+vu4QKuRDhIjSEiTVkj/s6BUbot8R+leW10iodsHBu7D5J+SXGXsz3vYZ6iGQrK5Wj5GcrKjmAOgZywqUiVhgDp9Qy2ePbIYT4Htf2N/nT93De7m5Ch/3Yy8DoOMgdtSUBstagupnIPMRnsgKiIbTNJu4ANa/fN9WPQ3neUTt72BKpBTDgixkSguvw8LAgMBAAECggEAez47WVMdQtf6uD7sujHE1FE0pZFIxNl78/Ajox4tbi53S9eJkXg+5dJuU2ptGdrrLdrHsbVEFbPiAb/xzENQc4OnGgTv8gcSai1SPIXcMZUt/Eh+vAGpH1DtQziG2uoJg81mwbdpfdl1yJtwE85pOxHzHLF5XV1Q4w6CfYH+AgRymIxbM/Oi2lX68PBvzxncohtT+UB4cK/QUlP4yxMBE9qO09cMzU2XsbMq8xc00sOtGn0dOW/pBGBUp/H57WyG4+xR5EkBjc14dKo/Gxw5MGhumxDskiiMVNExEVVSfjeq+CeZW1QOUMYfhONJ9yuEB/+/n0DbacV258tzZhGC4QKBgQDytpwsGYqA3rwOPDl/i/iJtuA5MBy0j5h27Yo4o45VzkJV8zs509j+/cMldv8mhsUQffrWAtVwkybMKh1jb5d550zxVXSUxXHLRu2rLAK9bX1x4L9bajSmu8QGuRmMIKixi2px2blHdjp3Es9EDRQ5vvqM3jdQoHsLh4n+AFTccQKBgQDMmtvzmRKXj514KdPmSQDV1ErZVSnCUFAz3tLiLblkxwSbUzcCswhwsK2YGXG2X8lpCNDXS7mJ/EUMNWPdzWIY5dfrzXvUHAelKesa7+jTHwBJR0R5etwhDHrUv17onm8dyy6QmIMN1YZ2KDRFByHocukUVeVRkruDzk95grjROwKBgQDNsY7XHOoxXxJ26fk8q+39IBwUuz6Ik2191MAgUhRNidjUKKeX0X8EUyEwPhHsn6ig2nNbLdmfZ0YpHsP7QlZYfHrLyEVd05sl0D2ZpFnKGeGHQhZKLc7jQxvVKseI5yCwHtweEougEYVo5mr7XC445i5Gdgjg7rB4y+xZdAR3MQKBgAR+4pZXprDt1uftA1rr/7izEXH1DRXX7es12ixeFNTCCrfe/DZy+JyBRU8yP+1h7WsBZpVUdJ8zi9DY23jkKmOhqTzlJvNiKkjWwvO0kckRxU/W4QpKSMvxGkHScqNp18cvz7Ydo8OKDYpLF9rsbTJOVBC+UmuYbcZXoidpV+ZBAoGAHSEJiUkgvYRkf169f3pH/62/ezGrYbR3sOuuJAQqNxvHvdINOSeKkQEhfuxJx8WSLM1C+9cU46IOrQEzU9wzw+2p21WA1SzA1PnOYfWlsUrXCkPrx7oX9TbVvDtNbFlJn0fjPX3bvsfvvGQOw8I2PxLLkZBf7fWulAWX55L2EbQ=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhBiCfXIDi/uVIlD4mGbKkQSvWLzMAvG3mUq0idLhBaxHvBOgMmrdseeTgKn+nFIiHsFDFyLG+Jzw4mkGGJI4xi25eoMUxL5JOmA4Kh9iZLP/mRdugNqVHwyyL3d0tL42TPZfEUjmXcQEDYLA2sV1U5De8sw3/Hl8Kmk+kBk76BaulZW/3MtM7yXP8EzLRnRyEl5PSrrF4Ur4v9Z8+2bHsxI7+HohfpbPrm+MvkNDjWPPL59FKbKotL5TEmFZCM5h6a/xsi8bUcWZ6xdje1CIej0cAYel0qDusRXpcHSoLDAJQHRf6i5q7kkL4EThk0yNTEGcqYehU4TxLwLke8jU5wIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://4gm8172060.qicp.vip/payed/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 自动收单时间
    private String timeout = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;

    }
}
