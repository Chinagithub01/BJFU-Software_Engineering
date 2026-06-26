import { reactive } from 'vue'

const state = reactive({ toasts: [] })
let seq = 0

function remove(id) {
  const i = state.toasts.findIndex((t) => t.id === id)
  if (i >= 0) state.toasts.splice(i, 1)
}

/** @param {'info'|'success'|'error'|'warning'} type */
export function toast(message, type = 'info', duration = 3200) {
  const id = ++seq
  state.toasts.push({ id, message, type })
  if (duration > 0) {
    setTimeout(() => remove(id), duration)
  }
  return id
}

export function toastSuccess(message) {
  return toast(message, 'success')
}

export function toastError(message) {
  return toast(message, 'error', 4500)
}

export function toastWarning(message) {
  return toast(message, 'warning')
}

export function useToastState() {
  return state
}
