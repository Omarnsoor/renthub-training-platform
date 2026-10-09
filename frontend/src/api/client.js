const API='http://localhost:8080/api';
export async function get(path){const r=await fetch(`${API}${path}`); if(!r.ok) throw new Error(`API ${r.status}`); return r.json();}
