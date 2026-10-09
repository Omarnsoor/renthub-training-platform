import React,{useEffect,useMemo,useState}from'react';
import{Routes,Route,Link,useParams,useLocation,useNavigate}from'react-router-dom';
import{get,post,patch,del,session}from'./api/client';
import{mediaFor}from'./data/media';

const today=()=>new Date().toISOString().slice(0,10);
const money=v=>`$${Number(v||0).toFixed(2)}`;
const prettyKey=k=>k.replace(/([A-Z])/g,' $1').replace(/^./,c=>c.toUpperCase());

function SmartImage({item,isCar,className=''}){
  const media=mediaFor(item,isCar);
  const fallback=isCar?'/assets/car-camry.svg':'/assets/property-apartment.svg';
  const[src,setSrc]=useState(media.local||item.imageUrl||fallback);
  useEffect(()=>setSrc(media.local||item.imageUrl||fallback),[media.local,item.imageUrl,fallback]);
  return <img className={className} src={src} alt={isCar?`${item.make} ${item.model}`:item.title} loading="lazy" onError={()=>{
    if(media.remote&&src!==media.remote)setSrc(media.remote);
    else if(item.imageUrl&&src!==item.imageUrl)setSrc(item.imageUrl);
    else if(src!==fallback)setSrc(fallback);
  }}/>;
}

function PageState({children,type='empty'}){return <div className={type==='error'?'errorBox':'empty'}>{children}</div>}

function AuthPage({mode,onAuth}){
  const nav=useNavigate();
  const[form,setForm]=useState({fullName:'',email:'',password:''});
  const[error,setError]=useState('');
  const[busy,setBusy]=useState(false);
  async function submit(e){
    e.preventDefault();setError('');setBusy(true);
    try{const payload=mode==='login'?{email:form.email,password:form.password}:form;const result=await post(`/auth/${mode}`,payload);session.set(result.token);onAuth(result.user);nav('/');}
    catch(err){setError(err.message)}finally{setBusy(false)}
  }
  return <section className="authWrap"><form className="authCard" onSubmit={submit}>
    <span className="eyebrow">RENTHUB ACCOUNT</span><h2>{mode==='login'?'Welcome back':'Create your account'}</h2>
    <p className="muted">{mode==='login'?'Sign in to manage bookings, favorites and payments.':'Create one account for cars, stays, reviews and trip services.'}</p>
    {mode==='register'&&<label>Full name<input required value={form.fullName} onChange={e=>setForm({...form,fullName:e.target.value})} autoComplete="name"/></label>}
    <label>Email<input required type="email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})} autoComplete="email"/></label>
    <label>Password<input required minLength="6" type="password" value={form.password} onChange={e=>setForm({...form,password:e.target.value})} autoComplete={mode==='login'?'current-password':'new-password'}/></label>
    {error&&<div className="errorBox">{error}</div>}
    <button disabled={busy} className="primary full">{busy?'Please wait…':mode==='login'?'Sign in':'Create account'}</button>
    <p>{mode==='login'?<>New here? <Link to="/register">Create an account</Link></>:<>Already have an account? <Link to="/login">Sign in</Link></>}</p>
  </form></section>;
}

function FeaturedStrip(){
  const[cars,setCars]=useState([]),[properties,setProperties]=useState([]);
  useEffect(()=>{get('/cars').then(x=>setCars(x.slice(0,2))).catch(()=>{});get('/properties').then(x=>setProperties(x.slice(0,2))).catch(()=>{})},[]);
  const items=[...cars.map(x=>({kind:'CAR',item:x})),...properties.map(x=>({kind:'PROPERTY',item:x}))];
  if(!items.length)return null;
  return <section className="featuredSection"><div className="sectionHeading"><div><span className="eyebrow">POPULAR NOW</span><h2>Start with a verified favorite</h2></div><Link to="/cars">Browse all →</Link></div><div className="featuredGrid">{items.map(({kind,item})=>{
    const isCar=kind==='CAR';return <Link className="miniCard" key={`${kind}-${item.id}`} to={`/${isCar?'cars':'properties'}/${item.id}`}><SmartImage item={item} isCar={isCar}/><div><span>{item.city}</span><b>{isCar?`${item.make} ${item.model}`:item.title}</b><small>From {money(isCar?item.dailyRate:item.nightlyRate)} / {isCar?'day':'night'}</small></div></Link>})}</div></section>;
}

