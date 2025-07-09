// src/pages/LoginPage.tsx
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { LogIn, Mail, Lock, AlertCircle, CheckCircle } from "lucide-react";
import { useForm } from "react-hook-form";
import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";

// IMPORT UserRole HERE!
import type { LoginRequestDto, LoginResponseDto, UserRole } from "@/types/api";
import { apiService } from "@/services/api";

type FormData = {
  email: string;
  password: string;
};

export default function LoginPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false);
  // FIX: Update userRole state to include "ADMIN" or use UserRole type directly
  const [userRole, setUserRole] = useState<UserRole | null>(null); // Use UserRole from types/api.ts

  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<FormData>();

  // Helper function to get the correct dashboard path based on role
  const getDashboardPath = useCallback(
    // FIX: Ensure this function also accepts the 'ADMIN' role
    (role: UserRole | null) => {
      // Now accepts UserRole type
      if (role === "USER") {
        return "/user_dashboard";
      } else if (role === "PROVIDER") {
        return "/provider_dashboard";
      } else if (role === "ADMIN") {
        return "/admin_dashboard"; // Add a path for the admin dashboard
      }
      return "/"; // Default or fallback path
    },
    []
  );

  // Effect to check login status on component mount and redirect if already logged in
  useEffect(() => {
    const token = localStorage.getItem("auth_token");
    // FIX: Cast storedRole to UserRole
    const storedRole = localStorage.getItem("user_role") as UserRole | null;

    if (token && storedRole) {
      setIsLoggedIn(true);
      setUserRole(storedRole);
      navigate(getDashboardPath(storedRole));
    }
  }, [navigate, getDashboardPath]);

  const onSubmit = async (data: FormData) => {
    setIsLoading(true);
    setError(null);
    setSuccess(null);
    setIsLoggedIn(false);
    setUserRole(null);

    try {
      const payload: LoginRequestDto = {
        email: data.email,
        password: data.password,
      };

      const response: LoginResponseDto = await apiService.login(payload);
      setSuccess(`Logged in successfully!`);

      localStorage.setItem("auth_token", response.token);
      localStorage.setItem("user_role", response.role);

      setIsLoggedIn(true);
      setUserRole(response.role); // This line is now type-safe!

      navigate(getDashboardPath(response.role));
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Login failed. Please check your credentials and try again."
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoToDashboard = () => {
    if (userRole) {
      navigate(getDashboardPath(userRole));
    } else {
      navigate("/");
    }
  };

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex items-center justify-center p-4 sm:p-6 font-sans">
      <Card className="w-full max-w-md bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl overflow-hidden">
        <CardHeader className="pb-6 pt-8 bg-neutral-800 border-b border-neutral-700">
          <CardTitle className="text-3xl font-extrabold text-neutral-50 text-center flex items-center justify-center gap-3">
            <LogIn className="h-8 w-8 text-amber-400" />
            Welcome Back!
          </CardTitle>
        </CardHeader>

        {isLoggedIn && userRole ? (
          <CardContent className="space-y-6 px-8 py-8 text-center">
            <div className="flex items-center justify-center gap-2 p-3 bg-green-950 border border-green-800 rounded-lg text-green-200">
              <CheckCircle className="h-4 w-4" />
              <span className="text-sm">You are already logged in.</span>
            </div>
            <p className="text-neutral-300 text-lg">
              Click the button below to go to your dashboard.
            </p>
            <Button
              onClick={handleGoToDashboard}
              className="w-full bg-amber-500 hover:bg-amber-600 text-neutral-900 font-bold py-3 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 text-lg"
            >
              Go To Dashboard
            </Button>
          </CardContent>
        ) : (
          <form onSubmit={handleSubmit(onSubmit)}>
            <CardContent className="space-y-6 px-8 py-8">
              {error && (
                <div className="flex items-center gap-2 p-3 bg-red-950 border border-red-800 rounded-lg text-red-200">
                  <AlertCircle className="h-4 w-4" />
                  <span className="text-sm">{error}</span>
                </div>
              )}

              {success && !isLoggedIn && (
                <div className="flex items-center gap-2 p-3 bg-green-950 border border-green-800 rounded-lg text-green-200">
                  <CheckCircle className="h-4 w-4" />
                  <span className="text-sm">{success}</span>
                </div>
              )}

              <div>
                <Label
                  htmlFor="email"
                  className="text-neutral-300 mb-2 block font-medium text-left"
                >
                  Email
                </Label>
                <div className="relative">
                  <Input
                    id="email"
                    type="email"
                    placeholder="your.email@example.com"
                    className="pl-10 bg-neutral-800 border-neutral-700 text-neutral-50 placeholder:text-neutral-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 h-11 rounded-lg transition-colors duration-200"
                    {...register("email", {
                      required: "Email is required",
                      pattern: {
                        value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                        message: "Invalid email format",
                      },
                    })}
                  />
                  <Mail className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-neutral-400" />
                </div>
                {errors.email && (
                  <span className="text-red-400 text-sm mt-1">
                    {errors.email.message}
                  </span>
                )}
              </div>

              <div>
                <Label
                  htmlFor="password"
                  className="text-neutral-300 mb-2 block font-medium text-left"
                >
                  Password
                </Label>
                <div className="relative">
                  <Input
                    id="password"
                    type="password"
                    placeholder="••••••••"
                    className="pl-10 bg-neutral-800 border-neutral-700 text-neutral-50 placeholder:text-neutral-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 h-11 rounded-lg transition-colors duration-200"
                    {...register("password", {
                      required: "Password is required",
                      minLength: {
                        value: 6,
                        message: "Password must be at least 6 characters",
                      },
                    })}
                  />
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-neutral-400" />
                </div>
                {errors.password && (
                  <span className="text-red-400 text-sm mt-1">
                    {errors.password.message}
                  </span>
                )}
              </div>
            </CardContent>

            <CardFooter className="pt-4 px-8 pb-8">
              <Button
                type="submit"
                disabled={isLoading}
                className="w-full bg-amber-500 hover:bg-amber-600 disabled:bg-amber-700 disabled:cursor-not-allowed text-neutral-900 font-bold py-3 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 text-lg"
              >
                {isLoading ? "Signing In..." : "Sign In"}
              </Button>
            </CardFooter>
          </form>
        )}
      </Card>
    </div>
  );
}
