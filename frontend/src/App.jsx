import React,{useEffect,useMemo,useState} from 'react';
import {Routes,Route,Link,useParams,useLocation} from 'react-router-dom';
import {get,post,patch} from './api/client';

const DEMO_USER_ID=1;

function Home(){
  return <>
    <section className="hero">
      <div>
        <span className="eyebrow">RENT • STAY • EXPLORE</span>
        <h1>One place for cars, stays, and useful services.</h1>
        <p>RentHub is a local-first rental platform with real catalog, booking, availability and add-on service flows.</p>
        <div className="actions"><Link to="/cars" className="primary">Browse cars</Link><Link to="/properties" className="secondary">Find a stay</Link></div>
      </div>
      <div className="heroCard"><b>Built for real workflows</b><span>Cars</span><span>Properties</span><span>Bookings</span><span>Extra services</span></div>
    </section>
    <section className="stats"><div><b>8</b><span>Cars ready to rent</span></div><div><b>8</b><span>Properties to explore</span></div><div><b>6</b><span>Extra services</span></div></section>
  </>;
}

function Catalog({type}){
  const [items,setItems]=useState([]);
  const [error,setError]=useState('');
  const isCar=type==='Cars';
  const path=isCar?'/cars':'/properties';
  const route=isCar?'cars':'properties';

  useEffect(()=>{get(path).then(setItems).catch(e=>setError(e.message))},[path]);

  return <section>
    <div className="sectionTitle"><div><span className="eyebrow">{type.toUpperCase()}</span><h2>{isCar?'Pick your ride':'Find your next stay'}</h2><p>{isCar?'Sedans, SUVs, compact cars and EVs across Jordan.':'Apartments, villas, studios, chalets and premium stays.'}</p></div></div>
    {error&&<div className="errorBox">{error}</div>}
    <div className="grid">{items.map(x=><Link className="card" to={`/${route}/${x.id}`} key={x.id}>
      <img src={x.imageUrl} alt={isCar?`${x.make} ${x.model}`:x.title}/>
      <div className="cardBody"><span className="pill">{x.city}</span><h3>{isCar?`${x.make} ${x.model}`:x.title}</h3><p>{isCar?`${x.modelYear} • ${x.transmission} • ${x.seats} seats`:`${x.type} • ${x.bedrooms} bedrooms • ${x.bathrooms} baths`}</p><div className="price">${isCar?x.dailyRate:x.nightlyRate}<small> / {isCar?'day':'night'}</small></div><span className="textLink">View details →</span></div>
    </Link>)}</div>
    {!error&&items.length===0&&<div className="empty">No listings found.</div>}
  </section>;
}

function AssetDetail({kind}){
  const {id}=useParams();
  const isCar=kind==='CAR';
  const [item,setItem]=useState(null);
  const [startDate,setStartDate]=useState('');
  const [endDate,setEndDate]=useState('');
  const [message,setMessage]=useState('');
  const [error,setError]=useState('');
  const [busy,setBusy]=useState(false);
  const path=isCar?`/cars/${id}`:`/properties/${id}`;

  useEffect(()=>{get(path).then(setItem).catch(e=>setError(e.message))},[path]);

  const estimatedDays=useMemo(()=>{
    if(!startDate||!endDate) return 0;
    const a=new Date(startDate); const b=new Date(endDate);
    return Math.max(0,Math.round((b-a)/86400000));
  },[startDate,endDate]);

  if(error) return <section><div className="errorBox">{error}</div></section>;
  if(!item) return <section><div className="empty">Loading details...</div></section>;

  const rate=isCar?item.dailyRate:item.nightlyRate;
  const title=isCar?`${item.make} ${item.model}`:item.title;

  async function book(){
    setMessage(''); setError('');
    if(!startDate||!endDate){setError('Choose start and end dates first.'); return;}
    setBusy(true);
    try{
      const booking=await post('/bookings',{userId:DEMO_USER_ID,assetType:kind,assetId:Number(id),startDate,endDate});
      setMessage(`Booking #${booking.id} created successfully. Total: $${booking.totalAmount}`);
    }catch(e){setError(e.message)}finally{setBusy(false)}
  }

  return <section>
    <Link to={isCar?'/cars':'/properties'} className="backLink">← Back to {isCar?'cars':'properties'}</Link>
    <div className="detailLayout">
      <div className="detailVisual"><img src={item.imageUrl} alt={title}/><div className="detailMeta"><span className="pill">{item.city}</span><span className="statusPill">{item.status}</span></div></div>
      <div className="detailPanel"><span className="eyebrow">{kind}</span><h2>{title}</h2><p>{isCar?`${item.modelYear} • ${item.transmission} • ${item.seats} seats`:item.description}</p>
        <div className="detailFacts">
          {isCar?<><div><b>{item.modelYear}</b><span>Model year</span></div><div><b>{item.seats}</b><span>Seats</span></div><div><b>{item.transmission}</b><span>Transmission</span></div></>:<><div><b>{item.bedrooms}</b><span>Bedrooms</span></div><div><b>{item.bathrooms}</b><span>Bathrooms</span></div><div><b>{item.type}</b><span>Property type</span></div></>}
        </div>
        <div className="bookingBox"><div className="price">${rate}<small> / {isCar?'day':'night'}</small></div><div className="dateGrid"><label>Start<input type="date" value={startDate} onChange={e=>setStartDate(e.target.value)}/></label><label>End<input type="date" value={endDate} onChange={e=>setEndDate(e.target.value)}/></label></div>{estimatedDays>0&&<p className="estimate">Estimated: {estimatedDays} {estimatedDays===1?'day':'days'} × ${rate} = <b>${Number(rate)*estimatedDays}</b></p>}<button className="primary full" onClick={book} disabled={busy}>{busy?'Booking...':'Book now'}</button>{message&&<div className="successBox">{message}</div>}{error&&<div className="errorBox">{error}</div>}</div>
      </div>
    </div>
  </section>;
}