function Home(){return <>
  <section className="marketHero"><div className="heroCopy"><span className="eyebrow light">JORDAN • RENT • STAY • EXPLORE</span><h1>Everything you need for your next trip.</h1><p>Book verified cars, hand-picked stays and practical travel services in one simple local marketplace.</p><div className="actions"><Link to="/cars" className="primary lightButton">Find a car</Link><Link to="/properties" className="glassButton">Explore stays</Link></div><div className="heroTrust"><span>✓ Transparent pricing</span><span>✓ Real availability checks</span><span>✓ Easy trip management</span></div></div><div className="heroPhoto"><img src="/assets/real/car-land-cruiser.jpg" alt="RentHub featured vehicle" onError={e=>{e.currentTarget.src='/assets/car-camry.svg'}}/></div></section>
  <section className="stats"><div><b>8+</b><span>Cars across Jordan</span></div><div><b>8+</b><span>Places to stay</span></div><div><b>6</b><span>Trip add-ons</span></div></section>
  <FeaturedStrip/>
  <section className="trustSection"><div><span>01</span><h3>Choose</h3><p>Search by location and compare transparent daily or nightly rates.</p></div><div><span>02</span><h3>Reserve</h3><p>Check exact dates before booking so conflicts are caught early.</p></div><div><span>03</span><h3>Customize & pay</h3><p>Add useful services, pay locally in the training flow and keep your receipt.</p></div></section>
</>}

function Catalog({type,user}){
  const[items,setItems]=useState([]),[query,setQuery]=useState(''),[city,setCity]=useState('ALL'),[sort,setSort]=useState('recommended'),[favs,setFavs]=useState([]),[error,setError]=useState(''),[loading,setLoading]=useState(true);
  const isCar=type==='Cars',path=isCar?'/cars':'/properties',route=isCar?'cars':'properties';
  useEffect(()=>{setLoading(true);Promise.all([get(path).then(setItems),user?get('/favorites').then(setFavs):Promise.resolve()]).catch(e=>setError(e.message)).finally(()=>setLoading(false))},[path,user]);
  const cities=useMemo(()=>['ALL',...new Set(items.map(x=>x.city))],[items]);
  const data=useMemo(()=>{let result=items.filter(x=>(isCar?`${x.make} ${x.model} ${x.city}`:`${x.title} ${x.type} ${x.city}`).toLowerCase().includes(query.toLowerCase())&&(city==='ALL'||x.city===city));const rate=x=>Number(isCar?x.dailyRate:x.nightlyRate);if(sort==='price-low')result=[...result].sort((a,b)=>rate(a)-rate(b));if(sort==='price-high')result=[...result].sort((a,b)=>rate(b)-rate(a));if(sort==='name')result=[...result].sort((a,b)=>(isCar?`${a.make} ${a.model}`:a.title).localeCompare(isCar?`${b.make} ${b.model}`:b.title));return result},[items,query,city,sort,isCar]);
  async function toggleFavorite(e,item){e.preventDefault();if(!user){location.href='/login';return}const kind=isCar?'CAR':'PROPERTY',found=favs.find(f=>f.assetType===kind&&f.assetId===item.id);try{if(found){await del(`/favorites/${kind}/${item.id}`);setFavs(favs.filter(f=>f.id!==found.id))}else{const saved=await post('/favorites',{assetType:kind,assetId:item.id});setFavs([saved,...favs])}}catch(err){setError(err.message)}}
  return <section className="catalogSection"><div className="catalogHeader"><div><span className="eyebrow">{type.toUpperCase()}</span><h2>{isCar?'Find the right car':'Stay somewhere memorable'}</h2><p className="muted">{isCar?'City cars, premium sedans, SUVs and EVs for every kind of trip.':'Apartments, villas and unique stays across Jordan.'}</p></div><div className="resultCount"><b>{data.length}</b><span>available listings</span></div></div>
    <div className="filterBar"><label><span>Search</span><input value={query} onChange={e=>setQuery(e.target.value)} placeholder={isCar?'Brand, model or city':'Name, type or city'}/></label><label><span>Location</span><select value={city} onChange={e=>setCity(e.target.value)}>{cities.map(c=><option key={c} value={c}>{c==='ALL'?'All locations':c}</option>)}</select></label><label><span>Sort</span><select value={sort} onChange={e=>setSort(e.target.value)}><option value="recommended">Recommended</option><option value="price-low">Price: low to high</option><option value="price-high">Price: high to low</option><option value="name">Name</option></select></label></div>
    {error&&<PageState type="error">{error}</PageState>}{loading?<PageState>Loading listings…</PageState>:<div className="listingGrid">{data.map(item=>{const kind=isCar?'CAR':'PROPERTY',saved=favs.some(f=>f.assetType===kind&&f.assetId===item.id);return <Link className="listingCard" to={`/${route}/${item.id}`} key={item.id}><div className="imageWrap"><SmartImage item={item} isCar={isCar}/><span className="availabilityBadge">● Available</span><button className={`heartButton ${saved?'saved':''}`} onClick={e=>toggleFavorite(e,item)} aria-label="Save favorite">{saved?'♥':'♡'}</button></div><div className="listingBody"><div className="listingTop"><span className="locationText">⌖ {item.city}</span><span className="rating">★ 4.{6+(item.id%4)}</span></div><h3>{isCar?`${item.make} ${item.model}`:item.title}</h3><p>{isCar?`${item.modelYear} · ${item.transmission} · ${item.seats} seats`:`${item.type} · ${item.bedrooms} beds · ${item.bathrooms} baths`}</p><div className="listingBottom"><div><strong>{money(isCar?item.dailyRate:item.nightlyRate)}</strong><small> / {isCar?'day':'night'}</small></div><span>View details →</span></div></div></Link>})}</div>}
    {!loading&&!error&&!data.length&&<PageState>No listings match your filters.</PageState>}
  </section>;
}

