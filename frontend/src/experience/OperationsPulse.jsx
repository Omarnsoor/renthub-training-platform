import React,{useEffect,useState}from'react';
import{get}from'../api/client';

function metric(label,value,tone=''){return <div className={`pulseMetric ${tone}`}><span>{label}</span><b>{value}</b></div>}

export default function OperationsPulse({user}){
 const[data,setData]=useState(null);
 useEffect(()=>{if(!user)return;const role=String(user.role||'CUSTOMER').toUpperCase();(async()=>{
  if(role==='ADMIN'){
   const[tickets,refunds,disputes,docs]=await Promise.all([get('/support/tickets/admin').catch(()=>[]),get('/refunds/admin').catch(()=>[]),get('/disputes/admin').catch(()=>[]),get('/documents/admin').catch(()=>[])]);
   setData({role,items:[['Open support',tickets.filter(x=>!['RESOLVED','CLOSED'].includes(x.status)).length,'warn'],['Refund review',refunds.filter(x=>x.status==='REQUESTED').length,'warn'],['Active disputes',disputes.filter(x=>['OPEN','UNDER_REVIEW'].includes(x.status)).length,'bad'],['Pending documents',docs.filter(x=>x.verificationStatus==='PENDING').length,'']]});return;
  }
  if(role==='OWNER'){
   const[payouts,maintenance,notes]=await Promise.all([get('/payouts/mine').catch(()=>[]),get('/maintenance/mine').catch(()=>[]),get('/notifications').catch(()=>[])]);
   setData({role,items:[['Ready payouts',payouts.filter(x=>x.status==='READY').length,'good'],['Payout holds',payouts.filter(x=>x.status==='ON_HOLD').length,'bad'],['Maintenance',maintenance.filter(x=>['SCHEDULED','IN_PROGRESS'].includes(x.status)).length,'warn'],['Unread alerts',notes.filter(x=>!x.readFlag).length,'']]});return;
  }
  const[bookings,refunds,disputes,docs]=await Promise.all([get('/bookings/mine').catch(()=>[]),get('/refunds/mine').catch(()=>[]),get('/disputes/mine').catch(()=>[]),get('/documents/mine').catch(()=>[])]);
  setData({role,items:[['Active trips',bookings.filter(x=>['PENDING','PAID','REFUND_PENDING'].includes(x.status)).length,'good'],['Refunds',refunds.filter(x=>['REQUESTED','APPROVED'].includes(x.status)).length,'warn'],['Disputes',disputes.filter(x=>['OPEN','UNDER_REVIEW'].includes(x.status)).length,'bad'],['Verified docs',docs.filter(x=>x.verificationStatus==='VERIFIED').length,'']]});
 })()},[user?.id,user?.role]);
 if(!user||!data)return null;
 return <div className="operationsPulse"><div className="pulseIntro"><span className="pulseLive"><i/> LIVE BUSINESS STATE</span><strong>{data.role} workspace</strong><small>Real-time signals from RentHub business workflows</small></div><div className="pulseMetrics">{data.items.map(([l,v,t])=><React.Fragment key={l}>{metric(l,v,t)}</React.Fragment>)}</div></div>
}
