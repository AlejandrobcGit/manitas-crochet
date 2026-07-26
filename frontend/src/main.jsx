import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import './Index.css'
import { FigurasProvider } from './contexts/FigurasContext'
import { CategoriasProvider } from './contexts/CategoriasContext'
import { ColoresProvider } from './contexts/ColoresContext'

import App from './App'

createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <CategoriasProvider>
      <ColoresProvider>
        <FigurasProvider>
          <App />
        </FigurasProvider>
      </ColoresProvider>
    </CategoriasProvider>
  </BrowserRouter>
)