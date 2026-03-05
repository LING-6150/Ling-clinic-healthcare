package com.ling.lingaicodegeneration.service.impl;

import com.ling.lingaicodegeneration.exception.BusinessException;
import com.ling.lingaicodegeneration.exception.ErrorCode;
import com.ling.lingaicodegeneration.model.dto.appointment.AppointmentCreateRequest;
import com.ling.lingaicodegeneration.model.dto.appointment.AppointmentQueryRequest;
import com.ling.lingaicodegeneration.model.entity.Appointment;
import com.ling.lingaicodegeneration.model.entity.User;
import com.ling.lingaicodegeneration.model.vo.AppointmentVO;
import com.ling.lingaicodegeneration.mapper.AppointmentMapper;
import com.ling.lingaicodegeneration.service.AppointmentService;
import com.ling.lingaicodegeneration.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment>
        implements AppointmentService {

    @Resource
    private UserService userService;

    // 固定时间段列表
    private static final List<String> ALL_TIME_SLOTS = Arrays.asList(
            "09:00-10:00", "10:00-11:00", "11:00-12:00",
            "14:00-15:00", "15:00-16:00", "16:00-17:00"
    );

    @Override
    public long createAppointment(AppointmentCreateRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        // 检查该时间段是否已被预约
        QueryWrapper qw = QueryWrapper.create()
                .eq("doctor_name", request.getDoctorName())
                .eq("appointment_date", request.getAppointmentDate())
                .eq("time_slot", request.getTimeSlot())
                .eq("status", "PENDING");
        long count = this.mapper.selectCountByQuery(qw);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "This time slot is already booked");
        }
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(request, appointment);
        appointment.setUserId(loginUser.getId());
        appointment.setStatus("PENDING");
        boolean result = this.save(appointment);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Failed to create appointment");
        }
        return appointment.getId();
    }

    @Override
    public boolean cancelAppointment(Long id, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        Appointment appointment = this.getById(id);
        if (appointment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Appointment not found");
        }
        // 只能取消自己的预约
        if (!appointment.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "No permission");
        }
        appointment.setStatus("CANCELLED");
        return this.updateById(appointment);
    }

    @Override
    public Page<AppointmentVO> listMyAppointments(AppointmentQueryRequest request, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        request.setUserId(loginUser.getId());
        QueryWrapper qw = getQueryWrapper(request);
        Page<Appointment> page = this.page(
                new Page<>(request.getPageNum(), request.getPageSize()), qw);
        return page.map(this::toVO);
    }

    @Override
    public Page<AppointmentVO> listAllAppointments(AppointmentQueryRequest request) {
        QueryWrapper qw = getQueryWrapper(request);
        Page<Appointment> page = this.page(
                new Page<>(request.getPageNum(), request.getPageSize()), qw);
        return page.map(this::toVO);
    }

    @Override
    public List<String> getAvailableTimeSlots(String doctorName, LocalDate date) {
        QueryWrapper qw = QueryWrapper.create()
                .eq("doctor_name", doctorName)
                .eq("appointment_date", date)
                .eq("status", "PENDING");
        List<Appointment> booked = this.list(qw);
        List<String> bookedSlots = booked.stream()
                .map(Appointment::getTimeSlot)
                .collect(Collectors.toList());
        return ALL_TIME_SLOTS.stream()
                .filter(slot -> !bookedSlots.contains(slot))
                .collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppointmentQueryRequest request) {
        QueryWrapper qw = QueryWrapper.create();
        if (request.getUserId() != null) {
            qw.eq("user_id", request.getUserId());
        }
        if (request.getStatus() != null) {
            qw.eq("status", request.getStatus());
        }
        if (request.getDoctorName() != null) {
            qw.like("doctor_name", request.getDoctorName());
        }
        qw.orderBy("create_time", false);
        return qw;
    }

    private AppointmentVO toVO(Appointment appointment) {
        AppointmentVO vo = new AppointmentVO();
        BeanUtils.copyProperties(appointment, vo);
        return vo;
    }
}