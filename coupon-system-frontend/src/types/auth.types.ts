export type ClientType = 'admin' | 'company' | 'customer';

export interface LoginRequest {
  email: string;
  password: string;
  clientType: ClientType;
}

export interface UserInfo {
  userId: number;
  email: string;
  clientType: ClientType;
  name: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  userInfo: UserInfo;
}

export interface AuthContextType {
  user: UserInfo | null;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  refreshToken: () => Promise<void>;
  isAuthenticated: boolean;
  isAdmin: boolean;
  isCompany: boolean;
  isCustomer: boolean;
}
