package com.ticket_online.domain.payment.strategy;

import com.ticket_online.domain.booking.domain.Order;
import com.ticket_online.domain.payment.dto.PaymentUrlResponse;
import com.ticket_online.global.config.vnpay.VnpayProperties;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VnPayStrategy implements PaymentStrategy {

    private final VnpayProperties vnpayProperties;

    @Override
    public PaymentUrlResponse createPayment(Order order) {
        String paymentUrl = buildVnpayUrl(order);
        return new PaymentUrlResponse(paymentUrl);
    }

    @Override
    public void handleCallback(Map<String, String> params) {

        String vnp_SecureHash = params.get("vnp_SecureHash");

        // remove hash fields
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        String hashData = buildHashData(params);
        String calculatedHash = hmacSHA512(vnpayProperties.hashSecret(), hashData);

        if (!calculatedHash.equalsIgnoreCase(vnp_SecureHash)) {
            throw new RuntimeException("Invalid VNPay signature");
        }
    }

    private String buildVnpayUrl(Order order) {

        String vnp_TmnCode = vnpayProperties.tmnCode();
        String vnp_HashSecret = vnpayProperties.hashSecret();
        String vnp_Url = vnpayProperties.payUrl();

        Map<String, String> params = new HashMap<>();

        // Required params
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
        params.put("vnp_ReturnUrl", vnpayProperties.returnUrl());

        params.put("vnp_IpAddr", "127.0.0.1");

        String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        params.put("vnp_CreateDate", createDate);

        // =========================
        // Build hash & query
        // =========================
        String hashData = buildHashData(params);
        String query = buildQuery(params);

        String secureHash = hmacSHA512(vnp_HashSecret, hashData);

        // Debug (nên giữ khi dev)
        System.out.println("HASH DATA: " + hashData);
        System.out.println("QUERY: " + query);
        System.out.println("HASH: " + secureHash);

        return vnp_Url + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    /** Build data for hashing (NO ENCODE) */
    private String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);

            if (value != null && !value.isEmpty()) {
                hashData.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                        .append("&");
            }
        }

        hashData.deleteCharAt(hashData.length() - 1);
        return hashData.toString();
    }

    /** Build query URL (ENCODE UTF-8) */
    private String buildQuery(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String value = params.get(fieldName);
            if (value != null && !value.isEmpty()) {
                query.append(fieldName)
                        .append("=")
                        .append(URLEncoder.encode(value, StandardCharsets.UTF_8))
                        .append("&");
            }
        }

        query.deleteCharAt(query.length() - 1);
        return query.toString();
    }

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey =
                    new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKey);

            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(2 * rawHmac.length);
            for (byte b : rawHmac) {
                hex.append(String.format("%02x", b & 0xff));
            }

            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException("Error while hashing VNPay", e);
        }
    }
}
