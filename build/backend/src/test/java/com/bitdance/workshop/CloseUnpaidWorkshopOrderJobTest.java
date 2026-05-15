package com.bitdance.workshop;

import com.bitdance.workshop.domain.WorkshopOrder;
import com.bitdance.workshop.job.CloseUnpaidWorkshopOrderJob;
import com.bitdance.workshop.repository.WorkshopOrderRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CloseUnpaidWorkshopOrderJobTest {

    private WorkshopOrder fakeOrder(long id, String status) {
        WorkshopOrder o = new WorkshopOrder() {
            @Override public Long getId() { return id; }
            @Override public String getOrderStatus() { return status; }
        };
        return o;
    }

    @Test
    void noStaleOrders_returnsZero() {
        WorkshopOrderRepository repo = mock(WorkshopOrderRepository.class);
        when(repo.findByOrderStatusAndCreatedAtBefore(anyString(), any()))
            .thenReturn(List.of());

        int closed = new CloseUnpaidWorkshopOrderJob(repo).runOnce();

        assertThat(closed).isZero();
        verify(repo, never()).save(any());
    }

    @Test
    void cancelsStaleOrders_andCountsThem() {
        WorkshopOrderRepository repo = mock(WorkshopOrderRepository.class);
        WorkshopOrder o1 = new WorkshopOrder();
        WorkshopOrder o2 = new WorkshopOrder();
        o1.setOrderStatus("pending_payment");
        o2.setOrderStatus("pending_payment");
        when(repo.findByOrderStatusAndCreatedAtBefore(anyString(), any()))
            .thenReturn(List.of(o1, o2));

        int closed = new CloseUnpaidWorkshopOrderJob(repo).runOnce();

        assertThat(closed).isEqualTo(2);
        verify(repo, times(2)).save(any(WorkshopOrder.class));

        ArgumentCaptor<WorkshopOrder> captor = ArgumentCaptor.forClass(WorkshopOrder.class);
        verify(repo, times(2)).save(captor.capture());
        for (WorkshopOrder saved : captor.getAllValues()) {
            assertThat(saved.getOrderStatus()).isEqualTo("canceled");
            assertThat(saved.getCanceledAt()).isNotNull();
        }
    }

    @Test
    void repoQueryFails_returnsZeroWithoutThrowing() {
        WorkshopOrderRepository repo = mock(WorkshopOrderRepository.class);
        when(repo.findByOrderStatusAndCreatedAtBefore(anyString(), any()))
            .thenThrow(new RuntimeException("db down"));

        int closed = new CloseUnpaidWorkshopOrderJob(repo).runOnce();

        assertThat(closed).isZero();
        verify(repo, never()).save(any());
    }

    @Test
    void singleSaveFailure_doesNotBlockOthers() {
        WorkshopOrderRepository repo = mock(WorkshopOrderRepository.class);
        WorkshopOrder o1 = new WorkshopOrder();
        WorkshopOrder o2 = new WorkshopOrder();
        o1.setOrderStatus("pending_payment");
        o2.setOrderStatus("pending_payment");
        when(repo.findByOrderStatusAndCreatedAtBefore(anyString(), any()))
            .thenReturn(List.of(o1, o2));
        when(repo.save(o1)).thenThrow(new RuntimeException("write fail"));
        when(repo.save(o2)).thenReturn(o2);

        int closed = new CloseUnpaidWorkshopOrderJob(repo).runOnce();

        // o1 失败、o2 成功 → 计数 1
        assertThat(closed).isEqualTo(1);
    }
}