function Services(){
  const [items,setItems]=useState([]);
  const [error,setError]=useState('');
  useEffect(()=>{get('/services').then(setItems).catch(e=>setError(e.message))},[]);
  return <section><span className="eyebrow">EXTRA SERVICES</span><h2>Make the booking easier</h2><p>Useful add-ons for trips, rentals and stays.</p>{error&&<div className="errorBox">{error}</div>}<div className="grid">{items.map(x=><article className="card" key={x.id}><img src={x.imageUrl} alt={x.name}/><div className="cardBody"><span className="pill">{x.category}</span><h3>{x.name}</h3><p>{x.description}</p><div className="price">${x.price}</div></div></article>)}</div></section>;
}

function Bookings(){
  const [items,setItems]=useState([]); const [error,setError]=useState('');
  const load=()=>get(`/bookings/user/${DEMO_USER_ID}`).then(setItems).catch(e=>setError(e.message));
  useEffect(load,[]);
  async function cancel(id){try{await patch(`/bookings/${id}/cancel`);load();}catch(e){setError(e.message)}}
  return <section><span className="eyebrow">MY BOOKINGS</span><h2>Your reservations</h2><p>Demo user bookings are shown here until authentication is added.</p>{error&&<div className="errorBox">{error}</div>}<div className="bookingList">{items.map(b=><article className="bookingRow" key={b.id}><div><span className="pill">{b.assetType}</span><h3>Booking #{b.id}</h3><p>{b.startDate} → {b.endDate}</p></div><div className="bookingRight"><b>${b.totalAmount}</b><span className={`bookingStatus ${b.status.toLowerCase()}`}>{b.status}</span>{b.status!=='CANCELLED'&&<button className="secondary" onClick={()=>cancel(b.id)}>Cancel</button>}</div></article>)}</div>{items.length===0&&!error&&<div className="empty">No bookings yet. Pick a car or property and create one.</div>}</section>;
}

function Header(){
  const location=useLocation();
  const linkClass=(path)=>location.pathname.startsWith(path)?'activeNav':'';
  return <header><Link className="logo" to="/">Rent<span>Hub</span></Link><nav><Link className={linkClass('/cars')} to="/cars">Cars</Link><Link className={linkClass('/properties')} to="/properties">Properties</Link><Link className={linkClass('/services')} to="/services">Services</Link><Link className={linkClass('/bookings')} to="/bookings">Bookings</Link></nav><button className="login">Sign in</button></header>;
}

export default function App(){return <><Header/><main><Routes><Route path="/" element={<Home/>}/><Route path="/cars" element={<Catalog type="Cars"/>}/><Route path="/cars/:id" element={<AssetDetail kind="CAR"/>}/><Route path="/properties" element={<Catalog type="Properties"/>}/><Route path="/properties/:id" element={<AssetDetail kind="PROPERTY"/>}/><Route path="/services" element={<Services/>}/><Route path="/bookings" element={<Bookings/>}/></Routes></main></>}
