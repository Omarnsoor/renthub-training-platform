import React,{useEffect,useState}from'react';
import ReactDOM from'react-dom/client';
import{BrowserRouter}from'react-router-dom';
import App from'./App';
import EnterpriseWorkspace from'./enterprise/EnterpriseWorkspace';
import LiveWorkspaceDock from'./experience/LiveWorkspaceDock';
import OperationsPulse from'./experience/OperationsPulse';
import{get,session}from'./api/client';
import'./styles/app.css';
import'./styles/enterprise.css';
import'./styles/live-experience.css';
import'./styles/operations-pulse.css';

function AuthFeedback({state}){
  if(!state)return null;
  return <div className={`authFeedback ${state.type}`} role="status"><div className="authFeedbackIcon">{state.type==='in'?'✓':'↗'}</div><div><b>{state.title}</b><span>{state.message}</span></div></div>;
}

function Root(){
  const operations=window.location.pathname==='/operations';
  const[user,setUser]=useState(null),[ready,setReady]=useState(!operations),[authTick,setAuthTick]=useState(0),[feedback,setFeedback]=useState(null);
  useEffect(()=>{
    if(!session.token){setReady(true);return}
    get('/auth/me').then(setUser).catch(()=>{}).finally(()=>setReady(true));
  },[]);
  useEffect(()=>{
    let timer;
    const onAuth=async e=>{
      setAuthTick(x=>x+1);
      clearTimeout(timer);
      if(e.detail?.authenticated){
        let current=null;
        try{current=await get('/auth/me');setUser(current)}catch{}
        setFeedback({type:'in',title:`Welcome${current?.fullName?`, ${current.fullName.split(' ')[0]}`:''}`,message:'You are signed in and your RentHub workspace is ready.'});
      }else{
        setUser(null);
        setFeedback({type:'out',title:'Signed out successfully',message:'Your session has ended. See you on the next trip.'});
      }
      timer=setTimeout(()=>setFeedback(null),2400);
    };
    window.addEventListener('renthub:auth-change',onAuth);
    return()=>{window.removeEventListener('renthub:auth-change',onAuth);clearTimeout(timer)};
  },[]);
  if(operations){
    if(!ready)return <div className="appLoading">Loading RentHub operations…</div>;
    return <><AuthFeedback state={feedback}/><div className="opsStandaloneHeader"><a className="brand" href="/">Rent<span>Hub</span></a><a className="opsBack" href="/">← Back to marketplace</a></div><OperationsPulse user={user}/><EnterpriseWorkspace user={user}/></>;
  }
  return <><AuthFeedback state={feedback}/><App key={authTick}/><LiveWorkspaceDock user={user}/></>;
}

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode><BrowserRouter><Root/></BrowserRouter></React.StrictMode>
);
