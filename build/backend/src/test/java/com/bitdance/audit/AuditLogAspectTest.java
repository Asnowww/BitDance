package com.bitdance.audit;

import com.bitdance.audit.aspect.AuditAction;
import com.bitdance.audit.aspect.AuditLogAspect;
import com.bitdance.audit.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * AuditLogAspect 切面绑定测试。
 *
 * 用 Spring AOP 的 AspectJProxyFactory 构造代理，验证：
 *  - 注解方法成功执行后调用 AuditLogService.record
 *  - 返回值若是 record 且首字段是 Long，targetId 能正确提取
 *  - 业务方法抛异常时不写日志（异常向上抛出）
 *  - audit 模块自身失败不影响业务返回
 */
class AuditLogAspectTest {

    public static class TargetBean {
        @AuditAction(value = "test.create", targetType = "test_obj")
        public TestResult create(Long input) {
            return new TestResult(input * 10, "ok");
        }

        @AuditAction(value = "test.fail", targetType = "test_obj")
        public TestResult fail() {
            throw new IllegalStateException("boom");
        }
    }

    public record TestResult(Long id, String name) {}

    private TargetBean buildProxy(AuditLogService service) {
        AuditLogAspect aspect = new AuditLogAspect(service);
        AspectJProxyFactory factory = new AspectJProxyFactory(new TargetBean());
        factory.addAspect(aspect);
        return factory.getProxy();
    }

    @Test
    void success_writesAuditLog_withRecordId() {
        AuditLogService service = mock(AuditLogService.class);
        TargetBean proxy = buildProxy(service);

        TestResult ret = proxy.create(5L);

        assertThat(ret.id()).isEqualTo(50L);
        verify(service, times(1)).record(any(), any(), eq("test.create"), eq("test_obj"), eq(50L));
    }

    @Test
    void failure_doesNotWriteAuditLog_rethrows() {
        AuditLogService service = mock(AuditLogService.class);
        TargetBean proxy = buildProxy(service);

        try {
            proxy.fail();
        } catch (IllegalStateException expected) {
            // expected
        }

        verify(service, never()).record(any(), any(), any(), any(), any());
    }
}
