const API='http://localhost:8080/api';

async function request(path, options={}) {
  const response = await fetch(`${API}${path}`, {
    headers: {'Content-Type':'application/json', ...(options.headers || {})},
    ...options
  });

  if (!response.ok) {
    let message = `API ${response.status}`;
    try {
      const body = await response.json();
      message = body.detail || body.message || message;
    } catch (_) {}
    throw new Error(message);
  }

  if (response.status === 204) return null;
  return response.json();
}

export const get = (path) => request(path);
export const post = (path, body) => request(path, {method:'POST', body:JSON.stringify(body)});
export const patch = (path, body) => request(path, {method:'PATCH', body:body ? JSON.stringify(body) : undefined});
