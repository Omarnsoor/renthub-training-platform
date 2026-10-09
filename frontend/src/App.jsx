import React,{useEffect,useMemo,useState} from 'react';
import {Routes,Route,Link,useParams,useLocation} from 'react-router-dom';
import {get,post,patch} from './api/client';
import {mediaFor} from './data/media';

const DEMO_USER_ID=1;

function SmartImage({item,isCar,className=''}){
  const media=mediaFor(item,isCar);
  const [src,setSrc]=useState(media.local||item.imageUrl);
  useEffect(()=>setSrc(media.local||item.imageUrl),[media.local,item.imageUrl]);
  return <img className={className} src={src} alt={isCar?`${item.make} ${item.model}`:item.title} loading="lazy" onError={()=>{
    if(src!==media.remote) setSrc(media.remote||item.imageUrl);
    else if(src!==item.imageUrl) setSrc(item.imageUrl);
  }}/>;
}

function Home(){
  return <>
    <section className="marketHero">
      <div className="heroCopy">
        <span className="eyebrow light">JORDAN • RENT • STAY • EXPLORE</span>
        <h1>Everything you need for your next trip.</h1>
        <p>Book verified cars, hand-picked stays and practical travel services in one place.</p>
        <div className="actions"><Link to="/cars" className="primary lightButton">Find a car</Link><Link to="/properties" className="glassButton">Explore stays</Link></div>
        <div className="trustRow"><span>✓ Flexible booking</span><span>✓ Transparent pricing</span><span>✓ Local support</span></div>
      </div>
      <div className="heroPhoto"><img src="/assets/real/car-land-cruiser.jpg" onError={e=>{e.currentTarget.src='https://commons.wikimedia.org/wiki/Special:FilePath/Toyota%20Land%20Cruiser%20J300%203.3%20ZX%202024.jpg?width=1600'}} alt="Toyota Land Cruiser"/></div>
    </section>

    <section className="quickSearch">
      <div><span className="eyebrow">START HERE</span><h2>What are you looking for?</h2></div>
      <div className="quickCards">
        <Link to="/cars" className="quickCard"><div className="quickIcon">🚘</div><div><b>Rent a car</b><span>8 vehicles across Jordan</span></div><strong>→</strong></Link>
        <Link to="/properties" className="quickCard"><div className="quickIcon">🏡</div><div><b>Book a stay</b><span>8 apartments, villas & homes</span></div><strong>→</strong></Link>
        <Link to="/services" className="quickCard"><div className="quickIcon">✦</div><div><b>Add services</b><span>Drivers, Wi-Fi, cleaning & more</span></div><strong>→</strong></Link>
      </div>
    </section>

    <section className="stats"><div><b>8</b><span>Cars ready to rent</span></div><div><b>8</b><span>Properties to explore</span></div><div><b>6</b><span>Useful add-on services</span></div></section>
  </>;
}

