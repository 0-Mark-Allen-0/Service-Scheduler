// src/pages/ProviderDashboard.tsx
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
import { Label } from "@/components/ui/label";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import {
  CalendarDays,
  Clock,
  Briefcase,
  ListChecks,
  PlusCircle,
  Trash2,
  CalendarCheck,
  Info,
  User,
  Calendar,
  Clock4,
  Ban,
  CalendarPlus,
  RefreshCw,
} from "lucide-react";
import { format, parseISO } from "date-fns";

// IMPORTS FROM api.ts and types/api.ts
import { apiService } from "@/services/api";
import type {
  AppointmentResponseDto,
  SlotResponseDto,
  SlotRequestDto,
  SlotDeleteDto,
  MessageDto,
} from "@/types/api";

export default function ProviderDashboardPage() {
  const [enrolledSlots, setEnrolledSlots] = useState<SlotResponseDto[]>([]);
  const [providerAppointments, setProviderAppointments] = useState<
    AppointmentResponseDto[]
  >([]);

  const [newSlotTime, setNewSlotTime] = useState<string>("");
  const [isAddSlotDialogOpen, setIsAddSlotDialogOpen] = useState(false);

  const [slotToDelete, setSlotToDelete] = useState<SlotResponseDto | null>(
    null
  );
  const [isDeleteSlotConfirmOpen, setIsDeleteSlotConfirmOpen] = useState(false);

  const [loadingEnrolledSlots, setLoadingEnrolledSlots] = useState(true);
  const [errorEnrolledSlots, setErrorEnrolledSlots] = useState<string | null>(
    null
  );
  const [loadingProviderAppointments, setLoadingProviderAppointments] =
    useState(true);
  const [errorProviderAppointments, setErrorProviderAppointments] = useState<
    string | null
  >(null);

  // --- Fetching Data from Backend ---

  const fetchEnrolledSlots = useCallback(async () => {
    setLoadingEnrolledSlots(true);
    setErrorEnrolledSlots(null);
    try {
      const slots = await apiService.getEnrolledSlotsByProvider();
      setEnrolledSlots(
        slots.sort(
          (a, b) =>
            new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
        )
      );
    } catch (error) {
      console.error("Error fetching enrolled slots:", error);
      setErrorEnrolledSlots("Failed to load your slots.");
    } finally {
      setLoadingEnrolledSlots(false);
    }
  }, []);

  const fetchProviderAppointments = useCallback(async () => {
    setLoadingProviderAppointments(true);
    setErrorProviderAppointments(null);
    try {
      const appointments = await apiService.viewProviderAppointments();
      setProviderAppointments(
        appointments.sort(
          (a, b) =>
            new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
        )
      );
    } catch (error) {
      console.error("Error fetching provider appointments:", error);
      setErrorProviderAppointments("Failed to load your appointments.");
    } finally {
      setLoadingProviderAppointments(false);
    }
  }, []);

  useEffect(() => {
    fetchEnrolledSlots();
    fetchProviderAppointments();
  }, [fetchEnrolledSlots, fetchProviderAppointments]);

  // --- Handlers for Provider Actions ---

  const handleAddSlot = async () => {
    if (!newSlotTime) {
      alert("Please select a time for the new slot.");
      return;
    }

    try {
      const requestDto: SlotRequestDto = {
        startTime: newSlotTime,
      };

      await apiService.addSlot(requestDto);

      fetchEnrolledSlots();
      fetchProviderAppointments();

      setIsAddSlotDialogOpen(false);
      setNewSlotTime("");
      alert("Slot added successfully!");
    } catch (error) {
      console.error("Error adding slot:", error);
      alert("Failed to add slot. Please try again.");
    }
  };

  const handleDeleteSlot = (slot: SlotResponseDto) => {
    if (slot.booked) {
      alert("Cannot delete a booked slot.");
      return;
    }
    setSlotToDelete(slot);
    setIsDeleteSlotConfirmOpen(true);
  };

  const confirmDeleteSlot = async () => {
    if (slotToDelete) {
      try {
        const deleteDto: SlotDeleteDto = {
          slotId: slotToDelete.slotId,
        };

        const messageResponse: MessageDto = await apiService.deleteSlot(
          deleteDto
        );

        fetchEnrolledSlots();
        fetchProviderAppointments();

        setIsDeleteSlotConfirmOpen(false);
        setSlotToDelete(null);
        alert(messageResponse.message);
      } catch (error) {
        console.error("Error deleting slot:", error);
        alert("This Slot is already booked. Cannot be deleted");
      }
    }
  };

  return (
    <div className="min-h-screen bg-neutral-950 text-neutral-100 flex flex-col p-4 sm:p-6 font-sans">
      {/* Dashboard Header */}
      <div className="w-full text-center mb-8 pt-4">
        <h1 className="text-4xl font-extrabold text-neutral-50 flex items-center justify-center gap-4">
          <CalendarDays className="h-10 w-10 text-indigo-400" />
          Provider Dashboard
        </h1>
        <p className="text-neutral-400 mt-2 text-lg">
          Manage your available slots and view your appointments.
        </p>
        {/* --- MOVED: Manual Refresh Button Here --- */}
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

      {/* Main Content Area: Enrolled Slots (Left) and Appointments (Right) */}
      <div className="flex flex-col lg:flex-row gap-6 mt-6 flex-1">
        {/* Enrolled Slots Section (Left) */}
        <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
          <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700 space-y-4 min-h-[135px]">
            <div className="flex justify-between items-center w-full">
              <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left">
                <Clock4 className="h-6 w-6 text-neutral-300" />
                Your Enrolled Slots
              </CardTitle>
              <div className="flex gap-2">
                <Dialog
                  open={isAddSlotDialogOpen}
                  onOpenChange={setIsAddSlotDialogOpen}
                >
                  <DialogTrigger asChild>
                    <Button
                      variant="outline"
                      className="bg-indigo-400 hover:bg-indigo-500 text-neutral-900 font-semibold py-2 rounded-md transition-colors duration-200"
                      onClick={() => setIsAddSlotDialogOpen(true)}
                    >
                      <PlusCircle className="h-4 w-4 mr-1" /> Add New Slot
                    </Button>
                  </DialogTrigger>
                  <DialogContent className="sm:max-w-[425px] bg-neutral-900 border border-neutral-700 text-neutral-100 rounded-lg shadow-xl">
                    <DialogHeader className="border-b border-neutral-700 pb-4">
                      <DialogTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3">
                        <CalendarPlus className="h-6 w-6 text-indigo-400" />
                        Add New Availability Slot
                      </DialogTitle>
                      <DialogDescription className="text-neutral-400 mt-2">
                        Enter the date and time for your new availability slot.
                      </DialogDescription>
                    </DialogHeader>
                    <div className="py-6 space-y-6">
                      <div>
                        <Label
                          htmlFor="new-slot-time"
                          className="text-neutral-300 mb-2 block font-medium text-left"
                        >
                          Start Time
                        </Label>
                        <Input
                          id="new-slot-time"
                          type="datetime-local"
                          value={newSlotTime}
                          onChange={(e) => setNewSlotTime(e.target.value)}
                          className="bg-neutral-800 border border-neutral-700 text-neutral-50 placeholder:text-neutral-500 focus:border-indigo-400 focus:ring-1 focus:ring-indigo-400 h-11 rounded-lg"
                        />
                      </div>
                    </div>
                    <DialogFooter className="flex justify-end gap-2 border-t border-neutral-700 pt-4">
                      <Button
                        variant="ghost"
                        onClick={() => setIsAddSlotDialogOpen(false)}
                        className="text-neutral-300 hover:bg-neutral-700"
                      >
                        Cancel
                      </Button>
                      <Button
                        type="submit"
                        onClick={handleAddSlot}
                        className="bg-indigo-500 hover:bg-indigo-700 text-white font-semibold"
                      >
                        Add Slot
                      </Button>
                    </DialogFooter>
                  </DialogContent>
                </Dialog>
              </div>
            </div>
          </CardHeader>
          <CardContent className="flex-1 p-6">
            {loadingEnrolledSlots ? (
              <div className="text-neutral-400 text-center py-10">
                Loading your slots...
              </div>
            ) : errorEnrolledSlots ? (
              <div className="text-red-500 text-center py-10">
                {errorEnrolledSlots}
              </div>
            ) : enrolledSlots.length === 0 ? (
              <div className="text-neutral-400 text-center py-10">
                <p>You have not added any availability slots yet.</p>
                <p>Click "Add New Slot" to get started!</p>
              </div>
            ) : (
              <ScrollArea className="h-[calc(100vh-350px)] lg:h-[calc(100vh-300px)] pr-4">
                <div className="space-y-4">
                  {enrolledSlots.map((slot) => (
                    <div
                      key={slot.slotId}
                      className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-start gap-4 shadow-sm text-left relative"
                    >
                      <div className="flex-shrink-0 pt-1">
                        <Info className="h-5 w-5 text-blue-400" />
                      </div>
                      <div className="flex-grow">
                        {slot.specialization && (
                          <Badge
                            variant="secondary"
                            className="mt-1 mb-2 px-2 py-1 rounded-full text-xs font-medium bg-neutral-600 text-neutral-50"
                          >
                            <Briefcase className="h-3 w-3 mr-1" />
                            {slot.specialization}
                          </Badge>
                        )}
                        <p className="text-neutral-300 flex items-center gap-1 text-lg pb-2">
                          <Calendar className="h-4 w-4" />
                          {format(parseISO(slot.startTime), "PPP")}
                        </p>
                        <Badge
                          variant="outline"
                          className="mt-1 px-2 py-1 rounded-full text-sm font-medium bg-neutral-700 text-neutral-50 flex items-center gap-1 border-neutral-600"
                        >
                          <Clock className="h-3 w-3" />
                          {format(parseISO(slot.startTime), "p")} -{" "}
                          {format(parseISO(slot.endTime), "p")}
                        </Badge>
                      </div>
                      {!slot.booked && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="absolute top-3 right-3 text-neutral-400 hover:text-red-500 hover:bg-neutral-700 rounded-full"
                          onClick={() => handleDeleteSlot(slot)}
                          title="Delete Slot"
                        >
                          <Trash2 className="h-5 w-5" />
                        </Button>
                      )}
                    </div>
                  ))}
                </div>
              </ScrollArea>
            )}
          </CardContent>
        </Card>

        {/* View Appointments Section (Right) */}
        <Card className="flex-1 bg-neutral-900 border border-neutral-800 shadow-lg rounded-xl flex flex-col">
          <CardHeader className="pb-4 pt-6 px-6 bg-neutral-800 border-b border-neutral-700 space-y-4">
            <CardTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3 text-left py-6">
              <ListChecks className="h-6 w-6 text-neutral-300" />
              Your Booked Appointments
            </CardTitle>
          </CardHeader>
          <CardContent className="flex-1 p-6">
            {loadingProviderAppointments ? (
              <div className="text-neutral-400 text-center py-10">
                Loading appointments...
              </div>
            ) : errorProviderAppointments ? (
              <div className="text-red-500 text-center py-10">
                {errorProviderAppointments}
              </div>
            ) : providerAppointments.length === 0 ? (
              <div className="text-neutral-400 text-center py-10">
                <p>You have no appointments booked by users yet.</p>
              </div>
            ) : (
              <ScrollArea className="h-[calc(100vh-350px)] lg:h-[calc(100vh-300px)] pr-4">
                <div className="space-y-4">
                  {providerAppointments.map((appointment) => (
                    <div
                      key={appointment.appointmentId}
                      className="bg-neutral-800 border border-neutral-700 rounded-lg p-4 flex items-start gap-4 shadow-sm text-left relative"
                    >
                      <div className="flex-shrink-0 pt-1">
                        {appointment.status === "CANCELLED" ? (
                          <Ban className="h-5 w-5 text-red-500" />
                        ) : (
                          <CalendarCheck className="h-5 w-5 text-green-500" />
                        )}
                      </div>
                      <div className="flex-grow">
                        <p className="text-lg font-semibold text-neutral-50">
                          <User className="inline-block h-4 w-4 mr-1" />
                          {appointment.userName}
                        </p>
                        <p className="text-neutral-300 text-sm flex items-center gap-1 mt-1">
                          <Calendar className="h-4 w-4" />
                          {format(parseISO(appointment.startTime), "PPP")}
                        </p>
                        <p className="text-neutral-300 text-sm flex items-center gap-1">
                          <Clock className="h-4 w-4" />
                          {format(parseISO(appointment.startTime), "p")} -{" "}
                          {format(parseISO(appointment.endTime), "p")}
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
                    </div>
                  ))}
                </div>
              </ScrollArea>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Delete Slot Confirmation Dialog */}
      <Dialog
        open={isDeleteSlotConfirmOpen}
        onOpenChange={setIsDeleteSlotConfirmOpen}
      >
        <DialogContent className="sm:max-w-[425px] bg-neutral-900 border border-neutral-700 text-neutral-100 rounded-lg shadow-xl">
          <DialogHeader className="border-b border-neutral-700 pb-4">
            <DialogTitle className="text-2xl font-bold text-neutral-50 flex items-center gap-3">
              <Trash2 className="h-6 w-6 text-red-400" />
              Confirm Slot Deletion
            </DialogTitle>
            <DialogDescription className="text-neutral-400 mt-2">
              Are you sure you want to delete this slot? This action cannot be
              undone.
              {slotToDelete && (
                <p className="mt-2 text-sm text-neutral-300">
                  Slot:{" "}
                  <strong>
                    {format(parseISO(slotToDelete.startTime), "PPP p")}
                  </strong>
                </p>
              )}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="flex justify-end gap-2 border-t border-neutral-700 pt-4">
            <Button
              variant="ghost"
              onClick={() => setIsDeleteSlotConfirmOpen(false)}
              className="text-neutral-300 hover:bg-neutral-700"
            >
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={confirmDeleteSlot}
              className="bg-red-600 hover:bg-red-700 text-white font-semibold"
            >
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
