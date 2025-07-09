/* eslint-disable @typescript-eslint/no-unused-vars */
// src/pages/AdminDashboardPage.tsx
import React, { useState, useEffect, useCallback } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import {
  ShieldCheck, // Admin specific icon
  Users, // For total users/providers
  CalendarCheck, // For appointments
  TrendingUp, // For stats/rates
  RefreshCw, // For refresh button
  Clock, // For peak hours
  Percent, // For cancellation rates
} from "lucide-react";

// IMPORTS FROM api.ts and types/api.ts
import { apiService } from "@/services/api";
import type { AdminStatsDto } from "@/types/api";

export default function AdminDashboardPage() {
  const [adminStats, setAdminStats] = useState<AdminStatsDto | null>(null);
  const [loadingStats, setLoadingStats] = useState(true);
  const [errorStats, setErrorStats] = useState<string | null>(null);

  // --- Fetching Data from Backend ---
  const fetchAdminStats = useCallback(async () => {
    setLoadingStats(true);
    setErrorStats(null);
    try {
      const stats = await apiService.getAdminStats();
      setAdminStats(stats);
    } catch (error) {
      console.error("Error fetching admin statistics:", error);
      setErrorStats("Failed to load admin statistics.");
    } finally {
      setLoadingStats(false);
    }
  }, []);

  useEffect(() => {
    fetchAdminStats();
  }, [fetchAdminStats]);

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col p-4 sm:p-6 font-sans">
      {/* Dashboard Header */}
      <div className="w-full text-center mb-8 pt-4">
        <h1 className="text-4xl font-extrabold text-neutral-50 flex items-center justify-center gap-4">
          <ShieldCheck className="h-10 w-10 text-emerald-400" />{" "}
          {/* Admin-specific color */}
          Admin Dashboard
        </h1>
        <p className="text-neutral-400 mt-2 text-lg">
          Overview of platform activities and key metrics.
        </p>
        <div className="mt-4 flex justify-center">
          <Button
            variant="outline"
            className="bg-neutral-700 hover:bg-neutral-600 text-neutral-100 font-semibold py-2 rounded-md transition-colors duration-200 flex items-center"
            onClick={() => window.location.reload()}
          >
            <RefreshCw className="h-4 w-4 mr-1" /> Refresh Data
          </Button>
        </div>
      </div>

      {/* Main Content Area: Statistics Cards */}
      <div className="flex flex-col lg:flex-row gap-6 mt-6 flex-1">
        {loadingStats ? (
          <div className="flex-1 text-neutral-400 text-center py-10">
            Loading admin statistics...
          </div>
        ) : errorStats ? (
          <div className="flex-1 text-red-500 text-center py-10">
            {errorStats}
          </div>
        ) : !adminStats ? (
          <div className="flex-1 text-neutral-400 text-center py-10">
            No statistics available.
          </div>
        ) : (
          <>
            {/* Total Appointments Per Provider */}
            <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
              <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700">
                <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left">
                  <CalendarCheck className="h-6 w-6 text-blue-400" />
                  Provider Appointments
                </CardTitle>
              </CardHeader>
              <CardContent className="flex-1 p-6">
                <ScrollArea className="h-[calc(100vh-350px)] lg:h-[calc(100vh-300px)] pr-4">
                  <div className="space-y-4">
                    {Object.entries(adminStats.totalAppointmentsPerProvider)
                      .length > 0 ? (
                      Object.entries(
                        adminStats.totalAppointmentsPerProvider
                      ).map(([providerName, count]) => (
                        <div
                          key={providerName}
                          className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-center gap-4 shadow-sm text-left"
                        >
                          <Users className="h-5 w-5 text-indigo-400 flex-shrink-0" />
                          <div>
                            <p className="text-lg font-semibold text-neutral-50">
                              {providerName}
                            </p>
                            <p className="text-neutral-300 text-sm">
                              Total Appointments:{" "}
                              <Badge className="bg-blue-600 text-blue-50 font-semibold">
                                {count}
                              </Badge>
                            </p>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="text-neutral-400 text-center py-10">
                        No appointment data available per provider.
                      </div>
                    )}
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>

            {/* Cancellation Rates */}
            <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
              <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700">
                <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left">
                  <Percent className="h-6 w-6 text-red-400" />
                  Cancellation Rates
                </CardTitle>
              </CardHeader>
              <CardContent className="flex-1 p-6">
                <ScrollArea className="h-[calc(100vh-350px)] lg:h-[calc(100vh-300px)] pr-4">
                  <div className="space-y-4">
                    {Object.entries(adminStats.cancellationRates).length > 0 ? (
                      Object.entries(adminStats.cancellationRates).map(
                        ([providerName, rate]) => (
                          <div
                            key={providerName}
                            className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-center gap-4 shadow-sm text-left"
                          >
                            <TrendingUp className="h-5 w-5 text-red-400 flex-shrink-0" />
                            <div>
                              <p className="text-lg font-semibold text-neutral-50">
                                {providerName}
                              </p>
                              <p className="text-neutral-300 text-sm">
                                Cancellation Rate:{" "}
                                <Badge className="bg-red-600 text-red-50 font-semibold">
                                  {rate.toFixed(2)}%
                                </Badge>
                              </p>
                            </div>
                          </div>
                        )
                      )
                    ) : (
                      <div className="text-neutral-400 text-center py-10">
                        No cancellation rate data available.
                      </div>
                    )}
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>

            {/* Peak Booking Hours */}
            <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
              <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700">
                <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left">
                  <Clock className="h-6 w-6 text-yellow-400" />
                  Peak Booking Hours
                </CardTitle>
              </CardHeader>
              <CardContent className="flex-1 p-6">
                <ScrollArea className="h-[calc(100vh-350px)] lg:h-[calc(100vh-300px)] pr-4">
                  <div className="space-y-4">
                    {Object.entries(adminStats.peakBookingHours).length > 0 ? (
                      Object.entries(adminStats.peakBookingHours)
                        .sort(
                          ([hourA, countA], [hourB, countB]) => countB - countA
                        ) // Sort by count descending
                        .map(([hour, count]) => (
                          <div
                            key={hour}
                            className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-center gap-4 shadow-sm text-left"
                          >
                            <Clock className="h-5 w-5 text-yellow-400 flex-shrink-0" />
                            <div>
                              <p className="text-lg font-semibold text-neutral-50">
                                {hour}:00 - {parseInt(hour) + 1}:00
                              </p>
                              <p className="text-neutral-300 text-sm">
                                Total Bookings:{" "}
                                <Badge className="bg-yellow-600 text-yellow-50 font-semibold">
                                  {count}
                                </Badge>
                              </p>
                            </div>
                          </div>
                        ))
                    ) : (
                      <div className="text-neutral-400 text-center py-10">
                        No peak booking hour data available.
                      </div>
                    )}
                  </div>
                </ScrollArea>
              </CardContent>
            </Card>
          </>
        )}
      </div>
    </div>
  );
}
