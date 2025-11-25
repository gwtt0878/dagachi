import '../styles/common.css'

interface InputProps {
  type?: string
  name: string
  value: string
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void
  label: string
  required?: boolean
  minLength?: number
  maxLength?: number
  helpText?: string
  placeholder?: string
}

function Input({
  type = 'text',
  name,
  value,
  onChange,
  label,
  required = false,
  minLength,
  maxLength,
  helpText,
  placeholder
}: InputProps) {
  return (
    <div className="form-group">
      <label className="form-label" htmlFor={name}>
        {label}
      </label>
      <input
        id={name}
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        required={required}
        minLength={minLength}
        maxLength={maxLength}
        placeholder={placeholder}
        className="form-input"
      />
      {helpText && <small className="help-text">{helpText}</small>}
    </div>
  )
}

export default Input