function Catalog({type}){
  const [items,setItems]=useState([]);
  const [error,setError]=useState('');
  const [query,setQuery]=useState('');
  const [city,setCity]=useState('ALL');
  const [sort,setSort]=useState('recommended');
  const isCar=type==='Cars';
  const path=isCar?'/cars':'/properties';
  const route=isCar?'cars':'properties';

  useEffect(()=>{get(path).then(setItems).catch(e=>setError(e.message))},[path]);
  const cities=useMemo(()=>['ALL',...Array.from(new Set(items.map(x=>x.city))).sort()],[items]);
  const filtered=useMemo(()=>{
    let data=items.filter(x=>{
      const text=isCar?`${x.make} ${x.model} ${x.city}`:`${x.title} ${x.type} ${x.city}`;
      return text.toLowerCase().includes(query.toLowerCase())&&(city==='ALL'||x.city===city);
    });
    const rate=x=>Number(isCar?x.dailyRate:x.nightlyRate);
    if(sort==='price-low') data=[...data].sort((a,b)=>rate(a)-rate(b));
    if(sort==='price-high') data=[...data].sort((a,b)=>rate(b)-rate(a));
    return data;
  },[items,query,city,sort,isCar]);

  return <section className="catalogSection">
    <div className="catalogHeader"><div><span className="eyebrow">{type.toUpperCase()}</span><h2>{isCar?'Find the right car':'Stay somewhere memorable'}</h2><p>{isCar?'City cars, premium sedans, SUVs and EVs available across Jordan.':'Apartments, villas, studios, chalets and unique stays for every trip.'}</p></div><div className="resultCount"><b>{filtered.length}</b><span>available listings</span></div></div>
    <div className="filterBar"><label className="searchField"><span>Search</span><input value={query} onChange={e=>setQuery(e.target.value)} placeholder={isCar?'Brand, model or city':'Property, type or city'}/></label><label><span>Location</span><select value={city} onChange={e=>setCity(e.target.value)}>{cities.map(c=><option value={c} key={c}>{c==='ALL'?'All locations':c}</option>)}</select></label><label><span>Sort by</span><select value={sort} onChange={e=>setSort(e.target.value)}><option value="recommended">Recommended</option><option value="price-low">Price: low to high</option><option value="price-high">Price: high to low</option></select></label></div>
    {error&&<div className="errorBox">{error}</div>}
    <div className="listingGrid">{filtered.map(x=>{
      const rate=isCar?x.dailyRate:x.nightlyRate;
      return <Link className="listingCard" to={`/${route}/${x.id}`} key={x.id}>
        <div className="imageWrap"><SmartImage item={x} isCar={isCar}/><span className="availabilityBadge">Available</span><button className="heartButton" onClick={e=>e.preventDefault()} aria-label="Save">♡</button></div>
        <div className="listingBody"><div className="listingTop"><span className="locationText">⌖ {x.city}</span><span className="rating">★ 4.{(x.id%4)+6}</span></div><h3>{isCar?`${x.make} ${x.model}`:x.title}</h3><p>{isCar?`${x.modelYear} · ${x.transmission} · ${x.seats} seats`:`${x.type} · ${x.bedrooms} bedrooms · ${x.bathrooms} baths`}</p><div className="listingBottom"><div><strong>${rate}</strong><span> / {isCar?'day':'night'}</span></div><span className="detailsArrow">View details →</span></div></div>
      </Link>;
    })}</div>
    {!error&&filtered.length===0&&<div className="empty">No listings match your search.</div>}
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
  const estimatedDays=useMemo(()=>{if(!startDate||!endDate)return 0;return Math.max(0,Math.round((new Date(endDate)-new Date(startDate))/86400000));},[startDate,endDate]);
  if(error) return <section><div className="errorBox">{error}</div></section>;
  if(!item) return <section><div className="empty">Loading details...</div></section>;
  const rate=Number(isCar?item.dailyRate:item.nightlyRate);
  const title=isCar?`${item.make} ${item.model}`:item.title;

  async function book(){
    setMessage('');setError('');
    if(!startDate||!endDate){setError('Choose start and end dates first.');return;}
    setBusy(true);
    try{const booking=await post('/bookings',{userId:DEMO_USER_ID,assetType:kind,assetId:Number(id),startDate,endDate});setMessage(`Booking #${booking.id} created. Total: $${booking.totalAmount}`);}catch(e){setError(e.message)}finally{setBusy(false)}
  }

  return <section className="detailSection">
    <Link to={isCar?'/cars':'/properties'} className="backLink">← Back to {isCar?'cars':'properties'}</Link>
    <div className="detailHeading"><div><span className="eyebrow">{isCar?'CAR RENTAL':'STAY'}</span><h2>{title}</h2><p>⌖ {item.city} &nbsp; · &nbsp; ★ 4.8 &nbsp; · &nbsp; Verified listing</p></div><div className="detailPrice"><strong>${rate}</strong><span> / {isCar?'day':'night'}</span></div></div>
    <div className="detailLayout">
      <div><div className="detailVisual"><SmartImage item={item} isCar={isCar}/><div className="photoBadge">Real listing photo</div></div><div className="detailDescription"><h3>{isCar?'Vehicle details':'About this stay'}</h3><p>{isCar?`${item.modelYear} ${item.make} ${item.model} with ${item.transmission.toLowerCase()} transmission and comfortable seating for ${item.seats}.`:item.description}</p><div className="detailFacts">{isCar?<><div><b>{item.modelYear}</b><span>Model year</span></div><div><b>{item.seats}</b><span>Seats</span></div><div><b>{item.transmission}</b><span>Transmission</span></div></>:<><div><b>{item.bedrooms}</b><span>Bedrooms</span></div><div><b>{item.bathrooms}</b><span>Bathrooms</span></div><div><b>{item.type}</b><span>Property type</span></div></>}</div></div></div>
      <aside className="bookingCard"><div className="bookingCardHead"><div><strong>${rate}</strong><span> / {isCar?'day':'night'}</span></div><span className="statusDot">● Available</span></div><div className="dateGrid"><label>Pick-up / check-in<input type="date" value={startDate} onChange={e=>setStartDate(e.target.value)}/></label><label>Return / check-out<input type="date" value={endDate} onChange={e=>setEndDate(e.target.value)}/></label></div>{estimatedDays>0&&<div className="priceBreakdown"><span>${rate} × {estimatedDays} {estimatedDays===1?'day':'days'}</span><b>${rate*estimatedDays}</b></div>}<button className="primary full" onClick={book} disabled={busy}>{busy?'Confirming...':'Reserve now'}</button><p className="smallNote">You won't be charged. This is a local training booking flow.</p>{message&&<div className="successBox">{message}</div>}{error&&<div className="errorBox">{error}</div>}<div className="bookingBenefits"><span>✓ Instant confirmation</span><span>✓ Clear pricing</span><span>✓ Easy cancellation</span></div></aside>
    </div>
  </section>;
}

