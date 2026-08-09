import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import './Index.css'
import { CategoriasProvider } from './contexts/CategoriasContext'
import { ColoresProvider } from './contexts/ColoresContext'
import { FigurasProvider } from './contexts/FigurasContext'
import { UserProvider } from './contexts/UserContext'
import { FavoritosProvider } from './contexts/FavoritosContext'

import App from './App'


createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <UserProvider>
      <CategoriasProvider>
        <ColoresProvider>
        <FavoritosProvider>
          <FigurasProvider>
            <App />
          </FigurasProvider>
          </FavoritosProvider>
        </ColoresProvider>
      </CategoriasProvider>
    </UserProvider>
  </BrowserRouter>
)