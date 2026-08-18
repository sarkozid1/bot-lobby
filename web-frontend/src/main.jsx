import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import { AuthProvider } from './context/AuthContext.jsx';
import { ToastProvider } from './context/ToastContext.jsx';
import './styles/index.css';

// A React alkalmazas belepesi pontja - ez a fajl "kapcsolja be" Reactot az index.html
// <div id="root"></div> elemebe (lasd az index.html <script type="module" src="/src/main.jsx">
// sorat, ami ezt a fajlt tolti be).
//
// Nincs React.StrictMode: a dev-modbeli dupla mount/unmount ciklusa osszeakad a Framer Motion
// AnimatePresence kilepes-kovetesevel (ismert kompatibilitasi surlodas) - bezarodo sheet/modal
// hatterek DOM-ban ragadtak, lathatatlanul leblokkolva az egesz oldalt kattintasra.
ReactDOM.createRoot(document.getElementById('root')).render(
  // A Context Provider-ek "egymasba agyazasa" - a BELUL levo Provider-ek/komponensek elerik
  // a KIVUL levo Context-eket, de forditva nem. A sorrend itt szandekos: a Router legkivul
  // kell legyen (mindenkinek szuksege lehet URL-navigaciora), a ToastProvider/AuthProvider
  // pedig azert van a Router BELUL, mert egyik se hasznal URL-fuggo logikat kozvetlenul.
  <BrowserRouter>
    <ToastProvider>
      <AuthProvider>
        <App />
      </AuthProvider>
    </ToastProvider>
  </BrowserRouter>
);