function Detail({kind,user}){
  const{id}=useParams(),isCar=kind==='CAR';
  const[item,setItem]=useState(null),[dates,setDates]=useState({startDate:'',endDate:''}),[msg,setMsg]=useState(''),[err,setErr]=useState(''),[reviews,setReviews]=useState([]),[review,setReview]=useState({rating:5,comment:''}),[availability,setAvailability]=useState(null),[checking,setChecking]=useState(false),[busy,setBusy]=useState(false);
  useEffect(()=>{get(isCar?`/cars/${id}`:`/properties/${id}`).then(setItem).catch(e=>setErr(e.message));get(`/reviews/${kind}/${id}`).then(setReviews).catch(()=>{})},[id,kind,isCar]);
  useEffect(()=>{if(!dates.startDate||!dates.endDate){setAvailability(null);return}setChecking(true);get(`/bookings/availability?assetType=${kind}&assetId=${id}&startDate=${dates.startDate}&endDate=${dates.endDate}`).then(setAvailability).catch(e=>{setAvailability(null);setErr(e.message)}).finally(()=>setChecking(false))},[dates.startDate,dates.endDate,id,kind]);
  if(!item)return <section className="detailSection">{err?<PageState type="error">{err}</PageState>:<PageState>Loading details…</PageState>}</section>;
  const rate=Number(isCar?item.dailyRate:item.nightlyRate),average=reviews.length?(reviews.reduce((sum,r)=>sum+r.rating,0)/reviews.length).toFixed(1):'New';
  async function book(){setErr('');setMsg('');if(!user){location.href='/login';return}if(!availability?.available){setErr('Choose available dates first.');return}setBusy(true);try{const booking=await post('/bookings',{assetType:kind,assetId:Number(id),...dates});setMsg(`Booking #${booking.id} created. Continue in My Bookings to add services or pay.`)}catch(e){setErr(e.message)}finally{setBusy(false)}}
  async function addReview(){setErr('');try{const saved=await post('/reviews',{assetType:kind,assetId:Number(id),...review});setReviews([saved,...reviews.filter(x=>x.id!==saved.id)]);setReview({rating:5,comment:''});setMsg('Your review was saved.')}catch(e){setErr(e.message)}}
  return <section className="detailSection"><Link className="backLink" to={isCar?'/cars':'/properties'}>← Back to {isCar?'cars':'stays'}</Link><div className="detailHeading"><div><span className="eyebrow">{isCar?'CAR RENTAL':'STAY'}</span><h1>{isCar?`${item.make} ${item.model}`:item.title}</h1><p>⌖ {item.city} · ★ {average} {reviews.length?`(${reviews.length} reviews)`:'(no reviews yet)'}</p></div><div className="detailPrice"><b>{money(rate)}</b><span>/ {isCar?'day':'night'}</span></div></div>
    <div className="detailLayout"><div><div className="detailVisual"><SmartImage item={item} isCar={isCar}/><span className="photoBadge">Verified listing</span></div><div className="detailDescription"><h3>{isCar?'Vehicle details':'About this stay'}</h3><p>{isCar?`${item.modelYear} ${item.make} ${item.model}, ${item.transmission.toLowerCase()} transmission and comfortable seating for ${item.seats}.`:item.description}</p><div className="detailFacts">{isCar?<><div><b>{item.modelYear}</b><span>Model year</span></div><div><b>{item.seats}</b><span>Seats</span></div><div><b>{item.transmission}</b><span>Transmission</span></div></>:<><div><b>{item.bedrooms}</b><span>Bedrooms</span></div><div><b>{item.bathrooms}</b><span>Bathrooms</span></div><div><b>{item.type}</b><span>Property type</span></div></>}</div><div className="reviewsBlock"><div className="sectionHeading"><h3>Guest reviews</h3><span>{average==='New'?'Be the first to review':`${average} / 5`}</span></div>{reviews.length?reviews.map(r=><div className="reviewRow" key={r.id}><b>{'★'.repeat(r.rating)}</b><span>{r.comment||'Rating only'}</span></div>):<p className="muted">No reviews yet.</p>}{user&&<div className="reviewForm"><select value={review.rating} onChange={e=>setReview({...review,rating:Number(e.target.value)})}>{[5,4,3,2,1].map(n=><option key={n} value={n}>{n} stars</option>)}</select><input maxLength="1000" placeholder="Review after a paid booking" value={review.comment} onChange={e=>setReview({...review,comment:e.target.value})}/><button className="secondary" onClick={addReview}>Submit</button></div>}</div></div></div>
      <aside className="bookingCard"><div className="bookingCardHead"><div><strong>{money(rate)}</strong><span>/ {isCar?'day':'night'}</span></div><span className="statusDot">● Available</span></div><div className="dateGrid"><label>Start<input min={today()} type="date" value={dates.startDate} onChange={e=>setDates({...dates,startDate:e.target.value})}/></label><label>End<input min={dates.startDate||today()} type="date" value={dates.endDate} onChange={e=>setDates({...dates,endDate:e.target.value})}/></label></div>{checking&&<div className="availabilityInfo">Checking availability…</div>}{availability&&<div className={availability.available?'availabilityInfo good':'availabilityInfo bad'}><b>{availability.available?'Dates available':'Not available for these dates'}</b>{availability.available&&<><span>{availability.days} {availability.days===1?'day':'days'}</span><span>Estimated total <strong>{money(availability.estimatedTotal)}</strong></span></>}</div>}<button className="primary full" disabled={busy||!availability?.available} onClick={book}>{busy?'Reserving…':'Reserve now'}</button><p className="smallNote">No payment is taken until you review your booking and optional services.</p>{msg&&<div className="successBox">{msg}</div>}{err&&<div className="errorBox">{err}</div>}</aside></div>
  </section>;
}

