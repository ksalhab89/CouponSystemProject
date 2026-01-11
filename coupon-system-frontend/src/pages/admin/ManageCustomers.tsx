import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  AppBar,
  Toolbar,
  Link,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import AddIcon from '@mui/icons-material/Add';
import { adminApi } from '../../api/adminApi';
import { Customer } from '../../types/coupon.types';
import { CustomerCreateRequest, CustomerUpdateRequest } from '../../types/api.types';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import Footer from '../../components/common/Footer';
import Logo from '../../logo.svg';
import { CustomerTable } from '../../components/admin/CustomerTable';
import CustomerForm from '../../components/admin/CustomerForm';

interface AlertMessage {
  message: string;
  severity: 'error' | 'warning' | 'info' | 'success';
}

const ManageCustomers: React.FC = () => {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [loading, setLoading] = useState(true);
  const [alert, setAlert] = useState<AlertMessage | null>(null);
  const [formDialogOpen, setFormDialogOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<Customer | undefined>(undefined);

  // Fetch customers on component mount
  useEffect(() => {
    fetchCustomers();
  }, []);

  const fetchCustomers = async () => {
    try {
      setLoading(true);
      const data = await adminApi.getAllCustomers();
      setCustomers(data);
      setAlert(null);
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to fetch customers';
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
      console.error('Error fetching customers:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenAddDialog = () => {
    setEditingCustomer(undefined);
    setFormDialogOpen(true);
  };

  const handleOpenEditDialog = (customer: Customer) => {
    setEditingCustomer(customer);
    setFormDialogOpen(true);
  };

  const handleCloseDialog = () => {
    setFormDialogOpen(false);
    setEditingCustomer(undefined);
  };

  const handleSaveCustomer = async (
    formData: CustomerCreateRequest | CustomerUpdateRequest
  ) => {
    try {
      if (editingCustomer) {
        // Update existing customer
        await adminApi.updateCustomer(editingCustomer.id, formData as CustomerUpdateRequest);
        setAlert({
          message: 'Customer updated successfully',
          severity: 'success',
        });
      } else {
        // Create new customer
        await adminApi.createCustomer(formData as CustomerCreateRequest);
        setAlert({
          message: 'Customer created successfully',
          severity: 'success',
        });
      }

      handleCloseDialog();
      await fetchCustomers();
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to save customer';
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
      console.error('Error saving customer:', error);
      throw error;
    }
  };

  const handleDeleteCustomer = async (customerId: number) => {
    try {
      await adminApi.deleteCustomer(customerId);
      setAlert({
        message: 'Customer deleted successfully',
        severity: 'success',
      });
      await fetchCustomers();
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to delete customer';
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
      console.error('Error deleting customer:', error);
      throw error;
    }
  };

  const handleUnlockAccount = async (customerId: number) => {
    try {
      const customer = customers.find(c => c.id === customerId);
      if (!customer) return;

      await adminApi.unlockAccount(customer.email);
      setAlert({
        message: `Account unlocked successfully for ${customer.email}`,
        severity: 'success',
      });
      await fetchCustomers();
    } catch (error: any) {
      const errorMessage = error?.response?.data?.message || 'Failed to unlock account';
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
      console.error('Error unlocking account:', error);
      throw error;
    }
  };

  const handleCloseAlert = () => {
    setAlert(null);
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
      }}
    >
      {/* Navbar */}
      <AppBar position="static" elevation={0}>
        <Toolbar>
          <Box
            component="img"
            src={Logo}
            alt="Coupon System Logo"
            sx={{ height: 40, marginRight: 2, cursor: 'pointer' }}
            onClick={() => navigate('/admin')}
          />
          <Typography
            variant="h6"
            sx={{ flexGrow: 1, fontWeight: 600, cursor: 'pointer' }}
            onClick={() => navigate('/admin')}
          >
            Coupon System - Admin
          </Typography>
          <Link
            onClick={() => navigate('/admin')}
            sx={{
              color: 'white',
              textDecoration: 'none',
              marginRight: 2,
              fontSize: '1rem',
              cursor: 'pointer',
              '&:hover': {
                textDecoration: 'underline',
              },
            }}
          >
            Dashboard
          </Link>
          <Link
            onClick={() => {
              localStorage.removeItem('token');
              navigate('/');
            }}
            sx={{
              color: 'white',
              textDecoration: 'none',
              fontSize: '1rem',
              cursor: 'pointer',
              '&:hover': {
                textDecoration: 'underline',
              },
            }}
          >
            Logout
          </Link>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Box
        sx={{
          flex: 1,
          padding: { xs: 2, sm: 3, md: 4 },
          backgroundColor: '#f5f5f5',
        }}
      >
        <Container maxWidth="lg">
          {/* Page Header */}
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              marginBottom: 4,
              flexDirection: { xs: 'column', sm: 'row' },
              gap: 2,
            }}
          >
            <Typography
              variant="h4"
              component="h1"
              sx={{
                fontWeight: 700,
                color: '#333',
              }}
            >
              Manage Customers
            </Typography>
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              onClick={handleOpenAddDialog}
            >
              Add New Customer
            </Button>
          </Box>

          {/* Customers Table */}
          <CustomerTable
            customers={customers}
            onEdit={handleOpenEditDialog}
            onDelete={handleDeleteCustomer}
            onUnlock={handleUnlockAccount}
            loading={loading}
          />
        </Container>
      </Box>

      {/* Customer Form Dialog */}
      <CustomerForm
        customer={editingCustomer}
        onSubmit={handleSaveCustomer}
        onCancel={handleCloseDialog}
        open={formDialogOpen}
      />

      {/* Alert */}
      {alert && (
        <ErrorAlert
          message={alert.message}
          severity={alert.severity}
          open={!!alert}
          onClose={handleCloseAlert}
        />
      )}

      {/* Footer */}
      <Footer copyrightText="Coupon System Admin" />
    </Box>
  );
};

export default ManageCustomers;
