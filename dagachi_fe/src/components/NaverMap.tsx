import { useEffect, useRef, useState } from 'react'
import { getCurrentLocation } from '../utils/location'

declare global {
  interface Window {
    naver: any
  }
}

interface MapProps {
  setPickedLocation?: (lat: number, lng: number) => void
  withInteraction?: boolean
  latitude?: number
  longitude?: number
}

function NaverMap(Props: MapProps) {
  const clientId = import.meta.env.VITE_NCP_CLIENT_ID;

  const mapRef = useRef<HTMLDivElement | null>(null)
  const [centerLat, setCenterLat] = useState<number | null>(null)
  const [centerLng, setCenterLng] = useState<number | null>(null)

  useEffect(() => {
    getCurrentLocation().then((location) => {
      setCenterLat(location.latitude);
      setCenterLng(location.longitude);
    }).catch((error) => {
      console.error(error);
      setCenterLat(37.3595704);
      setCenterLng(127.105399);
    });

    if (!clientId) {
      console.error('NCP Client ID is not set');
      return;
    }

    if (window.naver) return;

    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${clientId}`;
    script.async = true;
    document.head.appendChild(script);
  }, []);

  useEffect(() => {
    if (centerLat === null || centerLng === null) return;
    const navermaps = window.naver;
    const map = new navermaps.maps.Map('map', {
      center: new navermaps.maps.LatLng(centerLat, centerLng),
      zoom: 15,
    })

    map.setOptions({
      minZoom: 9,
      maxZoom: 18,
      zoomControl: true,
    });

    const pickedPoint = new navermaps.maps.Marker({
      position: new navermaps.maps.LatLng(centerLat, centerLng),
      map: map,
    });

    if (Props.withInteraction) {
      map.setOptions({
        draggable: true,
        pinchZoom: true,
        scrollWheel: true,
        keyboardShortcuts: true,
        disableDoubleTapZoom: false,
        disableDoubleClickZoom: false,
        disableTwoFingerTapZoom: false
      });
      map.addListener('click', (event: any) => {
        const naverCoord = event.coord;
        if (Props.setPickedLocation) 
          Props.setPickedLocation(naverCoord.y, naverCoord.x);
  
        pickedPoint.setMap(map);
        pickedPoint.setPosition(event.coord);
      })
      if (Props.latitude && Props.longitude) {
        map.setCenter(new navermaps.maps.LatLng(Props.latitude, Props.longitude));
        pickedPoint.setPosition(new navermaps.maps.LatLng(Props.latitude, Props.longitude));
      }
    } else {
      map.setOptions({
        zoomControl: false,
        draggable: false,
        pinchZoom: false,
        scrollWheel: false,
        keyboardShortcuts: false,
        disableDoubleTapZoom: true,
        disableDoubleClickZoom: true,
        disableTwoFingerTapZoom: true
      });
      map.setCenter(new navermaps.maps.LatLng(Props.latitude, Props.longitude));
      pickedPoint.setPosition(new navermaps.maps.LatLng(Props.latitude, Props.longitude));

    }

  }, [centerLat, centerLng, Props.latitude, Props.longitude, Props.withInteraction])
  return (
    <div ref={mapRef} id="map" style={{ width: '100%', height: '400px' }}></div>
  )
}

export default NaverMap
