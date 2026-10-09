const API='http://localhost:8080/api';

async function request(path,options={}){
  const response=await fetch(`${API}${path}`,{
    headers:{'Content-Type':'application/json',...(options.headers||{})},
    ...options
  });
  const text=await response.text();
  let data=null;
  if(text){try{data=JSON.parse(text)}catch{data=text}}
  if(!response.ok){
    const message=(data&&typeof data==='object'&&(data.detail||data.message||data.error))||`Request failed (${response.status})`;
    throw new Error(message);
  }
  return data;
}

export const get=(path)=>request(path);
export const post=(path,body)=>request(path,{method:'POST',body:JSON.stringify(body)});
export const patch=(path,body)=>request(path,{method:'PATCH',body:body?JSON.stringify(body):undefined});