function Services(){
  const[items,setItems]=useState([]),[error,setError]=useState('');
  useEffect(()=>{get('/services').then(setItems).catch(e=>setError(e.message))},[]);
  const icons={TRANSPORT:'🚙',CLEANING:'✨',SUPPORT:'🛟',CONNECTIVITY:'📶',CAR_ADDON:'🧸'};
  return <section className="catalogSection"><div className="catalogHeader"><div><span className="eyebrow">EXTRA SERVICES</span><h2>Customize your trip</h2><p className="muted">Add these extras to a pending reservation from My Bookings before payment.</p></div></div>{error&&<PageState type="error">{error}</PageState>}<div className="serviceGrid">{items.map(item=><article className="serviceCard" key={item.id}><div className="serviceIcon">{icons[item.category]||'✦'}</div><span className="pill">{item.category.replaceAll('_',' ')}</span><h3>{item.name}</h3><p>{item.description}</p><div className="serviceFooter"><strong>{money(item.price)}</strong><Link className="secondary" to="/bookings">Add to booking</Link></div></article>)}</div></section>;
}

function BookingCard({booking,services,onChanged}){
  const[asset,setAsset]=useState(null),[addons,setAddons]=useState([]),[payment,setPayment]=useState(null),[error,setError]=useState(''),[busy,setBusy]=useState(false);
  const isCar=booking.assetType==='CAR';
  async function loadDetails(){
    get(`/${isCar?'cars':'properties'}/${booking.assetId}`).then(setAsset).catch(()=>setAsset(null));
    get(`/booking-services/${booking.id}`).then(setAddons).catch(()=>setAddons([]));
    if(booking.status==='PAID')get(`/payments/booking/${booking.id}`).then(setPayment).catch(()=>{});else setPayment(null);
  }
  useEffect(()=>{loadDetails()},[booking.id,booking.status]);
  const extrasTotal=addons.reduce((sum,x)=>sum+Number(x.price),0),grandTotal=Number(booking.totalAmount)+extrasTotal;
  async function addService(serviceId){if(!serviceId)return;setError('');try{await post(`/booking-services/${booking.id}/${serviceId}`);await loadDetails()}catch(e){setError(e.message)}}
  async function removeService(serviceId){setError('');try{await del(`/booking-services/${booking.id}/${serviceId}`);await loadDetails()}catch(e){setError(e.message)}}
  async function pay(){setBusy(true);setError('');try{const result=await post(`/payments/booking/${booking.id}`,{method:'CARD'});setPayment(result);await onChanged()}catch(e){setError(e.message)}finally{setBusy(false)}}
  async function cancel(){setBusy(true);setError('');try{await patch(`/bookings/${booking.id}/cancel`);await onChanged()}catch(e){setError(e.message)}finally{setBusy(false)}}
  const serviceName=id=>services.find(s=>Number(s.id)===Number(id))?.name||`Service #${id}`;
  return <article className="bookingCardRow"><div className="bookingMedia">{asset?<SmartImage item={asset} isCar={isCar}/>:<div className="imagePlaceholder"/>}</div><div className="bookingMain"><div className="bookingTitle"><div><span className="pill">{booking.assetType}</span><h3>{asset?(isCar?`${asset.make} ${asset.model}`:asset.title):`Booking #${booking.id}`}</h3><p>{booking.startDate} → {booking.endDate}</p></div><span className={`bookingStatus ${booking.status.toLowerCase()}`}>{booking.status}</span></div><div className="bookingExtras"><h4>Trip extras</h4>{addons.length?addons.map(addon=><div className="addonLine" key={addon.id}><span>{serviceName(addon.serviceId)}</span><div><b>{money(addon.price)}</b>{booking.status==='PENDING'&&<button className="linkButton" onClick={()=>removeService(addon.serviceId)}>Remove</button>}</div></div>):<p className="muted">No extras added.</p>}{booking.status==='PENDING'&&<select onChange={e=>{addService(e.target.value);e.target.value=''}} defaultValue=""><option value="">+ Add a service</option>{services.filter(s=>!addons.some(a=>Number(a.serviceId)===Number(s.id))).map(s=><option key={s.id} value={s.id}>{s.name} ({money(s.price)})</option>)}</select>}</div>{error&&<div className="errorBox">{error}</div>}</div><aside className="bookingSummary"><div><span>Booking</span><b>{money(booking.totalAmount)}</b></div><div><span>Extras</span><b>{money(extrasTotal)}</b></div><div className="grandTotal"><span>Total</span><b>{money(payment?.amount??grandTotal)}</b></div>{payment&&<div className="receiptMini"><span>Paid · {payment.method}</span><code>{payment.referenceNo}</code></div>}{booking.status==='PENDING'&&<button disabled={busy} className="primary full" onClick={pay}>{busy?'Processing…':'Pay now'}</button>}{booking.status==='PENDING'&&<button disabled={busy} className="secondary full" onClick={cancel}>Cancel booking</button>}{booking.status==='PAID'&&asset&&<Link className="secondary full centerButton" to={`/${isCar?'cars':'properties'}/${booking.assetId}`}>Leave a review</Link>}</aside></article>;
}

