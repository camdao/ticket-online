package com.ticket_online.domain.payment.strategy;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.payment.dto.PaymentUrlResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class VnPayStrategy implements PaymentStrategy {

    @Override
    public PaymentUrlResponse createPayment(Order order) {
        String paymentUrl = buildVnpayUrl(order);
        return new PaymentUrlResponse(paymentUrl);
    }

    @Override
    public void handleCallback(Map<String, String> params) {
        // verify hash
        // update order
    }

    private String buildVnpayUrl(Order order) {

        String vnp_TmnCode = "YOUR_TMN_CODE";
        String vnp_HashSecret = "YOUR_HASH_SECRET";
        String vnp_Url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

        Map<String, String> params = new HashMap<>();

        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnp_TmnCode);
        params.put(
                "vnp_Amount", order.getTotalAmount().multiply(BigDecimal.valueOf(100)).toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", String.valueOf(order.getId()));
        params.put("vnp_OrderInfo", "Thanh toan don hang");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", "http://localhost:8080/payment/return");

        String query = buildQuery(params);

        String hash = hmacSHA512(vnp_HashSecret, query);

        return vnp_Url + "?" + query + "&vnp_SecureHash=" + hash;
    }

    public String buildQuery(Map<String, String> params) {

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);

            if (value != null && !value.isEmpty()) {

                query.append(fieldName);
                query.append("=");
                query.append(URLEncoder.encode(value, StandardCharsets.US_ASCII));

                query.append("&");
            }
        }

        query.deleteCharAt(query.length() - 1);

        return query.toString();
    }

    public static String hmacSHA512(String key, String data) {

        try {

            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(data.getBytes());

            StringBuilder hex = new StringBuilder(2 * rawHmac.length);
            for (byte b : rawHmac) {
                hex.append(String.format("%02x", b & 0xff));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
