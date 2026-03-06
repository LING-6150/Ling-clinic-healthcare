// RAG 问答用 SSE，不能用普通 axios，需要用 fetch
export async function chatWithRag(
  body: {
    question: string
    docType?: string
    symptomTag?: string
    history?: { role: string; content: string }[]
  },
  onToken: (token: string) => void,
  onDone: () => void,
) {
  const response = await fetch((import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api') + '/rag/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(body),
  })

  const reader = response.body!.getReader()
  const decoder = new TextDecoder()

  while (true) {
    const { done, value } = await reader.read()
    if (done) {
      onDone()
      break
    }
    const text = decoder.decode(value)
    const lines = text.split('\n')
    for (const line of lines) {
      if (line.startsWith('data:')) {
        const data = line.slice(5).trim()
        if (data === '[DONE]') {
          onDone()
          return
        }
        if (data) onToken(data)
      }
    }
  }
}
