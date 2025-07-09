// src/pages/HomePage.tsx
"use client";

import { Button } from "@/components/ui/button";
import { Sparkles, UserPlus, LogIn } from "lucide-react"; // Icons for the landing page
import { Link } from "react-router-dom";

export default function HomePage() {
  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex items-center justify-center p-4 sm:p-6 font-sans">
      <div className="w-full max-w-2xl bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl p-8 sm:p-12 text-center space-y-8">
        {/* Hero Section */}
        <div className="space-y-4">
          <Sparkles className="h-20 w-20 text-amber-400 mx-auto animate-pulse" />{" "}
          {/* Large, engaging icon */}
          <h1 className="text-5xl font-extrabold text-neutral-50 leading-tight">
            Scheduling,{" "}
            <span className="hover:text-amber-300 transition-all delay-100">
              Simplified
            </span>
            .
          </h1>
          <p className="text-neutral-300 text-lg max-w-prose mx-auto">
            Connect with service providers or manage your availability with
            ease.
          </p>
        </div>

        {/* Call to Action Buttons */}
        <div className="flex flex-col sm:flex-row justify-center gap-4 pt-4">
          <Link to="register">
            <Button className="w-full sm:w-auto bg-amber-500 hover:bg-neutral-100 hover:text-amber-500 text-neutral-900 font-bold py-3 px-8 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 text-lg flex items-center justify-center gap-2">
              <UserPlus className="h-5 w-5" />
              Register Now
            </Button>
          </Link>
          <Link to="login">
            <Button
              variant="outline"
              className="w-full sm:w-auto bg-neutral-700 delay-75 hover:text-amber-500 text-neutral-50 border-neutral-600 font-semibold py-3 px-8 rounded-lg shadow-md hover:shadow-lg transition-all duration-200 text-lg flex items-center justify-center gap-2"
            >
              <LogIn className="h-5 w-5" />
              Login
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
