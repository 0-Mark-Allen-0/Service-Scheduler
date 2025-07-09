// File: src/pages/RegisterPage.tsx
"use client"; // This directive is typically for Next.js App Router, not needed for Vite/React.

import { useState } from "react";
import {
  Card,
  CardHeader,
  CardTitle,
  CardContent,
  CardFooter,
} from "@/components/ui/card";
import { Label } from "@/components/ui/label";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useForm } from "react-hook-form";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  User,
  Briefcase,
  UserPlus,
  AlertCircle,
  CheckCircle,
} from "lucide-react";
import { apiService } from "@/services/api";
import { useNavigate } from "react-router-dom"; // Import useNavigate

import type {
  UserRegistrationDto,
  ProviderRegistrationDto,
  UserResponseDto,
  ProviderResponseDto,
} from "@/types/api";

type FormData = {
  name: string;
  email: string;
  password: string;
  role: "USER" | "PROVIDER";
  specialization?: string;
};

export default function RegisterPage() {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [isRegistered, setIsRegistered] = useState(false); // New state for registration success

  const navigate = useNavigate(); // Initialize useNavigate hook

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<FormData>({
    defaultValues: {
      role: "USER",
    },
  });

  const role = watch("role");

  const onSubmit = async (data: FormData) => {
    setIsLoading(true);
    setError(null);
    setSuccess(null); // Clear previous success messages
    setIsRegistered(false); // Reset registration status

    try {
      if (data.role === "USER") {
        const userPayload: UserRegistrationDto = {
          name: data.name,
          email: data.email,
          password: data.password,
        };

        const response: UserResponseDto = await apiService.registerUser(
          userPayload
        );
        setSuccess(`User registered successfully! Welcome, ${response.name}!`);
      } else {
        const providerPayload: ProviderRegistrationDto = {
          name: data.name,
          email: data.email,
          password: data.password,
          specialization: data.specialization || "",
        };

        const response: ProviderResponseDto = await apiService.registerProvider(
          providerPayload
        );
        setSuccess(
          `Provider registered successfully! Welcome, ${response.name}! Specialization: ${response.specialization}`
        );
      }
      setIsRegistered(true); // Set to true on successful registration
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Registration failed. Please try again."
      );
    } finally {
      setIsLoading(false);
    }
  };

  const handleGoToLogin = () => {
    navigate("/login"); // Redirect to the login page
  };

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex items-center justify-center p-4 sm:p-6 font-sans">
      <Card className="w-full max-w-md bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl overflow-hidden">
        <CardHeader className="pb-6 pt-8 bg-neutral-800 border-b border-neutral-700">
          <CardTitle className="text-3xl font-extrabold text-neutral-50 text-center flex items-center justify-center gap-3">
            <UserPlus className="h-8 w-8 text-amber-400" />
            Register Account
          </CardTitle>
        </CardHeader>

        {isRegistered && success ? ( // Conditionally render if registration was successful
          <CardContent className="space-y-6 px-8 py-8 text-center">
            <div className="flex items-center justify-center gap-2 p-3 bg-green-950 border border-green-800 rounded-lg text-green-200">
              <CheckCircle className="h-4 w-4" />
              <span className="text-sm">{success}</span>
            </div>
            <p className="text-neutral-300 text-lg mt-4">
              Your account has been successfully created.
            </p>
            <Button
              onClick={handleGoToLogin}
              className="w-full bg-amber-600 hover:bg-amber-700 text-white font-bold py-3 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 text-lg mt-6"
            >
              Go To Login
            </Button>
          </CardContent>
        ) : (
          // Render the form if not yet registered
          <form onSubmit={handleSubmit(onSubmit)}>
            <CardContent className="space-y-6 px-8 py-8">
              {/* Error Message */}
              {error && (
                <div className="flex items-center gap-2 p-3 bg-red-950 border border-red-800 rounded-lg text-red-200">
                  <AlertCircle className="h-4 w-4" />
                  <span className="text-sm">{error}</span>
                </div>
              )}

              {/* Success Message (will only show briefly before isRegistered becomes true and the button renders) */}
              {success && !isRegistered && (
                <div className="flex items-center gap-2 p-3 bg-green-950 border border-green-800 rounded-lg text-green-200">
                  <CheckCircle className="h-4 w-4" />
                  <span className="text-sm">{success}</span>
                </div>
              )}

              {/* Name Input */}
              <div>
                <Label
                  htmlFor="name"
                  className="text-neutral-300 mb-2 block font-medium text-left"
                >
                  Name
                </Label>
                <Input
                  id="name"
                  {...register("name", {
                    required: "Name is required",
                    minLength: {
                      value: 2,
                      message: "Name must be at least 2 characters",
                    },
                  })}
                  className="bg-neutral-800 border-neutral-700 text-neutral-50 placeholder:text-neutral-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 h-11 rounded-lg transition-colors duration-200"
                  placeholder="Enter your full name"
                />
                {errors.name && (
                  <span className="text-red-400 text-sm mt-1">
                    {errors.name.message}
                  </span>
                )}
              </div>

              {/* Email Input */}
              <div>
                <Label
                  htmlFor="email"
                  className="text-neutral-300 mb-2 block font-medium text-left"
                >
                  Email
                </Label>
                <Input
                  id="email"
                  type="email"
                  {...register("email", {
                    required: "Email is required",
                    pattern: {
                      value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
                      message: "Invalid email format",
                    },
                  })}
                  className="bg-neutral-800 border-neutral-700 text-neutral-50 placeholder:text-neutral-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 h-11 rounded-lg transition-colors duration-200"
                  placeholder="your.email@example.com"
                />
                {errors.email && (
                  <span className="text-red-400 text-sm mt-1">
                    {errors.email.message}
                  </span>
                )}
              </div>

              {/* Password Input */}
              <div>
                <Label
                  htmlFor="password"
                  className="text-neutral-300 mb-2 block font-medium text-left"
                >
                  Password
                </Label>
                <Input
                  id="password"
                  type="password"
                  {...register("password", {
                    required: "Password is required",
                    minLength: {
                      value: 6,
                      message: "Password must be at least 6 characters",
                    },
                  })}
                  className="bg-neutral-800 border-neutral-700 text-neutral-50 placeholder:text-neutral-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 h-11 rounded-lg transition-colors duration-200"
                  placeholder="••••••••"
                />
                {errors.password && (
                  <span className="text-red-400 text-sm mt-1">
                    {errors.password.message}
                  </span>
                )}
              </div>

              {/* Role Selection */}
              <div>
                <Label className="text-neutral-300 mb-3 block font-medium text-left">
                  I'm a
                </Label>
                <RadioGroup
                  onValueChange={(val: "USER" | "PROVIDER") =>
                    setValue("role", val)
                  }
                  value={role}
                  className="flex flex-col sm:flex-row gap-4"
                >
                  <div className="flex items-center space-x-3 bg-neutral-800 border border-neutral-700 p-4 rounded-lg flex-1 cursor-pointer hover:border-amber-500 transition-all duration-200">
                    <RadioGroupItem
                      value="USER"
                      id="role-user"
                      className="h-5 w-5 text-amber-500 border-neutral-500 focus:ring-amber-500"
                    />
                    <Label
                      htmlFor="role-user"
                      className="flex items-center gap-2 text-neutral-100 text-lg font-semibold cursor-pointer"
                    >
                      <User className="h-5 w-5 text-neutral-300" />
                      User
                    </Label>
                  </div>

                  <div className="flex items-center space-x-3 bg-neutral-800 border border-neutral-700 p-4 rounded-lg flex-1 cursor-pointer hover:border-amber-500 transition-all duration-75">
                    <RadioGroupItem
                      value="PROVIDER"
                      id="role-provider"
                      className="h-5 w-5 text-amber-500 border-neutral-500 focus:ring-amber-500"
                    />
                    <Label
                      htmlFor="role-provider"
                      className="flex items-center gap-2 text-neutral-100 text-lg font-semibold cursor-pointer"
                    >
                      <Briefcase className="h-5 w-5 text-neutral-300" />
                      Provider
                    </Label>
                  </div>
                </RadioGroup>
              </div>

              {/* Specialization Input */}
              {role === "PROVIDER" && (
                <div>
                  <Label
                    htmlFor="specialization"
                    className="text-neutral-300 mb-2 block font-medium text-left"
                  >
                    My profession
                  </Label>
                  <Input
                    id="specialization"
                    {...register("specialization", {
                      required:
                        role === "PROVIDER"
                          ? "Specialization is required for providers"
                          : false,
                    })}
                    className="bg-neutral-800 border-neutral-700 text-neutral-50 placeholder:text-neutral-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 h-11 rounded-lg transition-colors duration-200"
                    placeholder="e.g., Dentist, Cardiologist, etc."
                  />
                  {errors.specialization && (
                    <span className="text-red-400 text-sm mt-1">
                      {errors.specialization.message}
                    </span>
                  )}
                </div>
              )}
            </CardContent>

            <CardFooter className="pt-4 px-8 pb-8">
              <Button
                type="submit"
                disabled={isLoading}
                className="w-full bg-amber-500 hover:bg-amber-600 disabled:bg-amber-700 disabled:cursor-not-allowed text-neutral-900 font-bold py-3 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 text-lg"
              >
                {isLoading ? "Registering..." : "Register"}
              </Button>
            </CardFooter>
          </form>
        )}
      </Card>
    </div>
  );
}