function Bookings({user}){
  const[items,setItems]=useState([]),[services,setServices]=useState([]),[error,setError]=useState(''),[filter,setFilter]=useState('ALL');
  const load=()=>user?get('/bookings/mine').then(setItems).catch(e=>setError(e.message)):Promise.resolve();
  useEffect(()=>{load();get('/services').then(setServices).catch(()=>{})},[user]);
  if(!user)return <section className="catalogSection"><PageState>Sign in to see your bookings.</PageState></section>;
  const visible=filter==='ALL'?items:items.filter(x=>x.status===filter);
  return <section className="catalogSection"><div className="catalogHeader"><div><span className="eyebrow">MY TRIPS</span><h2>Bookings</h2><p className="muted">Add services, pay, keep receipts and return to leave a review.</p></div><select className="compactSelect" value={filter} onChange={e=>setFilter(e.target.value)}><option>ALL</option><option>PENDING</option><option>PAID</option><option>CANCELLED</option></select></div>{error&&<PageState type="error">{error}</PageState>}<div className="bookingList">{visible.map(b=><BookingCard key={`${b.id}-${b.status}`} booking={b} services={services} onChanged={load}/>)}</div>{!visible.length&&<PageState>No bookings in this view yet.</PageState>}</section>;
}

function Favorites({user}){
  const[items,setItems]=useState([]),[loading,setLoading]=useState(true);
  useEffect(()=>{if(!user){setLoading(false);return}get('/favorites').then(async favs=>{const detailed=await Promise.all(favs.map(async f=>{try{const item=await get(`/${f.assetType==='CAR'?'cars':'properties'}/${f.assetId}`);return{...f,item}}catch{return null}}));setItems(detailed.filter(Boolean))}).finally(()=>setLoading(false))},[user]);
  if(!user)return <section className="catalogSection"><PageState>Sign in to see favorites.</PageState></section>;
  async function remove(fav){await del(`/favorites/${fav.assetType}/${fav.assetId}`);setItems(items.filter(x=>x.id!==fav.id))}
  return <section className="catalogSection"><span className="eyebrow">SAVED</span><h2>Your favorites</h2>{loading?<PageState>Loading favorites…</PageState>:items.length?<div className="listingGrid">{items.map(f=>{const isCar=f.assetType==='CAR',x=f.item;return <article className="listingCard" key={f.id}><Link to={`/${isCar?'cars':'properties'}/${x.id}`}><div className="imageWrap"><SmartImage item={x} isCar={isCar}/></div><div className="listingBody"><span className="locationText">⌖ {x.city}</span><h3>{isCar?`${x.make} ${x.model}`:x.title}</h3><div className="listingBottom"><strong>{money(isCar?x.dailyRate:x.nightlyRate)}</strong><span>Open →</span></div></div></Link><button className="removeFavorite" onClick={()=>remove(f)}>Remove from favorites</button></article>})}</div>:<PageState>No favorites yet. Tap the heart on a listing to save it.</PageState>}</section>;
}

