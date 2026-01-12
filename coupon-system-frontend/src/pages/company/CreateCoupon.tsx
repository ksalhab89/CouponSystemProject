import React, { useState } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  AppBar,
  Toolbar,
  CssBaseline,
  Paper,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { CouponForm } from '../../components/coupons/CouponForm';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { Footer } from '../../components/common/Footer';
import { companyApi } from '../../api/companyApi';
import { CouponCreateRequest } from '../../types/api.types';
import Logo from '../../logo.svg';

interface AlertState {
  message: string;
  severity: 'error' | 'warning' | 'info' | 'success';
}

const CreateCoupon: React.FC = () => {
  const navigate = useNavigate();
  const [alert, setAlert] = useState<AlertState | null>(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (data: CouponCreateRequest) => {
    try {
      setLoading(true);
      await companyApi.createCoupon(data);
      setAlert({
        message: 'Coupon created successfully!',
        severity: 'success',
      });

      // Navigate to my coupons after a short delay
      setTimeout(() => {
        navigate('/company/coupons');
      }, 1500);
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to create coupon';
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
      setLoading(false);
    }
  };

  const handleCancel = () => {
    navigate('/company');
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        backgroundColor: '#fafafa',
      }}
    >
      <CssBaseline />

      {/* Navbar */}
      <AppBar position="static" elevation={1}>
        <Toolbar>
          <Box
            component="img"
            src={Logo}
            alt="Coupon System Logo"
            sx={{ height: 40, marginRight: 2, cursor: 'pointer' }}
            onClick={() => navigate('/company')}
          />
          <Typography
            variant="h6"
            sx={{ flexGrow: 1, fontWeight: 600, cursor: 'pointer' }}
            onClick={() => navigate('/company')}
          >
            Create Coupon
          </Typography>
          <Button
            color="inherit"
            onClick={() => navigate('/company')}
            startIcon={<ArrowBackIcon />}
            sx={{
              textTransform: 'none',
              fontSize: '1rem',
            }}
          >
            Back to Dashboard
          </Button>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Box sx={{ flex: 1, py: 4 }}>
        <Container maxWidth="md">
          {/* Page Title */}
          <Typography
            variant="h4"
            component="h1"
            sx={{
              fontWeight: 700,
              color: '#333',
              mb: 3,
            }}
          >
            Create New Coupon
          </Typography>

          {/* Form Container */}
          <Paper elevation={2} sx={{ p: 4 }}>
            <CouponForm
              onSubmit={handleSubmit}
              onCancel={handleCancel}
              loading={loading}
            />
          </Paper>
        </Container>
      </Box>

      {/* Success/Error Alert */}
      {alert && (
        <ErrorAlert
          message={alert.message}
          severity={alert.severity}
          open={!!alert}
          onClose={() => setAlert(null)}
        />
      )}

      {/* Footer */}
      <Footer />
    </Box>
  );
};

export default CreateCoupon;
