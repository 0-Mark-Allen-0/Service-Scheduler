/* eslint-disable @typescript-eslint/no-unused-vars */
// src/pages/UserDashboard.tsx
import React, { useState, useEffect, useCallback } from "react";
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
} from "@/components/ui/dialog";
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
  Pencil, // Import Pencil Icon for rescheduling
  Info, // Import Info icon from old design
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

export default function UserDashboardPage() {
  // State variables
  const [availableSlots, setAvailableSlots] = useState<SlotResponseDto[]>([]);
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
      setUserAppointments(appointments);
    } catch (error) {
      console.error("Error fetching user appointments:", error);
      setErrorAppointments("Failed to load your appointments.");
    } finally {
      setLoadingAppointments(false);
    }
  }, []);

  const fetchAllAvailableSlots = useCallback(async () => {
    setLoadingSlots(true);
    setErrorSlots(null);
    try {
      const slots = await apiService.viewAllSlots();
      setAvailableSlots(slots);
    } catch (error) {
      console.error("Error fetching available slots:", error);
      setErrorSlots("Failed to load available slots.");
    } finally {
      setLoadingSlots(false);
    }
  }, []);

  const handleRefresh = useCallback(async () => {
    await Promise.all([fetchUserAppointments(), fetchAllAvailableSlots()]);
  }, [fetchUserAppointments, fetchAllAvailableSlots]);

  useEffect(() => {
    handleRefresh();
  }, [handleRefresh]);

  // --- Filter Logic ---

  // Filter availableSlots to only show slots that are NOT booked
  const unbookedAvailableSlots = availableSlots.filter(
    (slot) => !slot.isBooked
  );

  // Filter slots for the initial booking dialog
  const slotsForDialog = selectedProviderIdForDialog
    ? unbookedAvailableSlots.filter(
        (slot) => slot.providerId === selectedProviderIdForDialog
      )
    : [];

  // Filter slots for the reschedule dialog.
  // Since AppointmentResponseDto lacks a providerId, we use the providerName
  // from the appointment to find matching slots from the main availableSlots list.
  const slotsForRescheduleDialog = appointmentToReschedule
    ? unbookedAvailableSlots.filter(
        (slot) => slot.providerName === appointmentToReschedule.providerName
      )
    : [];

  // --- Action Handlers ---
  const handleBookAppointment = async () => {
    if (selectedSlotToBook && selectedProviderIdForDialog) {
      try {
        const requestDto: AppointmentRequestDto = {
          userId: 0,
          providerId: selectedProviderIdForDialog,
          slotId: selectedSlotToBook.slotId,
        };
        await apiService.bookAppointment(requestDto);
        await handleRefresh();
        setIsBookingDialogOpen(false);
        alert("Appointment booked successfully!");
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

  const filteredAvailableSlots = unbookedAvailableSlots.filter((slot) => {
    const lowerCaseQuery = searchQuery.toLowerCase();
    const matchesSearch = searchQuery
      ? (slot.specialization &&
          slot.specialization.toLowerCase().includes(lowerCaseQuery)) ||
        slot.providerName.toLowerCase().includes(lowerCaseQuery)
      : true;
    return matchesSearch;
  });

  const uniqueProviders = Array.from(
    new Map(
      unbookedAvailableSlots.map((slot) => [
        slot.providerId,
        {
          id: slot.providerId,
          name: slot.providerName,
          specialization: slot.specialization,
        },
      ])
    ).values()
  );

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
                  const selected = availableSlots.find(
                    (s) => s.slotId === parseInt(value, 10)
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

          {/* Main Content Area */}
          <div className="flex flex-col lg:flex-row gap-6 mt-6 flex-1">
            {/* View Appointments Section */}
            <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
              <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700 space-y-4 min-h-[100px]">
                <div className="flex justify-between items-center w-full">
                  <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left h-[97px]">
                    <ListChecks className="h-6 w-6 text-neutral-300" />
                    Your Appointments
                  </CardTitle>
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
                ) : userAppointments.length === 0 ? (
                  <div className="text-neutral-400 text-center py-10">
                    <p>You have no appointments booked yet.</p>
                  </div>
                ) : (
                  <ScrollArea className="h-[calc(100vh-350px)] lg:h-[calc(100vh-300px)] pr-4">
                    <div className="space-y-4">
                      {userAppointments.map((appointment) => (
                        <div
                          key={appointment.appointmentId}
                          className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-start gap-4 shadow-sm text-left relative"
                        >
                          <div className="flex-shrink-0 pt-1">
                            {appointment.status === "CANCELLED" ? (
                              <Ban className="h-5 w-5 text-red-500" />
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
                              )} - {format(parseISO(appointment.endTime), "p")}
                            </p>
                            <Badge
                              className={`mt-2 px-2 py-1 rounded-full text-xs font-medium ${
                                appointment.status === "CANCELLED"
                                  ? "bg-red-600 text-red-50"
                                  : "bg-green-600 text-green-50"
                              }`}
                            >
                              {appointment.status === "CANCELLED"
                                ? "Cancelled"
                                : "Booked"}
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
                      ))}
                    </div>
                  </ScrollArea>
                )}
              </CardContent>
            </Card>

            {/* View Slots Section - Refactored to old design with new logic */}
            <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
              <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700 space-y-4">
                {/* Top Row: Title on left, Book button on right */}
                <div className="flex justify-between items-center w-full">
                  <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left">
                    <Clock4 className="h-6 w-6 text-neutral-300" />
                    Available Slots
                  </CardTitle>
                  {/* The DialogTrigger wraps the button to open the booking dialog */}
                  <Button
                    variant="outline"
                    className="bg-amber-500 hover:bg-amber-600 text-neutral-900 font-semibold py-2 rounded-md transition-colors duration-200"
                    onClick={() => {
                      setSelectedProviderIdForDialog(null); // Reset selection when opening from main button
                      setSelectedSlotToBook(null); // Reset selection
                      setIsBookingDialogOpen(true); // Open the dialog explicitly
                    }}
                  >
                    <CalendarPlus className="h-4 w-4 mr-1" /> Book
                  </Button>
                </div>

                {/* Bottom Row: Centered Search */}
                <div className="relative w-full flex justify-center">
                  <div className="relative w-full sm:w-96">
                    <Input
                      type="text"
                      placeholder="Search by specialization or provider..." // Updated placeholder for better search hint
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
                    Loading available slots...
                  </div>
                ) : errorSlots ? (
                  <div className="text-red-500 text-center py-10">
                    {errorSlots}
                  </div>
                ) : filteredAvailableSlots.length === 0 ? (
                  <div className="text-neutral-400 text-center py-10">
                    <p>No available slots found matching your criteria.</p>
                  </div>
                ) : (
                  <ScrollArea className="h-[calc(100vh-350px)] lg:h-[calc(100vh-300px)] pr-4">
                    <div className="space-y-4">
                      {filteredAvailableSlots.map((slot) => (
                        <div
                          key={slot.slotId} // Use backend ID
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
                            <Badge
                              variant="default" // Or a custom variant for green
                              className="mt-2 px-2 py-1 rounded-full text-xs font-medium bg-green-600 text-white flex items-center gap-1" // Green background, white text
                            >
                              <CheckCircle className="h-3 w-3" /> Available
                            </Badge>
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

        {/* Booking Dialog Content (remains the same from new version) */}
        <DialogContent className="bg-neutral-900 border-neutral-700 text-neutral-50">
          <DialogHeader>
            <DialogTitle className="text-xl font-bold text-amber-400">
              Book an Appointment
            </DialogTitle>
            <DialogDescription className="text-neutral-400 pt-2">
              Select a provider and an available time slot.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="provider-select" className="text-right">
                Provider
              </Label>
              <Select
                onValueChange={(value) =>
                  setSelectedProviderIdForDialog(Number(value))
                }
              >
                <SelectTrigger
                  id="provider-select"
                  className="col-span-3 bg-neutral-800 border-neutral-600"
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
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="slot-select" className="text-right">
                Time Slot
              </Label>
              <Select
                onValueChange={(value) => {
                  const selected = availableSlots.find(
                    (s) => s.slotId === parseInt(value, 10)
                  );
                  setSelectedSlotToBook(selected || null);
                }}
                disabled={!selectedProviderIdForDialog}
              >
                <SelectTrigger
                  id="slot-select"
                  className="col-span-3 bg-neutral-800 border-neutral-600"
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
                      {format(parseISO(slot.startTime), "PPP, p")}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button
              className="bg-amber-500 hover:bg-amber-600 text-neutral-900"
              onClick={handleBookAppointment}
              disabled={!selectedSlotToBook}
            >
              Confirm Booking
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Cancellation Confirmation Dialog (remains the same from new version) */}
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
