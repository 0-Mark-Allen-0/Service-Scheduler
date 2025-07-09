// src/services/api.ts
import type {
  LoginRequestDto,
  LoginResponseDto,
  ProviderRegistrationDto,
  ProviderResponseDto,
  UserRegistrationDto,
  UserResponseDto,
  AppointmentRequestDto,
  AppointmentResponseDto,
  AppointmentCancelDto,
  MessageDto,
  SlotResponseDto,
  SlotRequestDto,
  SlotDeleteDto,
  AdminStatsDto,
  AppointmentRescheduleRequestDto
  // UserRole,
} from "@/types/api";

const AUTH_BASE_URL = "http://localhost:8080/auth";
const USER_BASE_URL = "http://localhost:8080/users";
const PROVIDER_BASE_URL = "http://localhost:8080/providers";
const ADMIN_BASE_URL = "http://localhost:8080/admin"; // <-- NEW: Base URL for admin endpoints
const ACTUATOR_BASE_URL = "http://localhost:8080/actuator"; // <-- NEW: Base URL for actuator endpoints

class ApiService {
  private getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem("auth_token");
    return token
      ? {
          Authorization: `Bearer ${token}`,
        }
      : {};
  }

  // For auth-related requests like login/register
  private async authRequest<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${AUTH_BASE_URL}${endpoint}`;
    const config: RequestInit = {
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "An error occurred during auth request");
      }
      return await response.json();
    } catch (error) {
      console.error("API auth request failed:", error);
      throw error;
    }
  }

  // For JWT-protected user routes
  private async userRequest<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${USER_BASE_URL}${endpoint}`;
    const config: RequestInit = {
      headers: {
        "Content-Type": "application/json",
        ...this.getAuthHeaders(),
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "An error occurred during user request");
      }
      return await response.json();
    } catch (error) {
      console.error("API user request failed:", error);
      throw error;
    }
  }

  // --- For JWT-protected provider routes ---
  private async providerRequest<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${PROVIDER_BASE_URL}${endpoint}`;
    const config: RequestInit = {
      headers: {
        "Content-Type": "application/json",
        ...this.getAuthHeaders(),
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "An error occurred during provider request");
      }
      return await response.json();
    } catch (error) {
      console.error("API provider request failed:", error);
      throw error;
    }
  }
  // ---------------------------------------------

  // --- NEW: For JWT-protected admin routes ---
  private async adminRequest<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${ADMIN_BASE_URL}${endpoint}`;
    const config: RequestInit = {
      headers: {
        "Content-Type": "application/json",
        ...this.getAuthHeaders(), // Admin requests need auth headers
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "An error occurred during admin request");
      }
      return await response.json();
    } catch (error) {
      console.error("API admin request failed:", error);
      throw error;
    }
  }
  // ---------------------------------------------

  // --- NEW: For public actuator/health endpoint ---
  private async actuatorRequest<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<T> {
    const url = `${ACTUATOR_BASE_URL}${endpoint}`;
    const config: RequestInit = {
      headers: {
        "Content-Type": "application/json",
        // No auth headers for actuator/health as it's permitAll()
        ...options.headers,
      },
      ...options,
    };

    try {
      const response = await fetch(url, config);
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "An error occurred during actuator request");
      }
      return await response.json();
    } catch (error) {
      console.error("API actuator request failed:", error);
      throw error;
    }
  }
  // ---------------------------------------------

  //------------------------------------------------------------------------------------------------------
  //AUTH ENDPOINTS:
  async registerUser(userData: UserRegistrationDto): Promise<UserResponseDto> {
    return this.authRequest<UserResponseDto>("/register/user", {
      method: "POST",
      body: JSON.stringify(userData),
    });
  }

  async registerProvider(
    providerData: ProviderRegistrationDto
  ): Promise<ProviderResponseDto> {
    return this.authRequest<ProviderResponseDto>("/register/provider", {
      method: "POST",
      body: JSON.stringify(providerData),
    });
  }

  async login(credentials: LoginRequestDto): Promise<LoginResponseDto> {
    return this.authRequest<LoginResponseDto>("/login", {
      method: "POST",
      body: JSON.stringify(credentials),
    });
  }
  //------------------------------------------------------------------------------------------------------

  //------------------------------------------------------------------------------------------------------
  //USER DASHBOARD ENDPOINTS:

  //Book Appointment:
  async bookAppointment(dto: AppointmentRequestDto): Promise<AppointmentResponseDto> {
    return this.userRequest<AppointmentResponseDto>("/appointment/book", {
      method: "POST",
      body: JSON.stringify(dto)
    });
  }

  //Reschedule Appointment:
  async rescheduleAppointment(dto: AppointmentRescheduleRequestDto): Promise<AppointmentResponseDto> {
    return this.userRequest<AppointmentResponseDto>("/appointment/reschedule", {
      method: "POST",
      body: JSON.stringify(dto)
    });
  }

  //Cancel Appointment:
  async cancelAppointment(dto: AppointmentCancelDto): Promise<MessageDto> {
    return this.userRequest<MessageDto>("/appointment/cancel", {
      method: "DELETE",
      body: JSON.stringify(dto)
    });
  }

  //View All Appointments:
  async viewBookedSlots() : Promise<AppointmentResponseDto[]> {
    return this.userRequest<AppointmentResponseDto[]>("/appointments");
  }

  //View All Available Slots:
  async viewAllSlots(): Promise<SlotResponseDto[]> {
    return this.userRequest<SlotResponseDto[]>("/view/slots");
  }
  //------------------------------------------------------------------------------------------------------

  //------------------------------------------------------------------------------------------------------
  //PROVIDER DASHBOARD ENDPOINTS:

  async getAllProviders(): Promise<ProviderResponseDto[]> {
    return this.providerRequest<ProviderResponseDto[]>("/all");
  }

  // View the slots the current provider has enrolled for
  async getEnrolledSlotsByProvider(): Promise<SlotResponseDto[]> {
    return this.providerRequest<SlotResponseDto[]>("/slots/enrolled");
  }

  // Add slot
  async addSlot(dto: SlotRequestDto): Promise<SlotResponseDto> {
    return this.providerRequest<SlotResponseDto>("/slots/add", {
      method: "POST",
      body: JSON.stringify(dto),
    });
  }

  // Delete slot
  async deleteSlot(dto: SlotDeleteDto): Promise<MessageDto> {
    return this.providerRequest<MessageDto>("/slots/delete", {
      method: "DELETE",
      body: JSON.stringify(dto),
    });
  }

  // View booked slots (appointments)
  async viewProviderAppointments(): Promise<AppointmentResponseDto[]> {
    return this.providerRequest<AppointmentResponseDto[]>("/appointments");
  }

  //------------------------------------------------------------------------------------------------------
  //ADMIN DASHBOARD ENDPOINTS:
  async getAdminStats(): Promise<AdminStatsDto> {
    return this.adminRequest<AdminStatsDto>("/summary-stats");
  }
}

export const apiService = new ApiService();