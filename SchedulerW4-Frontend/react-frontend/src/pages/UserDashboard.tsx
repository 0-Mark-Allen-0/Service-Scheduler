"use client";

/* eslint-disable @typescript-eslint/no-unused-vars */
// src/pages/UserDashboard.tsx
import { useState, useEffect, useCallback } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Label } from "@/components/ui/label";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  CalendarPlus,
  Clock,
  Briefcase,
  Calendar,
  ListChecks,
  CheckCircle,
  CalendarDays,
  Clock4,
  Trash2,
  Search,
  Ban,
  RefreshCw,
  Pencil,
  Info,
  AlertTriangle,
  Users,
  Filter,
  Clock3,
} from "lucide-react";
import { format, parseISO } from "date-fns";

// IMPORTS FROM YOUR PROJECT FILES
import { apiService } from "@/services/api";
import type {
  AppointmentRequestDto,
  AppointmentCancelDto,
  AppointmentResponseDto,
  SlotResponseDto,
  AppointmentRescheduleRequestDto,
} from "@/types/api";

// NEW: Status filter type
type AppointmentStatusFilter = "ALL" | "BOOKED" | "QUEUED" | "CANCELLED";

export default function UserDashboardPage() {
  // State variables
  const [allSlots, setAllSlots] = useState<SlotResponseDto[]>([]);
  const [userAppointments, setUserAppointments] = useState<
    AppointmentResponseDto[]
  >([]);
  const [selectedProviderIdForDialog, setSelectedProviderIdForDialog] =
    useState<number | null>(null);
  const [selectedSlotToBook, setSelectedSlotToBook] =
    useState<SlotResponseDto | null>(null);
  const [isBookingDialogOpen, setIsBookingDialogOpen] = useState(false);
  const [isCancelConfirmOpen, setIsCancelConfirmOpen] = useState(false);
  const [appointmentToCancel, setAppointmentToCancel] =
    useState<AppointmentResponseDto | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [loadingAppointments, setLoadingAppointments] = useState(true);
  const [errorAppointments, setErrorAppointments] = useState<string | null>(
    null
  );
  const [loadingSlots, setLoadingSlots] = useState(true);
  const [errorSlots, setErrorSlots] = useState<string | null>(null);

  // NEW: Status filter state
  const [statusFilter, setStatusFilter] =
    useState<AppointmentStatusFilter>("ALL");

  // States for the Reschedule Dialog
  const [isRescheduleDialogOpen, setIsRescheduleDialogOpen] = useState(false);
  const [appointmentToReschedule, setAppointmentToReschedule] =
    useState<AppointmentResponseDto | null>(null);
  const [newSlotForReschedule, setNewSlotForReschedule] =
    useState<SlotResponseDto | null>(null);

  // --- Data Fetching ---
  const fetchUserAppointments = useCallback(async () => {
    setLoadingAppointments(true);
    setErrorAppointments(null);
    try {
      const appointments = await apiService.viewBookedSlots();
      console.log("=== DEBUG: Fetched appointments ===", appointments); // DEBUG
      setUserAppointments(appointments);
    } catch (error) {
      console.error("Error fetching user appointments:", error);
      setErrorAppointments("Failed to load your appointments.");
    } finally {
      setLoadingAppointments(false);
    }
  }, []);

  const fetchAllSlots = useCallback(async () => {
    setLoadingSlots(true);
    setErrorSlots(null);
    try {
      const slots = await apiService.viewAllSlots();
      console.log("Fetched slots:", slots);
      setAllSlots(slots);
    } catch (error) {
      console.error("Error fetching all slots:", error);
      setErrorSlots("Failed to load slots.");
    } finally {
      setLoadingSlots(false);
    }
  }, []);

  const handleRefresh = useCallback(async () => {
    await Promise.all([fetchUserAppointments(), fetchAllSlots()]);
  }, [fetchUserAppointments, fetchAllSlots]);

  useEffect(() => {
    handleRefresh();
  }, [handleRefresh]);

  // --- Filter Logic ---
  // Filter slots for the initial booking dialog - show all slots
  const slotsForDialog = selectedProviderIdForDialog
    ? allSlots.filter((slot) => slot.providerId === selectedProviderIdForDialog)
    : [];

  // Filter slots for the reschedule dialog - only unbooked slots
  const slotsForRescheduleDialog = appointmentToReschedule
    ? allSlots.filter(
        (slot) =>
          slot.providerName === appointmentToReschedule.providerName &&
          !slot.booked
      )
    : [];

  // NEW: Filter appointments by status
  const filteredAppointments = userAppointments.filter((appointment) => {
    if (statusFilter === "ALL") return true;
    return appointment.status === statusFilter;
  });

  // --- Action Handlers ---
  const handleBookAppointment = async () => {
    if (selectedSlotToBook && selectedProviderIdForDialog) {
      try {
        console.log("=== DEBUG: Booking appointment ===");
        console.log("Slot ID:", selectedSlotToBook.slotId);
        console.log("Provider ID:", selectedProviderIdForDialog);
        console.log("Slot is booked:", selectedSlotToBook.booked);

        const requestDto: AppointmentRequestDto = {
          userId: 0,
          providerId: selectedProviderIdForDialog,
          slotId: selectedSlotToBook.slotId,
        };
        const response = await apiService.bookAppointment(requestDto);
        console.log("=== DEBUG: Booking response ===", response);

        await handleRefresh();
        setIsBookingDialogOpen(false);

        // Show different messages based on booking status
        if (response.status === "BOOKED") {
          alert("Appointment booked successfully!");
        } else if (response.status === "QUEUED") {
          alert("Slot is booked. You have been added to the queue!");
        } else if (response.status === "ALREADY_QUEUED") {
          alert("You are already in the queue for this slot!");
        } else {
          alert("Appointment request processed!");
        }
      } catch (error) {
        console.error("Error booking appointment:", error);
        alert("Failed to book appointment. Please try again.");
      }
    }
  };

  const handleCancelAppointment = (appointment: AppointmentResponseDto) => {
    setAppointmentToCancel(appointment);
    setIsCancelConfirmOpen(true);
  };

  const confirmCancelAppointment = async () => {
    if (appointmentToCancel) {
      try {
        const cancelDto: AppointmentCancelDto = {
          appointmentId: appointmentToCancel.appointmentId,
        };
        const messageResponse = await apiService.cancelAppointment(cancelDto);
        await handleRefresh();
        setIsCancelConfirmOpen(false);
        setAppointmentToCancel(null);
        alert(messageResponse.message);
      } catch (error) {
        console.error("Error cancelling appointment:", error);
        alert("Failed to cancel appointment. Please try again.");
      }
    }
  };

  const handleOpenRescheduleDialog = (appointment: AppointmentResponseDto) => {
    setAppointmentToReschedule(appointment);
    setNewSlotForReschedule(null);
    setIsRescheduleDialogOpen(true);
  };

  const handleConfirmReschedule = async () => {
    if (!appointmentToReschedule || !newSlotForReschedule) {
      alert("Please select a new slot to reschedule.");
      return;
    }
    try {
      const rescheduleDto: AppointmentRescheduleRequestDto = {
        appointmentId: appointmentToReschedule.appointmentId,
        newSlotId: newSlotForReschedule.slotId,
      };
      await apiService.rescheduleAppointment(rescheduleDto);
      await handleRefresh();
      setIsRescheduleDialogOpen(false);
      setAppointmentToReschedule(null);
      alert("Appointment rescheduled successfully!");
    } catch (error) {
      console.error("Error rescheduling appointment:", error);
      alert(
        "Failed to reschedule appointment. The slot may no longer be available."
      );
    }
  };

  // Filter all slots by search query only
  const filteredSlots = allSlots.filter((slot) => {
    const lowerCaseQuery = searchQuery.toLowerCase();
    const matchesSearch = searchQuery
      ? (slot.specialization &&
          slot.specialization.toLowerCase().includes(lowerCaseQuery)) ||
        slot.providerName.toLowerCase().includes(lowerCaseQuery)
      : true;
    return matchesSearch;
  });

  // Get unique providers from all slots
  const uniqueProviders = Array.from(
    new Map(
      allSlots.map((slot) => [
        slot.providerId,
        {
          id: slot.providerId,
          name: slot.providerName,
          specialization: slot.specialization,
        },
      ])
    ).values()
  );

  // NEW: Helper function to get badge styling based on status
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

  return (
    <>
      {/* Reschedule Dialog */}
      <Dialog
        open={isRescheduleDialogOpen}
        onOpenChange={setIsRescheduleDialogOpen}
      >
        <DialogContent className="bg-neutral-900 border-neutral-700 text-neutral-50">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold text-amber-400 flex items-center gap-2">
              <Pencil className="h-5 w-5" />
              Reschedule Appointment
            </DialogTitle>
            <DialogDescription className="text-neutral-400 pt-2">
              Select a new available time for your appointment with{" "}
              <span className="font-semibold text-neutral-200">
                {appointmentToReschedule?.providerName}
              </span>
              .
            </DialogDescription>
          </DialogHeader>
          <div className="py-4 space-y-4">
            <div className="p-3 bg-neutral-800 rounded-md border border-neutral-700 space-y-1">
              <Label className="text-sm text-neutral-400">Current Slot</Label>
              <p className="text-md font-semibold text-neutral-100">
                {appointmentToReschedule &&
                  format(parseISO(appointmentToReschedule.startTime), "PPP, p")}
              </p>
            </div>
            <div className="space-y-2">
              <Label htmlFor="new-slot-select" className="text-neutral-300">
                New Available Slots
              </Label>
              <Select
                onValueChange={(value) => {
                  const selected = allSlots.find(
                    (s) => s.slotId === Number.parseInt(value, 10)
                  );
                  setNewSlotForReschedule(selected || null);
                }}
              >
                <SelectTrigger
                  id="new-slot-select"
                  className="w-full bg-neutral-800 border-neutral-600"
                >
                  <SelectValue placeholder="Choose a new time slot" />
                </SelectTrigger>
                <SelectContent className="bg-neutral-800 border-neutral-700 text-neutral-50">
                  {slotsForRescheduleDialog.length > 0 ? (
                    slotsForRescheduleDialog.map((slot) => (
                      <SelectItem
                        key={slot.slotId}
                        value={String(slot.slotId)}
                        className="focus:bg-neutral-700"
                      >
                        {format(parseISO(slot.startTime), "PPP, p")}
                      </SelectItem>
                    ))
                  ) : (
                    <div className="px-4 py-2 text-neutral-400">
                      No other slots available for this provider.
                    </div>
                  )}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter className="mt-4">
            <Button
              variant="outline"
              className="bg-neutral-800 hover:bg-neutral-700"
              onClick={() => setIsRescheduleDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              className="bg-amber-500 hover:bg-amber-600 text-neutral-900"
              onClick={handleConfirmReschedule}
              disabled={!newSlotForReschedule}
            >
              Confirm Reschedule
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Main Page Structure */}
      <Dialog
        open={isBookingDialogOpen}
        onOpenChange={(open) => {
          setIsBookingDialogOpen(open);
          if (!open) {
            setSelectedProviderIdForDialog(null);
            setSelectedSlotToBook(null);
          }
        }}
      >
        <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col p-4 sm:p-6 font-sans">
          {/* Dashboard Header */}
          <div className="w-full text-center mb-8 pt-4">
            <h1 className="text-4xl font-extrabold text-neutral-50 flex items-center justify-center gap-4">
              <CalendarDays className="h-10 w-10 text-amber-400" />
              Your Dashboard
            </h1>
            <p className="text-neutral-400 mt-2 text-lg">
              Manage your appointments and discover available slots.
            </p>
            <div className="mt-4 justify-center flex">
              <Button
                variant="outline"
                className="bg-neutral-800 hover:bg-neutral-700 text-neutral-50 hover:text-amber-500 border border-neutral-700 rounded-lg px-4 py-2 text-base flex items-center gap-2 transition-colors duration-200"
                onClick={() => window.location.reload()}
                disabled={loadingAppointments || loadingSlots}
              >
                <RefreshCw
                  className={`h-5 w-5 ${
                    loadingAppointments || loadingSlots ? "animate-spin" : ""
                  }`}
                />
                Refresh Data
              </Button>
            </div>
          </div>

          {/* DEBUG INFO
          <div className="mb-4 p-4 bg-neutral-800 rounded-lg">
            <p className="text-neutral-300 text-sm">
              DEBUG: Total appointments: {userAppointments.length} | Filtered:{" "}
              {filteredAppointments.length} | Current filter: {statusFilter}
            </p>
            <p className="text-neutral-300 text-sm mt-1">
              Appointment statuses:{" "}
              {userAppointments.map((apt) => apt.status).join(", ")}
            </p>
          </div> */}

          {/* Main Content Area */}
          <div className="flex flex-col lg:flex-row gap-6 mt-6 flex-1">
            {/* View Appointments Section */}
            <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
              <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700 space-y-4 h-[150px]">
                <div className="flex justify-between items-center w-full">
                  <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left">
                    <ListChecks className="h-6 w-6 text-neutral-300" />
                    Your Appointments
                  </CardTitle>
                </div>
                {/* CHANGED: Status Filter with Radio Group */}
                <div className="space-y-3 flex flex-row">
                  {/* <div className="flex items-center gap-2">
                    <Filter className="h-4 w-4 text-neutral-400" />
                    <Label className="text-sm font-medium text-neutral-300">
                      Filter
                    </Label>
                  </div> */}
                  <RadioGroup
                    value={statusFilter}
                    onValueChange={(value: AppointmentStatusFilter) =>
                      setStatusFilter(value)
                    }
                    className="flex flex-wrap gap-4"
                  >
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value="ALL"
                        id="filter-all"
                        className="border-neutral-600 text-amber-500 focus:ring-amber-500"
                      />
                      <Label
                        htmlFor="filter-all"
                        className="text-sm text-neutral-300 cursor-pointer"
                      >
                        All
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value="BOOKED"
                        id="filter-booked"
                        className="border-neutral-600 text-amber-500 focus:ring-amber-500"
                      />
                      <Label
                        htmlFor="filter-booked"
                        className="text-sm text-neutral-300 cursor-pointer flex items-center gap-1"
                      >
                        <CheckCircle className="h-3 w-3 text-green-500" />
                        Booked
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value="QUEUED"
                        id="filter-queued"
                        className="border-neutral-600 text-amber-500 focus:ring-amber-500"
                      />
                      <Label
                        htmlFor="filter-queued"
                        className="text-sm text-neutral-300 cursor-pointer flex items-center gap-1"
                      >
                        <Clock3 className="h-3 w-3 text-purple-500" />
                        In Queue
                      </Label>
                    </div>
                    <div className="flex items-center space-x-2">
                      <RadioGroupItem
                        value="CANCELLED"
                        id="filter-cancelled"
                        className="border-neutral-600 text-amber-500 focus:ring-amber-500"
                      />
                      <Label
                        htmlFor="filter-cancelled"
                        className="text-sm text-neutral-300 cursor-pointer flex items-center gap-1"
                      >
                        <Ban className="h-3 w-3 text-red-500" />
                        Cancelled
                      </Label>
                    </div>
                  </RadioGroup>
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
                ) : filteredAppointments.length === 0 ? (
                  <div className="text-neutral-400 text-center py-10">
                    <p>
                      {statusFilter === "ALL"
                        ? "You have no appointments yet."
                        : `No ${statusFilter.toLowerCase()} appointments found.`}
                    </p>
                  </div>
                ) : (
                  <ScrollArea className="h-[calc(100vh-400px)] lg:h-[calc(100vh-350px)] pr-4">
                    <div className="space-y-4">
                      {filteredAppointments.map((appointment) => {
                        const statusBadge = getStatusBadge(appointment.status);
                        return (
                          <div
                            key={appointment.appointmentId}
                            className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-start gap-4 shadow-sm text-left relative"
                          >
                            <div className="flex-shrink-0 pt-1">
                              {appointment.status === "CANCELLED" ? (
                                <Ban className="h-5 w-5 text-red-500" />
                              ) : appointment.status === "QUEUED" ? (
                                <Clock3 className="h-5 w-5 text-purple-500" />
                              ) : (
                                <CheckCircle className="h-5 w-5 text-green-500" />
                              )}
                            </div>
                            <div className="flex-grow">
                              <p className="text-lg font-semibold text-neutral-50">
                                {appointment.providerName}
                              </p>
                              {appointment.specialization && (
                                <Badge
                                  variant="secondary"
                                  className="mt-1 mb-2 px-2 py-1 rounded-full text-xs font-medium bg-sky-500 text-blue-50"
                                >
                                  <Briefcase className="h-3 w-3 mr-1" />
                                  {appointment.specialization}
                                </Badge>
                              )}
                              <p className="text-neutral-300 text-sm flex items-center gap-1">
                                <Calendar className="h-4 w-4" />
                                {format(parseISO(appointment.startTime), "PPP")}
                              </p>
                              <p className="text-neutral-300 text-sm flex items-center gap-1">
                                <Clock className="h-4 w-4" />
                                {format(
                                  parseISO(appointment.startTime),
                                  "p"
                                )} -{" "}
                                {format(parseISO(appointment.endTime), "p")}
                              </p>
                              {/* CHANGED: Use dynamic status badge */}
                              <Badge
                                className={`mt-2 px-2 py-1 rounded-full text-xs font-medium flex items-center w-fit ${statusBadge.className}`}
                              >
                                {statusBadge.icon}
                                {statusBadge.text}
                              </Badge>
                            </div>
                            {appointment.status !== "CANCELLED" && (
                              <div className="absolute top-2 right-2 flex items-center gap-1">
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  className="text-neutral-400 hover:text-amber-500 hover:bg-neutral-700 rounded-full"
                                  onClick={() =>
                                    handleOpenRescheduleDialog(appointment)
                                  }
                                  title="Reschedule Appointment"
                                >
                                  <Pencil className="h-5 w-5" />
                                </Button>
                                <Button
                                  variant="ghost"
                                  size="icon"
                                  className="text-neutral-400 hover:text-red-500 hover:bg-neutral-700 rounded-full"
                                  onClick={() =>
                                    handleCancelAppointment(appointment)
                                  }
                                  title="Cancel Appointment"
                                >
                                  <Trash2 className="h-5 w-5" />
                                </Button>
                              </div>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </ScrollArea>
                )}
              </CardContent>
            </Card>

            {/* View Slots Section */}
            <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
              <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700 space-y-4">
                <div className="flex justify-between items-center w-full">
                  <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left">
                    <Clock4 className="h-6 w-6 text-neutral-300" />
                    All Slots
                  </CardTitle>
                  <Button
                    variant="outline"
                    className="bg-amber-500 hover:bg-amber-600 text-neutral-900 font-semibold py-2 rounded-md transition-colors duration-200"
                    onClick={() => {
                      setSelectedProviderIdForDialog(null);
                      setSelectedSlotToBook(null);
                      setIsBookingDialogOpen(true);
                    }}
                  >
                    <CalendarPlus className="h-4 w-4 mr-1" /> Book
                  </Button>
                </div>
                <div className="relative w-full flex justify-center">
                  <div className="relative w-full sm:w-96">
                    <Input
                      type="text"
                      placeholder="Search by specialization or provider..."
                      className="pl-10 bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 focus:border-amber-500 focus:ring-1 focus:ring-amber-500 h-11 rounded-lg"
                      value={searchQuery}
                      onChange={(e) => setSearchQuery(e.target.value)}
                    />
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-5 w-5 text-neutral-400" />
                  </div>
                </div>
              </CardHeader>
              <CardContent className="flex-1 p-6">
                {loadingSlots ? (
                  <div className="text-neutral-400 text-center py-10">
                    Loading slots...
                  </div>
                ) : errorSlots ? (
                  <div className="text-red-500 text-center py-10">
                    {errorSlots}
                  </div>
                ) : filteredSlots.length === 0 ? (
                  <div className="text-neutral-400 text-center py-10">
                    <p>No slots found matching your criteria.</p>
                  </div>
                ) : (
                  <ScrollArea className="h-[calc(100vh-350px)] lg:h-[calc(100vh-300px)] pr-4">
                    <div className="space-y-4">
                      {filteredSlots.map((slot) => (
                        <div
                          key={slot.slotId}
                          className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-start gap-4 shadow-sm text-left"
                        >
                          <div className="flex-shrink-0 pt-1">
                            <Info className="h-5 w-5 text-blue-400" />
                          </div>
                          <div className="flex-grow">
                            <p className="text-lg font-semibold text-neutral-50">
                              {slot.providerName}
                            </p>
                            {slot.specialization && (
                              <Badge
                                variant="secondary"
                                className="mt-1 mb-2 px-2 py-1 rounded-full text-xs font-medium bg-neutral-600 text-neutral-50"
                              >
                                <Briefcase className="h-3 w-3 mr-1" />
                                {slot.specialization}
                              </Badge>
                            )}
                            <p className="text-neutral-300 text-sm flex items-center gap-1">
                              <Calendar className="h-4 w-4" />
                              {format(parseISO(slot.startTime), "PPP")}
                            </p>
                            <p className="text-neutral-300 text-sm flex items-center gap-1">
                              <Clock className="h-4 w-4" />
                              {format(parseISO(slot.startTime), "p")} -{" "}
                              {format(parseISO(slot.endTime), "p")}
                            </p>
                            {slot.booked ? (
                              <Badge
                                variant="secondary"
                                className="mt-2 px-2 py-1 rounded-full text-xs font-medium bg-yellow-600 text-yellow-50 flex items-center gap-1 w-fit"
                              >
                                <Users className="h-3 w-3" />
                                Booked
                              </Badge>
                            ) : (
                              <Badge
                                variant="default"
                                className="mt-2 px-2 py-1 rounded-full text-xs font-medium bg-green-600 text-white flex items-center gap-1 w-fit"
                              >
                                <CheckCircle className="h-3 w-3" /> Available
                              </Badge>
                            )}
                          </div>
                        </div>
                      ))}
                    </div>
                  </ScrollArea>
                )}
              </CardContent>
            </Card>
          </div>
        </div>

        {/* FIXED: Booking Dialog Content - Better Layout */}
        <DialogContent className="bg-neutral-900 border-neutral-700 text-neutral-50 max-w-md w-full mx-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold text-amber-400">
              Book an Appointment
            </DialogTitle>
            <DialogDescription className="text-neutral-400 pt-2">
              Select a provider and a time slot.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-6 py-4">
            <div className="space-y-2">
              <Label
                htmlFor="provider-select"
                className="text-neutral-300 font-medium"
              >
                Provider
              </Label>
              <Select
                onValueChange={(value) =>
                  setSelectedProviderIdForDialog(Number(value))
                }
              >
                <SelectTrigger
                  id="provider-select"
                  className="w-full bg-neutral-800 border-neutral-600"
                >
                  <SelectValue placeholder="Select a provider" />
                </SelectTrigger>
                <SelectContent className="bg-neutral-800 border-neutral-700 text-neutral-50">
                  {uniqueProviders.map((provider) => (
                    <SelectItem
                      key={provider.id}
                      value={String(provider.id)}
                      className="focus:bg-neutral-700"
                    >
                      {provider.name} - {provider.specialization}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label
                htmlFor="slot-select"
                className="text-neutral-300 font-medium"
              >
                Time Slot
              </Label>
              <Select
                onValueChange={(value) => {
                  const selected = allSlots.find(
                    (s) => s.slotId === Number.parseInt(value, 10)
                  );
                  setSelectedSlotToBook(selected || null);
                }}
                disabled={!selectedProviderIdForDialog}
              >
                <SelectTrigger
                  id="slot-select"
                  className="w-full bg-neutral-800 border-neutral-600"
                >
                  <SelectValue placeholder="Select a time slot" />
                </SelectTrigger>
                <SelectContent className="bg-neutral-800 border-neutral-700 text-neutral-50">
                  {slotsForDialog.map((slot) => (
                    <SelectItem
                      key={slot.slotId}
                      value={String(slot.slotId)}
                      className="focus:bg-neutral-700"
                    >
                      <div className="flex items-center justify-between w-full">
                        <span>
                          {format(parseISO(slot.startTime), "PPP, p")}
                        </span>
                        {slot.booked && (
                          <Badge className="ml-2 bg-yellow-600 text-yellow-50 text-xs">
                            Booked
                          </Badge>
                        )}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Warning for booked slots */}
            {selectedSlotToBook && selectedSlotToBook.booked && (
              <div className="p-3 bg-yellow-900/20 border border-yellow-600/50 rounded-md">
                <div className="flex items-center gap-2 text-yellow-400">
                  <AlertTriangle className="h-4 w-4 flex-shrink-0" />
                  <span className="text-sm font-medium">Queue Warning</span>
                </div>
                <p className="text-yellow-300 text-sm mt-1">
                  This slot is already booked. By booking this slot, you will be
                  entering a queue.
                </p>
              </div>
            )}
          </div>
          <DialogFooter className="mt-4">
            <Button
              variant="outline"
              className="bg-neutral-800 hover:bg-neutral-700"
              onClick={() => setIsBookingDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button
              className="bg-amber-500 hover:bg-amber-600 text-neutral-900"
              onClick={handleBookAppointment}
              disabled={!selectedSlotToBook}
            >
              {selectedSlotToBook && selectedSlotToBook.booked
                ? "Join Queue"
                : "Confirm Booking"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Cancellation Confirmation Dialog */}
      <Dialog open={isCancelConfirmOpen} onOpenChange={setIsCancelConfirmOpen}>
        <DialogContent className="bg-neutral-900 border-neutral-700 text-neutral-50">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold text-red-500 flex items-center gap-2">
              <Trash2 className="h-5 w-5" />
              Confirm Cancellation
            </DialogTitle>
            <DialogDescription className="text-neutral-400 pt-2">
              Are you sure you want to cancel your appointment with{" "}
              <span className="font-semibold text-neutral-200">
                {appointmentToCancel?.providerName}
              </span>{" "}
              on{" "}
              <span className="font-semibold text-neutral-200">
                {appointmentToCancel &&
                  format(parseISO(appointmentToCancel.startTime), "PPP")}
              </span>
              ?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="mt-4">
            <Button
              variant="outline"
              className="bg-neutral-800 hover:bg-neutral-700"
              onClick={() => setIsCancelConfirmOpen(false)}
            >
              Back
            </Button>
            <Button
              className="bg-red-600 hover:bg-red-700 text-white"
              onClick={confirmCancelAppointment}
            >
              Yes, Cancel Appointment
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
