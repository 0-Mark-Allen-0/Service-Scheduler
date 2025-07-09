//First Set of DTOs:

//Registration DTOs:

export interface UserRegistrationDto {
  name: string;
  email: string;
  password: string;
}

export interface ProviderRegistrationDto {
  name: string;
  email: string;
  password: string;
  specialization: string;
}

export type UserRole = "USER" | "PROVIDER" | "ADMIN";

export interface LoginRequestDto {
  email: string;
  password: string;
}

export interface UserResponseDto {
  id: number;
  name: string;
  email: string;
}

export interface ProviderResponseDto {
  id: number;
  name: string;
  email: string;
  specialization: string;
}

export interface LoginResponseDto {
  message: string;
  role: UserRole;
  token: string;
}

//For User Controller:
// 1. AppointmentCancelDto
// 2. AppointmentRequestDto
// 3. AppointmentResponseDto
// 4. UserResponseDto -- already defined
// 5. SlotResponseDto
// 6. MessageDto
// 7. AppointmentRescheduleRequestDto

//1. AppointmentCancelDto
export interface AppointmentCancelDto {
  appointmentId: number;
}

//2. AppointmentRequestDto
export interface AppointmentRequestDto {
  userId: number;
  providerId: number;
  slotId: number;
}

//3. AppointmentResponseDto
export interface AppointmentResponseDto {
  appointmentId: number;
  slotId: number;
  userName: string;
  providerId: number;
  providerName: string;
  specialization: string;
  startTime: string;
  endTime: string;
  status: string;
}

//5. SlotResponseDto
export interface SlotResponseDto {
  slotId: number;
  startTime: string;
  endTime: string;
  isBooked: boolean;
  providerId: number;
  providerName: string;
  specialization: string;
}

//6. MessageDto
export interface MessageDto {
  message: string;
}

//7. AppointmentRescheduleRequestDto
export interface AppointmentRescheduleRequestDto {
  appointmentId: number;
  newSlotId: number;
}

//For Provider Controller: (only unique DTOs)
//1. SlotRequestDto
//2. SlotDeleteDto

//1. SlotRequestDto
export interface SlotRequestDto {
  startTime: string;
}

//2. SlotDeleteDto
export interface SlotDeleteDto {
  slotId: number;
}

//For Admin Controller: (only unique DTOs)

//1. AdminStatsDto
export interface AdminStatsDto {
  totalAppointmentsPerProvider: { [providerName: string]: number };
  cancellationRates: { [providerName: string]: number };
  peakBookingHours: { [hour: string]: number };
}
export interface ApiError {
  message: string;
  errors?: { [key: string]: string };
}