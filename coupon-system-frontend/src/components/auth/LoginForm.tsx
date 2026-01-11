import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Card,
  CardContent,
  CardHeader,
  TextField,
  Button,
  Box,
  CircularProgress,
  InputAdornment,
  IconButton,
} from '@mui/material';
import { Visibility, VisibilityOff } from '@mui/icons-material';
import { ClientType } from '../../types/auth.types';
import { RoleSelector } from './RoleSelector';
import { ErrorAlert } from '../common/ErrorAlert';
import { useAuth } from '../../hooks/useAuth';
import { isValidEmail, isValidPassword, getValidationError } from '../../utils/validators';
import { authApi } from '../../api/authApi';

export const LoginForm: React.FC = () => {
  const navigate = useNavigate();
  const { login } = useAuth();

  // Form state
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [clientType, setClientType] = useState<ClientType>('customer');
  const [showPassword, setShowPassword] = useState(false);

  // UI state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showAlert, setShowAlert] = useState(false);

  // Validation state
  const [emailError, setEmailError] = useState('');
  const [passwordError, setPasswordError] = useState('');

  // Validate form fields
  const validateForm = (): boolean => {
    let isValid = true;

    // Validate email
    const emailValidationError = getValidationError('email', email);
    if (emailValidationError) {
      setEmailError(emailValidationError);
      isValid = false;
    } else {
      setEmailError('');
    }

    // Validate password
    const passwordValidationError = getValidationError('password', password);
    if (passwordValidationError) {
      setPasswordError(passwordValidationError);
      isValid = false;
    } else {
      setPasswordError('');
    }

    return isValid;
  };

  // Handle form submission
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    // Clear previous error
    setError(null);

    // Validate form
    if (!validateForm()) {
      return;
    }

    setLoading(true);

    try {
      // Call login from auth context
      await login({
        email,
        password,
        clientType,
      });

      // Redirect based on role after successful login
      switch (clientType) {
        case 'customer':
          navigate('/customer');
          break;
        case 'company':
          navigate('/company');
          break;
        case 'admin':
          navigate('/admin');
          break;
        default:
          navigate('/');
      }
    } catch (err: any) {
      const errorMessage =
        err?.response?.data?.message ||
        err?.message ||
        'Login failed. Please try again.';
      setError(errorMessage);
      setShowAlert(true);
    } finally {
      setLoading(false);
    }
  };

  // Handle password visibility toggle
  const handleClickShowPassword = () => {
    setShowPassword(!showPassword);
  };

  const handleMouseDownPassword = (event: React.MouseEvent<HTMLButtonElement>) => {
    event.preventDefault();
  };

  // Handle role change
  const handleRoleChange = (role: ClientType) => {
    setClientType(role);
  };

  return (
    <Card
      sx={{
        maxWidth: 450,
        width: '100%',
        mx: 'auto',
        boxShadow: 3,
        borderRadius: 2,
      }}
    >
      <CardHeader
        title="Login"
        subheader="Enter your credentials to access your account"
        sx={{
          textAlign: 'center',
          pb: 1,
        }}
      />
      <CardContent>
        <Box component="form" onSubmit={handleSubmit} noValidate sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
          {/* Error Alert */}
          <ErrorAlert
            message={error || ''}
            severity="error"
            open={showAlert}
            onClose={() => setShowAlert(false)}
          />

          {/* Email Field */}
          <TextField
            fullWidth
            label="Email"
            type="email"
            variant="outlined"
            value={email}
            onChange={(e) => {
              setEmail(e.target.value);
              // Clear error on change
              if (emailError) {
                setEmailError('');
              }
            }}
            onBlur={() => {
              const error = getValidationError('email', email);
              if (error) {
                setEmailError(error);
              }
            }}
            error={!!emailError}
            helperText={emailError}
            placeholder="Enter your email"
            size="small"
            disabled={loading}
            autoComplete="email"
          />

          {/* Password Field */}
          <TextField
            fullWidth
            label="Password"
            type={showPassword ? 'text' : 'password'}
            variant="outlined"
            value={password}
            onChange={(e) => {
              setPassword(e.target.value);
              // Clear error on change
              if (passwordError) {
                setPasswordError('');
              }
            }}
            onBlur={() => {
              const error = getValidationError('password', password);
              if (error) {
                setPasswordError(error);
              }
            }}
            error={!!passwordError}
            helperText={passwordError}
            placeholder="Enter your password"
            size="small"
            disabled={loading}
            autoComplete="current-password"
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton
                    aria-label="toggle password visibility"
                    onClick={handleClickShowPassword}
                    onMouseDown={handleMouseDownPassword}
                    edge="end"
                    disabled={loading}
                    size="small"
                  >
                    {showPassword ? <VisibilityOff /> : <Visibility />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />

          {/* Role Selector */}
          <RoleSelector value={clientType} onChange={handleRoleChange} />

          {/* Submit Button */}
          <Button
            type="submit"
            fullWidth
            variant="contained"
            color="primary"
            disabled={loading}
            sx={{
              mt: 1,
              py: 1.25,
              fontSize: '0.95rem',
              fontWeight: 600,
              textTransform: 'none',
              transition: 'all 0.3s ease-in-out',
              '&:hover:not(:disabled)': {
                transform: 'translateY(-2px)',
                boxShadow: 4,
              },
              '&:disabled': {
                opacity: 0.7,
              },
            }}
          >
            {loading ? (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <CircularProgress size={20} color="inherit" />
                <span>Logging in...</span>
              </Box>
            ) : (
              'Login'
            )}
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
};
