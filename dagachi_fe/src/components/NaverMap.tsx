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

// 기본 위치: 판교역
const DEFAULT_LAT = 37.3947261
const DEFAULT_LNG = 127.1112090

function NaverMap(Props: MapProps) {
  const clientId = import.meta.env.VITE_NCP_CLIENT_ID;

  const mapRef = useRef<HTMLDivElement | null>(null)
  const mapInstanceRef = useRef<any>(null)
  const markerRef = useRef<any>(null)
  const [centerLat, setCenterLat] = useState<number>(DEFAULT_LAT)
  const [centerLng, setCenterLng] = useState<number>(DEFAULT_LNG)
  const [isMapLoaded, setIsMapLoaded] = useState(false)
  const [locationStatus, setLocationStatus] = useState<'searching' | 'found' | 'error' | 'idle'>(
    Props.withInteraction ? 'searching' : 'idle'
  )

  useEffect(() => {
    if (Props.withInteraction) {
      getCurrentLocation().then((location) => {
        setCenterLat(location.latitude);
        setCenterLng(location.longitude);
        setLocationStatus('found');
        
        if (mapInstanceRef.current && markerRef.current && !Props.latitude && !Props.longitude) {
          const navermaps = window.naver;
          mapInstanceRef.current.setCenter(new navermaps.maps.LatLng(location.latitude, location.longitude));
          markerRef.current.setPosition(new navermaps.maps.LatLng(location.latitude, location.longitude));
        }
      }).catch((error) => {
        console.error(error);
        setLocationStatus('error');
      });
    }

    if (!clientId) {
      console.error('NCP Client ID is not set');
      return;
    }

    if (window.naver && window.naver.maps) {
      setTimeout(() => setIsMapLoaded(true), 0);
      return;
    }

    const script = document.createElement('script');
    script.type = 'text/javascript';
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${clientId}`;
    script.async = true;
    script.onload = () => {
      setIsMapLoaded(true)
    }
    document.head.appendChild(script);
  }, []);

  useEffect(() => {
    if (!isMapLoaded || !window.naver) return;
    
    const navermaps = window.naver;
    
    // 초기 센터 위치 결정
    const initialLat = Props.latitude ?? centerLat;
    const initialLng = Props.longitude ?? centerLng;
    
    const map = new navermaps.maps.Map('map', {
      center: new navermaps.maps.LatLng(initialLat, initialLng),
      zoom: 15,
    })
    
    mapInstanceRef.current = map;

    map.setOptions({
      minZoom: 9,
      maxZoom: 18,
      zoomControl: true,
    });

    const pickedPoint = new navermaps.maps.Marker({
      position: new navermaps.maps.LatLng(initialLat, initialLng),
      map: map,
    });
    
    markerRef.current = pickedPoint;

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
      if (Props.latitude && Props.longitude) {
        map.setCenter(new navermaps.maps.LatLng(Props.latitude, Props.longitude));
        pickedPoint.setPosition(new navermaps.maps.LatLng(Props.latitude, Props.longitude));
      }
    }

  }, [isMapLoaded, Props.latitude, Props.longitude, Props.withInteraction])

  const getStatusMessage = () => {
    switch (locationStatus) {
      case 'searching':
        return '📍 위치 정보를 가져오는 중...'
      case 'found':
        return '✅ 위치 정보를 찾았습니다!'
      case 'error':
        return '⚠️ 위치 정보를 가져올 수 없습니다. 기본 위치(판교역)를 사용합니다.'
      default:
        return null
    }
  }

  return (
    <div>
      <div ref={mapRef} id="map" style={{ width: '100%', height: '400px' }}></div>
      {Props.withInteraction && locationStatus !== 'idle' && (
        <div style={{
          padding: '8px 12px',
          marginTop: '8px',
          borderRadius: '6px',
          fontSize: '14px',
          backgroundColor: locationStatus === 'found' ? '#e8f5e9' : 
                          locationStatus === 'error' ? '#fff3e0' : '#e3f2fd',
          color: locationStatus === 'found' ? '#2e7d32' : 
                 locationStatus === 'error' ? '#e65100' : '#1565c0',
        }}>
          {getStatusMessage()}
        </div>
      )}
    </div>
  )
}

export default NaverMap
