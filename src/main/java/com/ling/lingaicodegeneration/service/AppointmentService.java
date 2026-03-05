package com.ling.lingaicodegeneration.service;

import com.ling.lingaicodegeneration.model.dto.appointment.AppointmentCreateRequest;
import com.ling.lingaicodegeneration.model.dto.appointment.AppointmentQueryRequest;
import com.ling.lingaicodegeneration.model.entity.Appointment;
import com.ling.lingaicodegeneration.model.vo.AppointmentVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;

public interface AppointmentService {
    // 创建预约
    long createAppointment(AppointmentCreateRequest request, HttpServletRequest httpRequest);
    // 取消预约
    boolean cancelAppointment(Long id, HttpServletRequest httpRequest);
    // 查询我的预约（分页）
    Page<AppointmentVO> listMyAppointments(AppointmentQueryRequest request, HttpServletRequest httpRequest);
    // 查询所有预约（admin）
    Page<AppointmentVO> listAllAppointments(AppointmentQueryRequest request);
    // 获取可用时间段
    java.util.List<String> getAvailableTimeSlots(String doctorName, java.time.LocalDate date);
    // 构建QueryWrapper
    QueryWrapper getQueryWrapper(AppointmentQueryRequest request);
}