export const getTypeLabel = (type: string) => {
    switch (type) {
      case 'PROJECT':
        return '프로젝트'
      case 'STUDY':
        return '스터디'
      default:
        return type
    }
  }

export const getStatusLabel = (status: string) => {
    switch (status) {
      case 'RECRUITING':
        return '모집중'
      case 'RECRUITED':
        return '모집완료'
      case 'COMPLETED':
        return '종료'
      default:
        return status
    }
  }

export const getStatusClass = (status: string) => {
    switch (status) {
      case 'RECRUITING':
        return 'status-recruiting'
      case 'RECRUITED':
        return 'status-recruited'
      case 'COMPLETED':
        return 'status-completed'
      default:
        return ''
    }
  }