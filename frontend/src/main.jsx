import React,{useEffect,useState}from'react';
import ReactDOM from'react-dom/client';
import{BrowserRouter}from'react-router-dom';
import App from'./App';
import EnterpriseWorkspace from'./enterprise/EnterpriseWorkspace';
import{get,session}from'./api/client';
import'./styles/app.css';
import'./styles/enterprise.css';

function Root(){
  const operations=window.location.pathname==='/operations';
  const[user,setUser]=useState(null),[ready,setReady]=useState(!operations);
  useEffect(()=>{
    if(!session.token){setReady(true);return}
    get('/auth/me').then(setUser).catch(()=>{}).finally(()=>setReady(true));
  },[]);
  if(operations){
    if(!ready)return <div className="appLoading">Loading RentHub operations…</div>;
    return <><div className="opsStandaloneHeader"><a className="brand" href="/">Rent<span>Hub</span></a><a className="opsBack" href="/">← Back to marketplace</a></div><EnterpriseWorkspace user={user}/></>;
  }
  return <><App/>{session.token&&<a className="opsLauncher" href="/operations">Open Operations</a>}</>;
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode><BrowserRouter><Root/></BrowserRouter></React.StrictMode>
);
