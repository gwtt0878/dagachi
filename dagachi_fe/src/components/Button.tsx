import '../styles/common.css'

interface ButtonProps {
  type?: 'button' | 'submit' | 'reset'
  variant?: 'primary' | 'secondary' | 'logout' | 'small'
  onClick?: () => void
  disabled?: boolean
  children: React.ReactNode
  style?: React.CSSProperties
}

function Button({ 
  type = 'button', 
  variant = 'primary', 
  onClick, 
  disabled, 
  children,
  style 
}: ButtonProps) {
  const getClassName = () => {
    const baseClass = 'btn'
    const variantClass = variant ? `btn-${variant}` : ''
    return `${baseClass} ${variantClass}`.trim()
  }

  return (
    <button
      type={type}
      className={getClassName()}
      onClick={onClick}
      disabled={disabled}
      style={style}
    >
      {children}
    </button>
  )
}

export default Button

