package com.ling.lingaicodegeneration.controller;

import com.ling.lingaicodegeneration.annotation.AuthCheck;
import com.ling.lingaicodegeneration.common.BaseResponse;
import com.ling.lingaicodegeneration.common.ResultUtils;
import com.ling.lingaicodegeneration.constant.UserConstant;
import com.ling.lingaicodegeneration.model.dto.appointment.AppointmentCancelRequest;
import com.ling.lingaicodegeneration.model.dto.appointment.AppointmentCreateRequest;
import com.ling.lingaicodegeneration.model.dto.appointment.AppointmentQueryRequest;
import com.ling.lingaicodegeneration.model.vo.AppointmentVO;
import com.ling.lingaicodegeneration.service.AppointmentService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    // 创建预约
    @PostMapping("/create")
    public BaseResponse<Long> createAppointment(
            @RequestBody AppointmentCreateRequest request,
            HttpServletRequest httpRequest) {
        long id = appointmentService.createAppointment(request, httpRequest);
        return ResultUtils.success(id);
    }

    // 取消预约
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelAppointment(
            @RequestBody AppointmentCancelRequest request,
            HttpServletRequest httpRequest) {
        boolean result = appointmentService.cancelAppointment(request.getId(), httpRequest);
        return ResultUtils.success(result);
    }

    // 查询我的预约
    @PostMapping("/my/list")
    public BaseResponse<Page<AppointmentVO>> listMyAppointments(
            @RequestBody AppointmentQueryRequest request,
            HttpServletRequest httpRequest) {
        return ResultUtils.success(appointmentService.listMyAppointments(request, httpRequest));
    }

    // 查询所有预约（仅admin）
    @PostMapping("/admin/list")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AppointmentVO>> listAllAppointments(
            @RequestBody AppointmentQueryRequest request) {
        return ResultUtils.success(appointmentService.listAllAppointments(request));
    }

    // 获取可用时间段
    @GetMapping("/slots")
    public BaseResponse<List<String>> getAvailableTimeSlots(
            @RequestParam String doctorName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResultUtils.success(appointmentService.getAvailableTimeSlots(doctorName, date));
    }
}