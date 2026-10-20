import { useState } from 'react'
import './App.css'

function App() {
  const [count, setCount] = useState(0)

  return (
    <>
      <div className="ticks"></div>
      <section id="spacer"></section>
    </>
  )
}

export default App
