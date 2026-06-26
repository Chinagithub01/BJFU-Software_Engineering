/** 上传文件访问路径（开发环境经 Vite 代理到 Tomcat） */
export function fileUrl(filePath) {
  if (!filePath) return '#'
  const normalized = filePath.replace(/\\/g, '/').replace(/^\//, '')
  return `/${normalized}`
}

/** 作业附件下载（走 API，兼容 Docker 卷存储） */
export function submissionDownloadUrl(submissionId, inline = false) {
  if (!submissionId) return '#'
  const inlineQs = inline ? '&inline=true' : ''
  return `/api/submissions/download?submissionId=${submissionId}${inlineQs}`
}

/** Excel 导出（相对路径，走 /api 代理） */
export function exportUrl(assignmentId) {
  return `/api/export?assignmentId=${assignmentId}`
}

/** 申诉分数阈值（与课设默认一致，前端展示用） */
export const APPEAL_SCORE_THRESHOLD = 60

export function canAppeal(score) {
  if (score == null || score === '') return false
  return Number(score) < APPEAL_SCORE_THRESHOLD
}
