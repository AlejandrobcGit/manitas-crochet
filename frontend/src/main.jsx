
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import "./Index.css"
import App from './App.jsx'

import { FigurasProvider } from './contexts/FigurasContext.jsx'

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <FigurasProvider>
      <App />
    </FigurasProvider>
  </BrowserRouter>
)
