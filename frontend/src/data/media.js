const commons=(name)=>`https://commons.wikimedia.org/wiki/Special:FilePath/${encodeURIComponent(name)}?width=1600`;

const carMedia={
  'Toyota Camry':{local:'/assets/real/car-camry.jpg',remote:commons('Toyota Camry XSE AWD (2026) (55213727536).jpg')},
  'BMW X5':{local:'/assets/real/car-bmw-x5.jpg',remote:commons('BMW G05 IMG 0919.jpg')},
  'Mercedes-Benz C200':{local:'/assets/real/car-mercedes-c200.jpg',remote:commons('Mercedes-Benz C 200 (W206, 2025) (54740572519).jpg')},
  'Kia Sportage':{local:'/assets/real/car-kia-sportage.jpg',remote:commons('Kia Sportage (NQ5) 1758033820001.jpg')},
  'Hyundai Tucson':{local:'/assets/real/car-hyundai-tucson.jpg',remote:commons('Hyundai Tucson (NX4).png')},
  'Tesla Model 3':{local:'/assets/real/car-tesla-model3.jpg',remote:commons('Tesla Model 3 (Facelift) – f 30082026.jpg')},
  'Nissan Sunny':{local:'/assets/real/car-nissan-sunny.jpg',remote:commons('Nissan Sunny in Jordan.jpg')},
  'Toyota Land Cruiser':{local:'/assets/real/car-land-cruiser.jpg',remote:commons('Toyota Land Cruiser J300 3.3 ZX 2024.jpg')}
};

const propertyMedia={
  'Modern City Apartment':{local:'/assets/real/property-modern-apartment.jpg',remote:commons('Apartment Modern.jpg')},
  'Mountain View Villa':{local:'/assets/real/property-villa.jpg',remote:commons('Moderne Villa.jpg')},
  'Dead Sea Resort Apartment':{local:'/assets/real/property-resort.jpg',remote:commons('Modern DC Apartment Complex.jpg')},
  'Aqaba Beach Studio':{local:'/assets/real/property-studio.jpg',remote:commons('Lima Peru city - Modern Apartment - interior.jpg')},
  'Amman Skyline Penthouse':{local:'/assets/real/property-penthouse.jpg',remote:commons('Interior of apartment hotel in Kotka.jpg')},
  'Ajloun Forest Chalet':{local:'/assets/real/property-chalet.jpg',remote:commons('Cozy wooden cabin interior with seating area and bedroom space.jpg')},
  'Petra Heritage House':{local:'/assets/real/property-house.jpg',remote:commons('Villa moderniste.jpg')},
  'Irbid Family Apartment':{local:'/assets/real/property-family.jpg',remote:commons('Modern DC Apartment Complex.jpg')}
};

export function mediaFor(item,isCar){
  const key=isCar?`${item.make} ${item.model}`:item.title;
  return (isCar?carMedia:propertyMedia)[key]||{local:item.imageUrl,remote:item.imageUrl};
}