function Account({user,onUser}){
  const[profile,setProfile]=useState({fullName:user?.fullName||'',email:user?.email||''}),[password,setPassword]=useState({currentPassword:'',newPassword:''}),[message,setMessage]=useState(''),[error,setError]=useState('');
  useEffect(()=>setProfile({fullName:user?.fullName||'',email:user?.email||''}),[user]);
  if(!user)return <section className="catalogSection"><PageState>Sign in to manage your account.</PageState></section>;
  async function saveProfile(e){e.preventDefault();setError('');setMessage('');try{const updated=await patch('/auth/me',profile);onUser(updated);setMessage('Profile updated.')}catch(err){setError(err.message)}}
  async function savePassword(e){e.preventDefault();setError('');setMessage('');try{await patch('/auth/password',password);setPassword({currentPassword:'',newPassword:''});setMessage('Password changed.')}catch(err){setError(err.message)}}
  return <section className="catalogSection"><span className="eyebrow">ACCOUNT</span><h2>Profile & security</h2><div className="accountGrid"><form className="panelCard" onSubmit={saveProfile}><h3>Personal information</h3><label>Full name<input required value={profile.fullName} onChange={e=>setProfile({...profile,fullName:e.target.value})}/></label><label>Email<input required type="email" value={profile.email} onChange={e=>setProfile({...profile,email:e.target.value})}/></label><div className="roleLine"><span>Role</span><b>{user.role}</b></div><button className="primary">Save profile</button></form><form className="panelCard" onSubmit={savePassword}><h3>Change password</h3><label>Current password<input required type="password" value={password.currentPassword} onChange={e=>setPassword({...password,currentPassword:e.target.value})}/></label><label>New password<input required minLength="6" type="password" value={password.newPassword} onChange={e=>setPassword({...password,newPassword:e.target.value})}/></label><button className="secondary">Update password</button></form></div>{message&&<div className="successBox">{message}</div>}{error&&<div className="errorBox">{error}</div>}</section>;
}

const emptyCar={make:'',model:'',modelYear:new Date().getFullYear(),transmission:'Automatic',seats:5,dailyRate:'',city:'Amman',imageUrl:'',status:'AVAILABLE'};
const emptyProperty={title:'',type:'Apartment',city:'Amman',bedrooms:1,bathrooms:1,nightlyRate:'',description:'',imageUrl:'',status:'AVAILABLE'};
const emptyService={name:'',category:'SUPPORT',price:'',description:'',imageUrl:'',status:'ACTIVE'};

