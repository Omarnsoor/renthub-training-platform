import React,{useEffect,useMemo,useState}from'react';
import{get,patch}from'../api/client';

const money=v=>`$${Number(v||0).toFixed(2)}`;
const shortDate=v=>v?new Date(v).toLocaleDateString(undefined,{month:'short',day:'numeric'}):'—';

function statusTone(v=''){const s=String(v).toUpperCase();if(['PAID','COMPLETED','VERIFIED','READY','MET'].includes(s))return'good';if(['PENDING','OPEN','UNDER_REVIEW','REFUND_PENDING','ON_HOLD'].includes(s))return'warn';if(['REJECTED','FAILED','BREACHED','CANCELLED','EXPIRED'].includes(s))return'bad';return'neutral'}

export default function LiveWorkspaceDock({user}){
 const[open,setOpen]=useState(false),[notifications,setNotifications]=useState([]),[bookings,setBookings]=useState([]),[documents,setDocuments]=useState([]),[payouts,setPayouts]=useState([]),[loading,setLoading]=useState(false);
 const load=async()=>{if(!user)return;setLoading(true);try{const tasks=[get('/notifications').catch(()=>[]),get('/bookings/mine').catch(()=>[]),get('/documents/mine').catch(()=>[])];if(['OWNER','ADMIN'].includes(String(user.role).toUpperCase()))tasks.push(get('/payouts/mine').catch(()=>[]));const[n,b,d,p=[]]=await Promise.all(tasks);setNotifications(n);setBookings(b);setDocuments(d);setPayouts(p)}finally{setLoading(false)}};
 useEffect(()=>{if(user)load();else{setNotifications([]);setBookings([]);setDocuments([]);setPayouts([]);setOpen(false)}},[user?.id]);
 if(!user)return null;
 const unread=notifications.filter(n=>!n.readFlag).length;
 const latest=bookings[0];
 const verified=documents.filter(d=>d.verificationStatus==='VERIFIED'&&(!d.expiryDate||new Date(d.expiryDate)>=new Date()));
 const activeHold=payouts.find(p=>p.status==='ON_HOLD');
 const compliance=latest?.assetType==='CAR'?verified.some(d=>d.documentType==='DRIVING_LICENSE'):verified.some(d=>['NATIONAL_ID','PASSPORT'].includes(d.documentType));
 const markRead=async()=>{await patch('/notifications/read-all').catch(()=>{});load()};
 return <>
  <div className="liveDock" aria-label="Live workspace">
   <div className="liveSignal"><i/><span>LIVE</span></div>
   {latest&&<a className="liveBooking" href="/bookings"><small>Latest booking</small><b>#{latest.id} · {latest.status}</b></a>}
   <button className="liveBell" onClick={()=>setOpen(true)} aria-label="Open workspace">🔔{unread>0&&<span>{unread}</span>}</button>
   <button className="liveControl" onClick={()=>setOpen(true)}>Workspace</button>
  </div>
  {open&&<div className="workspaceBackdrop" onClick={()=>setOpen(false)}>
   <aside className="workspaceDrawer" onClick={e=>e.stopPropagation()}>
    <div className="workspaceHead"><div><span className="eyebrow">LIVE WORKSPACE</span><h2>{user.fullName}</h2><p>{user.role} · active session</p></div><button onClick={()=>setOpen(false)}>×</button></div>
    <div className="workspaceHealth">
     <div><span className="healthLabel">Session</span><strong className="healthGood">● Live</strong></div>
     <div><span className="healthLabel">Notifications</span><strong>{unread} unread</strong></div>
     <div><span className="healthLabel">Compliance</span><strong className={compliance?'healthGood':'healthWarn'}>{latest?compliance?'Ready':'Action needed':'No active trip'}</strong></div>
    </div>
    {latest&&<section className="workspaceCard trip"><div className="workspaceCardHead"><div><span>Current trip</span><h3>Booking #{latest.id}</h3></div><span className={`workspaceStatus ${statusTone(latest.status)}`}>{latest.status}</span></div><div className="tripMeta"><div><small>Type</small><b>{latest.assetType}</b></div><div><small>Dates</small><b>{shortDate(latest.startDate)} → {shortDate(latest.endDate)}</b></div><div><small>Amount</small><b>{money(latest.totalAmount)}</b></div></div><a className="workspacePrimary" href="/bookings">Manage booking →</a></section>}
    {activeHold&&<section className="workspaceAlert"><b>Financial hold active</b><span>Payout #{activeHold.id} is on hold while a refund or dispute is active.</span></section>}
    <section className="workspaceCard"><div className="workspaceCardHead"><div><span>Notifications</span><h3>What changed</h3></div>{unread>0&&<button className="markRead" onClick={markRead}>Mark all read</button>}</div><div className="workspaceFeed">{loading?<p>Refreshing…</p>:notifications.slice(0,4).map(n=><div key={n.id} className={n.readFlag?'':'unread'}><i/><span><b>{n.title}</b><small>{n.message}</small></span></div>)}{!loading&&!notifications.length&&<p>No notifications yet.</p>}</div></section>
    <section className="workspaceShortcuts"><a href="/bookings"><span>🧳</span><b>Bookings</b><small>Trips, services & payments</small></a><a href="/account"><span>👤</span><b>Account</b><small>Profile & security</small></a><a href="/operations"><span>⚙️</span><b>Operations</b><small>Full business workspace</small></a></section>
   </aside>
  </div>}
 </>
}
