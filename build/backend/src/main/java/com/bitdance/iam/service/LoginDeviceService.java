package com.bitdance.iam.service;

import com.bitdance.common.exception.BizException;
import com.bitdance.iam.domain.UserLoginDevice;
import com.bitdance.iam.dto.LoginDeviceDto;
import com.bitdance.iam.repository.UserLoginDeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LoginDeviceService {

    private final UserLoginDeviceRepository repo;

    public LoginDeviceService(UserLoginDeviceRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<LoginDeviceDto> list(Long userId) {
        return repo.findByUserIdOrderByLastLoginAtDesc(userId).stream().map(this::toDto).toList();
    }

    @Transactional
    public LoginDeviceDto trust(Long userId, Long id) {
        UserLoginDevice device = repo.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new BizException("DEVICE_NOT_FOUND", "登录设备不存在"));
        device.setIsTrusted(true);
        return toDto(repo.save(device));
    }

    private LoginDeviceDto toDto(UserLoginDevice device) {
        return new LoginDeviceDto(
            device.getId(),
            device.getDeviceName(),
            device.getPlatform(),
            device.getIpAddress(),
            device.getLastLoginAt(),
            device.getIsCurrent(),
            device.getIsTrusted()
        );
    }
}
