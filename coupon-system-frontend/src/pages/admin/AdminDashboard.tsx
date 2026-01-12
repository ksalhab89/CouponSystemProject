import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  Stack,
  Alert,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import BusinessIcon from '@mui/icons-material/Business';
import PeopleIcon from '@mui/icons-material/People';
import LocalOfferIcon from '@mui/icons-material/LocalOffer';
import StorageIcon from '@mui/icons-material/Storage';
import Navbar from '../../components/common/Navbar';
import Footer from '../../components/common/Footer';
import { StatsCard } from '../../components/admin/StatsCard';
import { LoadingSpinner } from '../../components/common/LoadingSpinner';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { adminApi } from '../../api/adminApi';
import { useAuth } from '../../hooks/useAuth';

const AdminDashboard: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [companiesCount, setCompaniesCount] = useState<number>(0);
  const [customersCount, setCustomersCount] = useState<number>(0);
  const [couponsCount, setCouponsCount] = useState<number>(0);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [showError, setShowError] = useState<boolean>(false);

  // Fetch counts on component mount
  useEffect(() => {
    const fetchCounts = async () => {
      try {
        setLoading(true);
        setError('');
        setShowError(false);

        const [companies, customers, coupons] = await Promise.all([
          adminApi.getAllCompanies(),
          adminApi.getAllCustomers(),
          adminApi.getAllCoupons(),
        ]);

        setCompaniesCount(companies.length);
        setCustomersCount(customers.length);
        setCouponsCount(coupons.length);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load dashboard data';
        setError(errorMessage);
        setShowError(true);
      } finally {
        setLoading(false);
      }
    };

    fetchCounts();
  }, []);

  // System status (healthy if all data loaded)
  const systemStatus = loading ? 'Loading...' : error ? 'Warning' : 'Healthy';
  const statusColor = loading ? '#ff9800' : error ? '#f44336' : '#4caf50';
  const statusBgColor = loading ? '#fff3e0' : error ? '#ffebee' : '#e8f5e9';

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
        backgroundColor: '#fafafa',
      }}
    >
      {/* Navbar */}
      <Navbar title="Admin Dashboard" />

      {/* Main Content */}
      <Container maxWidth="lg" component="main" sx={{ py: 4, flex: 1 }}>
        {/* Error Alert */}
        <ErrorAlert
          message={error}
          severity="error"
          open={showError}
          onClose={() => setShowError(false)}
        />

        {/* Page Title */}
        <Typography
          variant="h4"
          component="h1"
          sx={{
            mb: 2,
            fontWeight: 'bold',
            color: '#333',
          }}
        >
          Admin Dashboard
        </Typography>

        {/* Welcome Message */}
        <Alert
          severity="success"
          sx={{
            mb: 3,
            backgroundColor: '#e8f5e9',
            '& .MuiAlert-icon': {
              color: '#2e7d32',
            },
          }}
        >
          <Typography variant="body1" sx={{ fontWeight: 500 }}>
            Welcome, {user?.name || 'Administrator'}! 👋
          </Typography>
          <Typography variant="body2" color="textSecondary">
            Monitor system activity and manage companies, customers, and coupons.
          </Typography>
        </Alert>

        {/* Loading State */}
        {loading ? (
          <Box sx={{ height: '400px', display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
            <LoadingSpinner message="Loading dashboard data..." />
          </Box>
        ) : (
          <>
            {/* Stats Cards Grid (2x2) */}
            <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 3, mb: 4 }}>
              {/* Total Companies Card */}
              <StatsCard
                title="Total Companies"
                count={companiesCount}
                icon={BusinessIcon}
                color="#1976d2"
                backgroundColor="#e3f2fd"
              />

              {/* Total Customers Card */}
              <StatsCard
                title="Total Customers"
                count={customersCount}
                icon={PeopleIcon}
                color="#2e7d32"
                backgroundColor="#e8f5e9"
              />

              {/* Total Coupons Card */}
              <StatsCard
                title="Total Coupons"
                count={couponsCount}
                icon={LocalOfferIcon}
                color="#f57c00"
                backgroundColor="#fff3e0"
              />

              {/* System Status Card */}
              <StatsCard
                title="System Status"
                count={systemStatus}
                icon={StorageIcon}
                color={statusColor}
                backgroundColor={statusBgColor}
              />
            </Box>

            {/* Quick Action Buttons */}
            <Box
              sx={{
                backgroundColor: '#fff',
                borderRadius: 2,
                padding: 3,
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
              }}
            >
              <Typography
                variant="h6"
                sx={{
                  mb: 2,
                  fontWeight: 'bold',
                  color: '#333',
                }}
              >
                Quick Actions
              </Typography>

              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <Button
                  variant="contained"
                  color="primary"
                  size="large"
                  onClick={() => navigate('/admin/companies')}
                  sx={{
                    px: 3,
                    py: 1.5,
                    borderRadius: 1,
                  }}
                  startIcon={<BusinessIcon />}
                >
                  Manage Companies
                </Button>

                <Button
                  variant="contained"
                  color="success"
                  size="large"
                  onClick={() => navigate('/admin/customers')}
                  sx={{
                    px: 3,
                    py: 1.5,
                    borderRadius: 1,
                  }}
                  startIcon={<PeopleIcon />}
                >
                  Manage Customers
                </Button>
              </Stack>
            </Box>
          </>
        )}
      </Container>

      {/* Footer */}
      <Footer copyrightText="Coupon System Admin" />
    </Box>
  );
};

export default AdminDashboard;
