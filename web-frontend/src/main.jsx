import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import App from './App.jsx';
import { AuthProvider } from './context/AuthContext.jsx';
import { ToastProvider } from './context/ToastContext.jsx';
import './styles/index.css';

// Nincs React.StrictMode: a dev-modbeli dupla mount/unmount ciklusa osszeakad a Framer Motion
// AnimatePresence kilepes-kovetesevel (ismert kompatibilitasi surlodas) - bezarodo sheet/modal
// hatterek DOM-ban ragadtak, lathatatlanul leblokkolva az egesz oldalt kattintasra.
ReactDOM.createRoot(document.getElementById('root')).render(
  <BrowserRouter>
    <ToastProvider>
      <AuthProvider>
        <App />
      </AuthProvider>
    </ToastProvider>
  </BrowserRouter>
);
