<template>
  <div class="document-manage-page">
    <a-card title="Document Management" style="margin-bottom: 24px">
      <a-form layout="inline" style="margin-bottom: 16px">
        <a-form-item label="Doc Type">
          <a-select v-model:value="uploadForm.docType" style="width: 150px">
            <a-select-option value="herb">Herb</a-select-option>
            <a-select-option value="treatment">Treatment</a-select-option>
            <a-select-option value="diagnosis">Diagnosis</a-select-option>
            <a-select-option value="formula">Formula</a-select-option>      <!-- 新加 -->
            <a-select-option value="condition">Condition</a-select-option>  <!-- 新加 -->
            <a-select-option value="general">General</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="Symptom Tag">
          <a-input v-model:value="uploadForm.symptomTag"
                   placeholder="e.g. insomnia" style="width: 150px" />
        </a-form-item>
        <a-form-item>
          <a-upload
            :before-upload="handleUpload"
            :show-upload-list="false"
            accept=".txt,.pdf"
          >
            <a-button type="primary" :loading="uploading">
              Upload Document
            </a-button>
          </a-upload>
        </a-form-item>
      </a-form>

      <a-table
        :dataSource="documents"
        :columns="columns"
        :loading="loading"
        rowKey="id"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-button type="link" danger @click="handleDelete(record.id)">
              Delete
            </a-button>
          </template>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { uploadDocument, listDocuments, deleteDocument } from '@/api/documentController'

const loading = ref(false)
const uploading = ref(false)
const documents = ref<any[]>([])

const uploadForm = ref({
  docType: 'herb',
  symptomTag: '',
})

const columns = [
  { title: 'Title', dataIndex: 'title', key: 'title' },
  { title: 'Type', dataIndex: 'docType', key: 'docType' },
  { title: 'Upload Time', dataIndex: 'createTime', key: 'createTime' },
  { title: 'Action', key: 'action' },
]

const fetchDocuments = async () => {
  console.log('fetchDocuments called')  // 加这行
  loading.value = true
  try {
    const res = await listDocuments({ pageNum: 1, pageSize: 100 })
    console.log('res:', res.data)  // 加这行
    if (res.data.code === 0) {
      documents.value = res.data.data.records ?? []
    }
  } finally {
    loading.value = false
  }
}

const handleUpload = async (file: File) => {
  uploading.value = true
  try {
    const res = await uploadDocument(file, uploadForm.value.docType, uploadForm.value.symptomTag)
    if (res.data.code === 0) {
      message.success('Document uploaded successfully')
      fetchDocuments()
    } else {
      message.error(res.data.message)
    }
  } finally {
    uploading.value = false
  }
  return false // 阻止 ant-design 默认上传行为
}

const handleDelete = (id: number) => {
  Modal.confirm({
    title: 'Delete Document',
    content: 'This will also delete all chunks and embeddings. Continue?',
    okType: 'danger',
    onOk: async () => {
      const res = await deleteDocument({ id })
      if (res.data.code === 0) {
        message.success('Document deleted')
        fetchDocuments()
      } else {
        message.error(res.data.message)
      }
    },
  })
}

onMounted(() => {
  fetchDocuments()
})
</script>

<style scoped>
.document-manage-page {
  padding: 24px;
}
</style>
