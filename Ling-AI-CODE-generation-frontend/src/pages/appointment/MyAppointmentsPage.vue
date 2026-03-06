<template>
  <div class="my-appointments-page">
    <a-card title="My Appointments">
      <a-table
        :dataSource="appointments"
        :columns="columns"
        :loading="loading"
        :pagination="{ pageSize: 10 }"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="statusColor(record.status)">{{ record.status }}</a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-button
              v-if="record.status === 'PENDING'"
              type="link"
              danger
              @click="handleCancel(record.id)"
            >
              Cancel
            </a-button>
            <span v-else>-</span>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { listMyAppointments, cancelAppointment } from '@/api/appointmentController'

const loading = ref(false)
const appointments = ref<any[]>([])

const columns = [
  { title: 'Doctor', dataIndex: 'doctorName', key: 'doctorName' },
  { title: 'Date', dataIndex: 'appointmentDate', key: 'appointmentDate' },
  { title: 'Time Slot', dataIndex: 'timeSlot', key: 'timeSlot' },
  { title: 'Status', dataIndex: 'status', key: 'status' },
  { title: 'Note', dataIndex: 'note', key: 'note' },
  { title: 'Action', key: 'action' },
]

const statusColor = (status: string) => {
  if (status === 'PENDING') return 'blue'
  if (status === 'CONFIRMED') return 'green'
  if (status === 'CANCELLED') return 'red'
  return 'default'
}

const fetchAppointments = async () => {
  loading.value = true
  try {
    const res = await listMyAppointments({ pageNum: 1, pageSize: 100 })
    if (res.data.code === 0) {
      appointments.value = res.data.data.records
    }
  } finally {
    loading.value = false
  }
}

const handleCancel = (id: number) => {
  Modal.confirm({
    title: 'Cancel Appointment',
    content: 'Are you sure you want to cancel this appointment?',
    onOk: async () => {
      const res = await cancelAppointment({ id })
      if (res.data.code === 0) {
        message.success('Appointment cancelled')
        fetchAppointments()
      } else {
        message.error(res.data.message)
      }
    },
  })
}

onMounted(() => {
  fetchAppointments()
})
</script>

<style scoped>
.my-appointments-page {
  padding: 24px;
}
</style>
