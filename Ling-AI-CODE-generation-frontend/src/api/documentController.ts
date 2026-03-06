import request from '@/request'

// 上传文档（admin）
export async function uploadDocument(
  file: File,
  docType: string,
  symptomTag?: string,
) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('docType', docType)
  if (symptomTag) formData.append('symptomTag', symptomTag)
  return request<any>('/document/upload', {
    method: 'POST',
    data: formData,
  })
}

// 文档列表
export async function listDocuments(params: {
  pageNum: number
  pageSize: number
}) {
  return request<any>('/document/list', {
    method: 'GET',
    params,
  })
}

// 删除文档（admin）
export async function deleteDocument(params: { id: number }) {
  return request<any>('/document/delete', {
    method: 'POST',
    params,
  })
}
