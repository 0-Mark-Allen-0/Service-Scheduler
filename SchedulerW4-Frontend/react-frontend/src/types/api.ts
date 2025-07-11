//First Set of DTOs:
//Registration DTOs:
export interface UserRegistrationDto {
  name: string
  email: string
  password: string
}

export interface ProviderRegistrationDto {
  name: string
  email: string
  password: string
  specialization: string
}

export type UserRole = "USER" | "PROVIDER" | "ADMIN"

export type LoginRequestDto = {
  email: string
  password: string
}

export interface UserResponseDto {
  id: number
  name: string
  email: string
}

export interface ProviderResponseDto {
  id: number
  name: string
  email: string
  specialization: string
}

export type LoginResponseDto = {
  message: string
  role?: UserRole // Make optional as it's null when OTP is required
  token?: string // Make optional as it's null when OTP is required
  otpRequired: boolean // NEW
  email?: string // NEW: The email for which OTP was sent
}

//New: OTP service DTO:
export type OtpRequestDto = {
  email: string
  otp: string
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
  appointmentId: number
}

//2. AppointmentRequestDto
export interface AppointmentRequestDto {
  userId: number
  providerId: number
  slotId: number
}

//3. AppointmentResponseDto
export interface AppointmentResponseDto {
  appointmentId: number
  slotId: number
  userName: string
  providerId: number
  providerName: string
  specialization: string
  startTime: string
  endTime: string
  status: string
}

//5. SlotResponseDto - FIXED: Changed from "isBooked" to "booked" to match backend
export interface SlotResponseDto {
  slotId: number
  startTime: string
  endTime: string
  booked: boolean // CHANGED: From "isBooked" to "booked" to match backend JSON
  providerId: number
  providerName: string
  specialization: string
}

//6. MessageDto
export interface MessageDto {
  message: string
}

//7. AppointmentRescheduleRequestDto
export interface AppointmentRescheduleRequestDto {
  appointmentId: number
  newSlotId: number
}

//8. For Queuing:
// NEW: Booking Status Enum (TypeScript type literal union)
export type BookingStatus = "BOOKED" | "QUEUED" | "ALREADY_QUEUED" | "FAILED"

// NEW: Booking Response DTO
export interface BookingResponseDto {
  status: BookingStatus
  message: string
  appointment?: AppointmentResponseDto // Will be present only if status is BOOKED
  queuedSlotId?: number // Will be present if status is QUEUED or ALREADY_QUEUED
}

//For Provider Controller: (only unique DTOs)
//1. SlotRequestDto
//2. SlotDeleteDto

//1. SlotRequestDto
export interface SlotRequestDto {
  startTime: string
}

//2. SlotDeleteDto
export interface SlotDeleteDto {
  slotId: number
}

//For Admin Controller: (only unique DTOs)
//1. AdminStatsDto
export interface AdminStatsDto {
  totalAppointmentsPerProvider: { [providerName: string]: number }
  cancellationRates: { [providerName: string]: number }
  peakBookingHours: { [hour: string]: number }
  totalUsers: number // NEW
  totalProviders: number // NEW
}

// Health Metrics DTOs - Properly typed to avoid 'any'
export interface DatabaseHealth {
  status: string
  details?: {
    database?: string
    validationQuery?: string
  }
}

export interface RedisHealth {
  status: string
  details?: {
    version?: string
  }
}

export interface DiskSpaceHealth {
  status: string
  details?: {
    total?: number
    free?: number
    threshold?: number
    exists?: boolean
  }
}

export interface JvmMetric {
  name: string
  description?: string
  baseUnit?: string
  measurements: Array<{
    statistic: string
    value: number
  }>
  availableTags: Array<{
    tag: string
    values: string[]
  }>
}

export interface HttpRequestsMetric {
  name: string
  description?: string
  baseUnit?: string
  measurements: Array<{
    statistic: string
    value: number
  }>
  availableTags: Array<{
    tag: string
    values: string[]
  }>
}

export interface HealthMetricsDto {
  overallStatus: string
  database?: DatabaseHealth
  redis?: RedisHealth
  diskSpace?: DiskSpaceHealth
  jvmMemoryUsed?: JvmMetric
  jvmMemoryMax?: JvmMetric
  httpRequests?: HttpRequestsMetric
  metricsError?: string
}

export interface ApiError {
  message: string
  errors?: { [key: string]: string }
}
