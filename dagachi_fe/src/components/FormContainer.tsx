import '../styles/common.css'

interface FormContainerProps {
  title: string
  children: React.ReactNode
}

function FormContainer({ title, children }: FormContainerProps) {
  return (
    <div className="form-container">
      <h1>{title}</h1>
      {children}
    </div>
  )
}

export default FormContainer

