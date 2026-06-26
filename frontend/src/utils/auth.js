export function getToken() {
  return sessionStorage.getItem('token') || ''
}

export function isLoggedIn() {
  return !!getToken()
}

export function clearAuth() {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('user')
}

export function setAuth(token, user) {
  sessionStorage.setItem('token', token)
  sessionStorage.setItem('user', JSON.stringify(user))
}
