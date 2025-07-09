import "./App.css";
import LoginPage from "./pages/Login";
import RegisterPage from "./pages/Register";
import ProviderDashboardPage from "./pages/ProviderDashboard";
import UserDashboardPage from "./pages/UserDashboard";
import HomePage from "./pages/Home";
import { Navbar } from "./react_components/Navbar";
import { Routes, Route } from "react-router-dom";
import AdminDashboardPage from "./pages/AdminDashboard";

function App() {
  return (
    <>
      <div className="App">
        <Navbar />
        <Routes>
          <Route element={<HomePage />} path="/" />
          <Route element={<LoginPage />} path="login" />
          <Route element={<RegisterPage />} path="register" />
          <Route element={<UserDashboardPage />} path="user_dashboard" />
          <Route
            element={<ProviderDashboardPage />}
            path="provider_dashboard"
          />
          <Route element={<AdminDashboardPage />} path="admin_dashboard" />
        </Routes>
      </div>
    </>
  );
}

export default App;
