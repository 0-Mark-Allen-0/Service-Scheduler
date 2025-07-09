// src/components/Navbar.tsx
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Link, useNavigate } from "react-router-dom";
import { useState, useEffect, useCallback } from "react"; // Import useCallback

export function Navbar() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userRole, setUserRole] = useState<"USER" | "PROVIDER" | null>(null);
  const navigate = useNavigate();

  // Memoize getDashboardPath to prevent unnecessary re-creations and for use in dependencies
  const getDashboardPath = useCallback((role: "USER" | "PROVIDER" | null) => {
    if (role === "USER") {
      return "/user_dashboard";
    } else if (role === "PROVIDER") {
      return "/provider_dashboard";
    }
    return "/"; // Default or fallback path
  }, []); // No dependencies for useCallback as it's a pure function of its argument

  useEffect(() => {
    const token = localStorage.getItem("auth_token");
    const role = localStorage.getItem("user_role") as
      | "USER"
      | "PROVIDER"
      | null;

    setIsLoggedIn(!!token);
    setUserRole(role);

    const handleStorageChange = () => {
      const updatedToken = localStorage.getItem("auth_token");
      const updatedRole = localStorage.getItem("user_role") as
        | "USER"
        | "PROVIDER"
        | null;
      setIsLoggedIn(!!updatedToken);
      setUserRole(updatedRole);
      // No need to navigate here, as the Link/Button will handle it on click
    };

    window.addEventListener("storage", handleStorageChange);

    return () => {
      window.removeEventListener("storage", handleStorageChange);
    };
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("auth_token");
    localStorage.removeItem("user_role");
    setIsLoggedIn(false);
    setUserRole(null);
    navigate("/login");
  };

  // New handler for the Dashboard button click
  const handleDashboardClick = () => {
    navigate(getDashboardPath(userRole)); // Use the current userRole state
  };

  return (
    <header className="bg-neutral-900 text-neutral-100">
      <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex items-center justify-between h-16">
        <div className="text-lg font-bold hover:scale-125 transition-all delay-75">
          Schedule<span className="text-amber-500">It</span>
        </div>
        <div className="space-x-4">
          <Link to={"/"}>
            <Button
              variant="ghost"
              className="text-neutral-100 hover:text-amber-500 cursor-pointer"
            >
              Home
            </Button>
          </Link>

          {isLoggedIn ? (
            // Render these links when the user is logged in
            <>
              {/* Change Link to a Button with an onClick handler for dynamic navigation */}
              <Button
                variant="ghost"
                className="text-neutral-100 hover:text-amber-500 cursor-pointer"
                onClick={handleDashboardClick} // Use the new handler here
              >
                Dashboard
              </Button>
              <Button
                onClick={handleLogout}
                variant="outline"
                className="border-red-500 text-red-500 hover:bg-red-500 hover:text-white cursor-pointer"
              >
                Logout
              </Button>
            </>
          ) : (
            // Render these links when the user is not logged in
            <>
              <Link to={"/login"}>
                <Button
                  variant="ghost"
                  className="text-neutral-100 hover:text-amber-500 cursor-pointer"
                >
                  Login
                </Button>
              </Link>
              <Link to={"/register"}>
                <Button
                  variant="outline"
                  className="border-amber-500 text-amber-500 hover:bg-amber-500 hover:text-neutral-900 cursor-pointer"
                >
                  Register
                </Button>
              </Link>
            </>
          )}
        </div>
      </nav>
      <Separator className="bg-neutral-700" />
    </header>
  );
}
