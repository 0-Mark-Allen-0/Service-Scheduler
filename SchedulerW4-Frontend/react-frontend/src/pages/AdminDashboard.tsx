/* eslint-disable @typescript-eslint/no-unused-vars */
"use client";

import { useState, useEffect, useCallback } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import {
  ShieldCheck,
  Users,
  CalendarCheck,
  TrendingUp,
  RefreshCw,
  Clock,
  Percent,
  UserCheck,
  Stethoscope,
  Calendar,
  ClipboardList,
  Activity,
  Database,
  CheckCircle,
  AlertTriangle,
  XCircle,
  Info,
  Briefcase,
  Ban,
  Clock3,
} from "lucide-react";
import { format, parseISO } from "date-fns";

import { apiService } from "@/services/api";
import type {
  AdminStatsDto,
  UserResponseDto,
  ProviderResponseDto,
  AppointmentResponseDto,
  SlotResponseDto,
  HealthMetricsDto,
} from "@/types/api";

export default function AdminDashboardPage() {
  // State variables
  const [adminStats, setAdminStats] = useState<AdminStatsDto | null>(null);
  const [users, setUsers] = useState<UserResponseDto[]>([]);
  const [providers, setProviders] = useState<ProviderResponseDto[]>([]);
  const [appointments, setAppointments] = useState<AppointmentResponseDto[]>(
    []
  );
  const [slots, setSlots] = useState<SlotResponseDto[]>([]);
  const [healthMetrics, setHealthMetrics] = useState<HealthMetricsDto | null>(
    null
  );

  const [loadingStats, setLoadingStats] = useState(true);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [loadingProviders, setLoadingProviders] = useState(true);
  const [loadingAppointments, setLoadingAppointments] = useState(true);
  const [loadingSlots, setLoadingSlots] = useState(true);
  const [loadingHealth, setLoadingHealth] = useState(true);

  const [errorStats, setErrorStats] = useState<string | null>(null);
  const [errorUsers, setErrorUsers] = useState<string | null>(null);
  const [errorProviders, setErrorProviders] = useState<string | null>(null);
  const [errorAppointments, setErrorAppointments] = useState<string | null>(
    null
  );
  const [errorSlots, setErrorSlots] = useState<string | null>(null);
  const [errorHealth, setErrorHealth] = useState<string | null>(null);

  // Fetch functions
  const fetchAdminStats = useCallback(async () => {
    setLoadingStats(true);
    setErrorStats(null);
    try {
      const stats = await apiService.getAdminStats();
      console.log("=== DEBUG: Admin Stats ===", stats);
      setAdminStats(stats);
    } catch (error) {
      console.error("Error fetching admin statistics:", error);
      setErrorStats("Failed to load admin statistics.");
    } finally {
      setLoadingStats(false);
    }
  }, []);

  const fetchUsers = useCallback(async () => {
    setLoadingUsers(true);
    setErrorUsers(null);
    try {
      const usersData = await apiService.getAllUsersAdmin();
      console.log("=== DEBUG: Users Data ===", usersData);
      setUsers(usersData);
    } catch (error) {
      console.error("Error fetching users:", error);
      setErrorUsers("Failed to load users.");
    } finally {
      setLoadingUsers(false);
    }
  }, []);

  const fetchProviders = useCallback(async () => {
    setLoadingProviders(true);
    setErrorProviders(null);
    try {
      const providersData = await apiService.getAllProvidersAdmin();
      console.log("=== DEBUG: Providers Data ===", providersData);
      setProviders(providersData);
    } catch (error) {
      console.error("Error fetching providers:", error);
      setErrorProviders("Failed to load providers.");
    } finally {
      setLoadingProviders(false);
    }
  }, []);

  const fetchAppointments = useCallback(async () => {
    setLoadingAppointments(true);
    setErrorAppointments(null);
    try {
      const appointmentsData = await apiService.getAllAppointmentsAdmin();
      console.log("=== DEBUG: Appointments Data ===", appointmentsData);
      setAppointments(appointmentsData);
    } catch (error) {
      console.error("Error fetching appointments:", error);
      setErrorAppointments("Failed to load appointments.");
    } finally {
      setLoadingAppointments(false);
    }
  }, []);

  const fetchSlots = useCallback(async () => {
    setLoadingSlots(true);
    setErrorSlots(null);
    try {
      const slotsData = await apiService.getAllSlotsAdmin();
      console.log("=== DEBUG: Slots Data ===", slotsData);
      setSlots(slotsData);
    } catch (error) {
      console.error("Error fetching slots:", error);
      setErrorSlots("Failed to load slots.");
    } finally {
      setLoadingSlots(false);
    }
  }, []);

  const fetchHealthMetrics = useCallback(async () => {
    setLoadingHealth(true);
    setErrorHealth(null);
    try {
      const healthData = await apiService.getHealthMetrics();
      console.log("=== DEBUG: Health Metrics ===", healthData);
      setHealthMetrics(healthData);
    } catch (error) {
      console.error("Error fetching health metrics:", error);
      setErrorHealth("Failed to load health metrics.");
    } finally {
      setLoadingHealth(false);
    }
  }, []);

  const handleRefresh = useCallback(async () => {
    console.log("=== DEBUG: Refreshing all admin data ===");
    await Promise.all([
      fetchAdminStats(),
      fetchUsers(),
      fetchProviders(),
      fetchAppointments(),
      fetchSlots(),
      fetchHealthMetrics(),
    ]);
  }, [
    fetchAdminStats,
    fetchUsers,
    fetchProviders,
    fetchAppointments,
    fetchSlots,
    fetchHealthMetrics,
  ]);

  useEffect(() => {
    handleRefresh();
  }, [handleRefresh]);

  // Helper functions
  const getStatusBadge = (status: string) => {
    switch (status) {
      case "BOOKED":
        return {
          className: "bg-green-600 text-green-50",
          icon: <CheckCircle className="h-3 w-3 mr-1" />,
          text: "Booked",
        };
      case "QUEUED":
        return {
          className: "bg-purple-600 text-purple-50",
          icon: <Clock3 className="h-3 w-3 mr-1" />,
          text: "In Queue",
        };
      case "CANCELLED":
        return {
          className: "bg-red-600 text-red-50",
          icon: <Ban className="h-3 w-3 mr-1" />,
          text: "Cancelled",
        };
      default:
        return {
          className: "bg-gray-600 text-gray-50",
          icon: <Info className="h-3 w-3 mr-1" />,
          text: status,
        };
    }
  };

  const getHealthStatusIcon = (status: string) => {
    switch (status?.toUpperCase()) {
      case "UP":
        return <CheckCircle className="h-4 w-4 text-green-500" />;
      case "DOWN":
        return <XCircle className="h-4 w-4 text-red-500" />;
      case "UNKNOWN":
        return <AlertTriangle className="h-4 w-4 text-yellow-500" />;
      default:
        return <Info className="h-4 w-4 text-blue-500" />;
    }
  };

  // Calculate derived stats
  const totalAppointments = appointments.length;
  const bookedAppointments = appointments.filter(
    (apt) => apt.status === "BOOKED"
  ).length;
  const queuedAppointments = appointments.filter(
    (apt) => apt.status === "QUEUED"
  ).length;
  const cancelledAppointments = appointments.filter(
    (apt) => apt.status === "CANCELLED"
  ).length;
  const totalSlots = slots.length;
  const bookedSlots = slots.filter((slot) => slot.booked).length;
  const availableSlots = totalSlots - bookedSlots;

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col p-4 sm:p-6 font-sans">
      {/* Dashboard Header */}
      <div className="w-full text-center mb-8 pt-4">
        <h1 className="text-4xl font-extrabold text-neutral-50 flex items-center justify-center gap-4">
          <ShieldCheck className="h-10 w-10 text-emerald-400" />
          Admin Dashboard
        </h1>
        <p className="text-neutral-400 mt-2 text-lg">
          Comprehensive overview of platform activities and system health.
        </p>
        <div className="mt-4 flex justify-center">
          <Button
            variant="outline"
            className="bg-neutral-800 hover:bg-neutral-700 text-neutral-50 hover:text-emerald-500 border border-neutral-700 rounded-lg px-4 py-2 text-base flex items-center gap-2 transition-colors duration-200"
            onClick={() => window.location.reload()}
            disabled={
              loadingStats ||
              loadingUsers ||
              loadingProviders ||
              loadingAppointments ||
              loadingSlots
            }
          >
            <RefreshCw
              className={`h-5 w-5 ${
                loadingStats ||
                loadingUsers ||
                loadingProviders ||
                loadingAppointments ||
                loadingSlots
                  ? "animate-spin"
                  : ""
              }`}
            />
            Refresh All Data
          </Button>
        </div>
      </div>

      {/* Statistics Grid */}
      <div className="mb-8">
        <h2 className="text-2xl font-bold text-neutral-50 mb-4 flex items-center gap-2">
          <TrendingUp className="h-6 w-6 text-emerald-400" />
          Key Statistics
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-4">
          {/* Total Users Card */}
          <Card className="bg-gradient-to-br from-blue-900 to-blue-800 border-blue-700">
            <CardContent className="flex flex-col items-center justify-center p-6">
              <UserCheck className="h-12 w-12 text-blue-300 mb-4" />
              <div className="text-3xl font-bold text-white mb-2">
                {loadingStats ? "..." : adminStats?.totalUsers || users.length}
              </div>
              <div className="text-blue-200 text-center">Total Users</div>
            </CardContent>
          </Card>

          {/* Total Providers Card */}
          <Card className="bg-gradient-to-br from-purple-900 to-purple-800 border-purple-700">
            <CardContent className="flex flex-col items-center justify-center p-6">
              <Stethoscope className="h-12 w-12 text-purple-300 mb-4" />
              <div className="text-3xl font-bold text-white mb-2">
                {loadingStats
                  ? "..."
                  : adminStats?.totalProviders || providers.length}
              </div>
              <div className="text-purple-200 text-center">Total Providers</div>
            </CardContent>
          </Card>

          {/* Total Appointments Card */}
          <Card className="bg-gradient-to-br from-green-900 to-green-800 border-green-700">
            <CardContent className="flex flex-col items-center justify-center p-6">
              <CalendarCheck className="h-12 w-12 text-green-300 mb-4" />
              <div className="text-3xl font-bold text-white mb-2">
                {loadingAppointments ? "..." : totalAppointments}
              </div>
              <div className="text-green-200 text-center">
                Total Appointments
              </div>
            </CardContent>
          </Card>

          {/* Total Slots Card */}
          <Card className="bg-gradient-to-br from-orange-900 to-orange-800 border-orange-700">
            <CardContent className="flex flex-col items-center justify-center p-6">
              <ClipboardList className="h-12 w-12 text-orange-300 mb-4" />
              <div className="text-3xl font-bold text-white mb-2">
                {loadingSlots ? "..." : totalSlots}
              </div>
              <div className="text-orange-200 text-center">Total Slots</div>
            </CardContent>
          </Card>

          {/* Peak Booking Hours Card
          <Card className="bg-gradient-to-br from-yellow-900 to-yellow-800 border-yellow-700">
            <CardContent className="flex flex-col items-center justify-center p-6">
              <Clock className="h-12 w-12 text-yellow-300 mb-4" />
              <div className="text-2xl font-bold text-white mb-2">
                {loadingStats
                  ? "..."
                  : adminStats?.peakBookingHours
                  ? Object.entries(adminStats.peakBookingHours).sort(
                      ([, a], [, b]) => b - a
                    )[0]?.[0] + ":00" || "N/A"
                  : "N/A"}
              </div>
              <div className="text-yellow-200 text-center">Peak Hour</div>
            </CardContent>
          </Card> */}

          {/* Average Cancellation Rate Card */}
          <Card className="bg-gradient-to-br from-red-900 to-red-800 border-red-700">
            <CardContent className="flex flex-col items-center justify-center p-6">
              <Percent className="h-12 w-12 text-red-300 mb-4" />
              <div className="text-3xl font-bold text-white mb-2">
                {loadingStats
                  ? "..."
                  : adminStats?.cancellationRates
                  ? (
                      Object.values(adminStats.cancellationRates).reduce(
                        (a, b) => a + b,
                        0
                      ) / Object.values(adminStats.cancellationRates).length ||
                      0
                    ).toFixed(1) + "%"
                  : totalAppointments > 0
                  ? ((cancelledAppointments / totalAppointments) * 100).toFixed(
                      1
                    ) + "%"
                  : "0%"}
              </div>
              <div className="text-red-200 text-center">Cancellation Rate</div>
            </CardContent>
          </Card>

          {/* System Health Card
          <Card className="bg-gradient-to-br from-emerald-900 to-emerald-800 border-emerald-700">
            <CardContent className="flex flex-col items-center justify-center p-6">
              <Activity className="h-12 w-12 text-emerald-300 mb-4" />
              <div className="flex items-center gap-2 mb-2">
                {getHealthStatusIcon(healthMetrics?.overallStatus || "UNKNOWN")}
                <div className="text-2xl font-bold text-white">
                  {loadingHealth
                    ? "..."
                    : healthMetrics?.overallStatus || "UNKNOWN"}
                </div>
              </div>
              <div className="text-emerald-200 text-center">System Status</div>
            </CardContent>
          </Card> */}

          {/* Database Health Card
          <Card className="bg-gradient-to-br from-indigo-900 to-indigo-800 border-indigo-700">
            <CardContent className="flex flex-col items-center justify-center p-6">
              <Database className="h-12 w-12 text-indigo-300 mb-4" />
              <div className="flex items-center gap-2 mb-2">
                {getHealthStatusIcon(
                  healthMetrics?.database?.status || "UNKNOWN"
                )}
                <div className="text-2xl font-bold text-white">
                  {loadingHealth
                    ? "..."
                    : healthMetrics?.database?.status || "UNKNOWN"}
                </div>
              </div>
              <div className="text-indigo-200 text-center">Database</div>
            </CardContent>
          </Card> */}
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 flex-1">
        {/* Users Section */}
        <Card className="bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
          <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700">
            <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3">
              <Users className="h-6 w-6 text-blue-400" />
              All Users ({users.length})
            </CardTitle>
          </CardHeader>
          <CardContent className="flex-1 p-6">
            {loadingUsers ? (
              <div className="text-neutral-400 text-center py-10">
                Loading users...
              </div>
            ) : errorUsers ? (
              <div className="text-red-500 text-center py-10">{errorUsers}</div>
            ) : users.length === 0 ? (
              <div className="text-neutral-400 text-center py-10">
                No users found.
              </div>
            ) : (
              <ScrollArea className="h-[400px] pr-4">
                <div className="space-y-3">
                  {users.map((user) => (
                    <div
                      key={user.id}
                      className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-center gap-4"
                    >
                      <UserCheck className="h-5 w-5 text-blue-400 flex-shrink-0" />
                      <div className="flex-grow">
                        <p className="text-lg font-semibold text-neutral-50">
                          {user.name}
                        </p>
                        <p className="text-neutral-400 text-sm">{user.email}</p>
                      </div>
                      <Badge className="bg-blue-600 text-blue-50">
                        ID: {user.id}
                      </Badge>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            )}
          </CardContent>
        </Card>

        {/* Providers Section */}
        <Card className="bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
          <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700">
            <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3">
              <Stethoscope className="h-6 w-6 text-purple-400" />
              All Providers ({providers.length})
            </CardTitle>
          </CardHeader>
          <CardContent className="flex-1 p-6">
            {loadingProviders ? (
              <div className="text-neutral-400 text-center py-10">
                Loading providers...
              </div>
            ) : errorProviders ? (
              <div className="text-red-500 text-center py-10">
                {errorProviders}
              </div>
            ) : providers.length === 0 ? (
              <div className="text-neutral-400 text-center py-10">
                No providers found.
              </div>
            ) : (
              <ScrollArea className="h-[400px] pr-4">
                <div className="space-y-3">
                  {providers.map((provider) => (
                    <div
                      key={provider.id}
                      className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-center gap-4"
                    >
                      <Stethoscope className="h-5 w-5 text-purple-400 flex-shrink-0" />
                      <div className="flex-grow">
                        <p className="text-lg font-semibold text-neutral-50">
                          {provider.name}
                        </p>
                        <p className="text-neutral-400 text-sm">
                          {provider.email}
                        </p>
                        <Badge className="mt-1 bg-purple-600 text-purple-50 text-xs">
                          <Briefcase className="h-3 w-3 mr-1" />
                          {provider.specialization}
                        </Badge>
                      </div>
                      <Badge className="bg-purple-600 text-purple-50">
                        ID: {provider.id}
                      </Badge>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            )}
          </CardContent>
        </Card>

        {/* Appointments Section */}
        <Card className="bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
          <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700">
            <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3">
              <CalendarCheck className="h-6 w-6 text-green-400" />
              All Appointments ({appointments.length})
            </CardTitle>
            <div className="flex gap-4 text-sm">
              <span className="text-green-400">
                Booked: {bookedAppointments}
              </span>
              <span className="text-purple-400">
                Queued: {queuedAppointments}
              </span>
              <span className="text-red-400">
                Cancelled: {cancelledAppointments}
              </span>
            </div>
          </CardHeader>
          <CardContent className="flex-1 p-6">
            {loadingAppointments ? (
              <div className="text-neutral-400 text-center py-10">
                Loading appointments...
              </div>
            ) : errorAppointments ? (
              <div className="text-red-500 text-center py-10">
                {errorAppointments}
              </div>
            ) : appointments.length === 0 ? (
              <div className="text-neutral-400 text-center py-10">
                No appointments found.
              </div>
            ) : (
              <ScrollArea className="h-[400px] pr-4">
                <div className="space-y-3">
                  {appointments.map((appointment) => {
                    const statusBadge = getStatusBadge(appointment.status);
                    return (
                      <div
                        key={appointment.appointmentId}
                        className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex flex-col justify-between"
                      >
                        <div className="flex items-start justify-between mb-2">
                          <div className="flex flex-col items-start">
                            <p className="text-lg font-semibold text-neutral-50">
                              {appointment.userName}
                            </p>
                            <p className="text-neutral-400 text-sm">
                              with {appointment.providerName}
                            </p>
                          </div>
                          <Badge
                            className={`${statusBadge.className} flex items-center text-xs`}
                          >
                            {statusBadge.icon}
                            {statusBadge.text}
                          </Badge>
                        </div>
                        <div className="flex items-center gap-4 text-sm text-neutral-400">
                          <span className="flex items-center gap-1">
                            <Calendar className="h-4 w-4" />
                            {format(parseISO(appointment.startTime), "PPP")}
                          </span>
                          <span className="flex items-center gap-1">
                            <Clock className="h-4 w-4" />
                            {format(parseISO(appointment.startTime), "p")}
                          </span>
                        </div>
                        <Badge className="mt-2 bg-neutral-700 text-neutral-300 text-xs">
                          ID: {appointment.appointmentId}
                        </Badge>
                      </div>
                    );
                  })}
                </div>
              </ScrollArea>
            )}
          </CardContent>
        </Card>

        {/* Slots Section */}
        <Card className="bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
          <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700">
            <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3">
              <ClipboardList className="h-6 w-6 text-yellow-400" />
              All Slots ({slots.length})
            </CardTitle>
            <div className="flex gap-4 text-sm">
              <span className="text-green-400">
                Available: {availableSlots}
              </span>
              <span className="text-red-400">Booked: {bookedSlots}</span>
            </div>
          </CardHeader>
          <CardContent className="flex-1 p-6">
            {loadingSlots ? (
              <div className="text-neutral-400 text-center py-10">
                Loading slots...
              </div>
            ) : errorSlots ? (
              <div className="text-red-500 text-center py-10">{errorSlots}</div>
            ) : slots.length === 0 ? (
              <div className="text-neutral-400 text-center py-10">
                No slots found.
              </div>
            ) : (
              <ScrollArea className="h-[400px] pr-4">
                <div className="space-y-3">
                  {slots.map((slot) => (
                    // Inside slots.map(...)
                    <div
                      key={slot.slotId}
                      className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex flex-col justify-between"
                    >
                      <div className="flex items-start justify-between mb-2">
                        <div className="flex flex-col items-start">
                          <p className="text-lg font-semibold text-neutral-50">
                            {slot.providerName}
                          </p>
                          <Badge className="mt-1 bg-neutral-700 text-neutral-300 text-xs flex items-center gap-1">
                            <Briefcase className="h-3 w-3" />
                            {slot.specialization}
                          </Badge>
                        </div>

                        <Badge
                          className={
                            slot.booked
                              ? "bg-amber-500 text-red-50"
                              : "bg-green-600 text-green-50"
                          }
                        >
                          {slot.booked ? "Booked" : "Available"}
                        </Badge>
                      </div>
                      <div className="flex items-center gap-4 text-sm text-neutral-400">
                        <span className="flex items-center gap-1">
                          <Calendar className="h-4 w-4" />
                          {format(parseISO(slot.startTime), "PPP")}
                        </span>
                        <span className="flex items-center gap-1">
                          <Clock className="h-4 w-4" />
                          {format(parseISO(slot.startTime), "p")} -{" "}
                          {format(parseISO(slot.endTime), "p")}
                        </span>
                      </div>
                      <Badge className="mt-2 bg-neutral-700 text-neutral-300 text-xs">
                        ID: {slot.slotId}
                      </Badge>
                    </div>
                  ))}
                </div>
              </ScrollArea>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
