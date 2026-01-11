import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Typography,
  AppBar,
  Toolbar,
  Button,
  CssBaseline,
  Fab,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { Company } from '../../types/coupon.types';
import { CompanyCreateRequest, CompanyUpdateRequest } from '../../types/api.types';
import { adminApi } from '../../api/adminApi';
import { ErrorAlert } from '../../components/common/ErrorAlert';
import { CompanyForm } from '../../components/admin/CompanyForm';
import { CompanyTable } from '../../components/admin/CompanyTable';
import { Footer } from '../../components/common/Footer';
import Logo from '../../logo.svg';

interface AlertState {
  message: string;
  severity: 'error' | 'warning' | 'info' | 'success';
}

type FormMode = 'create' | 'edit' | null;

const ManageCompanies: React.FC = () => {
  const navigate = useNavigate();
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);
  const [formLoading, setFormLoading] = useState(false);
  const [alert, setAlert] = useState<AlertState | null>(null);
  const [formMode, setFormMode] = useState<FormMode>(null);
  const [selectedCompany, setSelectedCompany] = useState<Company | null>(null);

  // Fetch all companies
  const fetchCompanies = async () => {
    try {
      setLoading(true);
      const data = await adminApi.getAllCompanies();
      setCompanies(data);
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Failed to fetch companies';
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
    } finally {
      setLoading(false);
    }
  };

  // Initial load
  useEffect(() => {
    fetchCompanies();
  }, []);

  // Handle Add New Company button
  const handleAddCompany = () => {
    setSelectedCompany(null);
    setFormMode('create');
  };

  // Handle Edit Company
  const handleEditCompany = (company: Company) => {
    setSelectedCompany(company);
    setFormMode('edit');
  };

  // Handle Delete Company
  const handleDeleteCompany = (companyId: number) => {
    // Delete is handled in CompanyTable component
    handleConfirmDelete(companyId);
  };

  // Confirm and execute delete
  const handleConfirmDelete = async (companyId: number) => {
    try {
      setLoading(true);
      await adminApi.deleteCompany(companyId);
      setAlert({
        message: 'Company deleted successfully',
        severity: 'success',
      });
      await fetchCompanies();
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || 'Failed to delete company';
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
      setLoading(false);
    }
  };

  // Handle Form Submit (Create or Update)
  const handleFormSubmit = async (
    data: { name: string; email: string; password?: string }
  ) => {
    try {
      setFormLoading(true);

      if (formMode === 'create') {
        // Create new company
        const createRequest: CompanyCreateRequest = {
          name: data.name,
          email: data.email,
          password: data.password || '',
        };
        await adminApi.createCompany(createRequest);
        setAlert({
          message: 'Company created successfully',
          severity: 'success',
        });
      } else if (formMode === 'edit' && selectedCompany) {
        // Update existing company
        const updateRequest: CompanyUpdateRequest = {
          name: data.name,
          email: data.email,
          ...(data.password && { password: data.password }),
        };
        await adminApi.updateCompany(selectedCompany.id, updateRequest);
        setAlert({
          message: 'Company updated successfully',
          severity: 'success',
        });
      }

      // Close form and refresh companies
      setFormMode(null);
      setSelectedCompany(null);
      await fetchCompanies();
    } catch (error: any) {
      const errorMessage =
        error.response?.data?.message ||
        (formMode === 'create'
          ? 'Failed to create company'
          : 'Failed to update company');
      setAlert({
        message: errorMessage,
        severity: 'error',
      });
    } finally {
      setFormLoading(false);
    }
  };

  // Handle Form Cancel
  const handleFormCancel = () => {
    setFormMode(null);
    setSelectedCompany(null);
  };

  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: 'column',
        minHeight: '100vh',
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
            onClick={() => navigate('/admin')}
          />
          <Typography
            variant="h6"
            sx={{ flexGrow: 1, fontWeight: 600, cursor: 'pointer' }}
            onClick={() => navigate('/admin')}
          >
            Coupon System - Manage Companies
          </Typography>
          <Button
            color="inherit"
            onClick={() => navigate('/admin')}
            sx={{
              textTransform: 'none',
              fontSize: '1rem',
              '&:hover': {
                backgroundColor: 'rgba(255, 255, 255, 0.1)',
              },
            }}
          >
            Back to Dashboard
          </Button>
        </Toolbar>
      </AppBar>

      {/* Main Content */}
      <Box
        sx={{
          flex: 1,
          py: 4,
          px: 2,
        }}
      >
        <Container maxWidth="lg">
          {/* Page Header */}
          <Box
            sx={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              mb: 4,
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
              Manage Companies
            </Typography>
            <Button
              variant="contained"
              color="primary"
              startIcon={<AddIcon />}
              onClick={handleAddCompany}
              sx={{
                textTransform: 'none',
                fontSize: '1rem',
                px: 3,
                py: 1,
              }}
            >
              Add New Company
            </Button>
          </Box>

          {/* Company Table */}
          <CompanyTable
            companies={companies}
            loading={loading}
            onEdit={handleEditCompany}
            onDelete={handleDeleteCompany}
          />
        </Container>
      </Box>

      {/* Floating Action Button (Alternative Add Button) */}
      <Fab
        color="primary"
        aria-label="add company"
        onClick={handleAddCompany}
        sx={{
          position: 'fixed',
          bottom: 80,
          right: 24,
        }}
      >
        <AddIcon />
      </Fab>

      {/* Company Form Dialog */}
      {formMode && (
        <CompanyForm
          company={selectedCompany || undefined}
          onSubmit={handleFormSubmit}
          onCancel={handleFormCancel}
          loading={formLoading}
        />
      )}

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

export default ManageCompanies;