function Management({user}){
  const[cars,setCars]=useState([]),[properties,setProperties]=useState([]),[services,setServices]=useState([]),[carForm,setCarForm]=useState(emptyCar),[propertyForm,setPropertyForm]=useState(emptyProperty),[serviceForm,setServiceForm]=useState(emptyService),[error,setError]=useState(''),[message,setMessage]=useState('');
  const isAdmin=user.role==='ADMIN';
  const load=()=>Promise.all([get('/cars/mine').then(setCars),get('/properties/mine').then(setProperties),isAdmin?get('/services/all').then(setServices):Promise.resolve()]).catch(e=>setError(e.message));
  useEffect(()=>{load()},[user.role]);
  async function createAsset(kind){setError('');setMessage('');try{if(kind==='car'){await post('/cars',{...carForm,modelYear:Number(carForm.modelYear),seats:Number(carForm.seats),dailyRate:Number(carForm.dailyRate)});setCarForm(emptyCar)}else{await post('/properties',{...propertyForm,bedrooms:Number(propertyForm.bedrooms),bathrooms:Number(propertyForm.bathrooms),nightlyRate:Number(propertyForm.nightlyRate)});setPropertyForm(emptyProperty)}await load();setMessage(`${kind==='car'?'Car':'Property'} added.`)}catch(e){setError(e.message)}}
  async function toggle(kind,item){try{const status=item.status==='AVAILABLE'?'UNAVAILABLE':'AVAILABLE';await patch(`/${kind==='car'?'cars':'properties'}/${item.id}`,{...item,status});await load()}catch(e){setError(e.message)}}
  async function archive(kind,id){try{await del(`/${kind==='car'?'cars':'properties'}/${id}`);await load()}catch(e){setError(e.message)}}
  async function createService(){try{await post('/services',{...serviceForm,price:Number(serviceForm.price)});setServiceForm(emptyService);await load();setMessage('Service added.')}catch(e){setError(e.message)}}
  async function archiveService(id){try{await del(`/services/${id}`);await load()}catch(e){setError(e.message)}}
  return <section className="catalogSection"><span className="eyebrow">MANAGEMENT</span><h2>{isAdmin?'Platform inventory':'Your listings'}</h2><p className="muted">Create listings, control availability and archive items without deleting business history.</p>{message&&<div className="successBox">{message}</div>}{error&&<div className="errorBox">{error}</div>}<div className="managementGrid"><div className="panelCard"><h3>Add a car</h3><div className="formGrid">{[['make','Make'],['model','Model'],['modelYear','Year'],['transmission','Transmission'],['seats','Seats'],['dailyRate','Daily rate'],['city','City'],['imageUrl','Image URL']].map(([key,label])=><label key={key}>{label}<input value={carForm[key]} onChange={e=>setCarForm({...carForm,[key]:e.target.value})}/></label>)}</div><button className="primary" onClick={()=>createAsset('car')}>Add car</button></div><div className="panelCard"><h3>Add a property</h3><div className="formGrid">{[['title','Title'],['type','Type'],['city','City'],['bedrooms','Bedrooms'],['bathrooms','Bathrooms'],['nightlyRate','Nightly rate'],['imageUrl','Image URL']].map(([key,label])=><label key={key}>{label}<input value={propertyForm[key]} onChange={e=>setPropertyForm({...propertyForm,[key]:e.target.value})}/></label>)}</div><label>Description<textarea value={propertyForm.description} onChange={e=>setPropertyForm({...propertyForm,description:e.target.value})}/></label><button className="primary" onClick={()=>createAsset('property')}>Add property</button></div></div><div className="manageList"><h3>Cars</h3>{cars.map(x=><div className="manageRow" key={x.id}><span><b>{x.make} {x.model}</b><small>{x.city} · {money(x.dailyRate)}/day</small></span><div><span className="pill">{x.status}</span><button className="secondary" onClick={()=>toggle('car',x)}>{x.status==='AVAILABLE'?'Pause':'Activate'}</button><button className="dangerButton" onClick={()=>archive('car',x.id)}>Archive</button></div></div>)}</div><div className="manageList"><h3>Properties</h3>{properties.map(x=><div className="manageRow" key={x.id}><span><b>{x.title}</b><small>{x.city} · {money(x.nightlyRate)}/night</small></span><div><span className="pill">{x.status}</span><button className="secondary" onClick={()=>toggle('property',x)}>{x.status==='AVAILABLE'?'Pause':'Activate'}</button><button className="dangerButton" onClick={()=>archive('property',x.id)}>Archive</button></div></div>)}</div>{isAdmin&&<><div className="panelCard adminServiceForm"><h3>Add platform service</h3><div className="formGrid"><label>Name<input value={serviceForm.name} onChange={e=>setServiceForm({...serviceForm,name:e.target.value})}/></label><label>Category<input value={serviceForm.category} onChange={e=>setServiceForm({...serviceForm,category:e.target.value})}/></label><label>Price<input value={serviceForm.price} onChange={e=>setServiceForm({...serviceForm,price:e.target.value})}/></label><label>Description<input value={serviceForm.description} onChange={e=>setServiceForm({...serviceForm,description:e.target.value})}/></label></div><button className="primary" onClick={createService}>Add service</button></div><div className="manageList"><h3>Services</h3>{services.map(x=><div className="manageRow" key={x.id}><span><b>{x.name}</b><small>{x.category} · {money(x.price)}</small></span><div><span className="pill">{x.status}</span>{x.status==='ACTIVE'&&<button className="dangerButton" onClick={()=>archiveService(x.id)}>Archive</button>}</div></div>)}</div></>}</section>;
}

