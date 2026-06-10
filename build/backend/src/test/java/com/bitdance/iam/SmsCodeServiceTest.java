package com.bitdance.iam;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.service.SmsCodeService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SmsCodeServiceTest {

    @Test
    void mockMode_sendAndVerify_doesNotNeedRedis() {
        SmsCodeService service = new SmsCodeService(null, true, "123456", 0);

        service.send("13800000000");

        assertDoesNotThrow(() -> service.verify("13800000000", "123456"));
        assertThrows(BizException.class, () -> service.verify("13800000000", "123456"));
    }

    @Test
    void mockMode_wrongCode_returnsInvalid() {
        SmsCodeService service = new SmsCodeService(null, true, "123456", 0);

        service.send("13800000000");

        assertThrows(BizException.class, () -> service.verify("13800000000", "000000"));
    }

    @Test
    void mockMode_cooldown_blocksRepeatedSend() {
        SmsCodeService service = new SmsCodeService(null, true, "123456", 60);

        service.send("13800000000");

        assertThrows(BizException.class, () -> service.send("13800000000"));
    }
}