function Services(){
  const [items,setItems]=useState([]);const [error,setError]=useState('');
  useEffect(()=>{get('/services').then(setItems).catch(e=>setError(e.message))},[]);
  const icons={TRANSPORT:'🚙',CLEANING:'✨',SUPPORT:'🛟',CONNECTIVITY:'📶',CAR_ADDON:'🧸'};
  return <section className="catalogSection"><div className="catalogHeader"><div><span className="eyebrow">EXTRA SERVICES</span><h2>Add what makes the trip easier</h2><p>Useful extras you can combine with a car rental or property booking.</p></div></div>{error&&<div className="errorBox">{error}</div>}<div className="serviceGrid">{items.map(x=><article className="serviceCard" key={x.id}><div className="serviceIcon">{icons[x.category]||'✦'}</div><span className="pill">{x.category.replace('_',' ')}</span><h3>{x.name}</h3><p>{x.description}</p><div className="serviceFooter"><strong>${x.price}</strong><button className="secondary">Add service</button></div></article>)}</div></section>;
}

function Bookings(){
  const [items,setItems]=useState([]);const [error,setError]=useState('');
  const load=()=>get(`/bookings/user/${DEMO_USER_ID}`).then(setItems).catch(e=>setError(e.message));useEffect(load,[]);
  async function cancel(id){try{await patch(`/bookings/${id}/cancel`);load();}catch(e){setError(e.message)}}
  return <section className="catalogSection"><span className="eyebrow">MY BOOKINGS</span><h2>Your reservations</h2><p>Manage your current demo reservations here.</p>{error&&<div className="errorBox">{error}</div>}<div className="bookingList">{items.map(b=><article className="bookingRow" key={b.id}><div><span className="pill">{b.assetType}</span><h3>Booking #{b.id}</h3><p>{b.startDate} → {b.endDate}</p></div><div className="bookingRight"><b>${b.totalAmount}</b><span className={`bookingStatus ${b.status.toLowerCase()}`}>{b.status}</span>{b.status!=='CANCELLED'&&<button className="secondary" onClick={()=>cancel(b.id)}>Cancel</button>}</div></article>)}</div>{items.length===0&&!error&&<div className="empty">No bookings yet. Choose a car or stay to create your first reservation.</div>}</section>;
}

function Header(){
  const location=useLocation();const linkClass=path=>location.pathname.startsWith(path)?'activeNav':'';
  return <header><Link className="logo" to="/">Rent<span>Hub</span></Link><nav><Link className={linkClass('/cars')} to="/cars">Cars</Link><Link className={linkClass('/properties')} to="/properties">Stays</Link><Link className={linkClass('/services')} to="/services">Services</Link><Link className={linkClass('/bookings')} to="/bookings">Bookings</Link></nav><div className="headerActions"><span className="supportLink">Help & support</span><button className="login">Sign in</button></div></header>;
}

function Footer(){return <footer><div><Link className="logo" to="/">Rent<span>Hub</span></Link><p>Local-first rental marketplace built for realistic business-flow training.</p></div><div><b>Explore</b><Link to="/cars">Cars</Link><Link to="/properties">Stays</Link><Link to="/services">Services</Link></div><div><b>Platform</b><Link to="/bookings">Bookings</Link><span>Jordan · USD</span><span>Local environment</span></div></footer>}

export default function App(){return <><Header/><main><Routes><Route path="/" element={<Home/>}/><Route path="/cars" element={<Catalog type="Cars"/>}/><Route path="/cars/:id" element={<AssetDetail kind="CAR"/>}/><Route path="/properties" element={<Catalog type="Properties"/>}/><Route path="/properties/:id" element={<AssetDetail kind="PROPERTY"/>}/><Route path="/services" element={<Services/>}/><Route path="/bookings" element={<Bookings/>}/></Routes></main><Footer/></>}
