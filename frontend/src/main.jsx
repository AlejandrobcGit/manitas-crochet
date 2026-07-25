import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import './Index.css'
import { FigurasProvider } from './contexts/FigurasContext'
import { CategoriasProvider } from './contexts/CategoriasContext'

import App from './App'

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <CategoriasProvider>
      <FigurasProvider>
        <App />
      </FigurasProvider>
    </CategoriasProvider>
  </BrowserRouter>
)