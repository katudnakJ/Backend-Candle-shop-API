package com.senior.candleShopProject.feature.order.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.order.controller.dto.request.RejectPaymentReq;
import com.senior.candleShopProject.feature.order.controller.dto.request.TrackOrderReq;
import com.senior.candleShopProject.feature.order.controller.dto.response.PDFResp;
import com.senior.candleShopProject.feature.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Order Service API.")
@RequestMapping("v1/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Get all carriers.", description = "ดึงข้อมูลผู้ให้บริการขนส่งทั้งหมด")
    @GetMapping("/carriers")
    @PreAuthorize("hasRole('CUST') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<GenericResponse> getAllCarriers(@RequestAttribute("userId") String userId) {
        log.info("Getting all carriers");

        GenericResponse response = orderService.getAllCarriers();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order by status.", description = "ดึงข้อมูลออเดอร์ตามสถานะ")
    @GetMapping()
    @PreAuthorize("hasRole('CUST') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<GenericResponse> getOrderByStatus(@RequestAttribute("userId") String userId,
                                                            @RequestParam(value ="status") String status,
                                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                                            @RequestParam(value = "size", defaultValue = "10") int size) throws ShopServiceApiException {
        log.info("Getting order by status for status: {}", status);
        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = orderService.getOrderByStatus(userUUID, status, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get order details by order id.", description = "ดึงข้อมูลรายละเอียดออเดอร์ตามรหัสออเดอร์")
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUST') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<GenericResponse> getOrderDetailsByOrderId(@RequestAttribute("userId") String userId,
                                                                   @PathVariable("orderId") String orderId) throws ShopServiceApiException {
        log.info("Getting order details by order id: {}", orderId);
        UUID userUUID = UUID.fromString(userId);
        UUID orderUUID = UUID.fromString(orderId);

        GenericResponse response = orderService.getOrderDetailsByOrderId(userUUID, orderUUID);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Confirm payment for order by seller.", description = "ยืนยันการชำระเงินสำหรับออเดอร์โดยผู้ขาย")
    @PatchMapping("/{orderId}/confirm")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity confirmPayment(@RequestAttribute("userId") String userId,
                                         @PathVariable("orderId") String orderId) throws ShopServiceApiException {
        log.info("Confirming payment for order {}", orderId);
        UUID userUUID = UUID.fromString(userId);
        UUID orderUUID = UUID.fromString(orderId);

        GenericResponse response = orderService.confirmPayment(userUUID, orderUUID);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reject payment for order by seller.", description = "ปฏิเสธการชำระเงินสำหรับออเดอร์โดยผู้ขาย")
    @PatchMapping("/{orderId}/reject")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity rejectPayment(@RequestAttribute("userId") String userId,
                                        @PathVariable ("orderId") String orderId,
                                        @RequestBody RejectPaymentReq rejectPaymentReq) throws ShopServiceApiException {
            log.info("Rejecting payment for order {}", orderId);
            UUID userUUID = UUID.fromString(userId);
            UUID orderUUID = UUID.fromString(orderId);

            GenericResponse response = orderService.rejectPayment(userUUID, orderUUID, rejectPaymentReq);
            return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add tracking number for order.", description = "ใส่เลขพัสดุสำหรับออเดอร์โดยผู้ขาย")
    @PatchMapping("/{orderId}/track")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity trackOrder(@RequestAttribute("userId") String userId,
                                     @PathVariable ("orderId") String orderId,
                                     @RequestBody TrackOrderReq trackOrderReq) throws ShopServiceApiException {
        log.info("Tracking order {}",orderId);
        UUID userUUID = UUID.fromString(userId);
        UUID orderUUID = UUID.fromString(orderId);

        GenericResponse response = orderService.trackOrder(userUUID,orderUUID, trackOrderReq);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/payment-proof")
    @Operation(summary = "Get signed URL for payment proof image.", description = "ดึง URL สำหรับรูปภาพหลักฐานการชำระเงิน")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<GenericResponse> getPaymentProofImage(@RequestAttribute("userId") String userId,
                                        @PathVariable("orderId") String orderId) throws ShopServiceApiException {
        log.info("Getting payment proof image for order {}", orderId);
        UUID userUUID = UUID.fromString(userId);
        UUID orderUUID = UUID.fromString(orderId);

        GenericResponse response = orderService.getPaymentSlipByOrderId(userUUID, orderUUID);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/received")
    @Operation(summary = "Confirm order received by customer.", description = "ยืนยันการได้รับสินค้าโดยลูกค้า")
    @PreAuthorize("hasRole('CUST') or hasRole('ADMIN')")
    public ResponseEntity confirmOrderReceived(@RequestAttribute("userId") String userId,
                                              @PathVariable("orderId") String orderId) throws ShopServiceApiException {
        log.info("Confirming order received for order {}", orderId);
        UUID userUUID = UUID.fromString(userId);
        UUID orderUUID = UUID.fromString(orderId);

        GenericResponse response = orderService.confirmReceipt(userUUID, orderUUID);
        return ResponseEntity.ok(response);
        }

        @GetMapping("/{orderId}/receipt")
        @Operation(summary = "Get receipt PDF for order.", description = "ดึงไฟล์ PDF ใบเสร็จสำหรับออเดอร์")
        @PreAuthorize("hasRole('CUST') or hasRole('SELLER') or hasRole('ADMIN')")
        public ResponseEntity<GenericResponse> getReceiptPDF(@RequestAttribute("userId") String userId,
                                              @PathVariable("orderId") String orderId) throws ShopServiceApiException, IOException {
            log.info("Getting receipt PDF for order {}", orderId);
            UUID userUUID = UUID.fromString(userId);
            UUID orderUUID = UUID.fromString(orderId);

            GenericResponse response = orderService.generateReceiptToPDF(userUUID, orderUUID);
            return ResponseEntity.ok(response);
        }
}