function Dashboard({user}){
  const[data,setData]=useState(null),[error,setError]=useState('');
  useEffect(()=>{if(user&&['ADMIN','OWNER'].includes(user.role))get(`/dashboard/${user.role.toLowerCase()}`).then(setData).catch(e=>setError(e.message))},[user]);
  if(!user||!['ADMIN','OWNER'].includes(user.role))return <section className="catalogSection"><PageState>Dashboard is available to owner/admin accounts.</PageState></section>;
  return <section className="catalogSection"><span className="eyebrow">{user.role} DASHBOARD</span><h2>Business overview</h2><p className="muted">Live metrics from your local RentHub database.</p>{error&&<PageState type="error">{error}</PageState>}<div className="metricGrid">{data&&Object.entries(data).map(([key,value])=><div className="metricCard" key={key}><span>{prettyKey(key)}</span><b>{key.toLowerCase().includes('revenue')?money(value):String(value)}</b></div>)}</div><div className="dashboardCta"><div><h3>Manage inventory</h3><p>Create, pause or archive listings and services from one place.</p></div><Link className="primary" to="/management">Open management</Link></div></section>;
}

function Header({user,onLogout}){
  const loc=useLocation(),[open,setOpen]=useState(false);const active=path=>loc.pathname.startsWith(path)?'activeNav':'';
  useEffect(()=>setOpen(false),[loc.pathname]);
  return <header><Link className="logo" to="/">Rent<span>Hub</span></Link><button className="menuButton" onClick={()=>setOpen(!open)}>☰</button><nav className={open?'open':''}><Link className={active('/cars')} to="/cars">Cars</Link><Link className={active('/properties')} to="/properties">Stays</Link><Link className={active('/services')} to="/services">Services</Link>{user&&<><Link className={active('/bookings')} to="/bookings">Bookings</Link><Link className={active('/favorites')} to="/favorites">Favorites</Link>{['ADMIN','OWNER'].includes(user.role)&&<Link className={active('/dashboard')} to="/dashboard">Dashboard</Link>}</>}</nav><div className="headerActions">{user?<><Link className="accountLink" to="/account"><span className="avatar">{user.fullName?.[0]?.toUpperCase()}</span><span>{user.fullName}</span></Link><button className="login" onClick={onLogout}>Sign out</button></>:<Link className="login" to="/login">Sign in</Link>}</div></header>;
}

function Footer(){return <footer><div className="footerBrand"><Link className="logo" to="/">Rent<span>Hub</span></Link><p>Local-first rental marketplace built for realistic business-flow training.</p></div><div><b>Explore</b><Link to="/cars">Cars</Link><Link to="/properties">Stays</Link><Link to="/services">Services</Link></div><div><b>Account</b><Link to="/bookings">Bookings</Link><Link to="/favorites">Favorites</Link><span>Jordan · USD</span></div></footer>}

export default function App(){
  const[user,setUser]=useState(null),[authReady,setAuthReady]=useState(false);
  useEffect(()=>{if(session.token)get('/auth/me').then(setUser).catch(()=>session.set('')).finally(()=>setAuthReady(true));else setAuthReady(true)},[]);
  async function logout(){try{await post('/auth/logout')}catch{}session.set('');setUser(null)}
  if(!authReady)return <div className="appLoading">Loading RentHub…</div>;
  return <><Header user={user} onLogout={logout}/><main><Routes><Route path="/" element={<Home/>}/><Route path="/login" element={<AuthPage mode="login" onAuth={setUser}/>}/><Route path="/register" element={<AuthPage mode="register" onAuth={setUser}/>}/><Route path="/cars" element={<Catalog type="Cars" user={user}/>}/><Route path="/properties" element={<Catalog type="Properties" user={user}/>}/><Route path="/cars/:id" element={<Detail kind="CAR" user={user}/>}/><Route path="/properties/:id" element={<Detail kind="PROPERTY" user={user}/>}/><Route path="/services" element={<Services/>}/><Route path="/bookings" element={<Bookings user={user}/>}/><Route path="/favorites" element={<Favorites user={user}/>}/><Route path="/account" element={<Account user={user} onUser={setUser}/>}/><Route path="/dashboard" element={<Dashboard user={user}/>}/><Route path="/management" element={<Management user={user}/>}/><Route path="*" element={<section className="catalogSection"><PageState>Page not found. <Link to="/">Go home</Link></PageState></section>}/></Routes></main><Footer/></>;
}
