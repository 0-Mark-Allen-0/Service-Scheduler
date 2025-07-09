// File: src/contexts/AuthContext.tsx
import type { ProviderResponseDto, UserResponseDto } from "@/types/api";
import { jwtDecode } from "jwt-decode";
import {
  createContext,
  useContext,
  useState,
  useEffect,
  type ReactNode,
} from "react";

// Shape of JWT token payload
interface DecodedToken {
  sub: string; // userId
  email: string;
  role: "USER" | "PROVIDER";
  exp: number;
  [key: string]: unknown; // for any other claims
}

interface AuthContextType {
  token: string | null;
  user: UserResponseDto | ProviderResponseDto | null;
  decodedToken: DecodedToken | null;
  login: (
    token: string,
    userData: UserResponseDto | ProviderResponseDto
  ) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<
    UserResponseDto | ProviderResponseDto | null
  >(null);
  const [decodedToken, setDecodedToken] = useState<DecodedToken | null>(null);

  useEffect(() => {
    const savedToken = localStorage.getItem("auth_token");
    const savedUser = localStorage.getItem("auth_user");

    if (savedToken && savedUser) {
      try {
        const decoded: DecodedToken = jwtDecode(savedToken);

        // Check expiration
        if (decoded.exp * 1000 < Date.now()) {
          logout();
        } else {
          setToken(savedToken);
          setUser(JSON.parse(savedUser));
          setDecodedToken(decoded);
        }
      } catch (err) {
        console.error("Invalid token:", err);
        logout();
      }
    }
  }, []);

  const login = (
    newToken: string,
    userData: UserResponseDto | ProviderResponseDto
  ) => {
    try {
      const decoded: DecodedToken = jwtDecode(newToken);
      setToken(newToken);
      setUser(userData);
      setDecodedToken(decoded);
      localStorage.setItem("auth_token", newToken);
      localStorage.setItem("auth_user", JSON.stringify(userData));
    } catch (err) {
      console.error("Failed to decode token during login: ", err);
    }
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    setDecodedToken(null);
    localStorage.removeItem("auth_token");
    localStorage.removeItem("auth_user");
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        user,
        decodedToken,
        login,
        logout,
        isAuthenticated: !!token,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
