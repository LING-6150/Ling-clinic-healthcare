import request from '@/request'

// 创建预约
export async function createAppointment(body: {
  doctorName: string
  appointmentDate: string
  timeSlot: string
  note?: string
}) {
  return request<any>('/appointment/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

// 取消预约
export async function cancelAppointment(body: { id: number }) {
  return request<any>('/appointment/cancel', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

// 我的预约列表
export async function listMyAppointments(body: {
  pageNum: number
  pageSize: number
  status?: string
}) {
  return request<any>('/appointment/my/list', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}

// 获取可用时间段
export async function getAvailableTimeSlots(params: {
  doctorName: string
  date: string
}) {
  return request<any>('/appointment/slots', {
    method: 'GET',
    params,
  })
}

// 管理员查看所有预约
export async function listAllAppointments(body: {
  pageNum: number
  pageSize: number
  status?: string
  doctorName?: string
}) {
  return request<any>('/appointment/admin/list', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    data: body,
  })
}
