<template>
  <div class="appointment-page">
    <a-card title="Book an Appointment" style="max-width: 600px; margin: 0 auto">
      <a-form :model="form" layout="vertical" @finish="handleSubmit">
        <a-form-item label="Doctor" name="doctorName" :rules="[{ required: true }]">
          <a-select v-model:value="form.doctorName" placeholder="Select a doctor"
                    @change="fetchTimeSlots">
            <a-select-option value="Dr. Li">Dr. Li</a-select-option>
            <a-select-option value="Dr. Wang">Dr. Wang</a-select-option>
            <a-select-option value="Dr. Chen">Dr. Chen</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Date" name="appointmentDate" :rules="[{ required: true }]">
          <a-date-picker v-model:value="form.appointmentDate"
                         style="width: 100%"
                         :disabled-date="disabledDate"
                         @change="fetchTimeSlots" />
        </a-form-item>

        <a-form-item label="Time Slot" name="timeSlot" :rules="[{ required: true }]">
          <a-select v-model:value="form.timeSlot" placeholder="Select a time slot"
                    :disabled="!availableSlots.length">
            <a-select-option v-for="slot in availableSlots" :key="slot" :value="slot">
              {{ slot }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="Note (optional)" name="note">
          <a-textarea v-model:value="form.note" :rows="3" placeholder="Any special notes..." />
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" :loading="submitting" block>
            Book Appointment
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { createAppointment, getAvailableTimeSlots } from '@/api/appointmentController'
import { useRouter } from 'vue-router'

const router = useRouter()
const submitting = ref(false)
const availableSlots = ref<string[]>([])

const form = reactive({
  doctorName: '',
  appointmentDate: null as dayjs.Dayjs | null,
  timeSlot: '',
  note: '',
})

const disabledDate = (current: dayjs.Dayjs) => {
  return current && current < dayjs().startOf('day')
}

const fetchTimeSlots = async () => {
  if (!form.doctorName || !form.appointmentDate) return
  const date = dayjs(form.appointmentDate).format('YYYY-MM-DD')
  const res = await getAvailableTimeSlots({ doctorName: form.doctorName, date })
  if (res.data.code === 0) {
    availableSlots.value = res.data.data
    form.timeSlot = ''
  }
}

const handleSubmit = async () => {
  submitting.value = true
  try {
    const res = await createAppointment({
      doctorName: form.doctorName,
      appointmentDate: dayjs(form.appointmentDate).format('YYYY-MM-DD'),
      timeSlot: form.timeSlot,
      note: form.note,
    })
    if (res.data.code === 0) {
      message.success('Appointment booked successfully!')
      router.push('/my-appointments')
    } else {
      message.error(res.data.message)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.appointment-page {
  padding: 24px;
}
</style>
