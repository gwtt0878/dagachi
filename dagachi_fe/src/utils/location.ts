export interface UserLocation {
  latitude: number
  longitude: number
}

export interface LocationError {
  code: number
  message: string
}

// 사용자의 현재 위치를 가져오는 함수
export const getCurrentLocation = (): Promise<UserLocation> => {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject({
        code: 0,
        message: '이 브라우저에서는 위치 정보를 지원하지 않습니다.'
      })
      return
    }

    const options = {
      enableHighAccuracy: true,
      timeout: 10000,
      maximumAge: 300000 // 5분간 캐시된 위치 정보 사용
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        })
      },
      (error) => {
        let message = '위치 정보를 가져올 수 없습니다.'
        
        switch (error.code) {
          case error.PERMISSION_DENIED:
            message = '위치 정보 접근이 거부되었습니다. 브라우저 설정에서 위치 정보 접근을 허용해주세요.'
            break
          case error.POSITION_UNAVAILABLE:
            message = '위치 정보를 사용할 수 없습니다.'
            break
          case error.TIMEOUT:
            message = '위치 정보 요청 시간이 초과되었습니다.'
            break
        }
        
        reject({
          code: error.code,
          message
        })
      },
      options
    )
  })
}

// 두 지점 간의 거리를 계산하는 함수 (Haversine 공식)
export const calculateDistance = (
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number => {
  const R = 6371 // 지구의 반지름 (km)
  const dLat = toRadians(lat2 - lat1)
  const dLon = toRadians(lon2 - lon1)
  
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2)
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  const distance = R * c
  
  return Math.round(distance * 100) / 100 // 소수점 둘째 자리까지
}

const toRadians = (degrees: number): number => {
  return degrees * (Math.PI / 180)
}

// 거리를 사용자 친화적인 문자열로 변환
export const formatDistance = (distance: number): string => {
  if (distance < 1) {
    return `${Math.round(distance * 1000)}m`
  } else {
    return `${distance}km`
  }
}